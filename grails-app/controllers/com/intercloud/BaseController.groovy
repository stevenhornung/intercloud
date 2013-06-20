package com.intercloud

class BaseController {
	
	static final CLOUD_STORES = ['dropbox', 'googledrive', 'box', 'skydrive', 'azure', 'amazonaws']

	// Need to set up these error pages as gsp's
    def respondServerError() {
		render 'Internal Server Error'
		return
	}
	
	def respondPageNotFound() {
		render 'Page Not Found'
		return
	}
	
	def respondInvalidAction() {
		render "Invalid Action"
		return
	}
}
