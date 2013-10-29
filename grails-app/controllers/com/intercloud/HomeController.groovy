package com.intercloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HomeController extends BaseController {
	
	private static Logger log = LoggerFactory.getLogger(HomeController.class)

	public def baseUrl() {
		Account account = getCurrentAccount()
		if(account) {
			redirect(uri: '/home')
		}
		else {
			render (view: 'index')
		}
	}
	
    public def index() {
		Account account = getCurrentAccount()
		if(account) {
			def accountFileResources = retrieveAccountFileResources(account)
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
		
		// Add inter cloud first, want it at the top of the home view
		def fileResources = controller.getHomeCloudStoreResources(account, "intercloud")
		fileInstanceMap << ["intercloud" : fileResources]
		
		account.cloudStores.each {
			if(it.storeName != 'intercloud') {
				
				fileResources = controller.getHomeCloudStoreResources(account, it.storeName)
				if(fileResources != null) {
					fileInstanceMap << ["$it.storeName" : fileResources]
				}
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
