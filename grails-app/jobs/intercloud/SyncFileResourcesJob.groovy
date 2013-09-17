package intercloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.Account
import com.intercloud.CloudStore
import com.intercloud.cloudstore.DropboxCloudStore

class SyncFileResourcesJob {
	
	private static Logger log = LoggerFactory.getLogger(SyncFileResourcesJob.class)
	
    static triggers = {
      simple name: 'syncFileResources', startDelay: 300000, repeatInterval: 300000 // execute job after 5 min, every 5 min
    }

    def execute() {
		log.debug "Starting SyncFileResources job"
        syncAllUsersCloudStores()
    }
	
	private void syncAllUsersCloudStores() {
		List<Account> accounts = Account.list()
		
		for(Account account : accounts) {
			def accountCloudStores = account.cloudStores
			for(CloudStore cloudStore : accountCloudStores) {
				if(cloudStore.storeName == 'dropbox') {
					runDropboxUpdater(cloudStore)
				}
				else if(cloudStore.storeName == 'googledrive') {
					runGoogledriveUpdater(cloudStore)
				}
				else {
					// non supported store name yet
				}
			}
		}
	}
	
	private void runDropboxUpdater(CloudStore cloudStore) {
		log.debug "Running auto dropbox updater for user '{}'", cloudStore.account.email
		
		def credentials = cloudStore.credentials
		String updateCursor = cloudStore.updateCursor
		def currentFileResources = cloudStore.fileResources
		
		String newUpdateCursor = new DropboxCloudStore().updateResources(credentials, updateCursor, currentFileResources)
		cloudStore.updateCursor = newUpdateCursor
		cloudStore.save()
	}
	
	private void runGoogledriveUpdater(CloudStore cloudStore) {
		def credentials = cloudStore.credentials
	}
}
