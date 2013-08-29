package com.intercloud

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LogoutController {

	private static Logger log = LoggerFactory.getLogger(LogoutController.class)
	
	def index = {
		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
	}
}
