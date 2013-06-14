package com.intercloud

import com.intercloud.cloudstore.DropboxCloudStore

class CloudStoreController {
	
	static def currentCloudStore
	
	def index = {
		requestClientAccess()
	}

    private def requestClientAccess() {
		currentCloudStore = new DropboxCloudStore()
		def clientAccessRequestUrl = currentCloudStore.clientAccessRequestUrl
		
		redirect url : clientAccessRequestUrl
	}
	
	def authRedirect = {
		currentCloudStore.setClientAccessCredentials()
		
		def cloudStoreInstance = new CloudStore()
		currentCloudStore.populateCloudStoreInstance(cloudStoreInstance)
		cloudStoreInstance.save(flush: true)
		
		currentCloudStore = null
		
		render cloudStoreInstance.storeName
	}
}
