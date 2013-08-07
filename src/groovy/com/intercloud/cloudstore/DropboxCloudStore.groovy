package com.intercloud.cloudstore

import com.dropbox.core.DbxAccountInfo
import com.dropbox.core.DbxAuthFinish
import com.dropbox.core.DbxClient
import com.dropbox.core.DbxDelta
import com.dropbox.core.DbxEntry
import com.dropbox.core.DbxStandardSessionStore
import com.dropbox.core.DbxWebAuth
import com.dropbox.core.DbxAuthInfo
import com.dropbox.core.DbxSessionStore
import com.dropbox.core.DbxAppInfo
import com.dropbox.core.DbxRequestConfig

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import org.codehaus.groovy.grails.web.mapping.DefaultUrlMappingParser

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.*
import com.intercloud.util.ZipUtilities

class DropboxCloudStore implements CloudStoreInterface {
	
	private static Logger log = LoggerFactory.getLogger(DropboxCloudStore.class)
	
	final static STORE_NAME = "dropbox"
	static String APP_KEY
	static String APP_SECRET
	static String REDIRECT_URL
	static String ZIP_TEMP_STORAGE_PATH
	
	static DbxWebAuth auth
	static DbxAuthInfo authInfo
	static DbxClient dropboxClient
	
	static String access_token
	
	public def configure(boolean isAuthRedirect, HttpServletRequest request) {
		if(!isAuthRedirect) {
			log.debug "Getting authorize url for dropbox"
			String authorizeUrl = getAuthorizeUrl(request)
			return authorizeUrl
		}	
		else {
			log.debug "Auth redirect from dropbox"
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
			log.debug "No request token in dropbox auth request"
			session.removeAttribute("dropbox-auth-csrf-token")
		}
		if(request.getParameter("state") != sessionToken) {
			log.debug "Invalid oauth token in dropbox auth request"
		}
		
		DbxAuthFinish authFinish
		try {
			authFinish = auth.finish(request.parameterMap)
		}
		catch(Exception) {
			log.warn "Dropbox authorization failed: {}", Exception
		} 
		
		setAccessTokenForConfigure(authFinish)
	}

	private def setAccessTokenForConfigure(def authFinish) {
		access_token = authFinish?.accessToken
	}
	
	public def setCloudStoreProperties(CloudStore cloudStoreInstance, Account account) {
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
		
		String updateCursor = getUpdateCursor()
		cloudStoreInstance.updateCursor = updateCursor
	}
	
	private def getAccountInfo() {
		DbxRequestConfig requestConfig = new DbxRequestConfig("intercloud/1.0", "english")
		dropboxClient = new DbxClient(requestConfig, access_token)
		DbxAccountInfo dropboxAccountInfo = dropboxClient.accountInfo
		
		return dropboxAccountInfo
	}
	
