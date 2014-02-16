package com.intercloud

import spock.lang.Specification

class AdminControllerSpec extends Specification {

	AdminController controller
	
	def "test index"(){
		given: 
			setup()	
	}
	
	def setup() {
		controller = new AdminController()
	}
}
