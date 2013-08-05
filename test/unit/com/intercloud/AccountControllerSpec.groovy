package com.intercloud

import spock.lang.Specification
import javax.servlet.http.HttpServletResponse

import grails.plugins.springsecurity.SpringSecurityService

class AccountControllerSpec extends Specification {
	
	AccountController controller
	Account currentAccount
	
	def "test index with no account"() {
		given: "no account is logged in"
			setup()
			
		when: "I call index"
			controller.index()
		
		then: "I should recieve a server error"
			response.status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR
	}
	
	def "test index with account logged in"() {
		given: "a logged in account"
			setup()
			setCurrentAccount()
		
		when: "I call index"
			controller.index()
			
		then: "I am directed to /account and the current account is correct"
			response.status == HttpServletResponse.SC_OK
			controller.controllerUri == "/account"
			controller.currentAccount == currentAccount
	}
	
	def "test showRegisterErrors"() {
		given:
			setup()
		
		when: "I call showRegisterErrors"
			controller.showRegisterErrors()
			
		then: "I am redirected to '/login#toregister'"
			response.status == HttpServletResponse.SC_FOUND
			response.redirectedUrl == "/login#toregister"
	}
	
	def "test register with non matching passwords"() {
		given: "non matching passwords"
			setup()
			setNonMatchingPasswordParams()
			
		when: "I call register"
			controller.register()
			
		then: "I am redirected to '/login#toregister' with mismatch password message"
			response.status == HttpServletResponse.SC_FOUND
			response.redirectedUrl == "/login#toregister"
			controller.flash.message == "account.password.mismatch"
	}
	
	def "test register with non unique email"() {
		given: "a non unique email"
			setup()
			setIsUniqueEmailFalse()
			setNonUniqueEmailParams()
			
		when: "I call register"
			controller.register()
			
		then: "I am redirected to '/login#toregister' with non unique email message"
			response.status == HttpServletResponse.SC_FOUND
			response.redirectedUrl == "/login#toregister"
			controller.flash.message == "account.email.notunique"
	}
	
	def setup() {
		controller = new AccountController()
	}
	
	def setCurrentAccount() {
		currentAccount = new Account(email: "steven.hornung@icloud.com", password: "password", fullName: "Steven Hornung")
		controller.metaClass.getCurrentAccount = {return currentAccount}
	}
	
	def setNonMatchingPasswordParams() {
		controller.params.password = "password"
		controller.params.confirmPass = "diffPassword"
	}
	
	def setIsUniqueEmailFalse() {
		controller.metaClass.isUniqueEmail = {return false}
	}
	
	def setNonUniqueEmailParams() {
		controller.params.email = "notunique@email.com"
		controller.params.password = "password"
		controller.params.confirmPass = "password"
		controller.params.fullName = "Steven Hornung"
	}
}
