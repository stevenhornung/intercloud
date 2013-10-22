package com.intercloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.cloudstore.DropboxCloudStore
import com.intercloud.cloudstore.GoogledriveCloudStore

class SyncFileResourcesJob {
	
	private static Logger log = LoggerFactory.getLogger(SyncFileResourcesJob.class)
	
	def grailsApplication
	
    static triggers = {
      simple name: 'syncFileResources', startDelay: 180000, repeatInterval: 180000 // execute job after 3 min, every 3 min
    }

    def execute() {
		log.debug "Starting SyncFileResources job"
        syncAllUsersCloudStores()
    }
	
	private void syncAllUsersCloudStores() {
		def loggedInUsers = grailsApplication.config.get('loggedInUsers')
		
		for(def loggedInUser : loggedInUsers) {
			String loggedInUserEmail = loggedInUser.value.authentication.principal.username
			Account account = Account.findByEmail(loggedInUserEmail)
			def accountCloudStores = account.cloudStores
			for(CloudStore cloudStore : accountCloudStores) {
				if(cloudStore.storeName == 'dropbox') {
					runDropboxUpdater(cloudStore, account)
				}
				else if(cloudStore.storeName == 'googledrive') {
					runGoogledriveUpdater(cloudStore, account)
				}
				else {
					// intercloud cloud store, no sync needed
				}
			}
		}
	}
	
	private void runDropboxUpdater(CloudStore cloudStore, Account account) {
		log.debug "Running auto dropbox updater for user '{}'", account.email
		
		def credentials = cloudStore.credentials
		String updateCursor = cloudStore.updateCursor
		def currentFileResources = cloudStore.fileResources
		
		String newUpdateCursor = new DropboxCloudStore().updateResources(cloudStore, updateCursor, currentFileResources)

		cloudStore.updateCursor = newUpdateCursor
		cloudStore.save()
	}
	
	private void runGoogledriveUpdater(CloudStore cloudStore, Account account) {
		log.debug "Running auto google drive updater for user '{}'", account.email
		
		def credentials = cloudStore.credentials
		String updateCursor = cloudStore.updateCursor
		def currentFileResources = cloudStore.fileResources
		
		String newUpdateCursor = new GoogledriveCloudStore().updateResources(cloudStore, updateCursor, currentFileResources)

		cloudStore.updateCursor = newUpdateCursor
		cloudStore.save(flush:true)
	}
}
