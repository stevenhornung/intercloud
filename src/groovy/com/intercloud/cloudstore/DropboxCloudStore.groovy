package com.intercloud.cloudstore

import com.dropbox.client2.DropboxAPI
import com.dropbox.client2.DropboxAPI.Entry
import com.dropbox.client2.session.AccessTokenPair
import com.dropbox.client2.session.AppKeyPair
import com.dropbox.client2.session.RequestTokenPair
import com.dropbox.client2.session.WebAuthSession
import com.dropbox.client2.session.Session.AccessType
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo
import com.intercloud.*

class DropboxCloudStore implements CloudStoreInterface {
	
	final static STORE_NAME = "Dropbox"
	final static ACCOUNT_KEY_STRING = "account_key"
	final static ACCOUNT_SECRET_STRING = "account_secret"
	
	static String APP_KEY = "ujdofnwh516yrg0"
	static String APP_SECRET = "43itigcfb9y59dy"
	static AccessType ACCESS_TYPE = AccessType.DROPBOX
	static DropboxAPI<WebAuthSession> dropboxApi
	static WebAuthSession session
	static WebAuthInfo authInfo
	
	static String account_key
	static String account_secret

	def getClientAccessRequestUrl() {
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET)
		session = new WebAuthSession(appKeys, ACCESS_TYPE)
		authInfo = session.getAuthInfo()
		
		String redirectUrlParam = "&oauth_callback=http://localhost:8080/InterCloud/auth_redirect"
		String url = authInfo.url+redirectUrlParam
		
		return url
	}
	
	def setClientAccessCredentials() {
		RequestTokenPair pair = authInfo.requestTokenPair
		session.retrieveWebAccessToken(pair)
		setAccountTokens()
		
		dropboxApi = new DropboxAPI<WebAuthSession>(session)
	}
	
	private def setAccountTokens() {
		AccessTokenPair tokens = session.getAccessTokenPair()
		account_key = tokens.key
		account_secret = tokens.secret
	}
	
	def populateCloudStoreInstance(def cloudStoreInstance) {
		def accountInfo = retrieveAccountInfo()
		
		cloudStoreInstance.storeName = STORE_NAME
		cloudStoreInstance.credentials = [ACCOUNT_KEY_STRING : account_key, ACCOUNT_SECRET_STRING : account_secret]
		cloudStoreInstance.uid = accountInfo.uid
		cloudStoreInstance.fullName = accountInfo.displayName
		cloudStoreInstance.spaceUsed = accountInfo.quotaNormal
		cloudStoreInstance.totalSpace = accountInfo.quota
	}

	private def retrieveAccountInfo() {
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE, new AccessTokenPair(account_key, account_secret));	
		def dbAccount = dropboxApi.accountInfo()
		
		return dbAccount
	}

	def retrieveAllResourcesInfo() {
		
		//Testing shenans
		Entry entries = dropboxApi.metadata("/", 100, null, true, null);
		
		for (Entry e : entries.contents) {
			if (!e.isDeleted) {
				if(e.isDir) {
					Entry entries2 = dropboxApi.metadata(e.path, 100, null, true, null);
					
					for (Entry e2 : entries2.contents) {
						if(e.isDir) {
							Entry entries3 = dropboxApi.metadata(e2.path, 100, null, true, null);
					
							for (Entry e3 : entries3.contents) {
								System.out.println("Item Name: "+e3.path);
							}
						}
						else {
							System.out.println("Item Name: "+e2.path);
						}
					}
				}
				else {
					System.out.println("Item Name: "+e.path);
				}
				
			}
		}
		
	}

	def retrieveSingleResourceInfo(String resource_id) {
		// TODO Auto-generated method stub
		return null;
	}

	def uploadResources(List<FileResource> fileResources) {
		// TODO Auto-generated method stub
		
	}

	def updateResources(List<FileResource> fileResources) {
		// TODO Auto-generated method stub
		
	}

	def downloadResources(List<FileResource> fileResources) {
		// TODO Auto-generated method stub
		return null;
	}

}
