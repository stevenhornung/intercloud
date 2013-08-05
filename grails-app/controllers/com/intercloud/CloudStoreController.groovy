package com.intercloud

import com.intercloud.cloudstore.*

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CloudStoreController extends BaseController {
	
	private static Logger log = LoggerFactory.getLogger(CloudStoreController.class)
	
	public def index() {
		if(getCurrentAccount()) {
			if(params.cloudStore) {
				requestClientAccessToCloudStore(params.cloudStore)
			}
			else {
				redirect(controller: 'home', action: 'index')
			}
		}
		else {
			redirect(controller: 'home', action: 'index')
		}
	}

    private def requestClientAccessToCloudStore(String cloudStoreToAdd) {
		def currentCloudStoreLink = getCloudStoreLink(cloudStoreToAdd)
		if(currentCloudStoreLink) {
			def clientAccessRequestUrl = currentCloudStoreLink.configure(false, request)
			flash.currentCloudStoreLink = currentCloudStoreLink
			
			redirect(url : clientAccessRequestUrl)
		}
		else {
			// Bad Cloud Store
			redirect(controller: 'home', action: 'index')
		}
	}
	
	private def getCloudStoreLink(String cloudStoreName) {
		def cloudStoreLink = null
		
		if(cloudStoreName == 'dropbox') {
			cloudStoreLink = new DropboxCloudStore()
		}
		else if(cloudStoreName == 'googledrive') {
			cloudStoreLink = new GoogledriveCloudStore()
		}
		
		return cloudStoreLink
	}
	
	def authRedirect = {
		log.debug "Auth redirect"
		def currentCloudStoreLink = flash.currentCloudStoreLink
		currentCloudStoreLink.configure(true, request)
		saveCloudStoreInstance(currentCloudStoreLink)
		
		redirect(controller: 'home', action:'index')
	}
	
	private def saveCloudStoreInstance(def currentCloudStoreLink) {
		CloudStore cloudStoreInstance = new CloudStore()
		Account account = getCurrentAccount()
		currentCloudStoreLink.setCloudStoreProperties(cloudStoreInstance, account)

		if(!cloudStoreInstance.save(flush: true)) {
			// show message that cloud store link failed, and ask to retry
			log.warn "Cloud store link failed: {}", cloudStoreInstance.errors.allErrors
		}
	}
	
	public def getHomeCloudStoreResources(String storeName) {
		def dir = "/"
		def fileResource = getFileResourceFromPath(storeName, dir)
		return retrieveFilesInDir(fileResource)	
	}
	
	public def getCloudStoreResources() {
		def storeName = params.cloudStore
		def dir = "/"
		def fileResource = getFileResourceFromPath(storeName, dir)

		renderFilesInFileResourceFolder(storeName, fileResource)
	}
	
	public def getSpecificCloudStoreResources() {
		def storeName = params.cloudStore
		def fileResourcePath = '/' + params.fileResourcePath
		
		FileResource fileResource = getFileResourceFromPath(storeName, fileResourcePath)
		if(fileResource) {
			if(fileResource.isDir) {
				renderFilesInFileResourceFolder(storeName, fileResource)
			}
			else {
				retrieveFileResource(storeName, fileResource)
			}
		}
		else {
			log.debug "Could not find specific cloud store resources: {}", fileResourcePath
			forward(controller: 'base', action: 'respondPageNotFound')
		}
	}
	
	private def getFileResourceFromPath(String storeName, String fileResourcePath) {
		Account account = getCurrentAccount()
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		if(cloudStore) {
			def fileResources = cloudStore.fileResources
			return cloudStore.fileResources.find { it.path == fileResourcePath }
		}
	}
	
	private def renderFilesInFileResourceFolder(String storeName, FileResource fileResource) {
		def cloudStoreFiles = retrieveFilesInDir(fileResource)
		render (view : storeName, model: [fileInstanceList: cloudStoreFiles])
	}
	
	private def retrieveFilesInDir(FileResource fileResource) {
		return fileResource?.childFileResources
	}
	
	private def retrieveFileResource(String storeName, FileResource fileResource) {
		if(fileResource.mimeType in RENDER_TYPES) {
			def cloudStoreFileData = getFileResourceData(storeName, fileResource)
			if(cloudStoreFileData) {
				renderBytesToScreen(fileResource, cloudStoreFileData)
			}
			else {
				log.warn "File resource data could not be retrieved from {}", storeName
				forward(controller: 'base', action: 'respondServerError')
			}
		}
		else if(fileResource.mimeType in VIDEO_TYPES){
			def cloudStoreFileData = getFileResourceData(storeName, fileResource)
			if(cloudStoreFileData) {
				displayVideo(fileResource, cloudStoreFileData, storeName /*wont need storename normally*/)
			}
			else {
				log.warn "File resource data could not be retrieved from {}", storeName
				forward(controller: 'base', action: 'respondServerError')
			}
		}
		else {
			renderDownloadLink(fileResource, storeName)
		}
	}
	
	private def renderBytesToScreen(FileResource fileResource, byte[] cloudStoreFileData) {
		try{
			response.contentType = fileResource.mimeType
			response.contentLength = fileResource.byteSize.toInteger()
			response.outputStream << cloudStoreFileData
			response.outputStream.flush()
		}
		catch (Exception) {
			//Do nothing, Client clicked out during load of data
		}
	}
	
	private def displayVideo(FileResource fileResource, byte[] cloudStoreFileData, String storeName /*wont need storeName normally*/) {
		// need to figure out how to buffer video then show render in a video
		// just allow downloads for now 
		renderDownloadLink(fileResource, storeName)
	}
	
	private def renderDownloadLink(FileResource fileResource, String storeName) {
		try{
			def downloadLink = "<html><head></head><body><a href='/download?fileResourceId=${fileResource.id}&storeName=${storeName}'>Download File</a></body></html>"

			response.outputStream << downloadLink
			response.outputStream.flush()
		}
		catch (Exception) {
			log.debug "Download link could not be rendered to output stream: {}", Exception
			forward(controller: 'base', action: 'respondServerError')
		}
	}
	
	public def getFileResourceData(String storeName, FileResource fileResource) {
		Account account = getCurrentAccount()
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		
		byte[] resourceData = null
		String locationOnFileSystem = fileResource.locationOnFileSystem
		
		if(locationOnFileSystem) {
			resourceData = getBytesFromFileLocation(locationOnFileSystem)
		}
		else {
			resourceData = downloadFileResourceFromCloudStore(cloudStore, fileResource)
		}
		
		return resourceData
	}
	
	private byte[] getBytesFromFileLocation(String locationOnFileSystem) {
		File file = new File(locationOnFileSystem)
		byte[] resourceData = new Byte[(int) file.length()]
		
		try {
			FileInputStream fileInputStream = new FileInputStream(file)
			fileInputStream.read(resourceData)
			// need to build zip file
		}
		catch(FileNotFoundException) {
			log.warn "File not found on file system"
		}
		catch(IOException) {
			log.warn "File could not be read on file system"
		}
		
		return resourceData
	}
	
	private def downloadFileResourceFromCloudStore(CloudStore cloudStore, FileResource fileResource) {
		def cloudStoreLink = getCloudStoreLink(cloudStore.storeName)
		def downloadedFile = cloudStoreLink.downloadResource(cloudStore.credentials, fileResource)
		return downloadedFile
	}
	
	public def deleteResource() {
		def storeName = params.cloudStore
		def fileResource = FileResource.get(params.fileResourceId)
		
		deleteFromFileResource(storeName, fileResource)
		if(storeName != 'intercloud') {
			deleteFromCloudStoreLink(storeName, fileResource)
		}

		redirect(uri: params.targetUri)
	}

	private def deleteFromFileResource(String storeName, FileResource fileResource) {
		// Delete parent file resource relationship
		def parentDirPath = fileResource.path.substring(0, fileResource.path.lastIndexOf('/')+1)
		FileResource parentResource = getFileResourceFromPath(storeName, parentDirPath) 
		parentResource.removeFromFileResources(fileResource)
		parentResource.save()
		
		// Delete cloud store relationship
		Account account = getCurrentAccount()
		CloudStore cloudStore = account.cloudStores.find { it.storeName == storeName }
		cloudStore.removeFromFileResources(fileResource)
		cloudStore.save()
	}
	
	private def deleteFromCloudStoreLink(String storeName, FileResource fileResource) {
		Account account = getCurrentAccount()
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		def credentials = cloudStore.credentials
		
		if(storeName == 'dropbox') {
			DropboxCloudStore dropboxCloudStore = new DropboxCloudStore()
			dropboxCloudStore.deleteResource(credentials, fileResource)
		}
		else if(storeName == 'googledrive') {
			GoogledriveCloudStore googledriveCloudStore = new GoogledriveCloudStore()
			googledriveCloudStore.deleteResource(credentials, fileResource)
		}
		else {
			log.debug "Attempt to delete from unsuppored cloud store"
		}
	}
	
	public def showDownloadDialog() {
		FileResource fileResource = FileResource.get(params.fileResourceId)
		def storeName = params.storeName
		
		if(fileResource) {
			showFileResourceDownload(storeName, fileResource)
		}
		else {
			log.debug "File resource not found from download dialog"
			forward(controller: 'base', action: 'respondPageNotFound')
		}
	}
	
	private def showFileResourceDownload(String storeName, FileResource fileResource) {
		byte[] fileResourceData = getFileResourceData(storeName, fileResource)
		try{
			response.contentType = fileResource.mimeType
			response.contentLength = fileResourceData.length
			response.setHeader "Content-disposition", "attachment;filename=${fileResource.fileName}"
			response.outputStream << fileResourceData
			response.outputStream.flush()
		}
		catch (Exception) {
			log.debug "File could not be sent to output stream: {}", Exception
			forward(controller: 'base', action: 'respondServerError')
		}
	}
}
