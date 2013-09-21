package com.intercloud

import java.io.InputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.cloudstore.*
import com.intercloud.util.*

class CloudStoreService {
	
	private static Logger log = LoggerFactory.getLogger(CloudStoreService.class)
	
	static String INTERCLOUD_STORAGE_PATH = "storage/InterCloudStorage"
	
	public def getClientAccessRequestUrl(def cloudStoreLink, def request) {
		def clientAccessRequestUrl = cloudStoreLink.configure(false, request)
		return clientAccessRequestUrl
	}
	
	public def getCloudStoreLink(String storeName) {
		def cloudStoreLink = null
		
		if(storeName == 'dropbox') {
			cloudStoreLink = new DropboxCloudStore()
		}
		else if(storeName == 'googledrive') {
			cloudStoreLink = new GoogledriveCloudStore()
		}
		
		return cloudStoreLink
	}
	
	public void authRedirect(Account account, def cloudStoreLink, request) {
		cloudStoreLink.configure(true, request)
		saveCloudStoreInstance(account, cloudStoreLink)
	}
	
	private void saveCloudStoreInstance(Account account, def currentCloudStoreLink) {
		CloudStore cloudStoreInstance = new CloudStore()
		currentCloudStoreLink.setCloudStoreProperties(cloudStoreInstance, account)

		if(!cloudStoreInstance.save()) {
			// show message that cloud store link failed, and ask to retry
			log.warn "Cloud store link failed: {}", cloudStoreInstance.errors.allErrors
		}
	}
	
	public def getHomeCloudStoreResources(Account account, String storeName) {
		def dir = "/"
		def fileResource = getFileResourceFromPath(account, storeName, dir)
		return retrieveFilesInDir(fileResource)
	}
	
	private def getFileResourceFromPath(Account account, String storeName, String fileResourcePath) {
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		if(cloudStore) {
			def fileResources = cloudStore.fileResources
			return cloudStore.fileResources.find { it.path == fileResourcePath }
		}
	}
	
	public def retrieveFilesInDir(FileResource fileResource) {
		return fileResource?.childFileResources
	}
	
	public CloudStore getAccountCloudStore(Account account, String storeName) {
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		return cloudStore
	}
	
	public def getAllCloudStoreResources(Account account, String storeName) {
		def dir = "/"
		def fileResource = getFileResourceFromPath(account, storeName, dir)
		def cloudStoreResources = retrieveFilesInDir(fileResource)
		
		return cloudStoreResources
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
		def cloudStoreLink = getCloudStoreLink(cloudStore.storeName)
		InputStream downloadedFileStream = cloudStoreLink.downloadResource(cloudStore.credentials, fileResource)
		return downloadedFileStream
	}
	
	public void deleteResource(Account account, String storeName, def fileResourceId) {
		FileResource fileResource = FileResource.get(fileResourceId)
		CloudStoreUtilities.deleteFromDatabase(fileResource)
		if(storeName == 'intercloud') {
			deleteFromLocalFileSystem(fileResource)
			
			CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
			BigInteger spaceToDelete = -(new BigInteger(fileResource.byteSize))
			updateIntercloudSpace(cloudStore, spaceToDelete)
		}
		else {
			deleteFromCloudStoreLink(account, storeName, fileResource)
		}
	}
	
	private void deleteFromLocalFileSystem(FileResource fileResource) {
		File file = new File(fileResource.locationOnFileSystem)
		file.delete()
	}
	
	private def deleteFromCloudStoreLink(Account account, String storeName, FileResource fileResource) {
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		
		if(storeName == 'dropbox') {
			DropboxCloudStore dropboxCloudStore = new DropboxCloudStore()
			dropboxCloudStore.deleteResource(cloudStore, fileResource)
		}
		else if(storeName == 'googledrive') {
			GoogledriveCloudStore googledriveCloudStore = new GoogledriveCloudStore()
			googledriveCloudStore.deleteResource(cloudStore, fileResource)
		}
		else {
			log.debug "Attempt to delete from unsuppored cloud store"
		}
	}
	
