package com.intercloud

import spock.lang.Specification
import com.intercloud.cloudstore.DropboxCloudStore
import javax.servlet.http.HttpServletResponse

class CloudStoreControllerSpec extends Specification {
	
	CloudStoreController controller
	Account currentAccount
	def cloudStoreLink
	
	def "test index with no logged in account"() {
		given: "no logged in account"
			setup()
			setNoAccount()
			
		when: "I call index"
			controller.index()
			
		then: "I am redirected to the home page"
			response.status == HttpServletResponse.SC_FOUND
			response.redirectedUrl == "/home/index"
	}
	
	def "test index with logged in account and no cloudstore in params"() {
		given: "logged in account with no cloudstore in params"
			setup()
			setLoggedInAccount()
		
		when: "I call index"
			controller.index()
			
		then: "I am redirected to the home page"
			response.status == HttpServletResponse.SC_FOUND
			response.redirectedUrl == "/home/index"
	}
	
	def "test index with logged in account and bad cloudstore"() {
		given: "logged in account with bad cloudstore in params"
			setup()
			setLoggedInAccount()
			setBadCloudStoreParams()
		
		when: "I call index"
			controller.index()
			
		then: "I am redirected to the home page"
			response.status == HttpServletResponse.SC_FOUND
			response.redirectedUrl == "/home/index"
	}
	
	def setup() {
		controller = new CloudStoreController()
	}
	
	def setNoAccount() {
		controller.metaClass.getCurrentAccount = {return null}
	}
	
	def setLoggedInAccount() {
		currentAccount = new Account(email: "steven.hornung@icloud.com", password: "password", fullName: "Steven Hornung")
		controller.metaClass.getCurrentAccount = {return currentAccount}
	}
	
	def setBadCloudStoreParams() {
		controller.params.cloudStore = "badcloudstore"
	}
	
	def setGoodCloudStoreParams() {
		controller.params.cloudStore = "dropbox"
	}
}