	private String getUpdateCursor() {
		DbxRequestConfig requestConfig = new DbxRequestConfig("intercloud/1.0", "english")
		dropboxClient = new DbxClient(requestConfig, access_token)
		
		DbxDelta delta = dropboxClient.getDelta(null)
		return delta.cursor
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
					log.debug "Couldn't save a file resource, repeat path"
				}
			}
		}
		cloudStoreInstance.fileResources = fileResources
	}
	
	private def getAllDropboxResources() {
		String rootPath = "/"
		def rootDropboxFolder = dropboxClient.getMetadataWithChildren(rootPath)
		FileResource parentFileResource = dropboxFolderToFileResource(rootDropboxFolder.entry)
		def fileResources = getFilesInDir(rootDropboxFolder, parentFileResource)
		return fileResources
	}
	
	private def getFilesInDir(def dropboxFolder, FileResource parentFileResource) {
		def dirResources = []
		def dirFileResourceChildren = []
		for (DbxEntry entry : dropboxFolder.children) {
			if(entry.isFolder()) {
				FileResource dirFileResource = dropboxFolderToFileResource(entry)
				dirFileResource.parentFileResource = parentFileResource
				dirFileResourceChildren.add(dirFileResource)
				def nestedDropboxFolder = dropboxClient.getMetadataWithChildren(entry.path)
				dirResources.addAll(getFilesInDir(nestedDropboxFolder, dirFileResource))
			}
			else {
				FileResource fileResource = dropboxFileToFileResource(entry)
				fileResource.parentFileResource = parentFileResource
				dirFileResourceChildren.add(fileResource)
				dirResources.add(fileResource)
			}
		}
		parentFileResource.childFileResources = dirFileResourceChildren
		dirResources.add(parentFileResource)
		return dirResources
	}
	
	private def dropboxFolderToFileResource(DbxEntry dropboxFolder) {
		FileResource fileResource = new FileResource()
		
		fileResource.path = dropboxFolder.path
		fileResource.isDir = dropboxFolder.isFolder()
		fileResource.fileName = dropboxFolder.name

		return fileResource
	}
	
	private def dropboxFileToFileResource(def dropboxResource) {
		FileResource fileResource = new FileResource()
		
		fileResource.byteSize = dropboxResource.numBytes
		fileResource.path = dropboxResource.path
		fileResource.modified = dropboxResource.lastModified
		fileResource.isDir = dropboxResource.isFolder()
		//fileResource.mimeType = dropboxResource.mimeType
		fileResource.fileName = dropboxResource.name

		return fileResource
	}
	
	private def setCloudStoreAccount(CloudStore cloudStoreInstance, Account account) {
		cloudStoreInstance.account = account
	}
	
	private void setDropboxApiWithCredentials(def credentials) {
		log.debug "Setting dropbox credentials for api access"
		access_token = credentials.ACCESS_TOKEN
		DbxRequestConfig requestConfig = new DbxRequestConfig("intercloud/1.0", "english")
		dropboxClient = new DbxClient(requestConfig, access_token)
	}

	public def uploadResource(def credentials, FileResource fileResource) {
		// TODO Auto-generated method stub
	}
	
	public def deleteResource(def credentials, FileResource fileResource) {
		setDropboxApiWithCredentials(credentials)
		deleteFromDropbox(fileResource)
	}
	
	private def deleteFromDropbox(FileResource fileResource) {
		try {
			dropboxClient.delete(fileResource.path)
		}
		catch(DbxException) {
			log.warn "File could not be deleted from dropbox: {}", DbxException
		}
	}

	public def downloadResource(def credentials, FileResource fileResource) {
		setDropboxApiWithCredentials(credentials)
		
		byte[] downloadedBytes = null
		if(fileResource.isDir) {
			log.debug "Downloading folder and building zip from dropbox"
			downloadedBytes = getZippedDropboxFolderBytes(fileResource)
		}
		else {
			log.debug "Downloading file from dropbox"
			downloadedBytes = getDropboxFileBytes(fileResource)
		}
		
		return downloadedBytes
	}
	
	private byte[] getZippedDropboxFolderBytes(FileResource fileResource) {
		String downloadedFolderPath = getDownloadedFolderPath(fileResource)
		String zipFileName = getSourceZipName(fileResource)
		
		if(!doesFolderExistInDropbox(fileResource)) {
			return []
		}
		
		log.debug "Downloading folder to temp zip storage"
		downloadFolderToPath(downloadedFolderPath, fileResource)
		
		log.debug "Zipping downloaded folder"
		ZipUtilities.zipDownloadedFolder(downloadedFolderPath, zipFileName)
		
		String zipFileLocation = downloadedFolderPath.substring(0, downloadedFolderPath.lastIndexOf('/'))
		byte[] zippedFolderBytes = ZipUtilities.getBytesFromZipFile(zipFileLocation, zipFileName)
		
		ZipUtilities.removeTempDownloadFolder(zipFileLocation)
		
		return zippedFolderBytes
	}
	
	private String getDownloadedFolderPath(FileResource fileResource) {
		String uniqueFolderId = UUID.randomUUID().toString()
		String fullPath = ZIP_TEMP_STORAGE_PATH + "/" + uniqueFolderId + "/downloadedFiles"
		new File(fullPath).mkdirs()
		return fullPath
	}
	
	private String getSourceZipName(FileResource fileResource) {
		String sourceZip
		if(!fileResource.fileName) {
			sourceZip = "DropboxRoot.zip"
		}
		else {
			sourceZip = fileResource.fileName + ".zip"
		}
		
		return sourceZip
	}
	
	private boolean doesFolderExistInDropbox(FileResource fileResource) {
		if(dropboxClient.getMetadata(fileResource.path) == null) {
			return false
		}
		else {
			return true
		}
	}
	
	private void downloadFolderToPath(String path, FileResource fileResource) {
		for(FileResource childResource : fileResource.childFileResources) {
			if(childResource.isDir) {
				String updatedPath = path + "/" + childResource.fileName
				new File(updatedPath).mkdir()
				downloadFolderToPath(updatedPath, childResource)
			}
			else {
				byte[] resourceData = getDropboxFileBytes(childResource)
				String fullFilePath = path + "/" + childResource.fileName
				FileOutputStream outputStream =  new FileOutputStream(fullFilePath)
				outputStream.write(resourceData)
				outputStream.close()
			}
		}
	}
	
	private byte[] getDropboxFileBytes(FileResource fileResource) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
		DbxEntry entry = dropboxClient.getFile(fileResource.path, null, outputStream)
		return outputStream.toByteArray()
	}
	
	public def updateResources(def credentials, String updateCursor, def currentFileResources) {
		setDropboxApiWithCredentials(credentials)
		
		DbxDelta delta = dropboxClient.getDelta(updateCursor)
		if(!delta.entries.empty) {
			log.debug "Updates to dropbox found. Syncing updates"
			addNewEntries(delta.entries, currentFileResources)
		}
		
		return delta.cursor
	}
	
	private void addNewEntries(def entries, def currentFileResources) {
		for(entry in entries) {
			if(entry.metadata) {
				boolean isUpdated = false
				for(FileResource fileResource : currentFileResources) {
					if(entry.metadata.path == fileResource.path) {
						updateChangedFileResource(fileResource, entry.metadata)
						isUpdated = true
						break
					}
				}
				if(!isUpdated) {
					currentFileResources = addNewFileResource(entry.metadata, currentFileResources)
				}
			}
		}
	}
	
	private void updateChangedFileResource(FileResource currentFileResource, def updatedEntry) {
		if(updatedEntry.isFolder()) {
			currentFileResource = setFolderFileResourceProperties(currentFileResource, updatedEntry)
		}
		else {
			currentFileResource = setFileResourceProperties(currentFileResource, updatedEntry)
		}
		
		currentFileResource.save()
	}
	
	private FileResource setFolderFileResourceProperties(FileResource fileResource, def entry) {
		fileResource.path = entry.path
		fileResource.isDir = entry.isFolder()
		fileResource.fileName = entry.name
		
		return fileResource
	}
	
	private FileResource setFileResourceProperties(FileResource fileResource, def entry) {
		fileResource.byteSize = entry.numBytes
		fileResource.path = entry.path
		fileResource.modified = entry.lastModified
		fileResource.isDir = entry.isFolder()
		//fileResource.mimeType = entry.mimeType
		fileResource.fileName = entry.name
		
		return fileResource
	}
	
	private def addNewFileResource(def entry, def currentFileResources) {
		FileResource fileResource = new FileResource()
		if(entry.isFolder()) {
			fileResource = setFolderFileResourceProperties(fileResource, entry)
		}
		else {
			fileResource = setFileResourceProperties(fileResource, entry)
		}
		
		currentFileResources = setParentAndChildFileResources(fileResource, currentFileResources)
		return currentFileResources
	}
	
	private def setParentAndChildFileResources(FileResource fileResource, def currentFileResources) {
		List<String> pathParts = new DefaultUrlMappingParser().parse(fileResource.path).getTokens() as List
		boolean parentFound = false

		if(pathParts.size() == 1) {
			for(FileResource currentResource : currentFileResources) {
				if(currentResource.path == "/") {
					currentResource.childFileResources.add(fileResource)
					fileResource.parentFileResource = currentResource
					currentResource.save(flush: true)
					fileResource.save(flush: true)
					parentFound = true
					break
				}
			}
		}
		
		if(!parentFound) {
			String parentPath = pathParts.join("/")
			parentPath = "/" + parentPath
			parentPath = parentPath.substring(0, parentPath.lastIndexOf("/"))
			for(FileResource currentResource : currentFileResources) {
				if(currentResource.path == parentPath) {
					if(currentResource.childFileResources == null) {
						def fileResources = [fileResource]
						currentResource.childFileResources = fileResources
					}
					else {
						currentResource.childFileResources.add(fileResource)
					}
					fileResource.parentFileResource = currentResource
					currentResource.save(flush: true)
					fileResource.save(flush: true)
					parentFound = true
					break
				}
			}
		}
		
		if(!parentFound) {
			pathParts.pop()
			FileResource parentFileResource = createParentDirectory(pathParts)
			currentFileResources.add(parentFileResource)
			def childFileResources = [fileResource]
			parentFileResource.childFileResources = childFileResources
			fileResource.parentFileResource = parentFileResource
			parentFileResource.save(flush: true)
			fileResource.save(flush: true)
			currentFileResources = setParentAndChildFileResources(parentFileResource, currentFileResources)
		}
		
		currentFileResources.add(fileResource)
		return currentFileResources
	}
	
	private FileResource createParentDirectory(def pathParts) {
		FileResource parentFileResource = new FileResource()
		
		String path = pathParts.join('/')
		path = '/' + path
		
		parentFileResource.path = path
		parentFileResource.isDir = true
		parentFileResource.fileName = pathParts.last()

		return parentFileResource
	}
}
