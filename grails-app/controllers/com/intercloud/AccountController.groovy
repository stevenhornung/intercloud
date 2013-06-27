package com.intercloud

class AccountController extends BaseController {

	def index() {
		if(getCurrentAccount()) {
			// show account overview
		}
		else {
			// redirect to login page
		}
	}
	
	def register() {		
		def newAccount = new Account()
		newAccount.email = params.email
		newAccount.password = params.password
		newAccount.fullName = params.name
		
		if(!newAccount.save(flush: true)) {
			// show message for email already in use (only reason for non save or params object somehow modified)
		}
		
		Role userRole = Role.findByAuthority('ROLE_USER')
		AccountRole.create newAccount, userRole
		
		redirect(controller: 'home', action: 'index')
	}
}
