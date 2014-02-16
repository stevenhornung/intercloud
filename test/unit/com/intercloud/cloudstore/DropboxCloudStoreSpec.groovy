package com.intercloud.cloudstore

import spock.lang.Specification

import com.dropbox.core.DbxSessionStore
import com.dropbox.core.DbxStandardSessionStore
import com.intercloud.Account;
import com.intercloud.CloudStore;
import com.intercloud.FileResource

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession

class DropboxCloudStoreSpec extends Specification {
	
	DropboxCloudStore dropboxCloudStore
	def request
	CloudStore cloudStoreInstance
	Account account
	
	def "test configure with false auth redirect"() {
		given:
			setup()
			
		when: "I call configure with false auth redirect"
			String authorizeUrl = dropboxCloudStore.configure(false, request)
		
		then: "I receive a dropbox authorize url for dropbox.com with redirect url back to app"
			authorizeUrl.contains("https://www.dropbox.com/1/oauth2/authorize?locale=english&client_id")
			authorizeUrl.contains("redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fauth_redirect")
	}
	
	def "test configure with true auth redirect with no request token in session"() {
		given: 
			setup()
			
		when: "I call configure with true auth redirect"
			dropboxCloudStore.configure(true, request)
			
		then: "Configuration failed and access_token was not set"
			dropboxCloudStore.access_token == null
	}
	
	def "test setCloudStoreProperties"() {
		given:
			setup()
			setAccessToken()
			
		when: "I call setCloudStoreProperties"
			callSetCloudStoreProperties()
			
		then: "the cloudstore info, file resources, and account get set"
			verifyCloudStore()
	}
	
	def "test downloadResource for an individual file that doesn't exist in dropbox"() {
		given:
			setup()
		
		when: "I call downloadResource"
			byte[] bytes = callDownloadResourceForBadSingleFile()
			
		then: "The bytes returned are empty"
			bytes == []
	}
	
	def "test downloadResource for an individual file that does exist in dropbox"() {
		given: 
			setup()
			
		when: "I call downloadResource"
			byte[] bytes = callDownloadResourceForGoodSingleFile()
			
		then: "The bytes returned aren't empty"
			bytes != []
			bytes.size() > 0
	}
	
	def "test downloadResource for a folder that doesn't exist in dropbox"() {
		given:
			setup()
			
		when: "I call downloadResource"
			byte[] bytes = callDownloadResourceForBadFolder()
			
		then: "The bytes returned are empty"
			bytes == []
	}
	
	def "test downloadResource for folder that does exist in dropbox"() {
		given:
			setup()
			
		when: "I call downloadResource"
			byte[] bytes = callDownloadResourceForGoodFolder()
			
		then: "The bytes returned arent't empty"
			bytes != []
			bytes.size() > 0
	}
	
	private def setup() {
		dropboxCloudStore = new DropboxCloudStore()
		dropboxCloudStore.STORE_NAME = "dropbox"
		dropboxCloudStore.APP_KEY = "ujdofnwh516yrg0"
		dropboxCloudStore.APP_SECRET = "43itigcfb9y59dy"
		dropboxCloudStore.REDIRECT_URL = "http://localhost:8080/auth_redirect"
		
		request =  new MockHttpServletRequest()
	}
	
	private def setAccessToken() {
		dropboxCloudStore.access_token = "pOKLYhrxkWIAAAAAAAAAAWy6F6GzoV0V4XScPYofC9ZrSnMqZzy1tY7Bl5CD82AV"
	}
	
	private def callSetCloudStoreProperties() {
		cloudStoreInstance = new CloudStore()
		account = new Account(email: "steven.hornung@icloud.com", password: "password", fullName: "Steven Hornung")
		
		dropboxCloudStore.setCloudStoreProperties(cloudStoreInstance, account)
	}
	
	private void verifyCloudStore() {
		verifyCloudStoreInfo()
		verifyCloudStoreFileResources()
		verifyCloudStoreAccount()
	}
	
	private void verifyCloudStoreInfo() {
		assert cloudStoreInstance.storeName == "dropbox"
		assert cloudStoreInstance.credentials == ['ACCESS_TOKEN': dropboxCloudStore.access_token]
	}
	
	private void verifyCloudStoreFileResources() {
		assert cloudStoreInstance.fileResources != null
		assert cloudStoreInstance.fileResources.size() > 0
	}
	
	private void verifyCloudStoreAccount() {
		assert cloudStoreInstance.account == account
		assert cloudStoreInstance.account.email == account.email
	}
	
	private byte[] callDownloadResourceForBadSingleFile() {
		def credentials = ['ACCESS_TOKEN': "pOKLYhrxkWIAAAAAAAAAAWy6F6GzoV0V4XScPYofC9ZrSnMqZzy1tY7Bl5CD82AV"]
		FileResource fileResource = new FileResource(path: "/bad/path", fileName: "nonexistant", isDir: false)
		
		return dropboxCloudStore.downloadResource(credentials, fileResource)
	}
	
	private byte[] callDownloadResourceForGoodSingleFile() {
		def credentials = ['ACCESS_TOKEN': "pOKLYhrxkWIAAAAAAAAAAWy6F6GzoV0V4XScPYofC9ZrSnMqZzy1tY7Bl5CD82AV"]
		FileResource fileResource = new FileResource(path: "/test.txt", fileName: 'test', isDir: false)
		
		return dropboxCloudStore.downloadResource(credentials, fileResource)
	}
	
	private byte[] callDownloadResourceForBadFolder() {
		def credentials = ['ACCESS_TOKEN': "pOKLYhrxkWIAAAAAAAAAAWy6F6GzoV0V4XScPYofC9ZrSnMqZzy1tY7Bl5CD82AV"]
		FileResource fileResource = new FileResource(path: "/nonexistantFolder", fileName: 'badfolder', isDir: true)
		
		return dropboxCloudStore.downloadResource(credentials, fileResource)
	}
	
	private byte[] callDownloadResourceForGoodFolder() {
		def credentials = ['ACCESS_TOKEN': "pOKLYhrxkWIAAAAAAAAAAWy6F6GzoV0V4XScPYofC9ZrSnMqZzy1tY7Bl5CD82AV"]
		FileResource fileResource = new FileResource(path: "/testFolder", fileName: 'testFolder', isDir: true)
		
		return dropboxCloudStore.downloadResource(credentials, fileResource)
	}
}
