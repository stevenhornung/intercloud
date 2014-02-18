package com.intercloud.sync

import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.transaction.annotation.Transactional

import com.intercloud.Account
import com.intercloud.sync.SyncFileResourcesHelper

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SecurityEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

	private static Logger log = LoggerFactory.getLogger(SecurityEventListener.class)

	def grailsApplication

	@Transactional
	void onApplicationEvent(AuthenticationSuccessEvent event) {
		event.authentication.with {

			// Add user to global list of logged in users
			grailsApplication.config.loggedInUsers.add(principal.username)

			def syncFileResourcesHelper = new SyncFileResourcesHelper()

			Account account = Account.findByEmail(principal.username)

			log.debug "Syncing resources at login for '{}'", account.email
			syncFileResourcesHelper.syncSingleUserCloudStores(account)
		}
    }
}
