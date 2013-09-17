package com.intercloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BaseController {
	
	private static Logger log = LoggerFactory.getLogger(BaseController.class)
	def springSecurityService
	
	static final CLOUD_STORES = ['intercloud', 'dropbox'/*, 'googledrive', 'box', 'skydrive', 'azure', 'amazonaws'*/]
	
	static final RENDER_TYPES = ['text/html', "image/jpeg", "image/png", "image/bmp", "text/x-java", "application/pdf", 
							"text/css", "image/gif", "text/plain", "image/x-icon", "application/xml", "application/json", 
							"text/json", "text/xml", "application/xhtml+xml", "text/csv", "text/x-c", "text/x-java-source"]
	
	static final VIDEO_TYPES = ["video/x-flv", "video/mp4", "application/x-mpegURL", "video/MP2T", "video/3gpp", 
								"video/quicktime", "video/x-msvideo", "video/x-ms-wmv"]
	
	public def respondUnauthorized() {
		log.debug "Unauthorized access attempt"
		flash.message = message(code: 'error.unauthorized')
		render view: 'error', status: 401
	}
	
	public def respondPageNotFound() {
		log.debug "Page not found"
		flash.message = message(code: 'error.notfound')
		render view: 'error', status: 404
	}
	
	public def respondInvalidAction() {
		log.debug "Invalid action"
		flash.message = message(code: 'error.invalid')
		render view: 'error', status: 405
	}
	
	public def respondServerError() {
		log.error "Server Error"
		flash.message = message(code: 'error.server')
		render view: 'error', status: 500
	}
	
	public Account getCurrentAccount() {
		return springSecurityService?.getCurrentUser()
	}
}
