package com.intercloud.util

import com.intercloud.Account
import com.intercloud.BaseController
import com.intercloud.CloudStore
import com.intercloud.FileResource;

class CloudStoreUtilities {
	
	public static def deleteFromDatabase(String storeName, FileResource fileResource) {
		// Delete parent file resource relationship if parent not already deleted
		def parentDirPath = fileResource.path.substring(0, fileResource.path.lastIndexOf('/'))
		if(!parentDirPath) {
			parentDirPath = '/'
		}
		FileResource parentResource = getFileResourceFromPath(storeName, parentDirPath)
		if(parentResource) {
			parentResource.removeFromChildFileResources(fileResource)
			parentResource.save()
		}
		
		// Delete cloud store relationship
		Account account = getCurrentAccount()
		CloudStore cloudStore = account.cloudStores.find { it.storeName == storeName }
		cloudStore.removeFromFileResources(fileResource)
		cloudStore.save()
		
		// Delete any children relationships
		fileResource.childFileResources.each { def childFileResource ->
			deleteFromDatabase(storeName, childFileResource)
		}
	}
	
	public static def getFileResourceFromPath(String storeName, String fileResourcePath) {
		BaseController baseController = new BaseController()
		Account account = getCurrentAccount()
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, account)
		if(cloudStore) {
			def fileResources = cloudStore.fileResources
			return cloudStore.fileResources.find { it.path == fileResourcePath }
		}
	}
	
	private static def getCurrentAccount() {
		BaseController baseController = new BaseController()
		Account account = baseController.getCurrentAccount()
		return account
	}
}
