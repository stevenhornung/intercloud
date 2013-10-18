package com.intercloud.cloudstore

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.http.FileContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.About
import com.google.api.services.drive.model.ChangeList
import com.google.api.services.drive.model.FileList
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.ParentReference

import javax.servlet.http.HttpServletRequest

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.FileResource
import com.intercloud.util.ZipUtilities
import com.intercloud.util.CloudStoreUtilities

class GoogledriveCloudStore implements CloudStoreInterface{
	
	private static Logger log = LoggerFactory.getLogger(GoogledriveCloudStore.class)
	
	static String STORE_NAME
	static String CLIENT_ID
	static String CLIENT_SECRET
	static String REDIRECT_URL
	
	private GoogleAuthorizationCodeFlow flow
	private Drive driveService
	private GoogleCredential credential
	
	public def configure(boolean isAuthRedirect, HttpServletRequest request) {
		if(!isAuthRedirect) {
			log.debug "Getting authorize url for google drive"
			String authorizeUrl = getAuthorizeUrl(request)
			return authorizeUrl
		}
		else {
			log.debug "Auth redirect from google drive"
			boolean isSuccess = setGoogledriveApiForConfigure(request)
			return isSuccess
		}
	}
	
	private String getAuthorizeUrl(HttpServletRequest request) {
		HttpTransport httpTransport = new NetHttpTransport()
		JsonFactory jsonFactory = new JacksonFactory()
	   
		flow = new GoogleAuthorizationCodeFlow.Builder(
			httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
			.setAccessType("offline").setApprovalPrompt("force").build()
		
		String authorizeUrl = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URL).build()
		return authorizeUrl
	}
	
