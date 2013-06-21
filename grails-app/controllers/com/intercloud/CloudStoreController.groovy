package com.intercloud

import com.intercloud.cloudstore.DropboxCloudStore

class CloudStoreController extends BaseController {
	
	def index() {
		if(session.user != null) {
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
		
		def clientAccessRequestUrl = currentCloudStore?.getClientAccessRequestUrl()

		flash.currentCloudStore = currentCloudStore
		redirect(url : clientAccessRequestUrl)
	}
	
	def authRedirect = {
		def currentCloudStore = flash.currentCloudStore
		currentCloudStore.setClientAccessCredentials()
		
		saveCloudStoreInstance(currentCloudStore)
		
		redirect(controller: 'home', action:'index')
	}
	
	private def saveCloudStoreInstance(def currentCloudStore) {
		def cloudStoreInstance = new CloudStore()
		currentCloudStore.setCloudStoreInstanceProperties(cloudStoreInstance, session)

		if(!cloudStoreInstance.save(flush: true)) {
			// show message that cloud store link failed, and ask to retry
			print cloudStoreInstance.errors.allErrors
		}
	}
	
	def listCloudStoreFiles() {
		def cloudStoreFiles = null
		def storeName = params.cloudStore
		
		if(session.user != null) {
			cloudStoreFiles = retrieveAllFilesByCloudStore(session, storeName)
		}
		render (view : storeName, model: [fileInstanceList: cloudStoreFiles])
	}
	
	def retrieveAllFilesByCloudStore(def session, def storeName) {
		Account account = Account.findByEmail(session.user.email)
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		return cloudStore?.fileResources
	}
	
	def retrieveFileResource() {
		def cloudStoreFileData = null
		def storeName = params.cloudStore
		def fileResourceId = params.fileResourceId
		
		if(session.user != null) {
			cloudStoreFileData = retrieveSingleFileResourceData(fileResourceId, session, storeName)
			response.outputStream << cloudStoreFileData
		}
		else {
			redirect(controller: 'home', action: 'index')
		}
	}
	
	def retrieveSingleFileResourceData(def fileResourceId, def session, def storeName) {
		def fileResource = FileResource.get(fileResourceId)
		def resourceData = fileResource.bytes
		if(!resourceData) {
			Account account = Account.findByEmail(session.user.email)
			CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)

			if(storeName == 'dropbox') {
				def dropboxCloudStore = new DropboxCloudStore()
				def downloadedFile = dropboxCloudStore.downloadResources(cloudStore.credentials, fileResource)
				resourceData = downloadedFile[0]
			}
		}
		return resourceData
	}
	
}
