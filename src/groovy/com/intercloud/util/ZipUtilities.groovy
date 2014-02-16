package com.intercloud.util

import com.google.api.services.drive.Drive.Files

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.intercloud.FileResource

class ZipUtilities {
	
	private static Logger log = LoggerFactory.getLogger(ZipUtilities.class)
	
	static String ZIP_TEMP_STORAGE_PATH
	
	public static String getDownloadedFolderPath(FileResource fileResource) {
		String uniqueFolderId = UUID.randomUUID().toString()
		String fullPath = ZIP_TEMP_STORAGE_PATH + "/" + uniqueFolderId + "/downloadedFiles"
		new File(fullPath).mkdirs()
		return fullPath
	}
	
	public static String getSourceZipName(String storeName, FileResource fileResource) {
		String sourceZip
		if(!fileResource.fileName) {
			if(storeName == 'intercloud') {
				sourceZip = 'InterCloudRoot.zip'
			}
			else if(storeName == 'dropbox') {
				sourceZip = "DropboxRoot.zip"
			}
			else {
				// other cloud stores
			}
		}
		else {
			sourceZip = fileResource.fileName + ".zip"
		}
		
		return sourceZip
	}
	
	public static void zipFolder(String directory, String zipFileName) {
		try {
			String zipPath = directory.substring(0, directory.lastIndexOf('/'))
			String fullZipPath = zipPath + "/" + zipFileName
			FileOutputStream fos = new FileOutputStream(fullZipPath);
			ZipOutputStream zos = new ZipOutputStream(fos)
			File srcFile = new File(directory)
			
			packZip(zos, srcFile)
			
			zos.close()
		}
		catch(IOException) {
			log.error "Error creating zip file: {}", IOException
		}
	}
	
	public static void packZip(ZipOutputStream outputStream, File srcFile) {
		File[] files = srcFile.listFiles()
		for (File file : files) {
			if (file.isDirectory()) {
				zipDir(outputStream, file, "")
			}
			else {
				zipFile(outputStream, file, "")
			}
		}
	}
	
	private static void zipDir(ZipOutputStream outputStream, File dir, String path) {
		File[] files = dir.listFiles()
		path = buildPath(path, dir.name)
		
		for(File file: files) {
			if(file.isDirectory()) {
				zipDir(outputStream, file, path)
			}
			else {
				zipFile(outputStream, file, path)
			}
		}
	}
	
	private static void zipFile(ZipOutputStream outputStream, File file, String path) {
		byte[] buffer = new byte[1024]
		FileInputStream fis = new FileInputStream(file)
		
		outputStream.putNextEntry(new ZipEntry(buildPath(path, file.name)))
						
		int length
		while ((length = fis.read(buffer)) > 0) {
			outputStream.write(buffer, 0, length)
		}
		outputStream.closeEntry();
		fis.close();
	}
	
	private static String buildPath(String path, String fileName) {
		if(path == null || path.isEmpty()) {
			return fileName
		}
		else {
			return path + "/" + fileName
		}
	}
	
	public static void removeTempFromFileSystem(String path) {
		boolean ret = false
		File fileToDelete = new File(path)
		
		if(fileToDelete.isDirectory()) {
			ret = fileToDelete.deleteDir()
		}
		else {
			ret = fileToDelete.delete()
		}
		
		if(!ret) {
			log.warn "Could not delete temporary download folder: {}", path
		}
	}
	
	public static InputStream getInputStreamFromZipFile(String path, String zipFileName) {
		String fullPathToZip = path + "/" + zipFileName
		InputStream inputStream = new FileInputStream(fullPathToZip)
		return inputStream
	}
	
	public static void removeZippedFolder(String locationOnFileSystem, String zipFileName) {
		log.debug "Deleting zipped file from file system '{}'", zipFileName
		String path = locationOnFileSystem + '/' + zipFileName
		File zippedFolder = new File(path)
		boolean ret = zippedFolder.delete()
		if(!ret) {
			log.warn "Could not delete downloaded zip file from file system: {}", path
		}
	}
}
