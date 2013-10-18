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
import com.dropbox.core.DbxWriteMode

import org.apache.tika.Tika

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.util.CloudStoreUtilities
import com.intercloud.util.ZipUtilities
import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.FileResource

class DropboxCloudStore implements CloudStoreInterface {
	
	private static Logger log = LoggerFactory.getLogger(DropboxCloudStore.class)
	
	static String STORE_NAME
	static String APP_KEY
	static String APP_SECRET
	static String REDIRECT_URL
	
	private DbxWebAuth auth
	private DbxAuthInfo authInfo
	private DbxClient dropboxClient
	private String access_token
	
	public def configure(boolean isAuthRedirect, HttpServletRequest request) {
		if(!isAuthRedirect) {
			log.debug "Getting authorize url for dropbox"
			String authorizeUrl = getAuthorizeUrl(request)
			return authorizeUrl
		}	
		else {
			log.debug "Auth redirect from dropbox"
			boolean isSuccess = setDropboxApiForConfigure(request)
			return isSuccess
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
	
	private boolean setDropboxApiForConfigure(HttpServletRequest request) {
		HttpSession session = request.getSession(true)
		def sessionToken = session.getAttribute('dropbox-auth-csrf-token')
		
		if(sessionToken == null) {
			log.debug "No request token in dropbox auth request"
			session.removeAttribute("dropbox-auth-csrf-token")
			return false
		}
		if(request.getParameter("state") != sessionToken) {
			log.debug "Invalid oauth token in dropbox auth request"
			return false 
		}
		
		DbxAuthFinish authFinish
		try {
			authFinish = auth.finish(request.parameterMap)
			setAccessTokenForConfigure(authFinish)
			return true
		}
		catch(Exception) {
			log.debug "Dropbox authorization failed, user denied access"
			return false
		} 
	}

	private def setAccessTokenForConfigure(def authFinish) {
		access_token = authFinish?.accessToken
	}
	
	public void setCloudStoreProperties(CloudStore cloudStoreInstance, Account account) {
		setCloudStoreInfo(cloudStoreInstance)
		setCloudStoreFileResources(cloudStoreInstance)
		setCloudStoreAccount(cloudStoreInstance, account)
	}
	
	private void setCloudStoreInfo(CloudStore cloudStoreInstance) {
		DbxAccountInfo accountInfo = getAccountInfo()
		
		cloudStoreInstance.storeName = STORE_NAME
		cloudStoreInstance.credentials << ['ACCESS_TOKEN': access_token]
		cloudStoreInstance.userId = accountInfo.userId
		cloudStoreInstance.spaceUsed = accountInfo.quota.normal
		cloudStoreInstance.totalSpace = accountInfo.quota.total
		
		String updateCursor = getInitialUpdateCursor()
		cloudStoreInstance.updateCursor = updateCursor
	}
	
	private def getAccountInfo() {
		DbxRequestConfig requestConfig = new DbxRequestConfig("intercloud/1.0", "english")
		dropboxClient = new DbxClient(requestConfig, access_token)
		DbxAccountInfo dropboxAccountInfo = dropboxClient.getAccountInfo()
		
		return dropboxAccountInfo
	}
	
	private String getInitialUpdateCursor() {
		DbxRequestConfig requestConfig = new DbxRequestConfig("intercloud/1.0", "english")
		dropboxClient = new DbxClient(requestConfig, access_token)
		
		DbxDelta delta = dropboxClient.getDelta(null)
		return delta.cursor
	}
	
	private def setCloudStoreFileResources(CloudStore cloudStoreInstance) {
		def fileResources = getAllDropboxResources(cloudStoreInstance)
		cloudStoreInstance.fileResources = fileResources
	}
	
	private def getAllDropboxResources(CloudStore cloudStoreInstance) {
		String rootPath = "/"
		def rootDropboxFolder = dropboxClient.getMetadataWithChildren(rootPath)
		FileResource parentFileResource = dropboxFolderToFileResource(rootDropboxFolder.entry, cloudStoreInstance)
		def fileResources = getFilesInDir(rootDropboxFolder, parentFileResource, cloudStoreInstance)
		return fileResources
	}
	
	private def getFilesInDir(def dropboxFolder, FileResource parentFileResource, CloudStore cloudStoreInstance) {
		def dirResources = []
		for (DbxEntry entry : dropboxFolder.children) {
			if(entry.isFolder()) {
				FileResource dirFileResource = dropboxFolderToFileResource(entry, cloudStoreInstance)
				dirFileResource.parentFileResource = parentFileResource
				def nestedDropboxFolder = dropboxClient.getMetadataWithChildren(entry.path)
				dirResources.addAll(getFilesInDir(nestedDropboxFolder, dirFileResource, cloudStoreInstance))
			}
			else {
				FileResource fileResource = dropboxFileToFileResource(entry, cloudStoreInstance)
				fileResource.parentFileResource = parentFileResource
				dirResources.add(fileResource)
			}
		}
		dirResources.add(parentFileResource)
		return dirResources
	}
	
	private def dropboxFolderToFileResource(DbxEntry dropboxFolder, CloudStore cloudStoreInstance) {
		FileResource fileResource = new FileResource()
		
		fileResource.cloudStore = cloudStoreInstance
		fileResource.path = dropboxFolder.path
		fileResource.isDir = dropboxFolder.isFolder()
		if(dropboxFolder.path == "/") {
			fileResource.fileName = "DropboxRoot"
		}
		else {
			fileResource.fileName = dropboxFolder.name
		}
		fileResource.mimeType = "application/octet-stream"

		return fileResource
	}
	
	private def dropboxFileToFileResource(def dropboxResource, CloudStore cloudStoreInstance) {
		FileResource fileResource = new FileResource()
		
		fileResource.cloudStore = cloudStoreInstance
		fileResource.byteSize = dropboxResource.numBytes
		fileResource.path = dropboxResource.path
		fileResource.modified = dropboxResource.lastModified
		fileResource.isDir = dropboxResource.isFolder()
		fileResource.fileName = dropboxResource.name
		
		// Guess mimeType by file extension
		fileResource.mimeType = new Tika().detect(fileResource.path)

		return fileResource
	}
	
	private def setCloudStoreAccount(CloudStore cloudStoreInstance, Account account) {
		cloudStoreInstance.account = account
		account.addToCloudStores(cloudStoreInstance)
	}
	
	private void setDropboxApiWithCredentials(def credentials) {
		log.debug "Setting dropbox credentials for api access"
		access_token = credentials.ACCESS_TOKEN
		DbxRequestConfig requestConfig = new DbxRequestConfig("intercloud/1.0", "english")
		dropboxClient = new DbxClient(requestConfig, access_token)
	}

	public def uploadResource(CloudStore cloudStore, def uploadedFile) {
		log.debug "Uploading file to dropbox"
		def credentials = cloudStore.credentials
		setDropboxApiWithCredentials(credentials)
		def dropboxUpload = uploadToDropbox(uploadedFile)
		
		updateDropboxSpace(cloudStore)
		
		return dropboxUpload
	}
	
	private def uploadToDropbox(def uploadedFile) {
		String filePath = "/" + uploadedFile.originalFilename
		
		try {
			def dropboxUpload = dropboxClient.uploadFile(filePath, DbxWriteMode.add(), uploadedFile.size, uploadedFile.inputStream)
			log.debug "Successfully uploaded file '{}' to dropbox", dropboxUpload.path
			return dropboxUpload.name
		} catch(DbxException) {
			log.warn "File could not be uploaded to dropbox. Exception {}", DbxException
			return null
		}
	}
	
	private void updateDropboxSpace(CloudStore cloudStore) {
		log.debug "Updating dropbox space"
		
		DbxAccountInfo accountInfo = getAccountInfo()
		
		cloudStore.spaceUsed = accountInfo.quota.normal
		cloudStore.totalSpace = accountInfo.quota.total
	}
	
	public boolean deleteResource(CloudStore cloudStore, FileResource fileResource) {
		log.debug "Deleting resource {}", fileResource.path
		def credentials = cloudStore.credentials
		setDropboxApiWithCredentials(credentials)
		boolean isSuccess = deleteFromDropbox(fileResource)
		
		updateDropboxSpace(cloudStore)
		return isSuccess
	}
	
	private boolean deleteFromDropbox(FileResource fileResource) {
		try {
			dropboxClient.delete(fileResource.path)
			return true
		}
		catch(DbxException) {
			log.warn "File could not be deleted from dropbox: {}", DbxException
			return false
		}
	}

	public InputStream downloadResource(CloudStore cloudStore, FileResource fileResource) {
		setDropboxApiWithCredentials(cloudStore.credentials)
		
		def downloadedStream = null
		if(fileResource.isDir) {
			log.debug "Downloading folder and building zip from dropbox"
			downloadedStream = getZippedDropboxFolderStream(fileResource)
		}
		else {
			log.debug "Downloading file from dropbox"
			downloadedStream = getDropboxFileStream(fileResource)
		}
		
		return downloadedStream
	}
	
	private InputStream getZippedDropboxFolderStream(FileResource fileResource) {
		String downloadedFolderPath = ZipUtilities.getDownloadedFolderPath(fileResource)
		String zipFileName = ZipUtilities.getSourceZipName(STORE_NAME, fileResource)
		
		if(!doesFolderExistInDropbox(fileResource)) {
			return null
		}
		
		log.debug "Downloading folder to temporary zip storage"
		downloadFolderToPath(downloadedFolderPath, fileResource)
		
		log.debug "Zipping downloaded folder to '{}'", zipFileName
		ZipUtilities.zipFolder(downloadedFolderPath, zipFileName)
		
		String zipFileLocation = downloadedFolderPath.substring(0, downloadedFolderPath.lastIndexOf('/'))
		InputStream zippedFolderInputStream = ZipUtilities.getInputStreamFromZipFile(zipFileLocation, zipFileName)
		
		ZipUtilities.removeTempFromFileSystem(zipFileLocation)
		
		return zippedFolderInputStream
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
				InputStream resourceDataStream = getDropboxFileStream(childResource)
				if(resourceDataStream != null) {
					String fullFilePath = path + "/" + childResource.fileName
					FileOutputStream outputStream =  new FileOutputStream(fullFilePath)
					byte[] buffer = new byte[1024]
					int bytesRead
					while((bytesRead = resourceDataStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead)
					}
					outputStream.close()
				}
			}
		}
	}
	
