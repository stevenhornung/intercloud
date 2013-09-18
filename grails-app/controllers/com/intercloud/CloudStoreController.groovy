package com.intercloud

import com.intercloud.cloudstore.*
import com.intercloud.util.CloudStoreUtilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CloudStoreController extends BaseController {
	
	private static Logger log = LoggerFactory.getLogger(CloudStoreController.class)
	static String INTERCLOUD_STORAGE_PATH = "storage/InterCloudStorage"
	
	public def index() {
		if(getCurrentAccount()) {
			String storeName = params.storeName
			if(storeName) {
				log.debug "Adding cloud store '{}'", storeName
				requestClientAccessToCloudStore(storeName)
			}
			else {
				redirect(controller: 'home', action: 'index')
			}
		}
		else {
			log.warn "Passed spring security as logged in user but getCurrentAccount returned null"
			forward(controller: 'base', action: 'respondServerError')
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
	
	public def getCloudStoreResources() {
		Account account = getCurrentAccount()
		def storeName = params.storeName
		def dir = "/"
		def fileResource = getFileResourceFromPath(account, storeName, dir)

		renderFilesInFileResourceFolder(storeName, fileResource)
	}
	
	public def getSpecificCloudStoreResources() {
		Account account = getCurrentAccount()
		def storeName = params.storeName
		def fileResourcePath = '/' + params.fileResourcePath
		
		FileResource fileResource = getFileResourceFromPath(account, storeName, fileResourcePath)
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
	
	private def renderFilesInFileResourceFolder(String storeName, FileResource fileResource) {
		def cloudStoreFiles = retrieveFilesInDir(fileResource)
		render (view : storeName, model: [fileInstanceList: cloudStoreFiles])
	}
	
	private def retrieveFilesInDir(FileResource fileResource) {
		return fileResource?.childFileResources
	}
	
	private def retrieveFileResource(String storeName, FileResource fileResource) {
		if(fileResource.mimeType in RENDER_TYPES) {
			def cloudStoreFileStream = getFileResourceStream(storeName, fileResource)
			if(cloudStoreFileStream) {
				renderBytesToScreen(fileResource, cloudStoreFileStream)
			}
			else {
				log.warn "File resource data could not be retrieved from {}", storeName
				forward(controller: 'base', action: 'respondServerError')
			}
		}
		else if(fileResource.mimeType in VIDEO_TYPES){
			def cloudStoreFileStream = getFileResourceStream(storeName, fileResource)
			if(cloudStoreFileStream) {
				displayVideo(fileResource, cloudStoreFileStream, storeName /*wont need storename normally*/)
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
	
	private def renderBytesToScreen(FileResource fileResource, InputStream cloudStoreFileStream) {
		try{
			response.contentType = fileResource.mimeType
			response.contentLength = fileResource.byteSize.toInteger()
			response.outputStream << cloudStoreFileStream
			response.outputStream.flush()
		}
		catch (Exception) {
			//Do nothing, Client clicked out during load of data
		}
	}
	
	private def displayVideo(FileResource fileResource, InputStream cloudStoreFileStream, String storeName /*wont need storeName normally*/) {
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
	
	public def getFileResourceStream(String storeName, FileResource fileResource) {
		def resourceDataStream = null
		if(storeName == 'intercloud') {
			String locationOnFileSystem = fileResource.locationOnFileSystem
			resourceDataStream = getStreamFromFileLocation(locationOnFileSystem)
		}
		else {
			CloudStore cloudStore = fileResource.cloudStore
			resourceDataStream = downloadFileResourceFromCloudStore(cloudStore, fileResource)
		}
		
		return resourceDataStream
	}
	
	private InputStream getStreamFromFileLocation(String locationOnFileSystem) {
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
	
	public def deleteResource() {
		def storeName = params.storeName
		def fileResource = FileResource.get(params.fileResourceId)
		
		CloudStoreUtilities.deleteFromDatabase(fileResource)
		if(storeName == 'intercloud') {
			deleteFromLocalFileSystem(fileResource)
		}
		else {
			deleteFromCloudStoreLink(storeName, fileResource)
		}

		redirect(uri: params.targetUri)
	}
	
	private void deleteFromLocalFileSystem(FileResource fileResource) {
		File file = new File(fileResource.locationOnFileSystem)
		file.delete()
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
		def storeName = params.storeName
		if(params.fileResourceId) {
			FileResource fileResource = FileResource.get(params.fileResourceId)
		
			if(fileResource) {
				showFileResourceDownload(storeName, fileResource)
			}
			else {
				log.debug "File resource not found from download dialog"
				forward(controller: 'base', action: 'respondPageNotFound')
			}
		}
		else {
			// Download entire root as zip
			Account account = getCurrentAccount()
			CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
			FileResource rootFileResource = cloudStore.fileResources.find {it.path == "/"}
			
			showFileResourceDownload(storeName, rootFileResource)
		}
	}
	
	private def showFileResourceDownload(String storeName, FileResource fileResource) {
		InputStream fileResourceStream = getFileResourceStream(storeName, fileResource)
		try{
			response.contentType = fileResource.mimeType
			if(fileResource.isDir) {
				response.setHeader "Content-disposition", "attachment;filename=${fileResource.fileName}.zip"
			}
			else {
				response.setHeader "Content-disposition", "attachment;filename=${fileResource.fileName}"
			}
			response.outputStream << fileResourceStream
			response.outputStream.flush()
		}
		catch (Exception) {
			log.debug "File could not be sent to output stream: {}", Exception
			forward(controller: 'base', action: 'respondServerError')
		}
	}
	
	public def updateResources() {
		String cloudStoreName = params.storeName
		String targetUri = params.targetUri ?: "/home"

		if(cloudStoreName) {
			log.debug "Manually updating {} file resources", cloudStoreName
			def cloudStoreLink = getCloudStoreLink(cloudStoreName)
			if(cloudStoreLink) {
				updateSingleCloudStore(cloudStoreName, cloudStoreLink)
			}
			else {
				log.debug "Bad cloud store specified when running manual update: {}", cloudStoreName
			}
		}
		else {
			log.debug "Manually updating all cloud store file resources"
			CLOUD_STORES.each {
				if(it != 'intercloud') {
					def cloudStoreLink = getCloudStoreLink(it)
					updateSingleCloudStore(it, cloudStoreLink)
				}
			}
		}
		
		redirect uri: targetUri
	}
	
	private def updateSingleCloudStore(String storeName, def cloudStoreLink) {
		Account account = getCurrentAccount()
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		
		def credentials = cloudStore.credentials
		String updateCursor = cloudStore.updateCursor
		def currentFileResources = cloudStore.fileResources
		
		def newUpdateCursor = cloudStoreLink.updateResources(credentials, updateCursor, currentFileResources)
		cloudStore.updateCursor = newUpdateCursor
		cloudStore.save()
	}
	
	public def uploadResources() {
		String cloudStoreName = params.storeName
		log.debug "Uploading file to {}", cloudStoreName
		
		def uploadedFile = request.getFile('file')
		uploadFileToCloudStore(cloudStoreName, uploadedFile)

		response.sendError(200)
	}
	
	private void uploadFileToCloudStore(String cloudStoreName, def uploadedFile) {
		Account account = getCurrentAccount()
		CloudStore cloudStore = account.cloudStores.find { it.storeName == cloudStoreName}
		def credentials = cloudStore.credentials
		
		createFileResourceFromUploadedFile(cloudStore, uploadedFile)
		if(cloudStore.storeName == 'intercloud') {
			// only need to create file resource so just pass
		}
		else if(cloudStore.storeName == 'dropbox') {
			DropboxCloudStore dropboxCloudStore = new DropboxCloudStore()
			dropboxCloudStore.uploadResource(credentials, uploadedFile)
		}
		else if(cloudStore.storeName == 'googledrive') {
			
		}
		else {
			log.debug "Bad cloud store specified when uploading file '{}'", cloudStoreName
		}
	}
	
	private void createFileResourceFromUploadedFile(CloudStore cloudStore, def uploadedFile) {
		String filePath = "/" + uploadedFile.originalFilename
		FileResource fileResource = new FileResource()
		
		fileResource.path = filePath
		fileResource.fileName = uploadedFile.originalFilename
		fileResource.byteSize = uploadedFile.size
		fileResource.mimeType = uploadedFile.contentType
		fileResource.isDir = false
		fileResource.cloudStore = cloudStore
		
		FileResource parentFileResource = FileResource.findByCloudStoreAndPath(cloudStore, '/')
		fileResource.parentFileResource = parentFileResource
		
		if(cloudStore.storeName == 'intercloud') {
			log.debug "Saving uploaded file to local file system for InterCloud cloud store"
			String accountEmail = getCurrentAccount().email
			String dirLocationOnFileSystem = INTERCLOUD_STORAGE_PATH + '/' + accountEmail
			new File(dirLocationOnFileSystem).mkdirs()
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
}
