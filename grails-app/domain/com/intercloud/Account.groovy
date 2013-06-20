package com.intercloud

class Account {
	
	String email
	String password
	String fullName
	String type='basic'
	String spaceUsed="0"
	String totalSpace="10737418240" // 10gb
	
	static hasMany = [fileResources: FileResource, cloudStores: CloudStore]

    static constraints = {
		email blank: false, email: true, unique: true
		password blank: false
		fullName blank: false
		fileResources nullable: true
		cloudStores nullable: true
    }
}
