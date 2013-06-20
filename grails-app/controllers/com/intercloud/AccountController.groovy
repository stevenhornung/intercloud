package com.intercloud

class AccountController extends BaseController {

	def index() {
		if(session.user != null) {
			// show account overview
		}
		else {
			// redirect to login page
		}
	}
	
    def login() {
		def account = Account.findWhere(email:params.email, password:params.password)
		session.user = account
		
		if(account) {
			redirect(controller: 'home', action: 'index')
		}
		else {
			// show message for invalid email or password and redirect to index
			render 'invalid email or password'
		}
	}
	
	def register() {		
		def account = new Account()
		account.email = params.email
		account.password = params.password
		account.fullName = params.name
		
		if(!account.save(flush: true)) {
			// show message for email already in use (only reason for non save or params object somehow modified)
		}
		else {
			session.user = account
		}
		
		redirect(controller: 'home', action: 'index')
	}
	
	def logout() {
		session.user = null
		redirect(controller: 'home', action: 'index')
	}
}
