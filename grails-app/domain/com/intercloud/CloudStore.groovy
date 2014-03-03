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
		version false
	}

	int compareTo(obj) {
		storeName.compareTo(obj.storeName)
	}
}
