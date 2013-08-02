package com.intercloud

class BaseController {
	
	def springSecurityService
	
	static final CLOUD_STORES = ['intercloud', 'dropbox', 'googledrive', 'box', 'skydrive', 'azure', 'amazonaws']
	
	static final RENDER_TYPES = ['text/html', "image/jpeg", "image/png", "image/bmp", "text/x-java", "application/pdf", "text/css", 
							"image/gif", "text/plain", "image/x-icon", "application/xml", "application/json", "text/json",
							"text/xml", "application/octet-stream", "application/xhtml+xml", "text/csv"]
	
	static final VIDEO_TYPES = ["video/x-flv", "video/mp4", "application/x-mpegURL", "video/MP2T", "video/3gpp", 
								"video/quicktime", "video/x-msvideo", "video/x-ms-wmv"]
	
	def respondUnauthorized() {
		flash.message = message(code: 'error.unauthorized')
		render view: 'error', status: 401
	}
	
	def respondPageNotFound() {
		flash.message = message(code: 'error.notfound')
		render view: 'error', status: 404
	}
	
	def respondInvalidAction() {
		flash.message = message(code: 'error.invalid')
		render view: 'error', status: 405
	}
	
	def respondServerError() {
		flash.message = message(code: 'error.server')
		render view: 'error', status: 500
	}
	
	def getCurrentAccount() {
		return springSecurityService.currentUser
	}
}
