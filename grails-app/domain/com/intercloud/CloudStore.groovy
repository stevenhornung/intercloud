package com.intercloud

class CloudStore {
	
	String storeName
	def credentials
	Integer spaceUsed
	Integer totalSpace
	
	static belongsTo = [account: Account]
	static hasMany = [fileResources: FileResource]

    static constraints = {
		fileResources: nullable: true
    }
}
