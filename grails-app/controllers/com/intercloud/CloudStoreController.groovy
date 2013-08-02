package com.intercloud

import com.intercloud.cloudstore.*

class CloudStoreController extends BaseController {
	
	def index() {
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

    private def requestClientAccessToCloudStore(def cloudStoreToAdd) {
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
	
	private def getCloudStoreLink(def cloudStoreName) {
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
			print cloudStoreInstance.errors.allErrors
		}
	}
	
	def getHomeCloudStoreResources(def storeName) {
		def dir = "/"
		def fileResource = getFileResourceFromPath(storeName, dir)
		return retrieveFilesInDir(fileResource)	
	}
	
	def getCloudStoreResources() {
		def storeName = params.cloudStore
		def dir = "/"
		def fileResource = getFileResourceFromPath(storeName, dir)

		renderFilesInFileResourceFolder(storeName, fileResource)
	}
	
	def getSpecificCloudStoreResources() {
		def storeName = params.cloudStore
		def fileResourcePath = '/' + params.fileResourcePath
		
		def fileResource = getFileResourceFromPath(storeName, fileResourcePath)
		if(fileResource) {
			def isDir = isFileResourceDir(fileResource)
			if(isDir) {
				renderFilesInFileResourceFolder(storeName, fileResource)
			}
			else {
				retrieveFileResource(storeName, fileResource)
			}
		}
		else {
			forward(controller: 'base', action: 'respondPageNotFound')
		}
	}
	
	private def getFileResourceFromPath(def storeName, def fileResourcePath) {
		Account account = getCurrentAccount()
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		if(cloudStore) {
			def fileResources = cloudStore.fileResources
			return cloudStore.fileResources.find { it.path == fileResourcePath }
		}
	}
	
	private def isFileResourceDir(def fileResource) {
		if(fileResource.isDir) {
			return true
		}
		else {
			return false
		}
	}
	
	private def renderFilesInFileResourceFolder(def storeName, def fileResource) {
		def cloudStoreFiles = retrieveFilesInDir(fileResource)
		render (view : storeName, model: [fileInstanceList: cloudStoreFiles])
	}
	
	private def retrieveFilesInDir(FileResource fileResource) {
		return fileResource?.fileResources
	}
	
	private def retrieveFileResource(def storeName, FileResource fileResource) {
		if(fileResource.mimeType in RENDER_TYPES) {
			def cloudStoreFileData = getFileResourceData(storeName, fileResource)
			if(cloudStoreFileData) {
				renderBytesToScreen(fileResource, cloudStoreFileData)
			}
			else {
				forward(controller: 'base', action: 'respondServerError')
			}
		}
		else if(fileResource.mimeType in VIDEO_TYPES){
			def cloudStoreFileData = getFileResourceData(storeName, fileResource)
			if(cloudStoreFileData) {
				displayVideo(fileResource, cloudStoreFileData, storeName /*wont need storename normally*/)
			}
			else {
				forward(controller: 'base', action: 'respondServerError')
			}
		}
		else {
			renderDownloadLink(fileResource, storeName)
		}
	}
	
	private def renderBytesToScreen(FileResource fileResource, def cloudStoreFileData) {
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
	
	private def displayVideo(FileResource fileResource, def cloudStoreFileData, def storeName /*wont need storeName normally*/) {
		// need to figure out how to buffer video then show render in a video
		// just allow downloads for now 
		renderDownloadLink(fileResource, storeName)
	}
	
	private def renderDownloadLink(FileResource fileResource, def storeName) {
		try{
			def downloadLink = "<html><head></head><body><a href='/download?fileResourceId=${fileResource.id}&storeName=${storeName}'>Download File</a></body></html>"

			//response.contentType = "text/html"
			response.outputStream << downloadLink
			response.outputStream.flush()
		}
		catch (Exception) {
			forward(controller: 'base', action: 'respondServerError')
		}
	}
	
	def getFileResourceData(def storeName, def fileResource) {
		Account account = getCurrentAccount()
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		
		def resourceData = fileResource.bytes
		if(!resourceData) {
			resourceData = downloadFileResourceFromCloudStore(cloudStore, fileResource)
		}
		return resourceData
	}
	
	private def downloadFileResourceFromCloudStore(CloudStore cloudStore, FileResource fileResource) {
		def cloudStoreLink = getCloudStoreLink(cloudStore.storeName)
		def downloadedFile = cloudStoreLink.downloadResource(cloudStore.credentials, fileResource)
		return downloadedFile
	}
	
	def deleteResource() {
		def storeName = params.cloudStore
		def fileResource = FileResource.get(params.fileResourceId)
		
		deleteFromFileResource(storeName, fileResource)
		if(storeName != 'intercloud') {
			deleteFromCloudStoreLink(storeName, fileResource)
		}

		redirect(uri: params.targetUri)
	}

	private def deleteFromFileResource(def storeName, FileResource fileResource) {
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
	
	private def deleteFromCloudStoreLink(def storeName, FileResource fileResource) {
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
			// not supported
		}
	}
	
	def showDownloadDialog() {
		FileResource fileResource = FileResource.get(params.fileResourceId)
		def storeName = params.storeName
		
		if(fileResource) {
			if(fileResource.isDir) {
				showZippedFolderDownload(storeName, fileResource)
			}
			else {
				showSingleFileDownload(storeName, fileResource)
			}
		}
		else {
			forward(controller: 'base', action: 'respondPageNotFound')
		}
	}
	
	private def showZippedFolderDownload(def storeName, FileResource fileResource) {
		// have to download all sub files and folders and zip together
		def fileResourceData = null //zipped file
		
		try {
			response.contentType = "application/octet-stream"
			response.contentLength = fileResourceData.length()
			response.setHeader "Content-disposition", "attachment;filename=${fileResource.fileName}.zip"
			response.outputStream << fileResourceData
			response.outputStream.flush()
		}
		catch(Exception) {
			forward(controller: 'base', action: 'respondServerError')
		}
	}
	
	private def showSingleFileDownload(def storeName, FileResource fileResource) {
		def fileResourceData = getFileResourceData(storeName, fileResource)
		try{
			response.contentType = fileResource.mimeType
			response.contentLength = fileResource.byteSize.toInteger()
			response.setHeader "Content-disposition", "attachment;filename=${fileResource.fileName}"
			response.outputStream << fileResourceData
			response.outputStream.flush()
		}
		catch (Exception) {
			forward(controller: 'base', action: 'respondServerError')
		}
	}
}
