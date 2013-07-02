package com.intercloud

class AccountController extends BaseController {

	def index() {
		def account = getCurrentAccount()
		if(account) {
			render account
		}
		else {
			render view: 'login'
		}
	}
	 
	def login() {
		render view: 'login'
	}
	
	def showRegisterErrors() {
		redirect url:"/login#toregister"
	}
	
	def register() {		
		def newAccount = new Account()

		if(params.password != params.confirmPass) {
			flash.message = message(code: 'account.password.mismatch', args: [message(code: 'account.label', default: 'Account'), newAccount.id])
			showRegisterErrors()
			return
		}
		
		if(Account.findByEmail(params.email)) {
			flash.message = message(code: 'account.email.notunique', args: [message(code: 'account.label', default: 'Account'), newAccount.id])
			showRegisterErrors()
			return
		}
		
		newAccount.email = params.email
		newAccount.password = params.password
		newAccount.fullName = params.name
		
		if(!newAccount.save(flush: true)) {
			flash.message = message(code: 'account.notcreated', args: [message(code: 'account.label', default: 'Account'), newAccount.id])
			showRegisterErrors()
			return
		}
		
		Role userRole = Role.findByAuthority('ROLE_USER')
		AccountRole.create newAccount, userRole
		
		flash.message = message(code: 'account.created', args: [message(code: 'account.label', default: 'Account'), newAccount.id])
		redirect(controller: 'account', action: 'login')
	}
}
