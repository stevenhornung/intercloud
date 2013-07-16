package com.intercloud

class FileResource {

	String path
	String fileName
	String byteSize
	byte[] bytes
	String mimeType
	boolean isDir=false
	String modified
	
	static hasMany = [fileResources: FileResource]

    static constraints = {
		byteSize nullable: true
		bytes nullable: true
		mimeType nullable: true
		modified nullable: true
		fileResources nullable: true
    }
	
	static mapping = {
		fileResources cascade: 'all-delete-orphan', lazy: false
	}
}
