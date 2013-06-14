package com.intercloud

class CloudStore {
	
	String storeName
	def credentials
	String uid
	String fullName
	Integer spaceUsed
	Integer totalSpace
	
	//static belongsTo = [account: Account]
	static hasMany = [fileResources: FileResource]

    static constraints = {
		storeName blank: false, unique: true
		credentials blank: false
    }
}
