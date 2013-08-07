package com.intercloud.cloudstore

import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.FileResource

import javax.servlet.http.HttpServletRequest

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GoogledriveCloudStore implements CloudStoreInterface{
	
	private static Logger log = LoggerFactory.getLogger(GoogledriveCloudStore.class)

	public def configure(boolean isAuthRedirect, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	public def setCloudStoreProperties(CloudStore cloudStoreInstance, Account account) {
		// TODO Auto-generated method stub
		return null;
	}

	public def uploadResource(def credentials, FileResource fileResource) {
		// TODO Auto-generated method stub
		return null;
	}

	public def downloadResource(def credentials, FileResource fileResource) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public def deleteResource(def credentials, FileResource fileResource) {
		
	}
	
	public def updateResources(def credentials, String updateCursor, def currentFileResources) {
		// TODO Auto-generated method stub
		return null;
	}

}
