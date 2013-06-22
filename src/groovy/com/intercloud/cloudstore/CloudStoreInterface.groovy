package com.intercloud.cloudstore

import com.intercloud.Account
import com.intercloud.FileResource

interface CloudStoreInterface {
	def configure(boolean isAuthRedirect)
	def setCloudStoreInstanceProperties(def cloudStoreInstance, def session)
	def uploadResource(def credentials, def fileResource)
	def updateResource(def credentials, def fileResource)
	def downloadResource(def credentials, def fileResource)
}
