package com.intercloud

import java.io.InputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.cloudstore.*
import com.intercloud.util.*

import com.intercloud.LinkCloudStoreJob

class CloudStoreService {

	private static Logger log = LoggerFactory.getLogger(CloudStoreService.class)

	static String ROOT_DIR
	static String INTERCLOUD_STORAGE_PATH
	static String INTERCLOUD
	static String DROPBOX
	static String GOOGLEDRIVE
	static String AWSS3


	public def getClientAccessRequestUrl(def cloudStoreClass, def request) {
		def clientAccessRequestUrl = cloudStoreClass.configure(false, request)
		return clientAccessRequestUrl
	}

	public def getCloudStoreClass(String cloudStoreName) {
		def cloudStoreClass = null

		if(cloudStoreName == DROPBOX) {
			cloudStoreClass = new DropboxCloudStore()
		}
		else if(cloudStoreName == GOOGLEDRIVE) {
			cloudStoreClass = new GoogledriveCloudStore()
		}
		else if(cloudStoreName == AWSS3) {
			cloudStoreClass = new AwsS3CloudStore()
		}

		return cloudStoreClass
	}

	public boolean authRedirect(Account account, def cloudStoreClass, def request) {
		def didFinishConfigure = cloudStoreClass.configure(true, request)
		if(didFinishConfigure) {
			// Run async job to link up cloud store

			def future = callAsync {
				return saveCloudStoreInstance(account, cloudStoreClass)
			}
			Map linkCloudStoreParam = ['future': future]
			LinkCloudStoreJob.triggerNow(linkCloudStoreParam)
		}
		return didFinishConfigure
	}

	private boolean saveCloudStoreInstance(Account account, def cloudStoreClass) {
		CloudStore cloudStoreInstance = new CloudStore()
		boolean isSuccess = cloudStoreClass.setCloudStoreProperties(cloudStoreInstance, account)

		if(isSuccess) {
			if(!cloudStoreInstance.save(flush:true)) {
				log.warn "Cloud store link failed: {}", cloudStoreInstance.errors.allErrors
				isSuccess = false
			}
		}
		return isSuccess
	}

	public def getHomeCloudStoreResources(Account account) {
		def homeResources = [:]

		// Add inter cloud first, want it at the top of the home view
		def fileResources = getSpecificCloudStoreResources(account, INTERCLOUD, ROOT_DIR)
		homeResources << [INTERCLOUD : fileResources]

		account.cloudStores.each {
			if(it.storeName != INTERCLOUD) {

				fileResources = getSpecificCloudStoreResources(account, it.storeName, ROOT_DIR)
				if(fileResources != null) {
					homeResources << ["$it.storeName" : fileResources]
				}
			}
		}

		return homeResources
	}

	public def getSpecificCloudStoreResources(Account account, String cloudStoreName, String directory) {
		def fileResource = getFileResourceFromPath(account, cloudStoreName, directory)
		def cloudStoreResources = retrieveFilesInDir(fileResource)

		return cloudStoreResources
	}

	private FileResource getFileResourceFromPath(Account account, String cloudStoreName, String fileResourcePath) {
		FileResource fileResource

		CloudStore cloudStore = getAccountCloudStore(account, cloudStoreName)
		if(cloudStore) {
			fileResource = cloudStore.fileResources.find { it.path == fileResourcePath }
		}
	}

	public CloudStore getAccountCloudStore(Account account, String cloudStoreName) {
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(cloudStoreName, account)
		return cloudStore
	}

	public def retrieveFilesInDir(FileResource fileResource) {
		return fileResource?.childFileResources
	}

	public BigDecimal getTotalSpaceInGb(CloudStore cloudStore) {
		BigDecimal totalSpaceInBytes = cloudStore.totalSpace
		BigDecimal totalSpaceInGb = Math.round(totalSpaceInBytes/(2**30)*100)/100
		return totalSpaceInGb
	}

