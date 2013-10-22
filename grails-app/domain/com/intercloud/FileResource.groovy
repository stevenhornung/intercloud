package com.intercloud

class FileResource implements Comparable {

	String path
	String fileName
	String byteSize
	String locationOnFileSystem
	String mimeType
	boolean isDir
	String modified
	String extraMetadata
	
	SortedSet childFileResources = new TreeSet()
	static hasMany = [childFileResources: FileResource]
	static belongsTo = [cloudStore: CloudStore, parentFileResource: FileResource]

    static constraints = {
		byteSize nullable: true
		locationOnFileSystem nullable: true
		mimeType nullable: true
		modified nullable: true
		childFileResources nullable: true
		parentFileResource nullable: true
		cloudStore nullable:true
		path nullable:true
		extraMetadata nullable:true
    }
	
	static mapping = {
		childFileResources cascade: 'all-delete-orphan', lazy: false
	}
	
	int compareTo(obj) {
		fileName.toUpperCase().compareTo(obj.fileName.toUpperCase())
	}
}
