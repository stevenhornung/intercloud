package com.intercloud

class HomeController extends BaseController {

	def redirectHome() {
		redirect(url: "/home")
	}
	
    def index() {
		if(session.user != null) {
			def accountFileResources = retrieveAccountFileResources()
			[fileInstanceMap : accountFileResources]
		}
	}
	
	private def retrieveAccountFileResources() {
		CloudStoreController controller = new CloudStoreController()
		def fileInstanceMap = getFilesForEachCloudStore(controller)
		return fileInstanceMap
	}
	
	private def getFilesForEachCloudStore(def controller) {
		def fileInstanceMap = [:]
		CLOUD_STORES.each {
			def fileResources = controller.retrieveAllFilesByCloudStore(session, it)
			if(fileResources) {
				fileInstanceMap << ["$it" : fileResources]
			}
		}
		return fileInstanceMap
	}
	
	def loginOrRegister() {
		def submit = params.submit
		if(submit == 'Login') {
			forward(controller: 'account', action: 'login', params: params)
		}
		else {
			forward(controller: 'account', action: 'register', params: params)
		}
	}
}
