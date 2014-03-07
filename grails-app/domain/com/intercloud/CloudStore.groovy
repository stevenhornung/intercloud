package com.intercloud

class CloudStore implements Comparable {

	String storeName
	Map credentials = new HashMap()
	String userId
	BigDecimal spaceUsed = 0
	BigDecimal totalSpace = 5368709120 // 5gb
	String updateCursor

	static belongsTo = [account: Account]

	Set fileResources = []
	static hasMany = [fileResources: FileResource]

    static constraints = {
		fileResources nullable: true
		updateCursor nullable: true
		userId nullable: true
    }

	static mapping = {
		fileResources cascade: 'all-delete-orphan', lazy: false

		// This is so we can do pessimistic locking.  However, as is, it only works when saves
		// happen only on cloudStores but need GoogledriveCloudStore to save fileResources when created
		// in order to get their id to set parents :( will need to figure out a way around this
		// path searching seems expensive but may be only option
		//version false
	}

	int compareTo(obj) {
		storeName.compareTo(obj.storeName)
	}
}
