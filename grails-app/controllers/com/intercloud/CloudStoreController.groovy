package com.intercloud

import com.intercloud.cloudstore.DropboxCloudStore
import grails.converters.JSON

class CloudStoreController {
	
	static def currentCloudStore
	
	def index = {
		def cloudStoreToAdd = params["cloudStore"]
		requestClientAccess(cloudStoreToAdd)
	}

    private def requestClientAccess(def cloudStoreToAdd) {
		if(cloudStoreToAdd == 'Dropbox') {
			currentCloudStore = new DropboxCloudStore()
			def clientAccessRequestUrl = currentCloudStore.clientAccessRequestUrl
		
			redirect url : clientAccessRequestUrl
		}
	}
	
	def authRedirect = {
		currentCloudStore.setClientAccessCredentials()
		
		def cloudStoreInstance = new CloudStore()
		currentCloudStore.populateCloudStoreInstance(cloudStoreInstance)
		cloudStoreInstance.save(flush: true)
		
		currentCloudStore = null
		
		def storeString = "storeName: $cloudStoreInstance.storeName, creds:$cloudStoreInstance.credentials, \
						uid:$cloudStoreInstance.uid, name:$cloudStoreInstance.fullName, \
						usedSpace:$cloudStoreInstance.spaceUsed, totalSpace:$cloudStoreInstance.totalSpace"
		
		render storeString
	}
}
