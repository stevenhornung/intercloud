package com.intercloud.cloudstore

import java.io.InputStream

import javax.servlet.http.HttpServletRequest

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.FileResource

class AwsS3CloudStore implements CloudStoreInterface {

	private static Logger log = LoggerFactory.getLogger(AwsS3CloudStore.class)

	static String STORE_NAME
	static String AWS_CREDENTIAL_URL
	static String REDIRECT_URL

	private String accessKey
	private String secretKey

	public def configure(boolean isAuthRedirect, HttpServletRequest request) {
		if(!isAuthRedirect) {
			log.debug "Requesting credentials for AWS S3 access"
			return AWS_CREDENTIAL_URL
		}
		else {
			setAccessTokenForConfigure(request.parameterMap)
		}
	}

	private void setAccessTokenForConfigure(def parameterMap) {
		accessKey = parameterMap.accessKey
		secretKey = parameterMap.secretKey
	}

	public boolean setCloudStoreProperties(CloudStore cloudStoreInstance, Account account) {
		setCloudStoreInfo(cloudStoreInstance)
		setCloudStoreFileResources(cloudStoreInstance)
		setCloudStoreAccount(cloudStoreInstance, account)
	}

	private void setCloudStoreInfo(CloudStore cloudStoreInstance) {

	}

	private def setCloudStoreFileResources(CloudStore cloudStoreInstance) {
		def fileResources = getAllAwsS3Resources(cloudStoreInstance)
		cloudStoreInstance.fileResources.clear()
		cloudStoreInstance.fileResources = fileResources
	}

	private def getAllAwsS3Resources(CloudStore cloudStoreInstance) {

	}

	private def setCloudStoreAccount(CloudStore cloudStoreInstance, Account account) {
		cloudStoreInstance.account = account
		account.addToCloudStores(cloudStoreInstance)
	}

	public def uploadResource(CloudStore cloudStore, def uploadedFile, String parentPath, boolean isDir) {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream downloadResource(CloudStore cloudStore, FileResource fileResource) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean deleteResource(CloudStore cloudStore, FileResource fileResource) {
		// TODO Auto-generated method stub

	}

	public def updateResources(CloudStore cloudStore, String updateCursor, def currentFileResources) {
		// TODO Auto-generated method stub
		return null;
	}

}
