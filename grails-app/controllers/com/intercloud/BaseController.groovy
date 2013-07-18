package com.intercloud

class BaseController {
	
	def springSecurityService
	
	static final CLOUD_STORES = ['intercloud', 'dropbox', 'googledrive', 'box', 'skydrive', 'azure', 'amazonaws']
	
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
