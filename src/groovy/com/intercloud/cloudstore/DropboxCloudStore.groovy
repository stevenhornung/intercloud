package com.intercloud.cloudstore

import com.dropbox.core.DbxAccountInfo
import com.dropbox.core.DbxAuthFinish
import com.dropbox.core.DbxClient
import com.dropbox.core.DbxEntry
import com.dropbox.core.DbxStandardSessionStore
import com.dropbox.core.DbxWebAuth
import com.dropbox.core.DbxAuthInfo
import com.dropbox.core.DbxSessionStore
import com.dropbox.core.DbxAppInfo
import com.dropbox.core.DbxRequestConfig

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import com.intercloud.*

class DropboxCloudStore implements CloudStoreInterface {
	
	final static STORE_NAME = "dropbox"
	static String APP_KEY
	static String APP_SECRET

	static DbxWebAuth auth
	static DbxAuthInfo authInfo
	static DbxClient dropboxClient
	
	static String access_token
	
	final static REDIRECT_URL = "http://localhost:8080/auth_redirect"

	def configure(boolean isAuthRedirect, HttpServletRequest request) {
		if(!isAuthRedirect) {
			String authorizeUrl = getAuthorizeUrl(request)
			return authorizeUrl
		}	
		else {
			setDropboxApiForConfigure(request)
		}
	}
	
	private def getAuthorizeUrl(HttpServletRequest request) {
		DbxRequestConfig requestConfig = new DbxRequestConfig("intercloud/1.0", "english")
		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET)
		HttpSession session = request.getSession(true)
		String sessionKey = "dropbox-auth-csrf-token"
		DbxSessionStore csrfTokenStore = new DbxStandardSessionStore(session, sessionKey);
		auth = new DbxWebAuth(requestConfig, appInfo, REDIRECT_URL, csrfTokenStore)
		
		return auth.start()
	}
	
	private void setDropboxApiForConfigure(HttpServletRequest request) {
		HttpSession session = request.getSession(true)
		def sessionToken = session.getAttribute('dropbox-auth-csrf-token')
		
		if(sessionToken == null) {
			print 'no requst token in session'
			// return no request token in session
			session.removeAttribute("dropbox-auth-csrf-token")
		}
		if(request.getParameter("state") != sessionToken) {
			print 'invalid oauth'
			// return invalid state (oauth)
		}
		DbxAuthFinish authFinish
		try {
			authFinish = auth.finish(request.parameterMap)
		}
		catch(Exception e) {
			print 'exception hurr'
			print e
			// return 'couldnt complete link 
		} 
		setAccessTokenForConfigure(authFinish)
		
	}

	private def setAccessTokenForConfigure(def authFinish) {
		access_token = authFinish.accessToken
	}
	
	def setCloudStoreProperties(CloudStore cloudStoreInstance, Account account) {
		setCloudStoreInfo(cloudStoreInstance)
		setCloudStoreFileResources(cloudStoreInstance)
		setCloudStoreAccount(cloudStoreInstance, account)
	}
	
	private def setCloudStoreInfo(CloudStore cloudStoreInstance) {
		DbxAccountInfo accountInfo = getAccountInfo()
		
		cloudStoreInstance.storeName = STORE_NAME
		cloudStoreInstance.credentials.put('ACCESS_TOKEN', access_token)
		cloudStoreInstance.userId = accountInfo.userId
		cloudStoreInstance.spaceUsed = accountInfo.quota.normal
		cloudStoreInstance.totalSpace = accountInfo.quota.total
	}
	
	private def getAccountInfo() {
		DbxRequestConfig requestConfig = new DbxRequestConfig("intercloud/1.0", "english")
		dropboxClient = new DbxClient(requestConfig, access_token)
		DbxAccountInfo dropboxAccountInfo = dropboxClient.accountInfo
		
		return dropboxAccountInfo
	}
	
	private def setCloudStoreFileResources(CloudStore cloudStoreInstance) {
		def fileResources = getAllDropboxResources()
		boolean repeatPath = false
		for(FileResource fileResource : fileResources) {
			for(FileResource checkPathResource : fileResources) {
				if(checkPathResource.path == fileResource.path) {
					repeatPath = true
					break
				}
			}
			if(!repeatPath) {
				repeatPath = false
				if(!fileResource.save()) {
					// TODO: show a better message that a resource couldnt be saved
					print fileResource.errors.allErrors
				}
			}
		}
		cloudStoreInstance.fileResources = fileResources
	}
	
	private def getAllDropboxResources() {
		String rootPath = "/"
		def rootDropboxFolder = dropboxClient.getMetadataWithChildren(rootPath)
		def fileResources = getFilesInDir(rootDropboxFolder)
		return fileResources
	}
	
	private def getFilesInDir(def dropboxFolder) {
		def dirResources = []
		def dirFileResourceChildren = []
		def dirFileResource = dropboxFolderToFileResource(dropboxFolder.entry)
		for (DbxEntry entry : dropboxFolder.children) {
			if(entry.isFolder()) {
				def directory = dropboxFolderToFileResource(entry)
				dirFileResourceChildren.add(directory)
				def nestedDropboxFolder = dropboxClient.getMetadataWithChildren(entry.path)
				dirResources.addAll(getFilesInDir(nestedDropboxFolder))
			}
			else {
				def fileResource = dropboxFileToFileResource(entry)
				dirFileResourceChildren.add(fileResource)
				dirResources.add(fileResource)
			}
		}
		dirFileResource.fileResources = dirFileResourceChildren
		dirResources.add(dirFileResource)
		return dirResources
	}
	
	private def dropboxFolderToFileResource(def dropboxFolder) {
		def fileResource = new FileResource()
		
		fileResource.path = dropboxFolder.path
		fileResource.isDir = dropboxFolder.isFolder()
		fileResource.fileName = dropboxFolder.name
		
		return fileResource
	}
	
	private def dropboxFileToFileResource(def dropboxResource) {
		def fileResource = new FileResource()
		
		fileResource.byteSize = dropboxResource.numBytes
		fileResource.path = dropboxResource.path
		fileResource.modified = dropboxResource.lastModified
		fileResource.isDir = dropboxResource.isFolder()
		fileResource.mimeType = dropboxResource.mimeType
		fileResource.fileName = dropboxResource.name

		return fileResource
	}
	
	private def setCloudStoreAccount(CloudStore cloudStoreInstance, Account account) {
		cloudStoreInstance.account = account
	}
	
	private void setDropboxApiWithCredentials(def credentials) {
		access_token = credentials.ACCESS_TOKEN
		DbxRequestConfig requestConfig = new DbxRequestConfig("intercloud/1.0", "english")
		dropboxClient = new DbxClient(requestConfig, access_token)
	}

	def uploadResource(def credentials, def fileResource) {
		// TODO Auto-generated method stub
		
	}

	def updateResource(def credentials, def fileResource) {
		// TODO Auto-generated method stub
		
	}
	
	def deleteResource(def credentials, def fileResource) {
		setDropboxApiWithCredentials(credentials)
		deleteFromDropbox(fileResource)
	}
	
	private def deleteFromDropbox(def fileResource) {
		// TODO find new way to do this
	}

	def downloadResource(def credentials, def fileResource) {
		setDropboxApiWithCredentials(credentials)

		byte[] downloadedBytes = null
		if(!fileResource.isDir) {
			downloadedBytes = getDropboxFileBytes(fileResource)
		}
		
		return downloadedBytes
	}
	
	private def getDropboxFileBytes(FileResource fileResource) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
		DbxEntry entry = dropboxClient.getFile(fileResource.path, null, outputStream)
		return outputStream.toByteArray()
	}
}