	public def getSpaceList(BigDecimal spaceInBytes) {
		BigDecimal spaceUsed
		def spaceList
		if(spaceInBytes < 2**30) {
			spaceUsed = Math.round(spaceInBytes/(2**20)*100)/100
			spaceList = [spaceUsed,'MB']
		}
		else {
			spaceUsed = Math.round(spaceInBytes/(2**30)*100)/100
			spaceList = [spaceUsed,'GB']
		}

		return spaceList

	}

	public def getFileResourceStream(String storeName, FileResource fileResource) {
		def resourceDataStream = null
		if(storeName == INTERCLOUD) {
			String locationOnFileSystem = fileResource.locationOnFileSystem
			resourceDataStream = getStreamFromFileLocation(storeName, fileResource, locationOnFileSystem)
		}
		else {
			CloudStore cloudStore = fileResource.cloudStore
			resourceDataStream = downloadFileResourceFromCloudStore(cloudStore, fileResource)
		}

		return resourceDataStream
	}

	private InputStream getStreamFromFileLocation(String storeName, FileResource fileResource, String locationOnFileSystem) {
		InputStream inputStream = null

		if(fileResource.isDir) {
			inputStream = buildZipAndGetStream(storeName, fileResource, locationOnFileSystem)
		}
		else {
			inputStream = getSingleFileStream(locationOnFileSystem)
		}

		return inputStream
	}

	private InputStream buildZipAndGetStream(String storeName, FileResource fileResource, String locationOnFileSystem) {
		String zipFileName = ZipUtilities.getSourceZipName(storeName, fileResource)

		log.debug "Zipping downloaded folder to '{}'", zipFileName
		ZipUtilities.zipFolder(locationOnFileSystem, zipFileName)

		String zipFileLocation = locationOnFileSystem.substring(0, locationOnFileSystem.lastIndexOf('/'))
		InputStream zippedFolderInputStream = ZipUtilities.getInputStreamFromZipFile(zipFileLocation, zipFileName)

		String zipPath = zipFileLocation + '/' + zipFileName
		ZipUtilities.removeTempFromFileSystem(zipPath)

		return zippedFolderInputStream
	}

	private InputStream getSingleFileStream(String locationOnFileSystem) {
		InputStream inputStream = null
		try {
			inputStream = new FileInputStream(locationOnFileSystem)
		}
		catch(FileNotFoundException) {
			log.warn "File not found on file system"
		}
		catch(IOException) {
			log.warn "File could not be read on file system"
		}

		return inputStream
	}

	private InputStream downloadFileResourceFromCloudStore(CloudStore cloudStore, FileResource fileResource) {
		def cloudStoreClass = getCloudStoreClass(cloudStore.storeName)
		InputStream downloadedFileStream = cloudStoreClass.downloadResource(cloudStore, fileResource)
		return downloadedFileStream
	}

	public boolean deleteResource(Account account, String cloudStoreName, def fileResourceId) {
		boolean isSuccess
		FileResource fileResource = FileResource.get(fileResourceId)

		if(fileResource) {
			log.debug "Deleting resource data from db"
			CloudStoreUtilities.deleteFromDatabase(fileResource)
			isSuccess = true

			if(cloudStoreName == INTERCLOUD) {
				deleteFromLocalFileSystem(fileResource)

				CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(cloudStoreName, account)

				BigInteger spaceToDelete = -(new BigInteger(fileResource.byteSize))
				updateIntercloudSpace(cloudStore, spaceToDelete)
			}
			else {
				isSuccess = deleteFromCloudStoreLink(account, cloudStoreName, fileResource)
			}
		}
		else {

			log.debug "Attempted to delete a file resource that did not exist"
			isSuccess = true
		}

		return isSuccess
	}

	private void deleteFromLocalFileSystem(FileResource fileResource) {
		log.debug "Deleting file resource from local file system"

		File file = new File(fileResource.locationOnFileSystem)

		if(file.isDirectory()) {
			deleteFolder(file)
		}
		else {
			file.delete()
		}
	}

	private void deleteFolder(File folder) {
		File[] files = folder.listFiles()
		if(files != null) {
			for(File f in files) {
				if(f.isDirectory()) {
					deleteFolder(f)
				}
				else {
					f.delete()
				}
			}
		}
		folder.delete()
	}

