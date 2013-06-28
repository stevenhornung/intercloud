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
			def clientAccessRequestUrl = currentCloudStoreLink.configure(false)
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
		currentCloudStoreLink.configure(true)
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
			cloudStoreFileData = getFileResourceData(fileResourcePath, storeName)
			if(cloudStoreFileData) {
				response.outputStream << cloudStoreFileData
			}
		}
		else {
			redirect(controller: 'home', action: 'index')
		}
	}
	
	def getFileResourceData(def fileResourcePath, def storeName) {
		Account account = getCurrentAccount()
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		FileResource fileResource = cloudStore?.fileResources.find { it.path == '/'+fileResourcePath }
		
		if(!fileResource) {
			// redirect to page not found
			respondPageNotFound()
		} 
		
		else {
			def resourceData = fileResource.bytes
			if(!resourceData) {
				resourceData = downloadFileResourceFromCloudStore(storeName, cloudStore, fileResource)
			}
			return resourceData
		}
	}
	private def downloadFileResourceFromCloudStore(def storeName, CloudStore cloudStore, FileResource fileResource) {
		def cloudStoreLink = getCloudStoreLink(storeName)
		def downloadedFile = cloudStoreLink.downloadResource(cloudStore.credentials, fileResource)
		return downloadedFile
	}
}
