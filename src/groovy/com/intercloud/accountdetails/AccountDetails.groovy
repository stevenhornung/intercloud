package com.intercloud.accountdetails

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class AccountDetails extends GrailsUser {
	
	final String fullName
	
    AccountDetails(String email, String password, boolean enabled,
				 boolean accountNonExpired, boolean credentialsNonExpired,
				 boolean accountNonLocked,
				 Collection<GrantedAuthority> authorities,
				 long id, String fullName) {
		super(email, password, enabled, accountNonExpired,
			credentialsNonExpired, accountNonLocked, authorities, id)

		this.fullName = fullName
    }

}
