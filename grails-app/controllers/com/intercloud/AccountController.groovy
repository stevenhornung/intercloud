package com.intercloud

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class AccountController extends BaseController {
	
	def index() {
		def account = getCurrentAccount()
		if(account) {
			render account.email + account.fullName + account.type
		}
		else {
			respondServerError()
		}
	}
	
	def showRegisterErrors() {
		redirect url:"/login#toregister"
	}
	
	def register() {		
		def newAccount = new Account()

		if(params.password != params.confirmPass) {
			flash.message = message(code: 'account.password.mismatch')
			showRegisterErrors()
			return
		}
		
		if(Account.findByEmail(params.email)) {
			flash.message = message(code: 'account.email.notunique')
			showRegisterErrors()
			return
		}
		
		newAccount.email = params.email
		newAccount.password = params.password
		newAccount.fullName = params.name
		
		if(!newAccount.save(flush: true)) {
			flash.message = message(code: 'account.notcreated')
			showRegisterErrors()
			return
		}
		
		Role userRole = Role.findByAuthority('ROLE_USER')
		AccountRole.create newAccount, userRole
		
		flash.loginMessage = message(code: 'account.created')
		redirect(controller: 'login', action: 'auth')
	}
}
