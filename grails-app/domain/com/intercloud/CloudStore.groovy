package com.intercloud

class CloudStore {
	
	String storeName
	Map credentials = new HashMap()
	Integer spaceUsed
	Integer totalSpace
	
	static belongsTo = [account: Account]
	static hasMany = [fileResources: FileResource]

    static constraints = {
		fileResources: nullable: true
    }
	
	static mapping = {
		fileResources lazy: false
	}
}
