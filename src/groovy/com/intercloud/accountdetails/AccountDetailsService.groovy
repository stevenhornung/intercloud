package com.intercloud.accountdetails

import com.intercloud.*

import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserDetailsService
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

class AccountDetailsService implements GrailsUserDetailsService {

   static final List NO_ROLES = [new GrantedAuthorityImpl(SpringSecurityUtils.NO_ROLE)]

   UserDetails loadUserByUsername(String username, boolean loadRoles)
            throws UsernameNotFoundException {
      return loadUserByUsername(username)
   }

   UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

      Account.withTransaction { status ->

         Account account = Account.findByEmail(email)
         if (!email) throw new UsernameNotFoundException(
                      'Email not found', email)

         def authorities = account.authorities.collect {
             new GrantedAuthorityImpl(it.authority)
         }

         return new AccountDetails(account.email, account.password, account.enabled,
            !account.accountExpired, !account.passwordExpired,
            !account.accountLocked, authorities ?: NO_ROLES, account.id,
            account.fullName)
      }
   }
}
