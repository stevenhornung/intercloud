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
}
