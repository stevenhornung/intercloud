package com.intercloud.cloudstore

import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.FileResource
import javax.servlet.http.HttpServletRequest

interface CloudStoreInterface {
	public static String STORE_NAME

	public def configure(boolean isAuthRedirect, HttpServletRequest request)
	public boolean setCloudStoreProperties(CloudStore cloudStoreInstance, Account account)
	public def uploadResource(CloudStore cloudStore, def uploadedFile, String parentPath)
	public InputStream downloadResource(CloudStore cloudStore, FileResource fileResource)
	public boolean deleteResource(CloudStore cloudStore, FileResource fileResource)
	public def updateResources(CloudStore cloudStore, String updateCursor, def currentFileResources)
}
