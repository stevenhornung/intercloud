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
	
	private def retrieveFileResource(def storeName, def fileResource) {
		def cloudStoreFileData = null
		cloudStoreFileData = getFileResourceData(storeName, fileResource)
		
		if(cloudStoreFileData) {
			try{
				response.outputStream << cloudStoreFileData
			}
			catch (Exception) {
				//Do nothing, Client clicked out during load of data
			}
		}
		else {
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
}
