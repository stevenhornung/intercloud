package com.intercloud

import com.intercloud.cloudstore.DropboxCloudStore

class CloudStoreController extends BaseController {
	
	static def currentCloudStore
	
	def index() {
		def cloudStoreToAdd = params["cloudStore"]
		requestClientAccessToCloudStore(cloudStoreToAdd)
	}

    private def requestClientAccessToCloudStore(def cloudStoreToAdd) {
		if(cloudStoreToAdd == 'dropbox') {
			currentCloudStore = new DropboxCloudStore()
			def clientAccessRequestUrl = currentCloudStore.getClientAccessRequestUrl()

			redirect(url : clientAccessRequestUrl)
		}
	}
	
	def authRedirect = {
		currentCloudStore.setClientAccessCredentials()
		
		populateAndSaveCloudStoreInstance()
		
		redirect(controller: 'home', action:'index')
	}
	
	private def populateAndSaveCloudStoreInstance() {
		def cloudStoreInstance = new CloudStore()
		currentCloudStore.populateCloudStoreInstance(cloudStoreInstance)
		
		def fileResources = currentCloudStore.retrieveAllResourcesInfo()
		for(fileResource in fileResources) {
			if(!fileResource.save()) {
				print fileResource.errors.allErrors
			}
		}
		cloudStoreInstance.fileResources = fileResources
		cloudStoreInstance.save(flush: true)
	}
	
	def retrieveAllFiles() {
		[fileInstanceList: CloudStore.findByStoreName('dropbox')]
	}
}