	private boolean setGoogledriveApiForConfigure(HttpServletRequest request) {
		String code = request.getParameter("code")
		if(code != null) {
			HttpTransport httpTransport = new NetHttpTransport()
			JsonFactory jsonFactory = new JacksonFactory()
			
			GoogleTokenResponse googleTokenResponse = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URL).execute()
			GoogleCredential googleCredential = new GoogleCredential.Builder().setTransport(httpTransport)
								.setJsonFactory(jsonFactory).setClientSecrets(CLIENT_ID, CLIENT_SECRET)
								.build().setFromTokenResponse(googleTokenResponse)

			driveService = new Drive.Builder(httpTransport, jsonFactory, googleCredential).build()
			
			setCredentialForConfigure(googleCredential)
			return true
		}
		else {
			return false
		}
	}
	
	private void setCredentialForConfigure(GoogleCredential googleCredential ) {
		credential = googleCredential
	}
	
	public void setCloudStoreProperties(CloudStore cloudStoreInstance, Account account) {
		setCloudStoreInfo(cloudStoreInstance)
		setCloudStoreFileResources(cloudStoreInstance)
		setCloudStoreAccount(cloudStoreInstance, account)
	}
	
	private void setCloudStoreInfo(CloudStore cloudStoreInstance) {
		About accountInfo = getAccountInfo()
		
		cloudStoreInstance.storeName = STORE_NAME
		
		cloudStoreInstance.credentials << ['REFRESH_TOKEN': credential.getRefreshToken()]
		cloudStoreInstance.credentials << ['ACCESS_TOKEN': credential.getAccessToken()]
		
		cloudStoreInstance.userId = accountInfo.getName()
		cloudStoreInstance.spaceUsed = accountInfo.getQuotaBytesUsed()
		cloudStoreInstance.totalSpace = accountInfo.getQuotaBytesTotal()
		
		String updateCursor = accountInfo.getLargestChangeId()
		cloudStoreInstance.updateCursor = updateCursor
	}
	
	private def getAccountInfo() {
		return driveService.about().get().execute()
	}
	
	private def setCloudStoreFileResources(CloudStore cloudStoreInstance) {
		def fileResources = getAllGoogledriveResources(cloudStoreInstance)
		cloudStoreInstance.fileResources = fileResources
	}
	
	private def getAllGoogledriveResources(CloudStore cloudStoreInstance) {
		def fileResources = []
		def driveFileIds = []
		
		FileResource rootFileResource = createRootResource(cloudStoreInstance)
		fileResources.add(rootFileResource)
		driveFileIds.add(['driveFileId': 'root', 'fileResourceId' :rootFileResource.id])
		
		def googleDriveResources = getGoogledriveResources()
		for(googleDriveResource in googleDriveResources) {
			if(googleDriveResource.shared) {
				continue
			}
			if(googleDriveResource.mimeType == 'application/vnd.google-apps.folder') {
				def fileResource = createFolderFileResource(cloudStoreInstance, googleDriveResource, driveFileIds, fileResources)
				fileResources.add(fileResource)
			}
			else {
				def fileResource = createFileResource(cloudStoreInstance, googleDriveResource, driveFileIds, fileResources)
				fileResources.add(fileResource)
			}
		}
		
		def updatedFileResourcesWithParent = setParentFileResource(googleDriveResources, fileResources, driveFileIds)
		def updatedFileResourcesWithPath = setPathOfFileResource(fileResources)
		
		return updatedFileResourcesWithPath
	}
	
	private FileResource createRootResource(CloudStore cloudStoreInstance) {
		FileResource fileResource = new FileResource()
		
		fileResource.cloudStore = cloudStoreInstance
		fileResource.fileName = "GoogleDriveRoot"
		fileResource.path = "/"
		fileResource.isDir = true
		fileResource.extraMetadata = 'root'
		
		return fileResource
	}
	
	private def getGoogledriveResources() {
		def googledriveResources = []
		def googledriveRequest = driveService.files().list()
		googledriveRequest.setQ("trashed=false")
		
		getPageOfFiles(googledriveResources, googledriveRequest)
		
		while(googledriveRequest.getPageToken() != null && googledriveRequest.getPageToken().length() > 0) {
			getPageOfFiles(googledriveResources, googledriveRequest)
		}
		
		return googledriveResources
	}
	
	private void getPageOfFiles(def googledriveResources, def googledriveRequest) {
		try {
			FileList fileList = googledriveRequest.execute()
			
			googledriveResources.addAll(fileList.getItems())
			googledriveRequest.setPageToken(fileList.getNextPageToken())
		} catch (IOException e) {
			log.warn "An error occured when getting changes for google drive, Exception: {}", e
			googledriveRequest.setPageToken(null);
		}
	}
	
	private FileResource createFolderFileResource(CloudStore cloudStoreInstance, def googleDriveResource, def driveFileIds, def fileResources) {
		FileResource fileResource = new FileResource()

		fileResource.cloudStore = cloudStoreInstance
		fileResource.fileName = googleDriveResource.title
		fileResource.modified = googleDriveResource.modifiedDate
		fileResource.mimeType = googleDriveResource.mimeType
		fileResource.isDir = true
		fileResource.extraMetadata = googleDriveResource.id
		
		if(googleDriveResource.parents[0].isRoot) {
			def rootDriveId = driveFileIds.find { it.driveFileId == 'root' }
			FileResource parentFileResource = fileResources.find { it.id == rootDriveId.fileResourceId }
			fileResource.parentFileResource = parentFileResource
		}

		String driveFileId = googleDriveResource.id.toString()
		driveFileIds.add(['driveFileId': driveFileId, 'fileResourceId' :fileResource.id])
		
		return fileResource
	}
	
	private FileResource createFileResource(CloudStore cloudStoreInstance, def googleDriveResource, def driveFileIds, def fileResources) {
		FileResource fileResource = new FileResource()
		
		fileResource.cloudStore = cloudStoreInstance
		fileResource.fileName = googleDriveResource.title
		fileResource.modified = googleDriveResource.modifiedDate
		fileResource.mimeType = googleDriveResource.mimeType
		fileResource.isDir = false
		fileResource.byteSize = googleDriveResource.fileSize.toString()
		fileResource.extraMetadata = googleDriveResource.id
		
		if(googleDriveResource.parents[0].isRoot) {
			def rootDriveId = driveFileIds.find { it.driveFileId == 'root' }
			FileResource parentFileResource = fileResources.find { it.id == rootDriveId.fileResourceId }
			fileResource.parentFileResource = parentFileResource
		}
		
		String driveFileId = googleDriveResource.id.toString()
		driveFileIds.add(['driveFileId': driveFileId, 'fileResourceId' :fileResource.id])
		
		return fileResource
	}
	
	private def setParentFileResource(def googleDriveResources, def fileResources, def driveFileIds) {
		def updatedResources = []
		for(fileResource in fileResources) {
			if(fileResource.parentFileResource != null || fileResource.path == '/') {
				updatedResources.add(fileResource)
				continue
			}
			for(fileId in driveFileIds) {
				if(fileResource.id == fileId.fileResourceId) {
					FileResource childFileResource = fileResources.find { it.id == fileId.fileResourceId }
					def parentFileResourceId = getParentFileResourceId(fileId.driveFileId, googleDriveResources, fileResources, driveFileIds)
					childFileResource.parentFileResource = FileResource.get(parentFileResourceId)
					updatedResources.add(childFileResource)
					break
				}
			}
		}
		return updatedResources
	}
	
	private Long getParentFileResourceId(def driveFileId, def googleDriveResources, def fileResources, def driveFileIds) {
		Long parentFileResourceId = null
		for(driveResource in googleDriveResources) {
			if(driveResource.id == driveFileId) {
				def parentId = driveResource.parents[0].id
				for(fileId in driveFileIds) {
					if(fileId.driveFileId == parentId) {
						parentFileResourceId = fileId.fileResourceId
					}
				}
			}
		}
		if(!parentFileResourceId) {
			parentFileResourceId = driveFileIds.find { it.driveFileId == "root" }.fileResourceId
		}
		return parentFileResourceId
	}
	
	private def setPathOfFileResource(def fileResources) {
		def updatedResources = []
		for(fileResource in fileResources) {
			if(fileResource.path == '/') {
				updatedResources.add(fileResource)
				continue
			}
			String path = '/' + fileResource.fileName
			boolean fullPath = false
			FileResource parentFileResource = fileResource.parentFileResource
			while(!fullPath) {
				if(!parentFileResource) {
					break
				}
				if(parentFileResource.path == '/') {
					fullPath = true
				}
				else {
					path = '/' + parentFileResource.fileName + path
					parentFileResource = parentFileResource.parentFileResource
				}
			}
			fileResource.path = path
			updatedResources.add(fileResource)
		}
		
		return updatedResources
	}
	
	private void setCloudStoreAccount(CloudStore cloudStoreInstance, Account account) {
		cloudStoreInstance.account = account
		account.addToCloudStores(cloudStoreInstance)
	}
	
	private void setGoogledriveApi(CloudStore cloudStore) {
		log.debug "Setting google drive credentials for api access"
		def credentials = cloudStore.credentials
		String refresh_token = credentials.REFRESH_TOKEN
		String access_token = credentials.ACCESS_TOKEN
		
		HttpTransport httpTransport = new NetHttpTransport()
		JsonFactory jsonFactory = new JacksonFactory()
		
		credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory)
					.setClientSecrets(CLIENT_ID, CLIENT_SECRET).build()
					.setRefreshToken(refresh_token)
		
		driveService = new Drive.Builder(httpTransport, jsonFactory, credential).build()
		
		cloudStore.credentials << ['REFRESH_TOKEN': credential.getRefreshToken()]
		cloudStore.credentials << ['ACCESS_TOKEN': credential.getAccessToken()]
	}
	
	public def uploadResource(CloudStore cloudStore, def uploadedFile) {
		log.debug "Uploading file to google drive"
		setGoogledriveApi(cloudStore)
		
		boolean isSuccess = uploadToGoogledrive(cloudStore, uploadedFile)
		if(isSuccess) {
			updateGoogledriveSpace(cloudStore)
			return uploadedFile.originalFilename
		}
		else {
			return null
		}
	}
	
	private boolean uploadToGoogledrive(CloudStore cloudStore, def uploadedFile) {
		File body = new File()
		body.title = uploadedFile.originalFilename
		body.mimeType =uploadedFile.contentType
		body.parents = Arrays.asList(new ParentReference().setId('root'))
		
		InputStreamContent mediaContent = new InputStreamContent(uploadedFile.contentType, uploadedFile.inputStream)
		
		try {
			File file = driveService.files().insert(body, mediaContent).execute()
			log.debug "Successfully uploaded file '{}' to googledrive", file.title
			return true
		} catch (IOException e) {
			log.warn "File could not be uploaded to googledrive. Exception {}", e
			return false
		}
	}

	public InputStream downloadResource(CloudStore cloudStore, FileResource fileResource) {
		setGoogledriveApi(cloudStore)
		
		def downloadedStream = null
		if(fileResource.isDir) {
			log.debug "Downloading folder and building zip from google drive"
			downloadedStream = getZippedGoogledriveFolderStream(fileResource)
		}
		else {
			log.debug "Downloading file from google drive"
			downloadedStream = getGoogledriveFileStream(fileResource)
		}
		
		return downloadedStream
	}
	
	private InputStream getZippedGoogledriveFolderStream(FileResource fileResource) {
		String downloadedFolderPath = ZipUtilities.getDownloadedFolderPath(fileResource)
		String zipFileName = ZipUtilities.getSourceZipName(STORE_NAME, fileResource)
		
		if(!doesFolderExistInGoogledrive(fileResource)) {
			return null
		}
		
		log.debug "Downloading folder to temporary zip storage"
		downloadFolderToPath(downloadedFolderPath, fileResource)
		
		log.debug "Zipping downloaded folder to '{}'", zipFileName
		ZipUtilities.zipFolder(downloadedFolderPath, zipFileName)
		
		String zipFileLocation = getZipFileLocation(downloadedFolderPath)
		InputStream zippedFolderInputStream = ZipUtilities.getInputStreamFromZipFile(zipFileLocation, zipFileName)
		
		ZipUtilities.removeTempFromFileSystem(zipFileLocation)
		
		return zippedFolderInputStream
	}
	
	private boolean doesFolderExistInGoogledrive(FileResource fileResource) {
		if(fileResource.path == '/') {
			return true
		}
		
		String fileId = fileResource.extraMetadata
		def file = driveService.files().get(fileId)
		
		if(file == null) {
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
				new java.io.File(updatedPath).mkdir()
				downloadFolderToPath(updatedPath, childResource)
			}
			else {
				InputStream resourceDataStream = getGoogledriveFileStream(childResource)
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
	
	private InputStream getGoogledriveFileStream(FileResource fileResource) {
		InputStream inputStream = null
		
		String fileId = fileResource.extraMetadata
		def file = driveService.files().get(fileId).execute()
		
		if (file.downloadUrl != null && file.downloadUrl.length() > 0) {
			try {
				HttpResponse resp = driveService.getRequestFactory().buildGetRequest(new GenericUrl(file.downloadUrl)).execute();
				inputStream = resp.getContent()
				log.debug "Downloaded file '{}' from google drive", fileResource.fileName
			} catch (IOException e) {
				log.warn "An error occured when downloading file from google drive, Exception: {}", e
			}
		}
		
		return inputStream
	}
	
	private String getZipFileLocation(String path) {
		String zipFileLocation = path.substring(0, path.lastIndexOf('/'))
		return zipFileLocation
	}
	
	public boolean deleteResource(CloudStore cloudStore, FileResource fileResource) {
		log.debug "Deleting resource {}", fileResource.path
		setGoogledriveApi(cloudStore)
		
		boolean isSuccess = deleteFromGoogledrive(fileResource)
		
		updateGoogledriveSpace(cloudStore)
		return isSuccess
	}
	
	private boolean deleteFromGoogledrive(FileResource fileResource) {
		String fileId = fileResource.extraMetadata
		
		try {
			driveService.files().delete(fileId).execute()
			return true
		} catch (IOException e) {
		  	log.warn "File could not be deleted from google drive: {}", e
			return false
		}
	}
	
	private void updateGoogledriveSpace(CloudStore cloudStore) {
		log.debug "Updating google drive space"
		
		About accountInfo = getAccountInfo()
		
		cloudStore.spaceUsed = accountInfo.getQuotaBytesUsed()
		cloudStore.totalSpace = accountInfo.getQuotaBytesTotal()
	}
	
	public def updateResources(CloudStore cloudStore, String updateCursor, def currentFileResources) {
		log.debug "Updating google drive file resources for account '{}'", cloudStore.account.email
		setGoogledriveApi(cloudStore)
		
		def changes = getChanges(updateCursor)
		def newUpdateCursor = changes.largestChangeId
		def changedResources = changes.changedResources
		
		if(changedResources) {
			log.debug "Updates to google drive found. Syncing updates"
			addNewEntries(cloudStore, changedResources, currentFileResources)
		}
		
		updateGoogledriveSpace(cloudStore)
		
		return newUpdateCursor
	}
	
	private def getChanges(String updateCursor) {
		def changes = []
		def largestChangeId = null
		def changedResources = []
		def changeRequest = driveService.changes().list()
		
		changeRequest.setIncludeSubscribed(false)
		changeRequest.setStartChangeId(updateCursor.toInteger())
		largestChangeId = getPageOfChanges(changedResources, changeRequest)
		
		while(changeRequest.getPageToken() != null && changeRequest.getPageToken().length() > 0) {
			largestChangeId = getPageOfChanges(changedResources, changeRequest)
		}
		
		return ['largestChangeId': largestChangeId.toString(), 'changedResources': changedResources]
	}
	
	private Long getPageOfChanges(def changedResources, def changeRequest) {
		try {
			ChangeList changeList = changeRequest.execute()
			
			changedResources.addAll(changeList.getItems())
			changeRequest.setPageToken(changeList.getNextPageToken())
			return changeList.getLargestChangeId()
		} catch (IOException e) {
			log.warn "An error occured when getting changes for google drive, Exception: {}", e
			changeRequest.setPageToken(null)
		}
	}
	
	private void addNewEntries(CloudStore cloudStore, def changedResources, def currentFileResources) {
		for(changedResource in changedResources) {
			if(changedResource.deleted || changedResource.file?.labels?.trashed) {
				if(!cloudStore.fileResources.find {it.extraMetadata == changedResource.fileId}) {
					log.debug "Deleted file from updates that we don't track, continue"
					continue
				}
				log.debug "Google drive resource was deleted"
				deleteChangedGoogledriveResouce(changedResource, currentFileResources)
			}
			else {
				log.debug "Google drive resource changed: '{}'", changedResource.file.title
				boolean isEntryUpdated = updateEntryIfExists(cloudStore, changedResource, currentFileResources)
				if(!isEntryUpdated) {
					currentFileResources = addToFileResources(cloudStore, changedResource, currentFileResources)
				}
			}
		}
	}
	
	private void deleteChangedGoogledriveResouce(def changedResource, def currentFileResources) {
		FileResource fileResource = null
		for(FileResource currentFileResource : currentFileResources) {
			if(changedResource.fileId == currentFileResource.extraMetadata) {
				fileResource = currentFileResource
				break
			}
		}
		if(fileResource) {
			CloudStoreUtilities.deleteFromDatabase(fileResource)
		}
		else {
			// we previously deleted file. Google drive doesn't know so is still informing, ignore this
		}
	}
	
	private boolean updateEntryIfExists(CloudStore cloudStore, def changedResource, def currentFileResources) {
		def googledriveFile = changedResource.file
		boolean isEntryUpdated = false
		for(FileResource fileResource : currentFileResources) {
			if(googledriveFile.id == fileResource.extraMetadata) {
				updateChangedFileResource(cloudStore, fileResource, googledriveFile)
				isEntryUpdated = true
				break
			}
		}
		
		return isEntryUpdated
	}
	
	private void updateChangedFileResource(CloudStore cloudStore, FileResource currentFileResource, def googledriveFile) {
		if(googledriveFile.mimeType == 'application/vnd.google-apps.folder') {
			currentFileResource = setFolderFileResourceProperties(cloudStore, currentFileResource, googledriveFile)
		}
		else {
			currentFileResource = setFileResourceProperties(cloudStore, currentFileResource, googledriveFile)
		}
	
	}
	
	private FileResource setFolderFileResourceProperties(CloudStore cloudStore, FileResource fileResource, def googledriveFile) {
		fileResource.cloudStore = cloudStore
		fileResource.isDir = true
		fileResource.fileName = googledriveFile.title
		fileResource.mimeType = "application/octet-stream"
		fileResource.extraMetadata = googledriveFile.id
		fileResource.path = getPathOfUpdatedFileResource(fileResource)
		
		return fileResource
	}
	
	private FileResource setFileResourceProperties(CloudStore cloudStore, FileResource fileResource, def googledriveFile) {
		fileResource.cloudStore = cloudStore
		fileResource.byteSize = googledriveFile.fileSize.toString()
		fileResource.modified = googledriveFile.modifiedDate
		fileResource.isDir = false
		fileResource.fileName = googledriveFile.title
		fileResource.extraMetadata = googledriveFile.id
		fileResource.mimeType = googledriveFile.mimeType
		fileResource.path = getPathOfUpdatedFileResource(fileResource)
		
		return fileResource
	}
	
	private def addToFileResources(CloudStore cloudStore, def changedResource, def currentFileResources) {
		def googledriveFile = changedResource.file
		FileResource fileResource = new FileResource()
		if(googledriveFile.mimeType == 'application/vnd.google-apps.folder') {
			fileResource = setFolderFileResourceProperties(cloudStore, fileResource, googledriveFile)
		}
		else {
			fileResource = setFileResourceProperties(cloudStore, fileResource, googledriveFile)
		}

		cloudStore.addToFileResources(fileResource)
		
		currentFileResources = CloudStoreUtilities.setParentAndChildFileResources(fileResource, currentFileResources)
		return currentFileResources
	}
	
	private String getPathOfUpdatedFileResource(FileResource fileResource) {
		def googledriveResources = getGoogledriveResources()
		String path = '/' + fileResource.fileName
		boolean fullPath = false
		def parentDriveResource = getParentDriveResource(fileResource, googledriveResources)
		while(!fullPath) {
			if(!parentDriveResource?.name || parentDriveResource?.name == "My Drive") {
				fullPath = true
			}
			else {
				path = '/' + parentDriveResource.name + path
				parentDriveResource = getNextParentResource(parentDriveResource.id, googledriveResources)
			}
		}
		return path
	}
	
	private def getParentDriveResource(FileResource fileResource, def googledriveResources) {
		def parentDriveResource = null
		for(googledriveResource in googledriveResources) {
			if(googledriveResource.id == fileResource.extraMetadata) {
				String parentFileId = googledriveResource.parents[0].id
				def parentGoogleFile = driveService.files().get(parentFileId).execute()
				
				parentDriveResource = ['name': parentGoogleFile.title, 'id': parentGoogleFile.id]
				return parentDriveResource
			}
		}
	}
	
	private def getNextParentResource(def parentDriveResourceId, def googledriveResources) {
		def parentDriveResource = null
		for(googledriveResource in googledriveResources) {
			if(googledriveResource.id == parentDriveResourceId) {
				String parentFileId = googledriveResource.parents[0].id
				def parentGoogleFile = driveService.files().get(parentFileId).execute()
				
				parentDriveResource = ['name': parentGoogleFile.title, 'id': parentGoogleFile.id]
				return parentDriveResource
			}
		}
	}
}
