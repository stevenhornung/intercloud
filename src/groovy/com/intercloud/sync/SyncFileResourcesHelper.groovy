package com.intercloud.sync

import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.cloudstore.DropboxCloudStore
import com.intercloud.cloudstore.GoogledriveCloudStore

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SyncFileResourcesHelper {

	private static Logger log = LoggerFactory.getLogger(SyncFileResourcesHelper.class)

	//def jmsService

	public void syncSingleUserCloudStores(Account account) {
		def accountCloudStores = account.cloudStores
		for(CloudStore cloudStore : accountCloudStores) {
			cloudStore.lock()

			if(cloudStore.storeName == 'dropbox') {
				runDropboxUpdater(cloudStore, account)
			}
			else if(cloudStore.storeName == 'googledrive') {
				runGoogledriveUpdater(cloudStore, account)
			}
			else {
				// intercloud cloud store, no sync needed
			}

			// Send message that client resources need updating
			//jmsService.send(queue:'cloudstore.isUpdated', [acctId: account.id])
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
		cloudStore.save()
	}

}
