package com.intercloud

class HomeController {

    def index() { 
		render "<html>select cloud service to add: <br/> <a href='/InterCloud/cs?cloudStore=Dropbox'>Dropbox</a></html>"
	}
}
