package com.intercloud.cloudstore

import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.FileResource
import javax.servlet.http.HttpServletRequest

interface CloudStoreInterface {
	def configure(boolean isAuthRedirect, HttpServletRequest request)
	def setCloudStoreProperties(CloudStore cloudStoreInstance, Account account)
	def uploadResource(def credentials, def fileResource)
	def updateResource(def credentials, def fileResource)
	def downloadResource(def credentials, def fileResource)
	def deleteResource(def credentials, def fileResource)
}
