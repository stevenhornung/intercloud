package com.intercloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HomeController extends BaseController {
	
	private static Logger log = LoggerFactory.getLogger(HomeController.class)

	public def redirectHome() {
		redirect(url: "/home")
	}
	
    public def index() {
		Account account = getCurrentAccount()
		if(account) {
			def accountFileResources = retrieveAccountFileResources(account)
			
			String errorMessage = params.errorMessage
			if(errorMessage) {
				flash.message = errorMessage
			}
			
			render (view: 'index', model: [fileInstanceMap : accountFileResources])
		}
	}
	
	private def retrieveAccountFileResources(Account account) {
		CloudStoreController controller = new CloudStoreController()
		def accountFileResources = getFilesForEachCloudStore(controller, account)
		return accountFileResources
	}
	
	private def getFilesForEachCloudStore(CloudStoreController controller, Account account) {
		def fileInstanceMap = [:]
		account.cloudStores.each {
			def fileResources = controller.getHomeCloudStoreResources(account, it.storeName)
			if(fileResources != null) {
				fileInstanceMap << ["$it.storeName" : fileResources]
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
