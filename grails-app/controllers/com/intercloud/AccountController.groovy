package com.intercloud

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class AccountController extends BaseController {
	
	def index() {
		def account = getCurrentAccount()
		if(account) {
			render view: 'index', model: [accountInstance: account]
		}
		else {
			respondServerError()
		}
	}
	
	def showRegisterErrors() {
		redirect url:"/login#toregister"
	}
	
	def register() {
		if(!isPasswordMatch()) {
			flash.message = message(code: 'account.password.mismatch')
			showRegisterErrors()
			return false
		}		
		
		if(!isUniqueEmail()) {
			flash.message = message(code: 'account.email.notunique')
			showRegisterErrors()
			return
		}
		
		Account newAccount = createNewAccount()
		if(!newAccount.save(flush: true)) {
			flash.message = message(code: 'account.notcreated')
			showRegisterErrors()
			return
		}
		
		addAccountToUserRole(newAccount)
		createIntercloudCloudStore(newAccount)
		createRootIntercloudFileResource(newAccount)

		flash.loginMessage = message(code: 'account.created')
		redirect(controller: 'login', action: 'auth')
	}
	
	private def isPasswordMatch() {
		if(params.password != params.confirmPass) {
			return false
		}
		return true
	}
	
	private def isUniqueEmail() {
		if(Account.findByEmail(params.email)) {
			return false
		}
		return true
	}
	
	private def createNewAccount() {
		Account newAccount = new Account()
		
		newAccount.email = params.email
		newAccount.password = params.password
		newAccount.fullName = params.name
		
		return newAccount
	}
	
	private def addAccountToUserRole(Account newAccount) {
		Role userRole = Role.findByAuthority('ROLE_USER')
		AccountRole.create newAccount, userRole
	}
	
	private def createIntercloudCloudStore(Account newAccount) {
		CloudStore cloudStoreInstance = new CloudStore()
		
		cloudStoreInstance.account = newAccount
		cloudStoreInstance.storeName = 'intercloud'
		cloudStoreInstance.userId = newAccount.email
		cloudStoreInstance.spaceUsed = newAccount.spaceUsed
		cloudStoreInstance.totalSpace = newAccount.totalSpace
		
		cloudStoreInstance.save(flush: true)
	}
	
	private def createRootIntercloudFileResource(Account newAccount) {
		FileResource rootIntercloudFileResource = new FileResource()
		rootIntercloudFileResource.path = '/'
		rootIntercloudFileResource.isDir = true
		rootIntercloudFileResource.fileName = ''
		rootIntercloudFileResource.save()
		
		def storeName = 'intercloud'
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, newAccount)
		def fileResources = []
		fileResources.add(rootIntercloudFileResource)
		cloudStore.fileResources = fileResources
		cloudStore.save()
	}
}
