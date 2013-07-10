package com.intercloud

class AdminController {

    def index() { 
		render view: 'index', model: [accountList: Account.list()]
	}
}
