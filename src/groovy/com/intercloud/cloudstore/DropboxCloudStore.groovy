package com.intercloud.cloudstore

import com.dropbox.client2.DropboxAPI
import com.dropbox.client2.DropboxAPI.Entry
import com.dropbox.client2.session.AccessTokenPair
import com.dropbox.client2.session.AppKeyPair
import com.dropbox.client2.session.RequestTokenPair
import com.dropbox.client2.session.WebAuthSession
import com.dropbox.client2.session.Session.AccessType
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo
import com.intercloud.*

class DropboxCloudStore implements CloudStoreInterface {
	
	final static STORE_NAME = "dropbox"
	final static String APP_KEY = "ujdofnwh516yrg0"
	final static String APP_SECRET = "43itigcfb9y59dy"
	final static AccessType ACCESS_TYPE = AccessType.DROPBOX
	
	static DropboxAPI<WebAuthSession> dropboxApi
	static WebAuthSession session
	static WebAuthInfo authInfo
	
	static String account_key
	static String account_secret

	def getClientAccessRequestUrl() {
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET)
		session = new WebAuthSession(appKeys, ACCESS_TYPE)
		authInfo = session.getAuthInfo()
		
		String redirectUrlParam = "&oauth_callback=http://localhost:8080/InterCloud/auth_redirect"
		String url = authInfo.url+redirectUrlParam

		return url
	}
	
	def setClientAccessCredentials() {
		RequestTokenPair pair = authInfo.requestTokenPair
		session.retrieveWebAccessToken(pair)
		setAccountTokens()
		
		dropboxApi = new DropboxAPI<WebAuthSession>(session)
	}
	
	private def setAccountTokens() {
		AccessTokenPair tokens = session.getAccessTokenPair()
		account_key = tokens.key
		account_secret = tokens.secret
	}
	
	def populateCloudStoreInstance(def cloudStoreInstance) {
		def accountInfo = retrieveAccountInfo()
		
		cloudStoreInstance.storeName = STORE_NAME
		cloudStoreInstance.credentials = [ACCOUNT_KEY : account_key, ACCOUNT_SECRET : account_secret]
		cloudStoreInstance.uid = accountInfo.uid
		cloudStoreInstance.fullName = accountInfo.displayName
		cloudStoreInstance.spaceUsed = accountInfo.quotaNormal
		cloudStoreInstance.totalSpace = accountInfo.quota
	}

	private def retrieveAccountInfo() {
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE, new AccessTokenPair(account_key, account_secret));	
		def dropboxAccountInfo = dropboxApi.accountInfo()
		
		return dropboxAccountInfo
	}

	def retrieveAllResourcesInfo() {
		Entry entries = dropboxApi.metadata("/", 100, null, true, null);
		def fileResources = retrieveFilesInDir(entries)

		return fileResources
	}
	
	private def retrieveFilesInDir(def entries) {
		def dirResources = []
		for (Entry e : entries.contents) {
			if (!e.isDeleted) {
				if(e.isDir) {
					def directory = convertFromDropboxResource(e)
					dirResources.add(directory)
					Entry dirEntries = dropboxApi.metadata(e.path, 100, null, true, null)
					dirResources.addAll(retrieveFilesInDir(dirEntries))
				}
				else {
					def fileResource = convertFromDropboxResource(e)
					dirResources.add(fileResource)
				}
			}
		}
		return dirResources
	}
	
	private def convertFromDropboxResource(def dropboxResource) {
		def fileResource = new FileResource()
		fileResource.byteSize = dropboxResource.bytes
		fileResource.path = dropboxResource.path
		fileResource.modified = dropboxResource.modified
		fileResource.isDir = dropboxResource.isDir
		fileResource.mimeType = dropboxResource.mimeType

		return fileResource
	}

	def uploadResources(List<FileResource> fileResources) {
		// TODO Auto-generated method stub
		
	}

	def updateResources(List<FileResource> fileResources) {
		// TODO Auto-generated method stub
		
	}

	def downloadResources(List<FileResource> fileResources) {
		// TODO Auto-generated method stub

	}

}
