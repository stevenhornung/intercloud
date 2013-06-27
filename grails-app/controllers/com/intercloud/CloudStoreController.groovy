package com.intercloud

import com.intercloud.cloudstore.DropboxCloudStore

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
		def currentCloudStore = null
		if(cloudStoreToAdd == 'dropbox') {
			currentCloudStore = new DropboxCloudStore()
		}
		
		def clientAccessRequestUrl = currentCloudStore?.configure(false)

		flash.currentCloudStore = currentCloudStore
		redirect(url : clientAccessRequestUrl)
	}
	
	def authRedirect = {
		def currentCloudStore = flash.currentCloudStore
		currentCloudStore.configure(true)
		
		saveCloudStoreInstance(currentCloudStore)
		
		redirect(controller: 'home', action:'index')
	}
	
	private def saveCloudStoreInstance(def currentCloudStore) {
		def cloudStoreInstance = new CloudStore()
		def account = getCurrentAccount()
		currentCloudStore.setCloudStoreInstanceProperties(cloudStoreInstance, account)

		if(!cloudStoreInstance.save(flush: true)) {
			// show message that cloud store link failed, and ask to retry
			print cloudStoreInstance.errors.allErrors
		}
	}
	
	def listCloudStoreFiles() {
		def cloudStoreFiles = null
		def storeName = params.cloudStore
		
		if(getCurrentAccount()) {
			cloudStoreFiles = retrieveAllFilesByCloudStore(storeName)
		}
		render (view : storeName, model: [fileInstanceList: cloudStoreFiles])
	}
	
	def retrieveAllFilesByCloudStore(def storeName) {
		Account account = getCurrentAccount()
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		return cloudStore?.fileResources
	}
	
	def retrieveFileResource() {
		def cloudStoreFileData = null
		def storeName = params.cloudStore
		def fileResourcePath = params.fileResourcePath

		if(getCurrentAccount()) {
			cloudStoreFileData = retrieveSingleFileResourceData(fileResourcePath, storeName)
			if(cloudStoreFileData) {
				response.outputStream << cloudStoreFileData
			}
		}
		else {
			redirect(controller: 'home', action: 'index')
		}
	}
	
	def retrieveSingleFileResourceData(def fileResourcePath, def storeName) {
		Account account = getCurrentAccount()
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		FileResource fileResource = cloudStore.fileResources.find { it.path == '/'+fileResourcePath }
		
		if(!fileResource) {
			respondPageNotFound()
		} 
		
		else {
			def resourceData = fileResource.bytes
			if(!resourceData) {
				if(storeName == 'dropbox') {
					def dropboxCloudStore = new DropboxCloudStore()
					def downloadedFile = dropboxCloudStore.downloadResource(cloudStore.credentials, fileResource)
					resourceData = downloadedFile
				}
			}
			return resourceData
		}
	}
}
