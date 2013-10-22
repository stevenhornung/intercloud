package com.intercloud.util

import org.codehaus.groovy.grails.web.mapping.DefaultUrlMappingParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.Account
import com.intercloud.BaseController
import com.intercloud.CloudStore
import com.intercloud.FileResource;

class CloudStoreUtilities {
	
	private static Logger log = LoggerFactory.getLogger(CloudStoreUtilities.class)
	
	public static void deleteFromDatabase(FileResource fileResource) {
		// Delete parent file resource relationship if parent not already deleted
		fileResource.parentFileResource?.removeFromChildFileResources(fileResource)
		
		// Delete cloud store relationship
		fileResource.cloudStore?.removeFromFileResources(fileResource)
		
		// Delete any children relationships
		def childFileResources = []
		fileResource.childFileResources.each { childFileResources << it }
		childFileResources.each { def childFileResource ->
			deleteFromDatabase(childFileResource)
		}
		
		fileResource.delete()
	}
	
	private static void deleteChildResource(FileResource fileResource) {
		// Delete cloud store relationship
		fileResource.cloudStore?.removeFromFileResources(fileResource)
		fileResource.cloudStore?.save()
		
		// Delete any children relationships
		fileResource.childFileResources.each { def childFileResource ->
			deleteChildResource(childFileResource)
		}
	}
	
	public static def setParentAndChildFileResources(FileResource fileResource, def currentFileResources) {
		List<String> pathParts = new DefaultUrlMappingParser().parse(fileResource.path).getTokens() as List
		boolean parentFound = false

		if(pathParts.size() == 1) {
			parentFound = setIfParentIsRoot(fileResource, currentFileResources)
		}
		
		if(!parentFound) {
			parentFound = setIfParentExists(fileResource, currentFileResources, pathParts)
		}
		
		if(!parentFound) {
			FileResource parentFileResource = createParentAndSetAllProperties(fileResource, pathParts)
			currentFileResources.add(parentFileResource)
			
			currentFileResources = setParentAndChildFileResources(parentFileResource, currentFileResources)
		}
		
		currentFileResources.add(fileResource)
		return currentFileResources
	}
	
	private static boolean setIfParentIsRoot(FileResource fileResource, def currentFileResources) {
		boolean parentFound = false
		for(FileResource currentResource : currentFileResources) {
			if(currentResource.path == "/") {
				currentResource.childFileResources.add(fileResource)
				fileResource.parentFileResource = currentResource
				parentFound = true
				break
			}
		}
		
		return parentFound
	}
	
	private static boolean setIfParentExists(FileResource fileResource, def currentFileResources, def pathParts) {
		boolean parentFound = false
		
		String parentPath = getParentPath(pathParts)
		for(FileResource currentResource : currentFileResources) {
			if(currentResource.path == parentPath) {
				currentResource.childFileResources.add(fileResource)
				fileResource.parentFileResource = currentResource
				parentFound = true
				break
			}
		}
		
		return parentFound
	}
	
	private static String getParentPath(def pathParts) {
		String parentPath = pathParts.join("/")
		parentPath = "/" + parentPath
		parentPath = parentPath.substring(0, parentPath.lastIndexOf("/"))
		
		return parentPath
	}
	
	private static FileResource createParentAndSetAllProperties(FileResource fileResource, def pathParts) {
		pathParts.pop()
		
		FileResource parentFileResource = createParentDirectory(pathParts)
		parentFileResource.childFileResources.add(fileResource)
		fileResource.parentFileResource = parentFileResource
		
		return parentFileResource
	}
	
	private static FileResource createParentDirectory(def pathParts) {
		FileResource parentFileResource = new FileResource()
		
		String path = pathParts.join('/')
		path = '/' + path
		
		parentFileResource.path = path
		parentFileResource.isDir = true
		parentFileResource.fileName = pathParts.last()

		return parentFileResource
	}
}
