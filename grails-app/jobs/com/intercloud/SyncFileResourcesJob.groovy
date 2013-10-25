package com.intercloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.sync.SyncFileResourcesHelper
import com.intercloud.Account

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
		def syncFileResourcesHelper = new SyncFileResourcesHelper()
		for(def loggedInUser : loggedInUsers) {
			String loggedInUserEmail = loggedInUser.value.authentication.principal.username
			Account account = Account.findByEmail(loggedInUserEmail)
			syncFileResourcesHelper.syncSingleUserCloudStores(account)
		}
	}
}
