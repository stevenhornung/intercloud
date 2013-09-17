package com.intercloud.util

import com.intercloud.Account
import com.intercloud.BaseController
import com.intercloud.CloudStore
import com.intercloud.FileResource;

class CloudStoreUtilities {
	
	public static void deleteFromDatabase(FileResource fileResource) {
		// Delete parent file resource relationship if parent not already deleted
		fileResource.parentFileResource?.removeFromChildFileResources(fileResource)
		fileResource.parentFileResource?.save()
		
		// Delete cloud store relationship
		fileResource.cloudStore?.removeFromFileResources(fileResource)
		fileResource.cloudStore?.save()
		
		// Delete any children relationships
		fileResource.childFileResources.each { def childFileResource ->
			deleteFromDatabase(childFileResource)
		}
	}
}
