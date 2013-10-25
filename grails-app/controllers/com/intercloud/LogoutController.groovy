package com.intercloud

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LogoutController extends BaseController {

	private static Logger log = LoggerFactory.getLogger(LogoutController.class)
	
	def index = {
		Account currentAccount = getCurrentAccount()
		log.debug "Logging out user '{}'", currentAccount.email
		
		def loggedInUsers = grailsApplication.config.get('loggedInUsers')
		def userToLogout
		
		loggedInUsers.each {
			if(currentAccount.email == it) {
				userToLogout = it
			}
		}
		loggedInUsers.remove(userToLogout)
		
		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
	}
}
