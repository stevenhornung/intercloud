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
	static String APP_KEY
	static String APP_SECRET
	final static AccessType ACCESS_TYPE = AccessType.DROPBOX
	
	static DropboxAPI<WebAuthSession> dropboxApi
	static WebAuthSession webAuthSession
	static WebAuthInfo authInfo
	
	static String account_key
	static String account_secret
	
	final static REDIRECT_URL_PARAM = "&oauth_callback=http://localhost:8080/auth_redirect"

	def configure(boolean isAuthRedirect) {
		if(!isAuthRedirect) {
			authInfo = getAuthInfoForConfigure()
			return authInfo.url + REDIRECT_URL_PARAM
		}	
		else {
			setDropboxApiForConfigure()
		}
	}
	
	private def getAuthInfoForConfigure() {
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET)
		webAuthSession = new WebAuthSession(appKeys, ACCESS_TYPE)
		return webAuthSession.getAuthInfo()
	}
	
	private void setDropboxApiForConfigure() {
		RequestTokenPair pair = authInfo.requestTokenPair
		webAuthSession.retrieveWebAccessToken(pair)
		setAccountTokensForConfigure()
		
		dropboxApi = new DropboxAPI<WebAuthSession>(webAuthSession)
	}

	private def setAccountTokensForConfigure() {
		AccessTokenPair tokens = webAuthSession.getAccessTokenPair()
		account_key = tokens.key
		account_secret = tokens.secret
	}
	
	def setCloudStoreProperties(CloudStore cloudStoreInstance, Account account) {
		setCloudStoreInfo(cloudStoreInstance)
		setCloudStoreFileResources(cloudStoreInstance)
		setCloudStoreAccount(cloudStoreInstance, account)
	}
	
	private def setCloudStoreInfo(CloudStore cloudStoreInstance) {
		def accountInfo = getAccountInfo()
		
		cloudStoreInstance.storeName = STORE_NAME
		cloudStoreInstance.credentials.put('ACCOUNT_KEY', account_key)
		cloudStoreInstance.credentials.put('ACCOUNT_SECRET', account_secret)
		cloudStoreInstance.spaceUsed = accountInfo.quotaNormal
		cloudStoreInstance.totalSpace = accountInfo.quota
	}
	
	private def getAccountInfo() {
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		WebAuthSession webAuthSession = new WebAuthSession(appKeys, ACCESS_TYPE, new AccessTokenPair(account_key, account_secret))
		def dropboxAccountInfo = dropboxApi.accountInfo()
		
		return dropboxAccountInfo
	}
	
	private def setCloudStoreFileResources(CloudStore cloudStoreInstance) {
		def fileResources = getAllDropboxResources()
		for(FileResource fileResource : fileResources) {
			// TODO: better way to do this? ->Ensure path is unique within account cloud store
			for(FileResource fileRes2 : fileResources) {
				if(fileResource.path == fileRes2.path) {
					continue
				}
			}
			if(!fileResource.save()) {
				// TODO: show a better message that a resource couldnt be saved
				print fileResource.errors.allErrors
			}
		}
		cloudStoreInstance.fileResources = fileResources
	}
	
	private def getAllDropboxResources() {
		Entry rootDirEntries = dropboxApi.metadata("/", 0, null, true, null);
		def fileResources = getFilesInDir(rootDirEntries)
		return fileResources
	}
	
	private def getFilesInDir(Entry entries) {
		def dirResources = []
		def dirFileResourceChildren = []
		def dirFileResource = convertFromDropboxResource(entries)
		for (Entry e : entries.contents) {
			if (!e.isDeleted) {
				if(e.isDir) {
					def directory = convertFromDropboxResource(e)
					dirFileResourceChildren.add(directory)
					Entry dirEntries = dropboxApi.metadata(e.path, 0, null, true, null)
					dirResources.addAll(getFilesInDir(dirEntries))
				}
				else {
					def fileResource = convertFromDropboxResource(e)
					dirFileResourceChildren.add(fileResource)
					dirResources.add(fileResource)
				}
			}
		}
		dirFileResource.fileResources = dirFileResourceChildren
		dirResources.add(dirFileResource)
		return dirResources
	}
	
	private def convertFromDropboxResource(def dropboxResource) {
		def fileResource = new FileResource()
		fileResource.byteSize = dropboxResource.bytes
		fileResource.path = dropboxResource.path
		fileResource.modified = dropboxResource.modified
		fileResource.isDir = dropboxResource.isDir
		fileResource.mimeType = dropboxResource.mimeType
		fileResource.fileName = dropboxResource.path.substring(dropboxResource.path.lastIndexOf('/')+1)

		return fileResource
	}
	
	private def setCloudStoreAccount(CloudStore cloudStoreInstance, Account account) {
		cloudStoreInstance.account = account
	}


	def uploadResource(def credentials, def fileResource) {
		// TODO Auto-generated method stub
		
	}

	def updateResource(def credentials, def fileResource) {
		// TODO Auto-generated method stub
		
	}

	def downloadResource(def credentials, def fileResource) {
		setDropboxApiWithCredentials(credentials)

		byte[] downloadedBytes = null
		if(!fileResource.isDir) {
			downloadedBytes = getDropboxFileBytes(fileResource)
		}
		
		return downloadedBytes
	}
	
	private void setDropboxApiWithCredentials(def credentials) {
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		account_key = credentials.ACCOUNT_KEY
		account_secret = credentials.ACCOUNT_SECRET
		webAuthSession = new WebAuthSession(appKeys, ACCESS_TYPE, new AccessTokenPair(account_key, account_secret));
		
		dropboxApi = new DropboxAPI<WebAuthSession>(webAuthSession);
	}
	
	private def getDropboxFileBytes(FileResource fileResource) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
		DropboxFileInfo info = dropboxApi.getFile(fileResource.path, null, outputStream, null)
		return outputStream.toByteArray()
	}
}
