package com.intercloud

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AccountController extends BaseController {
	
	private static Logger log = LoggerFactory.getLogger(AccountController.class)
	
	public def index() {
		def account = getCurrentAccount()
		if(account) {
			log.debug "Viewing account: {}", account.email
			render view: 'index', model: [accountInstance: account]
		}
		else {
			log.debug "No account logged in to view"
			respondServerError()
		}
	}
	
	public def showRegisterErrors() {
		redirect url:"/login#toregister"
	}
	
	public def register() {
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

		log.debug "Account: {} created", newAccount.email
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
		
		cloudStoreInstance.save()
		
		newAccount.cloudStores = [cloudStoreInstance]
	}
	
	private def createRootIntercloudFileResource(Account newAccount) {
		FileResource rootIntercloudFileResource = new FileResource()
		
		def storeName = 'intercloud'
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, newAccount)
		
		rootIntercloudFileResource.cloudStore = cloudStore
		rootIntercloudFileResource.path = '/'
		
		String locationOnFileSystem = "storage/InterCloudStorage/" + newAccount.email + '/InterCloudRoot'
		new File(locationOnFileSystem).mkdirs()
		rootIntercloudFileResource.locationOnFileSystem = locationOnFileSystem
		
		rootIntercloudFileResource.isDir = true
		rootIntercloudFileResource.fileName = 'InterCloudRoot'
		rootIntercloudFileResource.save(flush:true)
		
		def fileResources = []
		fileResources.add(rootIntercloudFileResource)
		cloudStore.fileResources = fileResources
	}
}
