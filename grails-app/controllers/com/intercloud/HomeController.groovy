package com.intercloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HomeController extends BaseController {
	
	private static Logger log = LoggerFactory.getLogger(HomeController.class)

	public def redirectHome() {
		redirect(url: "/home")
	}
	
    public def index() {
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
			def fileResources = controller.getHomeCloudStoreResources(it)
			if(fileResources != null) {
				fileInstanceMap << ["$it" : fileResources]
			}
		}
		return fileInstanceMap
	}
	
	public def loginOrRegister() {
		def submit = params.submit
		if(submit == 'Login') {
			log.debug "Logging in"
			forward(controller: 'login', params: params)
		}
		else {
			log.debug "Registering"
			forward(controller: 'account', action: 'register', params: params)
		}
	}
}
