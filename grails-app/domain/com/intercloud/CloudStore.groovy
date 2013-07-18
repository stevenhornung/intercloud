package com.intercloud

class CloudStore {
	
	String storeName
	Map credentials = new HashMap()
	String userId
	String spaceUsed
	String totalSpace
	
	static belongsTo = [account: Account]
	static hasMany = [fileResources: FileResource]

    static constraints = {
		fileResources nullable: true
    }
	
	static mapping = {
		fileResources cascade: 'all-delete-orphan', lazy: false
	}
}
