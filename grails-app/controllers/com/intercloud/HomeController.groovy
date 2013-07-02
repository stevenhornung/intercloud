package com.intercloud

class HomeController extends BaseController {

	def redirectHome() {
		redirect(url: "/home")
	}
	
    def index() {
		if(getCurrentAccount()) {
			def accountFileResources = retrieveAccountFileResources()
			[fileInstanceMap : accountFileResources]
		}
	}
	
	private def retrieveAccountFileResources() {
		CloudStoreController controller = new CloudStoreController()
		def accountFileResources = getFilesForEachCloudStore(controller)
		return accountFileResources
	}
	
	private def getFilesForEachCloudStore(CloudStoreController controller) {
		def fileInstanceMap = [:]
		CLOUD_STORES.each {
			def fileResources = controller.retrieveAllFilesByCloudStore(it)
			if(fileResources) {
				fileInstanceMap << ["$it" : fileResources]
			}
		}
		return fileInstanceMap
	}
	
	def loginOrRegister() {
		def submit = params.submit
		if(submit == 'Login') {
			forward(controller: 'login', params: params)
		}
		else {
			forward(controller: 'account', action: 'register', params: params)
		}
	}
}
