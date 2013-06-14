package com.intercloud.cloudstore

import com.intercloud.Account
import com.intercloud.FileResource

interface CloudStoreInterface {
	def getClientAccessRequestUrl()
	def setClientAccessCredentials()
	def populateCloudStoreInstance(def cloudStoreInstance)
	def retrieveAllResourcesInfo()
	def uploadResources(List<FileResource> fileResources)
	def updateResources(List<FileResource> fileResources)
	def downloadResources(List<FileResource> fileResources)
}
