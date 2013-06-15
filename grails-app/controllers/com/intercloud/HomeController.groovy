package com.intercloud

class HomeController extends BaseController {

	def home() {
		redirect(url: "/home")
	}
	
    def index() {
		def dropboxCloudStore = CloudStore.findByStoreName("dropbox")
		if(dropboxCloudStore) {
			[fileInstanceList: dropboxCloudStore.fileResources]
		}
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
