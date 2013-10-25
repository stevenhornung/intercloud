package com.intercloud.sync

import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event. AuthenticationSuccessEvent

import com.intercloud.Account
import com.intercloud.sync.SyncFileResourcesHelper

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SecurityEventListener implements ApplicationListener<AuthenticationSuccessEvent> {
	
	private static Logger log = LoggerFactory.getLogger(SecurityEventListener.class)

	void onApplicationEvent(AuthenticationSuccessEvent event) {
		event.authentication.with {
			/*def syncFileResourcesHelper = new SyncFileResourcesHelper()
			
			Account account = Account.findByEmail(principal.username)
			syncFileResourcesHelper.syncSingleUserCloudStores(account)*/
			
		}
    }
}
