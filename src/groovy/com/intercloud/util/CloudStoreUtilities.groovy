package com.intercloud.util

import com.intercloud.Account
import com.intercloud.BaseController
import com.intercloud.CloudStore
import com.intercloud.FileResource;

class CloudStoreUtilities {
	
	public static def deleteFromDatabase(String storeName, FileResource fileResource) {
		// Delete parent file resource relationship if parent not already deleted
		fileResource.parentFileResource?.removeFromChildFileResources(fileResource)
		fileResource.parentFileResource?.save()
		
		// Delete cloud store relationship
		fileResource.cloudStore?.removeFromFileResources(fileResource)
		fileResource.cloudStore?.save()
		
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
		return baseController.getCurrentAccount()
	}
}
