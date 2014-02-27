package com.intercloud

import com.intercloud.cloudstore.*
import com.intercloud.util.CloudStoreUtilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CloudStoreController extends BaseController {

	private static Logger log = LoggerFactory.getLogger(CloudStoreController.class)
	private static String ROOT_DIR = "/"

	def cloudStoreService

	public def index() {
		Account account = getCurrentAccount()
		if(account) {

			String cloudStoreName = params.storeName
			if(cloudStoreName) {
				linkCloudStore(account, cloudStoreName)
			}
			else {
				log.debug "No store name specified to link to"
				renderHomeResources()
			}
		}
		else {
			log.warn "Passed spring security as logged in user but getCurrentAccount returned null"
			forward(controller: 'base', action: 'respondServerError')
		}
	}

	private void linkCloudStore(Account account, String cloudStoreName) {
		log.debug "Adding cloud store '{}' to account '{}'", cloudStoreName, account.email

		def cloudStoreClass = cloudStoreService.getCloudStoreClass(cloudStoreName)
		if(cloudStoreClass) {
			def clientAccessRequestUrl = cloudStoreService.getClientAccessRequestUrl(cloudStoreClass, request)
			if(clientAccessRequestUrl) {
				session['cloudStoreClass'] = cloudStoreClass
				redirect(url : clientAccessRequestUrl)
			}
			else {
				log.debug "Retrieving of cloud store request url failed from '{}'", cloudStoreName
				flash.error = message(code: 'service.linkfailed', args: [cloudStoreName])
				renderHomeResources()
			}
		}
		else {
			log.debug "Bad cloud store specified to link"
			renderHomeResources()
		}
	}

	def authRedirect = {
		log.debug "Auth redirect from cloud store url"

		Account account = getCurrentAccount()
		def cloudStoreClass = session['cloudStoreClass']
		session.removeAttribute('cloudStoreClass')

		if(cloudStoreClass) {
			boolean didFinishConfigure = cloudStoreService.authRedirect(account, cloudStoreClass, request)

			if(!didFinishConfigure) {
				// Did not successfully configure link
				flash.error = message(code: 'cloudstore.linkfailed')
			}
			else {
				// Successfully configured. Currently linking async
				flash.info = message(code: 'cloudstore.linking', args: [cloudStoreClass.STORE_NAME.capitalize()])
			}
		}
		else {
			// No cloud store class in the session. something goofed
			flash.error = message(code: 'cloudstore.linkfailed')
		}

		renderHomeResources()
	}

	public def renderHomeResources() {
		Account account = getCurrentAccount()
		def homeResources = cloudStoreService.getHomeCloudStoreResources(account)
		render (view: "index", template: "layouts/homeResources", model: [homeResources : homeResources])
	}

	public def getAllCloudStoreResources() {
		Account account = getCurrentAccount()
		def cloudStoreName = params.storeName
		renderCloudStore(account, cloudStoreName, ROOT_DIR)
	}

	public def getSpecificCloudStoreResource() {
		Account account = getCurrentAccount()
		def cloudStoreName = params.storeName
		def fileResourcePath = '/' + params.fileResourcePath

		// when we open a folder, the /js/* gets handed to /{foldername}/js/* so not caught by resources plugin
		// handle this special case
		if(fileResourcePath.contains("/js/")) {
			if(cloudStoreName) {
				getAllCloudStoreResources()
				return
			}
			else {
				forward(controller: 'base', action: 'respondPageNotFound')
			}
		}

		log.debug "Getting '{}' file resource from '{}'", fileResourcePath, cloudStoreName
		FileResource specificCloudStoreResource = cloudStoreService.getFileResourceFromPath(account, cloudStoreName, fileResourcePath)

		if(specificCloudStoreResource) {
			if(specificCloudStoreResource.isDir) {
				renderCloudStore(account, cloudStoreName, specificCloudStoreResource.path)
			}
			else {
				renderFileResource(cloudStoreName, specificCloudStoreResource)
			}
		}
		else {
			log.debug "Could not find specific cloud store resource: {}", fileResourcePath
			if(cloudStoreName) {
				flash.error = message(code: 'cloudstore.specificnotfound', args: [fileResourcePath])
				getAllCloudStoreResources()
			}
			else {
				forward(controller: 'base', action: 'respondPageNotFound')
			}
		}
	}

	private void renderFileResource(String storeName, FileResource fileResource) {

		// Only attempt to render certain mime types
		if(fileResource.mimeType in RENDER_TYPES) {
			def cloudStoreFileStream = cloudStoreService.getFileResourceStream(storeName, fileResource)
			if(cloudStoreFileStream) {
				log.debug "Rendering file resource to screen"
				renderBytesToScreen(fileResource, cloudStoreFileStream)
			}
			else {
				log.warn "File resource data could not be retrieved from {}", storeName
				flash.error = message(code: 'cloudstore.datanotretrieved', args: [fileResource.fileName, storeName])
				getAllCloudStoreResources()
			}
		}
		else if(fileResource.mimeType in VIDEO_TYPES){

			// Render as video
			def cloudStoreFileStream = cloudStoreService.getFileResourceStream(storeName, fileResource)
			if(cloudStoreFileStream) {
				log.debug "Rendering video file resource to screen"
				displayVideo(fileResource, cloudStoreFileStream)
			}
			else {
				log.warn "File resource data could not be retrieved from {}", storeName
				flash.error = message(code: 'cloudstore.datanotretrieved', args: [fileResource.fileName, storeName])
				getAllCloudStoreResources()
			}
		}
		else {

			// Any other mimetype we just want to display a download link for
			renderDownloadLink(fileResource, storeName)
		}
	}

	private def renderBytesToScreen(FileResource fileResource, InputStream cloudStoreFileStream) {
		try{
			response.contentType = fileResource.mimeType
			response.contentLength = fileResource.byteSize.toInteger()
			response.outputStream << cloudStoreFileStream
			response.outputStream.flush()
			response.outputStream.close()
		}
		catch (Exception) {
			//Do nothing, Client probably clicked out during load of data
		}
	}

	private def displayVideo(FileResource fileResource, InputStream cloudStoreFileStream) {
		try {
			response.contentLength = fileResource.byteSize.toInteger()
		    response.addHeader("Content-disposition", "attachment; filename=${fileResource.fileName}")
		    response.contentType = fileResource.mimeType
		    response.outputStream << cloudStoreFileStream
		    response.outputStream.flush()
		    response.outputStream.close()
		}
		catch (Exception) {
			// do nothing, client probably clicked out during load
		}
	}

	private def renderDownloadLink(FileResource fileResource, String storeName) {
		try{
			def downloadLink = "<html><head></head><body><img src='${resource(dir: 'images', file: 'file.jpeg')}' height=50 width=50 ><br><a href='/download?fileResourceId=${fileResource.id}&storeName=${storeName}'>Download</a></body></html>"

			response.outputStream << downloadLink
			response.outputStream.flush()
			response.outputStream.close()
		}
		catch (Exception) {
			log.debug "Download link could not be rendered to output stream: {}", Exception
			flash.error = message(code: 'cloudstore.error')
			getAllCloudStoreResources()
		}
	}

	public def deleteResource() {
		log.debug "Deleting file resource"

		Account account = getCurrentAccount()
		def cloudStoreName = params.storeName
		String fileResourceId = params.fileResourceId
		boolean isSuccess = cloudStoreService.deleteResource(account, cloudStoreName, fileResourceId)

		if(!isSuccess) {
			flash.error = message(code: 'cloudstore.deletefailed', args: [cloudStoreName])
		}

		// Redirect back to where file resource was deleted from and then worry about rendering
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
				flash.error = message(code: 'cloudstore.error')
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
			flash.error = message(code: 'cloudstore.error')
			getAllCloudStoreResources()
		}
	}

	public def updateResources() {
		Account account = getCurrentAccount()
		String cloudStoreName = params.storeName

		cloudStoreService.updateResources(account, cloudStoreName)

		if(cloudStoreName) {
			renderCloudStore(account, cloudStoreName, ROOT_DIR)
		}
		else {
			forward(controller: 'home', action:'index')
		}
	}

	public def uploadResource() {
		Account account = getCurrentAccount()
		String cloudStoreName = params.storeName
		def fileInstanceList

		if(cloudStoreName in CLOUD_STORES) {
			log.debug "Uploading file to {}", cloudStoreName

			def uploadedFile = request.getFile('file')
			String targetDirectory = params.targetDir

			boolean isSuccess = cloudStoreService.uploadResource(account, cloudStoreName, uploadedFile, targetDirectory)

			if(!isSuccess) {
				flash.error = message(code: 'cloudstore.uploadfailed', args: [uploadedFile.originalFilename, cloudStoreName])
			}
		}
		else {
			forward(controller: 'base', action: 'respondPageNotFound')
		}

		renderCloudStore(account, cloudStoreName, ROOT_DIR)
	}

	private void renderCloudStore(Account account, String cloudStoreName, String directory) {
		CloudStore cloudStore = cloudStoreService.getAccountCloudStore(account, cloudStoreName)

		def totalSpaceList
		def spaceUsedList
		def fileInstanceList

		if(cloudStore) {
			totalSpaceList = cloudStoreService.getSpaceList(cloudStore.totalSpace)
			spaceUsedList = cloudStoreService.getSpaceList(cloudStore.spaceUsed)

			fileInstanceList = cloudStoreService.getSpecificCloudStoreResources(account, cloudStoreName, directory)
		}

		render(view : cloudStoreName, template: "layouts/${cloudStoreName}Resources", model: [fileInstanceList: fileInstanceList, totalSpaceList: totalSpaceList, spaceUsedList: spaceUsedList])
	}
}
