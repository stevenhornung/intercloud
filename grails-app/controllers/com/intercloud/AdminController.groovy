package com.intercloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AdminController {

	private static Logger log = LoggerFactory.getLogger(AdminController.class)
	
    public def index() { 
		log.info "Admin access"
		render view: 'index', model: [accountList: Account.list()]
	}
}
