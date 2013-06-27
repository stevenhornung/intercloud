package com.intercloud.cloudstore

import com.dropbox.client2.DropboxAPI
import com.dropbox.client2.DropboxAPI.DropboxFileInfo
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

	def configure(boolean isAuthRedirect) {
		if(!isAuthRedirect) {
			AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET)
			session = new WebAuthSession(appKeys, ACCESS_TYPE)
			authInfo = session.getAuthInfo()
			
			String redirectUrlParam = "&oauth_callback=http://localhost:8080/auth_redirect"
			String url = authInfo.url+redirectUrlParam
	
			return url
		}	
		else {
			RequestTokenPair pair = authInfo.requestTokenPair
			session.retrieveWebAccessToken(pair)
			setAccountTokens()
			
			dropboxApi = new DropboxAPI<WebAuthSession>(session)
		}
	}

	private def setAccountTokens() {
		AccessTokenPair tokens = session.getAccessTokenPair()
		account_key = tokens.key
		account_secret = tokens.secret
	}
	
	def setCloudStoreInstanceProperties(def cloudStoreInstance, def session) {
		setCloudStoreInfo(cloudStoreInstance)
		setCloudStoreFileResources(cloudStoreInstance, session)
		setCloudStoreAccount(cloudStoreInstance, session)
	}
	
	private def setCloudStoreInfo(def cloudStoreInstance) {
		def accountInfo = retrieveAccountInfo()
		
		cloudStoreInstance.storeName = STORE_NAME
		cloudStoreInstance.credentials.put('ACCOUNT_KEY', account_key)
		cloudStoreInstance.credentials.put('ACCOUNT_SECRET', account_secret)
		cloudStoreInstance.spaceUsed = accountInfo.quotaNormal
		cloudStoreInstance.totalSpace = accountInfo.quota
	}
	
	private def retrieveAccountInfo() {
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE, new AccessTokenPair(account_key, account_secret));
		def dropboxAccountInfo = dropboxApi.accountInfo()
		
		return dropboxAccountInfo
	}
	
	private def setCloudStoreFileResources(def cloudStoreInstance, def session) {
		def fileResources = retrieveAllResourcesInfo()
		for(fileResource in fileResources) {
			if(!fileResource.save()) {
				// show message that a resource couldnt be loaded
				print fileResource.errors.allErrors
			}
		}
		cloudStoreInstance.fileResources = fileResources
	}
	
	private def retrieveAllResourcesInfo() {
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
					Entry dirEntries = dropboxApi.metadata(e.path, 0, null, true, null)
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
	
	private def setCloudStoreAccount(def cloudStoreInstance, def session) {
		def account = Account.findByEmail(session.user.email)
		cloudStoreInstance.account = account
	}


	def uploadResource(def credentials, def fileResource) {
		// TODO Auto-generated method stub
		
	}

	def updateResource(def credentials, def fileResource) {
		// TODO Auto-generated method stub
		
	}

	def downloadResource(def credentials, def fileResource) {
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		account_key = credentials.ACCOUNT_KEY
		account_secret = credentials.ACCOUNT_SECRET
		session = new WebAuthSession(appKeys, ACCESS_TYPE, new AccessTokenPair(account_key, account_secret));
		dropboxApi = new DropboxAPI<WebAuthSession>(session);

		byte[] downloadedData = null
		if(!fileResource.isDir) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
			DropboxFileInfo info = dropboxApi.getFile(fileResource.path, null, outputStream, null)
			downloadedData = outputStream.toByteArray()
		}
		
		return downloadedData
	}

}
