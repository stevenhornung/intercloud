package com.intercloud.cloudstore

import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.FileResource
import javax.servlet.http.HttpServletRequest

interface CloudStoreInterface {
	public def configure(boolean isAuthRedirect, HttpServletRequest request)
	public void setCloudStoreProperties(CloudStore cloudStoreInstance, Account account)
	public def uploadResource(CloudStore cloudStore, def uploadedFile)
	public InputStream downloadResource(def credentials, FileResource fileResource)
	public void deleteResource(CloudStore cloudStore, FileResource fileResource)
	public def updateResources(CloudStore cloudStore, String updateCursor, def currentFileResources)
}
