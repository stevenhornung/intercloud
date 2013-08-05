package com.intercloud

import spock.lang.Specification
import javax.servlet.http.HttpServletResponse

class HomeControllerSpec extends Specification {
	
	HomeController controller
	
	def "test redirectHome"() {
		given:
			setup()
			
		when: "I call redirectHome"
			controller.redirectHome()
			
		then: "I am redirected to /home"
			response.status == HttpServletResponse.SC_FOUND
			response.redirectedUrl == "/home"
	}
	
	def "test loginOrRegister with login"() {
		given:
			setup()
			setSubmitParams("Login")
			
		when: "I call loginOrRegister"
			controller.loginOrRegister()
			
		then: "I am redirected to the login controller"
			controller.controllerUri == "/login"	
	}
	
	def "test loginOrRegister with register"() {
		given:
			setup()
			setSubmitParams("Register")
			
		when: "I call loginOrRegister"
			controller.loginOrRegister()
			
		then: "I am redirected to the account controller"
			controller.controllerUri == "/account"
	}
	
	private void setup() {
		controller = new HomeController()
	}
	
	private void setSubmitParams(String submit) {
		controller.params.submit = submit
	}
}
