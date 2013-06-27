package com.intercloud

class AdminController {

    def index() { 
		render Account.list().email
	}
}