	private InputStream getDropboxFileStream(FileResource fileResource) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
		DbxEntry entry = dropboxClient.getFile(fileResource.path, null, outputStream)
		log.debug "Downloaded file '{}' from dropbox", fileResource.fileName
		
		InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())
		return inputStream
	}
	
	public def updateResources(CloudStore cloudStore, String updateCursor, def currentFileResources) {
		log.debug "Updating dropbox file resources for account '{}'", cloudStore.account.email
		def credentials = cloudStore.credentials
		setDropboxApiWithCredentials(credentials)
		
		DbxDelta delta = dropboxClient.getDelta(updateCursor)
		if(!delta.entries.empty) {
			log.debug "Updates to dropbox found. Syncing updates"
			addNewEntries(cloudStore, delta.entries, currentFileResources)
		}
		
		updateDropboxSpace(cloudStore)
		
		return delta.cursor
	}
	
	private void addNewEntries(CloudStore cloudStore, def entries, def currentFileResources) {
		for(entry in entries) {
			if(entry.metadata) {
				log.debug "Dropbox entry changed: '{}'", entry.lcPath
				boolean isEntryUpdated = updateEntryIfExists(cloudStore, entry, currentFileResources)
				if(!isEntryUpdated) {
					currentFileResources = addToFileResources(cloudStore, entry.metadata, currentFileResources)
				}
			}
			else {
				log.debug "Dropbox entry deleted: '{}'", entry.lcPath
				FileResource fileResource = null
				for(FileResource currentFileResource : currentFileResources) {
					if(entry.lcPath == currentFileResource.path.toLowerCase()) {
						fileResource = currentFileResource
						break
					}
				}
				if(fileResource) {
					CloudStoreUtilities.deleteFromDatabase(fileResource)
				}
				else {
					// we previously deleted file. Dropbox doesn't know so is still informing, ignore this
				}
			}
		}
	}
	
	private boolean updateEntryIfExists(CloudStore cloudStore, def entry, def currentFileResources) {
		boolean isEntryUpdated = false
		for(FileResource fileResource : currentFileResources) {
			if(entry.metadata.path == fileResource.path) {
				updateChangedFileResource(cloudStore, fileResource, entry.metadata)
				isEntryUpdated = true
				break
			}
		}
		
		return isEntryUpdated
	}
	
	private void updateChangedFileResource(CloudStore cloudStore, FileResource currentFileResource, def updatedEntry) {
		if(updatedEntry.isFolder()) {
			currentFileResource = setFolderFileResourceProperties(cloudStore, currentFileResource, updatedEntry)
		}
		else {
			currentFileResource = setFileResourceProperties(cloudStore, currentFileResource, updatedEntry)
		}
	}
	
	private FileResource setFolderFileResourceProperties(CloudStore cloudStore, FileResource fileResource, def entry) {
		fileResource.cloudStore = cloudStore
		fileResource.path = entry.path
		fileResource.isDir = entry.isFolder()
		fileResource.fileName = entry.name
		fileResource.mimeType = "application/octet-stream"
		
		return fileResource
	}
	
	private FileResource setFileResourceProperties(CloudStore cloudStore, FileResource fileResource, def entry) {
		fileResource.cloudStore = cloudStore
		fileResource.byteSize = entry.numBytes
		fileResource.path = entry.path
		fileResource.modified = entry.lastModified
		fileResource.isDir = entry.isFolder()
		fileResource.fileName = entry.name
		
		// Guess mimeType by file extension
		fileResource.mimeType = new Tika().detect(fileResource.path)
		
		return fileResource
	}
	
	private def addToFileResources(CloudStore cloudStore, def entry, def currentFileResources) {
		FileResource fileResource = new FileResource()
		if(entry.isFolder()) {
			fileResource = setFolderFileResourceProperties(cloudStore, fileResource, entry)
		}
		else {
			fileResource = setFileResourceProperties(cloudStore, fileResource, entry)
		}
		cloudStore.addToFileResources(fileResource)
		
		currentFileResources = CloudStoreUtilities.setParentAndChildFileResources(fileResource, currentFileResources)
		return currentFileResources
	}
}
