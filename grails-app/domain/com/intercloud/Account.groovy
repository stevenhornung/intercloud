package com.intercloud

class Account {

	transient springSecurityService

	String email
	String fullName
	String password
	String type='basic'
	String spaceUsed="0"
	String totalSpace="10737418240" // 10gb
	boolean enabled=true
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	
	static hasMany = [fileResources: FileResource, cloudStores: CloudStore]
	
	static constraints = {
		email blank: false, email: true, unique: true
		password blank: false
		fullName blank: false
		fileResources nullable: true
		cloudStores nullable: true
	}

	static mapping = {
		password column: '`password`'
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
