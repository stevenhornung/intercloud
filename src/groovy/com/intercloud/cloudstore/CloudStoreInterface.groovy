package com.intercloud.cloudstore

import com.intercloud.Account
import com.intercloud.FileResource

interface CloudStoreInterface {
	def getClientAccessRequestUrl()
	def setClientAccessCredentials()
	def setCloudStoreInstanceProperties(def cloudStoreInstance, def session)
	def uploadResources(def credentials, def fileResources)
	def updateResources(def credentials, def fileResources)
	def downloadResources(def credentials, def fileResources)
}
