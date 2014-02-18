package com.intercloud

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HomeController extends BaseController {

	private static Logger log = LoggerFactory.getLogger(HomeController.class)

	public def baseUrl() {
		Account account = getCurrentAccount()

		if(account) {
			redirect(uri: '/home')
		}
		else {
			render (view: 'index')
		}
	}

    public def index() {
		Account account = getCurrentAccount()

		if(account) {
			forward(controller: "cloudStore", action: "renderHomeResources")
		}
		else {
			log.warn "Passed spring security as logged in user but getCurrentAccount returned null"
			render (view: 'index')
		}
	}

	public def loginOrRegister() {
		def submit = params.submit
		if(submit == 'Login') {
			log.debug "Logging in"
			forward(controller: 'login', params: params)
		}
		else {
			log.debug "Registering"
			forward(controller: 'account', action: 'register', params: params)
		}
	}
}
