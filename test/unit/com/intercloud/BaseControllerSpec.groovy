package com.intercloud

import spock.lang.Specification
import javax.servlet.http.HttpServletResponse

class BaseControllerSpec extends Specification {

	BaseController controller
	
	def "test respondUnauthorized"() {
		given:
			setup()
			
		when: "I call respondUnauthorized"
			controller.respondUnauthorized()
			
		then: "I receive 503 status with unauthorized message"
			response.status == HttpServletResponse.SC_UNAUTHORIZED
			controller.flash.message == "error.unauthorized"
	}
	
	def "test respondPageNotFound"() {
		given:
			setup()
		
		when: "I call respondPageNotFound"
			controller.respondPageNotFound()
			
		then: "I receive 404 status with page not found message"
			response.status == HttpServletResponse.SC_NOT_FOUND
			controller.flash.message == "error.notfound"
	}
	
	def "test respondInvalidAction"() {
		given:
			setup()
	
		when: "I call respondInvalidAction"
			controller.respondInvalidAction()
		
		then: "I receive 415 status with invalid action message"
			response.status == HttpServletResponse.SC_METHOD_NOT_ALLOWED
			controller.flash.message == "error.invalid"
	}
	
	def "test respondServerError"() {
		given:
			setup()

		when: "I call respondServerError"
			controller.respondServerError()
	
		then: "I receive 500 status with internal server error message"
			response.status == HttpServletResponse.SC_INTERNAL_SERVER_ERROR
			controller.flash.message == "error.server"
	}
	
	def setup() {
		controller = new BaseController()
	}
}