	private boolean deleteFromCloudStoreLink(Account account, String cloudStoreName, FileResource fileResource) {
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(cloudStoreName, account)
		def cloudStoreClass = getCloudStoreClass(cloudStoreName)
		boolean isSuccess = false

		if(cloudStoreClass) {
			isSuccess = cloudStoreClass.deleteResource(cloudStore, fileResource)
			cloudStore.save(flush:true)
		}
		else {
			log.debug "Attempt to delete from unsuppored cloud store"
		}

		return isSuccess
	}

	public void updateResources(Account account, String cloudStoreName) {
		if(cloudStoreName) {
			log.debug "Manually updating {} file resources", cloudStoreName
			def cloudStoreClass = getCloudStoreClass(cloudStoreName)
			if(cloudStoreClass) {
				updateSingleCloudStore(account, cloudStoreName, cloudStoreClass)
			}
			else {
				log.debug "Bad cloud store specified when running manual update: {}", cloudStoreName
			}
		}
		else {
			log.debug "Manually updating all cloud store file resources"
			account.cloudStores.each{
				if(it.storeName != INTERCLOUD) {
					def cloudStoreClass = getCloudStoreClass(it.storeName)
					updateSingleCloudStore(account, it.storeName, cloudStoreClass)
				}
			}
		}
	}

	private def updateSingleCloudStore(Account account, String storeName, def cloudStoreClass) {
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)

		String updateCursor = cloudStore.updateCursor
		def currentFileResources = cloudStore.fileResources

