package com.intercloud

class FileResource {

	String path
	String byteSize
	byte[] bytes
	String mimeType
	boolean isDir=false
	String modified
	
	static hasMany = [fileResources: FileResource]

    static constraints = {
		bytes nullable: true
		mimeType nullable: true
		modified nullable: true
		fileResources nullable: true
    }
}
