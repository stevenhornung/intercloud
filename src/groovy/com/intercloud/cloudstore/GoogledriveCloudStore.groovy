package com.intercloud.cloudstore

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.About
import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.FileResource

import javax.servlet.http.HttpServletRequest

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GoogledriveCloudStore implements CloudStoreInterface{
	
	private static Logger log = LoggerFactory.getLogger(GoogledriveCloudStore.class)
	
	String STORE_NAME = 'googledrive'
	String CLIENT_ID = "887098665005.apps.googleusercontent.com"
	String CLIENT_SECRET = "OZQKsV0dGM04h-FtNt-VpGIF"
	String REDIRECT_URL = "http://localhost:8080/auth_redirect"
	String ZIP_TEMP_STORAGE_PATH = "storage/TemporaryZipStorage"
	
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
			setGoogledriveApiForConfigure(request)
		}
	}
	
	private String getAuthorizeUrl(HttpServletRequest request) {
		HttpTransport httpTransport = new NetHttpTransport()
		JsonFactory jsonFactory = new JacksonFactory()
	   
		flow = new GoogleAuthorizationCodeFlow.Builder(
			httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
			.setAccessType("online")
			.setApprovalPrompt("force").build()
		
		String authorizeUrl = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URL).build()
		return authorizeUrl
	}
	
	private void setGoogledriveApiForConfigure(HttpServletRequest request) {
		String code = request.getParameter("code")
		if(code != null) {
			GoogleTokenResponse googleTokenResponse = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URL).execute()
			GoogleCredential googleCredential = new GoogleCredential().setFromTokenResponse(googleTokenResponse)
			
			HttpTransport httpTransport = new NetHttpTransport()
			JsonFactory jsonFactory = new JacksonFactory()
			
			driveService = new Drive.Builder(httpTransport, jsonFactory, googleCredential).build()
			
			setCredentialForConfigure(googleCredential)
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
		Map driveFileIds = [:]
		
		FileResource rootFileResource = createRootResource(cloudStoreInstance)
		fileResources.add(rootFileResource)
		driveFileIds << ["root":rootFileResource.id]
		
		def googleDriveResources = driveService.files().list().execute().getItems()
		for(googleDriveResource in googleDriveResources) {
			if(googleDriveResource.shared || googleDriveResource.labels.trashed) {
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
		
		fileResource.save()
		
		return fileResource
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
			FileResource parentFileResource = fileResources.find { it.id == driveFileIds['root'] }
			fileResource.parentFileResource = parentFileResource
		}
		
		fileResource.save()

		String driveFileId = googleDriveResource.id.toString()
		driveFileIds << [(driveFileId):fileResource.id]
		
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
			FileResource parentFileResource = fileResources.find { it.id == driveFileIds['root'] }
			fileResource.parentFileResource = parentFileResource
		}
		
		fileResource.save()
		
		String driveFileId = googleDriveResource.id.toString()
		driveFileIds << [(driveFileId):fileResource.id]
		
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
				if(fileResource.id == fileId.value) {
					FileResource childFileResource = fileResources.find { it.id == fileId.value }
					def parentFileResourceId = getParentFileResourceId(fileId.key, googleDriveResources, fileResources, driveFileIds)
					childFileResource.parentFileResource = FileResource.get(parentFileResourceId)
					updatedResources.add(childFileResource)
					break
				}
			}
		}
		return updatedResources
	}
	
	private long getParentFileResourceId(def driveFileId, def googleDriveResources, def fileResources, def driveFileIds) {
		for(driveResource in googleDriveResources) {
			if(driveResource.id == driveFileId) {
				def parentId = driveResource.parents[0].id
				for(fileId in driveFileIds) {
					if(fileId.key == parentId) {
						return fileId.value
					}
				}
			}
		}
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
	
	private void setGoogledriveApiWithCredentials(def credentials) {
		log.debug "Setting google drive credentials for api access"
		String refresh_token = credentials.REFRESH_TOKEN
		String access_token = credentials.ACCESS_TOKEN
		credential = new GoogleCredential().setRefreshToken(refresh_token)
		credential.setAccessToken(access_token)
		
		HttpTransport httpTransport = new NetHttpTransport()
		JsonFactory jsonFactory = new JacksonFactory()
		
		driveService = new Drive.Builder(httpTransport, jsonFactory, credential).build()
	}
	
	public def uploadResource(CloudStore cloudStore, def uploadedFile) {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream downloadResource(def credentials, FileResource fileResource) {
		setGoogledriveApiWithCredentials(credentials)
		
		String fileId = fileResource.extraMetadata
		def file = driveService.files().get(fileId).execute()
		
		if (file.downloadUrl != null && file.downloadUrl.length() > 0) {
			try {
				HttpResponse resp = driveService.getRequestFactory().buildGetRequest(new GenericUrl(file.downloadUrl)).execute();
				InputStream downloadedStream = resp.getContent()
				return downloadedStream
			} catch (IOException e) {
			  	log.warn "An error occured when downloading file from google drive, Exception: {}", e
				return null
			}
		} 
		else {
			return null
		 }
	}
	
	public void deleteResource(CloudStore cloudStore, FileResource fileResource) {
		
	}
	
	public def updateResources(CloudStore cloudStore, String updateCursor, def currentFileResources) {
		// TODO Auto-generated method stub
		return null;
	}

}
