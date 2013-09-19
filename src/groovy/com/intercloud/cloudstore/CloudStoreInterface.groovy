package com.intercloud.cloudstore

import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.FileResource
import javax.servlet.http.HttpServletRequest

interface CloudStoreInterface {
	public def configure(boolean isAuthRedirect, HttpServletRequest request)
	public def setCloudStoreProperties(CloudStore cloudStoreInstance, Account account)
	public def uploadResource(CloudStore cloudStore, def uploadedFile)
	public def downloadResource(def credentials, FileResource fileResource)
	public def deleteResource(CloudStore cloudStore, FileResource fileResource)
	public def updateResources(CloudStore cloudStore, String updateCursor, def currentFileResources)
}
