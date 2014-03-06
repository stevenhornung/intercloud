package com.intercloud

import com.intercloud.cloudstore.*
import com.intercloud.util.CloudStoreUtilities

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CloudStoreController extends BaseController {

	private static Logger log = LoggerFactory.getLogger(CloudStoreController.class)
	 static String ROOT_DIR

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

		// Just to rewrite the url so its not /auth_redirect..
		redirect(uri : "/home")
	}

	public def renderHomeResources() {
		Account account = getCurrentAccount()
		def homeResources = cloudStoreService.getHomeCloudStoreResources(account)
		render (view: "index", model: [homeResources : homeResources])
	}

	public def getAllCloudStoreResources() {
		Account account = getCurrentAccount()
		def cloudStoreName = params.storeName
		renderCloudStore(account, cloudStoreName, ROOT_DIR, false)
	}

	public def getSpecificCloudStoreResource() {
		Account account = getCurrentAccount()
		def cloudStoreName = params.storeName
		def fileResourcePath = '/' + params.fileResourcePath

		// when we open a folder, the /js/* gets handed to /{foldername}/js/* so not caught by resources plugin
		// handle this special case
		if(fileResourcePath.contains("/js/")) {
			handleJsPaths(cloudStoreName)
			return
		}

		log.debug "Getting '{}' file resource from '{}'", fileResourcePath, cloudStoreName
		handleSpecificCloudStoreResource(account, cloudStoreName, fileResourcePath)
	}

	private void handleJsPaths(String cloudstoreName) {
		if(cloudStoreName) {
			getAllCloudStoreResources()
		}
		else {
			forward(controller: 'base', action: 'respondPageNotFound')
		}
	}

	private void handleSpecificCloudStoreResource(Account account, String cloudStoreName, String fileResourcePath) {
		FileResource specificCloudStoreResource = cloudStoreService.getFileResourceFromPath(account, cloudStoreName, fileResourcePath)

		if(specificCloudStoreResource) {
			if(specificCloudStoreResource.isDir) {
				renderCloudStore(account, cloudStoreName, specificCloudStoreResource.path, false)
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

	private void renderFileResource(String cloudStoreName, FileResource fileResource) {

		// Only attempt to render certain mime types
		if(fileResource.mimeType in RENDER_TYPES) {
			handleRenderable(cloudStoreName, fileResource, false)
		}
		else if(fileResource.mimeType in VIDEO_TYPES){
			// display as video
			handleRenderable(cloudStoreName, fileResource, true)
		}
		else {
			// Any other mimetype we just want to display a download link for
			renderDownloadLink(fileResource, cloudStoreName)
		}
	}

	private void handleRenderable(String cloudStoreName, FileResource fileResource, boolean isVideo) {
		def cloudStoreFileStream = cloudStoreService.getFileResourceStream(cloudStoreName, fileResource)
		if(cloudStoreFileStream) {
			renderBytesToScreen(fileResource, cloudStoreFileStream, isVideo)
		}
		else {
			log.warn "File resource data could not be retrieved from {}", cloudStoreName
			flash.error = message(code: 'cloudstore.datanotretrieved', args: [fileResource.fileName, cloudStoreName])
			getAllCloudStoreResources()
		}
	}

	private def renderBytesToScreen(FileResource fileResource, InputStream cloudStoreFileStream, boolean isVideo) {
		try{
			if(isVideo) {
				log.debug "Rendering video resource to screen"
				// TODO : this is not right
				response.addHeader("Content-disposition", "attachment; filename=${fileResource.fileName}")
			}
			else {
				log.debug "Rendering file resource to screen"
			}

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

	private def renderDownloadLink(FileResource fileResource, String cloudStoreName) {
		render(view: "download", model: [fileResource: fileResource, cloudStoreName: cloudStoreName])
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

		String targetUri = params.targetUri
		FileResource parentFileResource = cloudStoreService.getParentFileResourceFromPath(account, cloudStoreName, targetUri)

		renderCloudStore(account, cloudStoreName, parentFileResource.path, true)
	}

	public def downloadResource() {
		def cloudStoreName = params.storeName
		def fileResourceId = params.fileResourceId
		if(fileResourceId) {
			handleDownloadById(cloudStoreName, fileResourceId)
		}
		else {
			handleDownloadCloudStore(cloudStoreName)
		}
	}

	private void handleDownloadById(String cloudStoreName, String fileResourceId) {
		FileResource fileResource = FileResource.get(params.fileResourceId)

		if(fileResource) {
			renderFileResourceDownload(cloudStoreName, fileResource)
		}
		else {
			log.debug "File resource not found by id for download"
			flash.info = message(code: 'cloudstore.notfound')
			getAllCloudStoreResources()
		}
	}

	private void handleDownloadCloudStore(String cloudStoreName) {
		log.debug "Downloading entire cloud store root as zip"
		Account account = getCurrentAccount()
		String fileResourcePath = '/'
		FileResource rootFileResource = cloudStoreService.getFileResourceFromPath(account, cloudStoreName, fileResourcePath)

		renderFileResourceDownload(cloudStoreName, rootFileResource)
	}

	private def renderFileResourceDownload(String cloudStoreName, FileResource fileResource) {
		InputStream fileResourceStream = cloudStoreService.getFileResourceStream(cloudStoreName, fileResource)
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
			renderCloudStore(account, cloudStoreName, ROOT_DIR, true)
		}
		else {
			forward(controller: 'home', action:'index')
		}
	}

	public def uploadResource() {
		Account account = getCurrentAccount()
		String cloudStoreName = params.storeName
		String targetDirectory = params.targetDir
		FileResource parentFileResource = cloudStoreService.getParentFileResourceFromPath(account, cloudStoreName, targetDirectory)
		def fileInstanceList

		if(cloudStoreName in account.cloudStores.storeName) {
			log.debug "Uploading file to '{}' cloud store under '{}'", cloudStoreName, parentFileResource.path

			def uploadedFile = params.file

			boolean isSuccess = cloudStoreService.uploadResource(account, cloudStoreName, uploadedFile, targetDirectory, false)

			if(!isSuccess) {
				flash.error = message(code: 'cloudstore.uploadfailed', args: [uploadedFile.originalFilename, cloudStoreName])
			}
		}
		else {
			forward(controller: 'base', action: 'respondPageNotFound')
		}

		renderCloudStore(account, cloudStoreName, parentFileResource.path, true)
	}

	private void renderCloudStore(Account account, String cloudStoreName, String directory, boolean isAjaxUpdate) {
		CloudStore cloudStore = cloudStoreService.getAccountCloudStore(account, cloudStoreName)

		def totalSpaceList
		def spaceUsedList
		def fileInstanceList

		if(cloudStore) {
			totalSpaceList = cloudStoreService.getSpaceList(cloudStore.totalSpace)
			spaceUsedList = cloudStoreService.getSpaceList(cloudStore.spaceUsed)

			fileInstanceList = cloudStoreService.getSpecificCloudStoreResources(account, cloudStoreName, directory)
		}

		log.debug "Rendering '{}' resources", cloudStoreName
		if(isAjaxUpdate) {
			render(template: "layouts/${cloudStoreName}Resources", model: [cloudStore: cloudStoreName, fileInstanceList: fileInstanceList, totalSpaceList: totalSpaceList, spaceUsedList: spaceUsedList])
		}
		else {
			render(view: cloudStoreName, template: "layouts/${cloudStoreName}Resources", model: [fileInstanceList: fileInstanceList, totalSpaceList: totalSpaceList, spaceUsedList: spaceUsedList])
		}
	}

	public def newFolder() {
		Account account = getCurrentAccount()
		String cloudStoreName = params.storeName
		String targetDirectory = params.targetDir
		FileResource parentFileResource = cloudStoreService.getParentFileResourceFromPath(account, cloudStoreName, targetDirectory)
		String folderName = params.folderName

		log.debug "Creating folder '{}' under '{}' in '{}' cloud store", folderName, parentFileResource.path, cloudStoreName

		boolean isSuccess = cloudStoreService.uploadResource(account, cloudStoreName, folderName, targetDirectory, true)

		if(!isSuccess) {
			flash.error = message(code: 'cloudstore.newfolderfailed')
		}
		else {
			flash.info = message(code: 'cloudstore.newfoldersuccess', args: [folderName])
		}

		renderCloudStore(account, cloudStoreName, parentFileResource.path, true)
	}
}
