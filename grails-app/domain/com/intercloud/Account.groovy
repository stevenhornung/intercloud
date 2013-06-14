package com.intercloud

class Account {
	
	String userName
	String password
	String email
	String fullName
	String type='basic'
	Integer spaceUsed
	Integer totalSpace
	
	static hasMany = [fileResources: FileResource, cloudStores: CloudStore]

    static constraints = {
		userName blank: false, unique: true
		password blank: false
		email blank: false, email: true
		fullName blank: false
		fileResources nullable: true
		cloudStores nullable: true
    }
}