		def newUpdateCursor = cloudStoreClass.updateResources(cloudStore, updateCursor, currentFileResources)
		if(newUpdateCursor) {
			cloudStore.updateCursor = newUpdateCursor
			cloudStore.save(flush:true)
		}
	}

	public boolean uploadResource(Account account, String cloudStoreName, def uploadedFile, String targetDirectory) {
		CloudStore cloudStore = account.cloudStores.find { it.storeName == cloudStoreName}
		def cloudStoreClass = getCloudStoreClass(cloudStoreName)
		def newUpdateCursor

		// Determine the parent file resource to upload under
		FileResource parentFileResource = getParentFileResourceFromPath(account, cloudStoreName, targetDirectory)

		if(cloudStoreName == INTERCLOUD) {
			createFileResourceFromUploadedFile(account, cloudStore, uploadedFile, parentFileResource, null)
			BigInteger spaceToAdd = uploadedFile.getSize()
			updateIntercloudSpace(cloudStore, spaceToAdd)
		}
		else if(cloudStoreClass) {
			log.debug "Checking for updates before upload"
			String updateCursor = cloudStore.updateCursor
			def currentFileResources = cloudStore.fileResources
			newUpdateCursor = cloudStoreClass.updateResources(cloudStore, updateCursor, currentFileResources)
			cloudStore.updateCursor = newUpdateCursor

			if(cloudStoreName == DROPBOX) {
				boolean isSuccess = uploadToDropbox(cloudStoreClass, cloudStore, uploadedFile, parentFileResource)
				if(!isSuccess) {
					return false
				}
			}
			else if(cloudStoreName == GOOGLEDRIVE) {
				boolean isSuccess = uploadToGoogledrive(cloudStoreClass, cloudStore, uploadedFile, parentFileResource)
				if(!isSuccess) {
					return false
				}
			}
		}
		else {
			log.debug "Bad cloud store specified when uploading file '{}'", cloudStoreName
			return false
		}

		cloudStore.save(flush:true)

		return true
	}

	private FileResource getParentFileResourceFromPath(Account account, String cloudStoreName, String targetDirectory) {
		String cloudStorePath

		// Remove the /${cloudStore} from beginning of target directory
		if(cloudStoreName.size()+1 != targetDirectory.size()) {
			cloudStorePath = targetDirectory[cloudStoreName.size()+1..-1]
		}

		// No cloud store path means we're at the ROOT_DIR
		if(!cloudStorePath) {
			cloudStorePath = "/"
		}

		FileResource parentFileResource = getFileResourceFromPath(account, cloudStoreName, cloudStorePath)

		return parentFileResource

	}

	private boolean uploadToDropbox(def cloudStoreClass, CloudStore cloudStore, def uploadedFile, FileResource parentFileResource) {
		String newFileName = null
		def cloudStoreUploadName = cloudStoreClass.uploadResource(cloudStore, uploadedFile, parentFileResource.path, false)

		if(!cloudStoreUploadName) {
			return false
		}
		if(cloudStoreUploadName != uploadedFile.originalFilename) {
			newFileName = cloudStoreUploadName
		}

		createFileResourceFromUploadedFile(cloudStore.account, cloudStore, uploadedFile, parentFileResource, newFileName)

		return true
	}

	private boolean uploadToGoogledrive(def cloudStoreClass, CloudStore cloudStore, def uploadedFile, FileResource parentFileResource) {
		String extraMetadata = cloudStoreClass.uploadResource(cloudStore, uploadedFile, parentFileResource.path, false)
		createFileResourceFromUploadedFile(cloudStore.account, cloudStore, uploadedFile, parentFileResource, extraMetadata)
		return true
	}

	private void createFileResourceFromUploadedFile(Account account, CloudStore cloudStore, def uploadedFile, FileResource parentFileResource, String extraData) {
		FileResource fileResource = new FileResource()

		String filePath

		// directories besides root do not have trailing forward slash
		if(parentFileResource.path == "/") {
			filePath = parentFileResource.path + uploadedFile.originalFilename
		}
		else {
			filePath = parentFileResource.path + "/" + uploadedFile.originalFilename
		}

		fileResource.fileName = uploadedFile.originalFilename

		if(cloudStore.storeName == INTERCLOUD) {
			log.debug "Saving uploaded file to local file system for InterCloud cloud store"
			String accountEmail = account.email
			String locationOnFileSystem = INTERCLOUD_STORAGE_PATH + '/' + accountEmail + '/InterCloudRoot' + filePath
			fileResource.locationOnFileSystem = locationOnFileSystem
			saveFileToLocalFileSystem(locationOnFileSystem, uploadedFile)
		}
		else if(cloudStore.storeName == DROPBOX) {
			if(extraData) {
				if(parentFileResource.path == "/") {
					filePath = parentFileResource.path + extraData
				}
				else {
					filePath = parentFileResource.path + "/" + uploadedFile.originalFilename
				}

				fileResource.fileName = extraData
			}
		}
		else if(cloudStore.storeName == GOOGLEDRIVE) {
			fileResource.extraMetadata = extraData
		}

		fileResource.path = filePath
		fileResource.byteSize = uploadedFile.size
		fileResource.mimeType = uploadedFile.contentType
		fileResource.isDir = false
		fileResource.cloudStore = cloudStore
		fileResource.modified = new Date()
		fileResource.parentFileResource = parentFileResource

		fileResource.save()
	}

	private void saveFileToLocalFileSystem(String pathToSaveFile, def newFile) {
		byte[] buffer = new byte[1024]
		int read = 0
		InputStream inputStream = null
		OutputStream outputStream = null
		try {
			inputStream = newFile.getInputStream()
			outputStream = new FileOutputStream(new File(pathToSaveFile))

			while((read = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, read)
			}

			log.debug "Wrote file '{}' to local file system", pathToSaveFile
		}
		catch(IOException) {
			log.warn "Could not save file to local file system. Exception: {}", IOException
		}
		finally {
			if(inputStream != null) {
				inputStream.close()
			}
			if(outputStream != null) {
				outputStream.close()
			}
		}
	}

	private void saveFolderToLocalFileSystem(String pathToSaveFolder) {
		try {
			new File(pathToSaveFolder).mkdirs();

			log.debug "Wrote folder '{}' to local file system", pathToSaveFolder
		}
		catch(IOException) {
			log.warn "Could not create file on local file system. Exception: {}", IOException
		}

	}

	private void updateIntercloudSpace(CloudStore cloudStore, BigInteger spaceToChange) {
		log.debug "Updating intercloud space"
		cloudStore.spaceUsed += spaceToChange
		cloudStore.save(flush:true)
	}

	public boolean createFolder(Account account, String cloudStoreName, FileResource parentFileResource, String folderName) {
		CloudStore cloudStore = account.cloudStores.find { it.storeName == cloudStoreName}
		def cloudStoreClass = getCloudStoreClass(cloudStoreName)
		def newUpdateCursor

		if(cloudStoreName == INTERCLOUD) {
			createFileResourceFromNewFolder(account, cloudStore, folderName, parentFileResource, null)
		}
		else if(cloudStoreClass) {
			log.debug "Checking for updates before creating new folder"
			String updateCursor = cloudStore.updateCursor
			def currentFileResources = cloudStore.fileResources
			newUpdateCursor = cloudStoreClass.updateResources(cloudStore, updateCursor, currentFileResources)
			cloudStore.updateCursor = newUpdateCursor

			if(cloudStoreName == DROPBOX) {
				boolean isSuccess = createFolderInDropbox(cloudStoreClass, cloudStore, folderName, parentFileResource)
				if(!isSuccess) {
					return false
				}
			}
			else if(cloudStoreName == GOOGLEDRIVE) {
				boolean isSuccess = createFolderInGoogledrive(cloudStoreClass, cloudStore, folderName, parentFileResource)
				if(!isSuccess) {
					return false
				}
			}
		}
		else {
			log.debug "Bad cloud store specified when creating folder in '{}'", cloudStoreName
			return false
		}

		cloudStore.save(flush:true)

		return true
	}

	private void createFileResourceFromNewFolder(Account account, CloudStore cloudStore, String folderName, FileResource parentFileResource, String extraData) {
		FileResource fileResource = new FileResource()

		String filePath

		// directories besides root do not have trailing forward slash
		if(parentFileResource.path == "/") {
			filePath = parentFileResource.path + folderName
		}
		else {
			filePath = parentFileResource.path + "/" + folderName
		}

		fileResource.fileName = folderName

		if(cloudStore.storeName == INTERCLOUD) {
			log.debug "Saving new folder to local file system for InterCloud cloud store"
			String accountEmail = account.email
			String locationOnFileSystem = INTERCLOUD_STORAGE_PATH + '/' + accountEmail + '/InterCloudRoot' + filePath
			fileResource.locationOnFileSystem = locationOnFileSystem
			saveFolderToLocalFileSystem(locationOnFileSystem)
		}
		else if(cloudStore.storeName == DROPBOX) {
			if(extraData) {
				if(parentFileResource.path == "/") {
					filePath = parentFileResource.path + extraData
				}
				else {
					filePath = parentFileResource.path + "/" + folderName
				}

				fileResource.fileName = extraData
			}
		}
		else if(cloudStore.storeName == GOOGLEDRIVE) {
			fileResource.extraMetadata = extraData
		}

		fileResource.path = filePath
		fileResource.byteSize = 0
		fileResource.mimeType = "application/octet-stream"
		fileResource.isDir = true
		fileResource.cloudStore = cloudStore
		fileResource.modified = new Date()
		fileResource.parentFileResource = parentFileResource

		fileResource.save()
	}

	private boolean createFolderInDropbox(def cloudStoreClass, CloudStore cloudStore, String folderName, FileResource parentFileResource) {
		String newFolderName = null
		def cloudStoreUploadName = cloudStoreClass.uploadResource(cloudStore, folderName, parentFileResource.path, true)

		if(!cloudStoreUploadName) {
			return false
		}
		if(cloudStoreUploadName != folderName) {
			newFolderName = cloudStoreUploadName
		}

		createFileResourceFromNewFolder(cloudStore.account, cloudStore, folderName, parentFileResource, newFolderName)

		return true
	}

	private boolean createFolderInGoogledrive(def cloudStoreClass, CloudStore cloudStore, String folderName, FileResource parentFileResource) {
		String extraMetadata = cloudStoreClass.uploadResource(cloudStore, folderName, parentFileResource.path, true)
		createFileResourceFromNewFolder(cloudStore.account, cloudStore, folderName, parentFileResource, extraMetadata)
		return true
	}
}
