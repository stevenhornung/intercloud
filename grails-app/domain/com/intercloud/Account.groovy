package com.intercloud

class Account {
	
	String email
	String password
	String fullName
	String type='basic'
	String spaceUsed="0"
	String totalSpace="10737418240"
	
	static hasMany = [fileResources: FileResource, cloudStores: CloudStore]

    static constraints = {
		password blank: false
		email blank: false, email: true
		fullName blank: false
		fileResources nullable: true
		cloudStores nullable: true
    }
}
