package com.intercloud

class AccountController extends BaseController {

    def login() {
		def account = Account.findWhere(email:params.email, password:params.password)
		session.user = account
		
		if(account) {
			redirect(controller: 'home', action: 'index')
		}
		else {
			render 'invalid email or password'
		}
	}
	
	def register() {		
		def account = new Account()
		account.email = params.email
		account.password = params.password
		account.fullName = params.name
		account.save(flush: true)
		
		session.user = account
		
		redirect(controller: 'home', action: 'index')
	}
	
	def logout() {
		session.user = null
		redirect(controller: 'home', action: 'index')
	}
}
