package com.intercloud

import java.io.InputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.cloudstore.*
import com.intercloud.util.*

import com.intercloud.LinkCloudStoreJob

class CloudStoreService {

	private static Logger log = LoggerFactory.getLogger(CloudStoreService.class)

	static String INTERCLOUD_STORAGE_PATH = "storage/InterCloudStorage"
	private static String ROOT_DIR = "/"

	public def getClientAccessRequestUrl(def cloudStoreClass, def request) {
		def clientAccessRequestUrl = cloudStoreClass.configure(false, request)
		return clientAccessRequestUrl
	}

	public def getCloudStoreClass(String cloudStoreName) {
		def cloudStoreClass = null

		if(cloudStoreName == 'dropbox') {
			cloudStoreClass = new DropboxCloudStore()
		}
		else if(cloudStoreName == 'googledrive') {
			cloudStoreClass = new GoogledriveCloudStore()
		}
		else if(cloudStoreName == 'awss3') {
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
		def fileResources = getSpecificCloudStoreResources(account, "intercloud", ROOT_DIR)
		homeResources << ["intercloud" : fileResources]

		account.cloudStores.each {
			if(it.storeName != "intercloud") {

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
		CloudStore cloudStore = getAccountCloudStore(account, cloudStoreName)
		if(cloudStore) {
			return cloudStore.fileResources.find { it.path == fileResourcePath }
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
		if(storeName == 'intercloud') {
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
		def cloudStoreLink = getCloudStoreClass(cloudStore.storeName)
		InputStream downloadedFileStream = cloudStoreLink.downloadResource(cloudStore, fileResource)
		return downloadedFileStream
	}

	public boolean deleteResource(Account account, String cloudStoreName, def fileResourceId) {
		FileResource fileResource = FileResource.get(fileResourceId)
		CloudStoreUtilities.deleteFromDatabase(fileResource)
		boolean isSuccess = true

		if(cloudStoreName == 'intercloud') {
			deleteFromLocalFileSystem(fileResource)

			CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(cloudStoreName, account)

			BigInteger spaceToDelete = -(new BigInteger(fileResource.byteSize))
			updateIntercloudSpace(cloudStore, spaceToDelete)
		}
		else {
			isSuccess = deleteFromCloudStoreLink(account, cloudStoreName, fileResource)
		}
		return isSuccess
	}

	private void deleteFromLocalFileSystem(FileResource fileResource) {
		File file = new File(fileResource.locationOnFileSystem)
		file.delete()
	}

	private boolean deleteFromCloudStoreLink(Account account, String cloudStoreName, FileResource fileResource) {
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(cloudStoreName, account)
		def cloudStoreLink = getCloudStoreClass(cloudStoreName)
		boolean isSuccess = false

		if(cloudStoreLink) {
			isSuccess = cloudStoreLink.deleteResource(cloudStore, fileResource)
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
			def cloudStoreLink = getCloudStoreClass(cloudStoreName)
			if(cloudStoreLink) {
				updateSingleCloudStore(account, cloudStoreName, cloudStoreLink)
			}
			else {
				log.debug "Bad cloud store specified when running manual update: {}", cloudStoreName
			}
		}
		else {
			log.debug "Manually updating all cloud store file resources"
			account.cloudStores.each{
				if(it.storeName != 'intercloud') {
					def cloudStoreLink = getCloudStoreClass(it.storeName)
					updateSingleCloudStore(account, it.storeName, cloudStoreLink)
				}
			}
		}
	}

	private def updateSingleCloudStore(Account account, String storeName, def cloudStoreLink) {
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)

		String updateCursor = cloudStore.updateCursor
		def currentFileResources = cloudStore.fileResources

		def newUpdateCursor = cloudStoreLink.updateResources(cloudStore, updateCursor, currentFileResources)
		if(newUpdateCursor) {
			cloudStore.updateCursor = newUpdateCursor
			cloudStore.save(flush:true)
		}
	}

	public boolean uploadResource(Account account, String cloudStoreName, def uploadedFile) {
		CloudStore cloudStore = account.cloudStores.find { it.storeName == cloudStoreName}
		def cloudStoreLink = getCloudStoreClass(cloudStoreName)
		def newUpdateCursor

		if(cloudStoreName == 'intercloud') {
			createFileResourceFromUploadedFile(account, cloudStore, uploadedFile, null)
			BigInteger spaceToAdd = uploadedFile.getSize()
			updateIntercloudSpace(cloudStore, spaceToAdd)
		}
		else if(cloudStoreLink) {
			log.debug "Checking for updates before upload"
			String updateCursor = cloudStore.updateCursor
			def currentFileResources = cloudStore.fileResources
			newUpdateCursor = cloudStoreLink.updateResources(cloudStore, updateCursor, currentFileResources)
			cloudStore.updateCursor = newUpdateCursor

			if(cloudStoreName == 'dropbox') {
				boolean isSuccess = uploadToDropbox(cloudStoreLink, cloudStore, uploadedFile)
				if(!isSuccess) {
					return false
				}
			}
			else if(cloudStoreName == 'googledrive') {
				boolean isSuccess = uploadToGoogledrive(cloudStoreLink, cloudStore, uploadedFile)
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

	private boolean uploadToDropbox(def cloudStoreLink, CloudStore cloudStore, def uploadedFile) {
		String newFileName = null
		def cloudStoreUploadName = cloudStoreLink.uploadResource(cloudStore, uploadedFile)

		if(!cloudStoreUploadName) {
			return false
		}
		if(cloudStoreUploadName != uploadedFile.originalFilename) {
			newFileName = cloudStoreUploadName
		}

		createFileResourceFromUploadedFile(cloudStore.account, cloudStore, uploadedFile, newFileName)

		return true
	}

	private boolean uploadToGoogledrive(def cloudStoreLink, CloudStore cloudStore, def uploadedFile) {
		String extraMetadata = cloudStoreLink.uploadResource(cloudStore, uploadedFile)
		createFileResourceFromUploadedFile(cloudStore.account, cloudStore, uploadedFile, extraMetadata)
		return true
	}

	private void createFileResourceFromUploadedFile(Account account, CloudStore cloudStore, def uploadedFile, String extraData) {
		FileResource fileResource = new FileResource()

		String filePath = "/" + uploadedFile.originalFilename
		fileResource.fileName = uploadedFile.originalFilename

		if(cloudStore.storeName == 'intercloud') {
			log.debug "Saving uploaded file to local file system for InterCloud cloud store"
			String accountEmail = account.email
			String dirLocationOnFileSystem = INTERCLOUD_STORAGE_PATH + '/' + accountEmail + '/InterCloudRoot'
			String locationOnFileSystem = dirLocationOnFileSystem + '/' + uploadedFile.originalFilename
			fileResource.locationOnFileSystem = locationOnFileSystem
			saveFileToLocalFileSystem(locationOnFileSystem, uploadedFile)
		}
		else if(cloudStore.storeName == 'dropbox') {
			if(extraData) {
				filePath = "/" + extraData
				fileResource.fileName = extraData
			}
		}
		else if(cloudStore.storeName == 'googledrive') {
			fileResource.extraMetadata = extraData
		}

		fileResource.path = filePath
		fileResource.byteSize = uploadedFile.size
		fileResource.mimeType = uploadedFile.contentType
		fileResource.isDir = false
		fileResource.cloudStore = cloudStore
		fileResource.modified = new Date()

		FileResource parentFileResource = cloudStore.fileResources.find { it.path == '/' }
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
			flash.message = message(code: 'localsystem.couldnotsave')
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

	private void updateIntercloudSpace(CloudStore cloudStore, BigInteger spaceToChange) {
		log.debug "Updating intercloud space"
		cloudStore.spaceUsed += spaceToChange
		cloudStore.save(flush:true)
	}
}
