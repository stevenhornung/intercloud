package com.intercloud

class Account {

	transient springSecurityService

	String email
	String fullName
	String password
	String type='trial'
	boolean enabled=true
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	boolean isUpdated = false

	static hasMany = [cloudStores: CloudStore]

	static constraints = {
		email blank: false, email: true, unique: true
		password blank: false
		fullName blank: false
		cloudStores nullable: true
	}

	static mapping = {
		password column: '`password`'
		cloudStores cascade: 'all-delete-orphan', lazy: false
	}

	Set<Role> getAuthorities() {
		AccountRole.findAllByAccount(this).collect { it.role } as Set
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
		password = springSecurityService.encodePassword(password)
	}
}