	public void updateResources(Account account, String cloudStoreName) {
		if(cloudStoreName) {
			log.debug "Manually updating {} file resources", cloudStoreName
			def cloudStoreLink = getCloudStoreLink(cloudStoreName)
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
					def cloudStoreLink = getCloudStoreLink(it.storeName)
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
		cloudStore.updateCursor = newUpdateCursor
		cloudStore.save()
	}
	
	public void uploadResource(Account account, String cloudStoreName, def uploadedFile) {
		CloudStore cloudStore = account.cloudStores.find { it.storeName == cloudStoreName}
		String newFileName = null
		
		if(cloudStore.storeName == 'intercloud') {
			// only need to create file resource so just pass
		}
		else if(cloudStore.storeName == 'dropbox') {
			DropboxCloudStore dropboxCloudStore = new DropboxCloudStore()
			
			log.debug "Checking for updates before upload to dropbox"
			String updateCursor = cloudStore.updateCursor
			def currentFileResources = cloudStore.fileResources
			def newUpdateCursor = dropboxCloudStore.updateResources(cloudStore, updateCursor, currentFileResources)
			cloudStore.updateCursor = newUpdateCursor
			cloudStore.save()
			
			def dropboxUpload = dropboxCloudStore.uploadResource(cloudStore, uploadedFile)
			if(dropboxUpload) {
				newFileName = dropboxUpload.name
			}
		}
		else if(cloudStore.storeName == 'googledrive') {
			GoogledriveCloudStore googledriveCloudStore = new GoogledriveCloudStore()
			
			log.debug "Checking for updates before upload to google drive"
			String updateCursor = cloudStore.updateCursor
			def currentFileResources = cloudStore.fileResources
			def newUpdateCursor = googledriveCloudStore.updateResources(cloudStore, updateCursor, currentFileResources)
			cloudStore.updateCursor = newUpdateCursor
			cloudStore.save()
			
			def googledriveUpload = googledriveCloudStore.uploadResource(cloudStore, uploadedFile)
			if(googledriveUpload) {
				newFileName = googledriveUpload.name
			}
		}
		else {
			log.debug "Bad cloud store specified when uploading file '{}'", cloudStoreName
			return
		}
		
		createFileResourceFromUploadedFile(account, cloudStore, uploadedFile, newFileName)
		if(cloudStore.storeName == 'intercloud') {
			BigInteger spaceToAdd = uploadedFile.getSize()
			updateIntercloudSpace(cloudStore, spaceToAdd)
		}
	}
	
	private void createFileResourceFromUploadedFile(Account account, CloudStore cloudStore, def uploadedFile, String newFileName) {
		FileResource fileResource = new FileResource()
		String filePath
		
		if(newFileName) {
			filePath = "/" + newFileName
			fileResource.fileName = newFileName
		}
		else {
			filePath = "/" + uploadedFile.originalFilename
			fileResource.fileName = uploadedFile.originalFilename
		}
		
		fileResource.path = filePath
		fileResource.byteSize = uploadedFile.size
		fileResource.mimeType = uploadedFile.contentType
		fileResource.isDir = false
		fileResource.cloudStore = cloudStore
		fileResource.modified = new Date()
		
		FileResource parentFileResource = FileResource.findByCloudStoreAndPath(cloudStore, '/')
		fileResource.parentFileResource = parentFileResource
		
		if(cloudStore.storeName == 'intercloud') {
			log.debug "Saving uploaded file to local file system for InterCloud cloud store"
			String accountEmail = account.email
			String dirLocationOnFileSystem = INTERCLOUD_STORAGE_PATH + '/' + accountEmail + '/InterCloudRoot'
			String locationOnFileSystem = dirLocationOnFileSystem + '/' + uploadedFile.originalFilename
			fileResource.locationOnFileSystem = locationOnFileSystem
			saveFileToLocalFileSystem(locationOnFileSystem, uploadedFile)
		}
		
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
	
	private void updateIntercloudSpace(CloudStore cloudStore, BigInteger spaceToChange) {
		log.debug "Updating intercloud space"
		cloudStore.spaceUsed += spaceToChange
		cloudStore.save()
	}
}
