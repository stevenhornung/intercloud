package com.intercloud

import com.intercloud.cloudstore.*
import com.intercloud.util.CloudStoreUtilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CloudStoreController extends BaseController {
	
	private static Logger log = LoggerFactory.getLogger(CloudStoreController.class)
	
	def cloudStoreService
	
	public def index() {
		if(getCurrentAccount()) {
			String storeName = params.storeName
			if(storeName) {
				log.debug "Adding cloud store '{}' to account '{}'", storeName, getCurrentAccount().email
				def cloudStoreLink = cloudStoreService.getCloudStoreLink(storeName)
				if(cloudStoreLink) {
					def clientAccessRequestUrl = cloudStoreService.getClientAccessRequestUrl(cloudStoreLink, request)
					if(clientAccessRequestUrl) {
						session['cloudStoreLink'] = cloudStoreLink
						redirect(url : clientAccessRequestUrl)
					}
					else {
						log.debug "Retrieving of cloud store request url failed from {}", storeName
						flash.message = message(code: 'service.linkfailed', args: [storeName])
						redirect(controller: 'home', action: 'index')
					}
				}
				else {
					log.debug "Bad cloud store specified to link"
					redirect(controller: 'home', action: 'index')
				}
			}
			else {
				log.debug "No store name specified to link to"
				redirect(controller: 'home', action: 'index')
			}
		}
		else {
			log.warn "Passed spring security as logged in user but getCurrentAccount returned null"
			forward(controller: 'base', action: 'respondServerError')
		}
	}
	
	def authRedirect = {
		log.debug "Auth redirect"
		Account account = getCurrentAccount()
		def cloudStoreLink = session['cloudStoreLink']
		session.removeAttribute('cloudStoreLink')
		if(cloudStoreLink) {
			boolean isSuccess = cloudStoreService.authRedirect(account, cloudStoreLink, request)
			
			if(!isSuccess) {
				flash.message = message(code: 'cloudstore.linkfailed')
			}
			else {
				flash.message = message(code: 'cloudstore.linking', args: [cloudStoreLink.STORE_NAME.capitalize()])
			}
		}
		else {
			flash.message = message(code: 'cloudstore.linkfailed')
		}
		
		forward(controller: 'home', action:'index')
	}

	
	public def getHomeCloudStoreResources(Account account, String storeName) {
		def homeCloudStoreResources = cloudStoreService.getHomeCloudStoreResources(account, storeName)
		return homeCloudStoreResources
	}
	
	public def getAllCloudStoreResources() {
		Account account = getCurrentAccount()
		def storeName = params.storeName
		def cloudStoreResources = cloudStoreService.getAllCloudStoreResources(account, storeName)
		if(cloudStoreResources != null) {
			def cloudStore = cloudStoreService.getAccountCloudStore(account, storeName)
			def totalSpaceList = cloudStoreService.getSpaceList(cloudStore.totalSpace)
			def spaceUsedList = cloudStoreService.getSpaceList(cloudStore.spaceUsed)

			render (view : storeName, model: [fileInstanceList: cloudStoreResources, totalSpaceList: totalSpaceList, spaceUsedList: spaceUsedList])
		}
		else {
			render (view : storeName, model: [fileInstanceList: cloudStoreResources])
		}
	}
	
	public def getSpecificCloudStoreResource() {
		Account account = getCurrentAccount()
		def storeName = params.storeName
		def fileResourcePath = '/' + params.fileResourcePath
		
		// for some reason when we open a folder, the /js/* patterns arent getting handled by resources plugin
		if(fileResourcePath =~ '^/js/') {
			return
		}
		
		def specificCloudStoreResource = cloudStoreService.getFileResourceFromPath(account, storeName, fileResourcePath)
		
		if(specificCloudStoreResource) {
			if(specificCloudStoreResource.isDir) {
				def directoryResources = cloudStoreService.retrieveFilesInDir(specificCloudStoreResource)
				def cloudStore = cloudStoreService.getAccountCloudStore(account, storeName)
				def totalSpaceList = cloudStoreService.getSpaceList(cloudStore.totalSpace)
				def spaceUsedList = cloudStoreService.getSpaceList(cloudStore.spaceUsed)
				
				render (view : storeName, model: [fileInstanceList: directoryResources, totalSpaceList: totalSpaceList, spaceUsedList: spaceUsedList])
			}
			else {
				renderFileResource(storeName, specificCloudStoreResource)
			}
		}
		else {
			log.debug "Could not find specific cloud store resources: {}", fileResourcePath
			if(storeName) {
				flash.message = message(code: 'cloudstore.specificnotfound', args: [fileResourcePath])
				getAllCloudStoreResources()
			}
			else {
				forward(controller: 'base', action: 'respondPageNotFound')
			}
		}
	}
	
	private void renderFileResource(String storeName, FileResource fileResource) {
		if(fileResource.mimeType in RENDER_TYPES) {
			def cloudStoreFileStream = cloudStoreService.getFileResourceStream(storeName, fileResource)
			if(cloudStoreFileStream) {
				renderBytesToScreen(fileResource, cloudStoreFileStream)
			}
			else {
				log.warn "File resource data could not be retrieved from {}", storeName
				flash.message = message(code: 'cloudstore.datanotretrieved', args: [fileResource.fileName, storeName])
				getAllCloudStoreResources()
			}
		}
		else if(fileResource.mimeType in VIDEO_TYPES){
			def cloudStoreFileStream = cloudStoreService.getFileResourceStream(storeName, fileResource)
			if(cloudStoreFileStream) {
				displayVideo(fileResource, cloudStoreFileStream, storeName /*wont need storename normally*/)
			}
			else {
				log.warn "File resource data could not be retrieved from {}", storeName
				flash.message = message(code: 'cloudstore.datanotretrieved', args: [fileResource.fileName, storeName])
				getAllCloudStoreResources()
			}
		}
		else {
			renderDownloadLink(fileResource, storeName)
		}
	}
	
	private def renderBytesToScreen(FileResource fileResource, InputStream cloudStoreFileStream) {
		try{
			response.contentType = fileResource.mimeType
			response.contentLength = fileResource.byteSize.toInteger()
			response.outputStream << cloudStoreFileStream
			response.outputStream.flush()
		}
		catch (Exception) {
			//Do nothing, Client clicked out during load of data
		}
	}
	
	private def displayVideo(FileResource fileResource, InputStream cloudStoreFileStream, String storeName /*wont need storeName normally*/) {
		// need to figure out how to buffer video then show render in a video
		// just allow downloads for now 
		renderDownloadLink(fileResource, storeName)
	}
	
	private def renderDownloadLink(FileResource fileResource, String storeName) {
		try{
			def downloadLink = "<html><head></head><body><a href='/download?fileResourceId=${fileResource.id}&storeName=${storeName}'>Download File</a></body></html>"

			response.outputStream << downloadLink
			response.outputStream.flush()
		}
		catch (Exception) {
			log.debug "Download link could not be rendered to output stream: {}", Exception
			flash.message = message(code: 'cloudstore.error')
			getAllCloudStoreResources()
		}
	}
	
	public def deleteResource() {
		Account account = getCurrentAccount()
		def storeName = params.storeName
		String fileResourceId = params.fileResourceId
		boolean isSuccess = cloudStoreService.deleteResource(account, storeName, fileResourceId)
		
		if(!isSuccess) {
			flash.message = message(code: 'cloudstore.deletefailed', args: [storeName])
		}
		
		redirect(uri: params.targetUri)
	}
	
	public def showDownloadDialog() {
		def storeName = params.storeName
		if(params.fileResourceId) {
			FileResource fileResource = FileResource.get(params.fileResourceId)
			if(fileResource) {
				showFileResourceDownload(storeName, fileResource)
			}
			else {
				log.debug "File resource not found from download dialog"
				flash.message = message(code: 'cloudstore.error')
				getAllCloudStoreResources()
			}
		}
		else {
			log.debug "Downloading entire cloud store root as zip"
			Account account = getCurrentAccount()
			String fileResourcePath = '/'
			FileResource rootFileResource = cloudStoreService.getFileResourceFromPath(account, storeName, fileResourcePath)

			showFileResourceDownload(storeName, rootFileResource)
		}
	}
	
	private def showFileResourceDownload(String storeName, FileResource fileResource) {
		InputStream fileResourceStream = cloudStoreService.getFileResourceStream(storeName, fileResource)
		try{
			response.contentType = fileResource.mimeType
			if(fileResource.isDir) {
				response.setHeader "Content-disposition", "attachment;filename=${fileResource.fileName}.zip"
			}
			else {
				response.setHeader "Content-disposition", "attachment;filename=${fileResource.fileName}"
			}
			response.outputStream << fileResourceStream
			response.outputStream.flush()
		}
		catch (Exception) {
			log.debug "File could not be sent to output stream: {}", Exception
			flash.message = message(code: 'cloudstore.error')
			getAllCloudStoreResources()
		}
	}
	
	public def updateResources() {
		Account account = getCurrentAccount()
		String cloudStoreName = params.storeName
		String targetUri = params.targetUri ?: "/home"
		
		cloudStoreService.updateResources(account, cloudStoreName)

		redirect uri: targetUri
	}
	
	public def uploadResource() {
		Account account = getCurrentAccount()
		String cloudStoreName = params.storeName
		if(cloudStoreName in CLOUD_STORES) {
			log.debug "Uploading file to {}", cloudStoreName
		
			def uploadedFile = request.getFile('file')
			boolean isSuccess = cloudStoreService.uploadResource(account, cloudStoreName, uploadedFile)
			
			if(isSuccess) {
				response.sendError(200)
			}
			else {
				flash.message = message(code: 'cloudstore.uploadfailed', args: [uploadedFile.originalFilename, cloudStoreName])
			}
		}
		else {
			forward(controller: 'base', action: 'respondPageNotFound')
		}
	}
}
