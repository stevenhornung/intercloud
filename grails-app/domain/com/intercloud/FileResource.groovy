package com.intercloud

class FileResource {

	String path
	String fileName
	String byteSize
	String locationOnFileSystem
	String mimeType
	boolean isDir
	String modified
	
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
    }
	
	static mapping = {
		childFileResources cascade: 'all-delete-orphan', lazy: false
	}
}
