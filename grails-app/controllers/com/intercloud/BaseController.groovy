package com.intercloud

class BaseController {

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
