package com.intercloud.util

import java.io.File;
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ZipUtilities {
	
	private static Logger log = LoggerFactory.getLogger(ZipUtilities.class)
	
	public static void zipDownloadedFolder(String directory, String zipFileName) {
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
	
	public static void removeTempDownloadFolder(String path) {
		File tempDir = new File(path)
		boolean ret = tempDir.deleteDir()
		
		if(!ret) {
			log.warn "Could not delete temporary download folder: {}", path
		}
	}
	
	public static  byte[] getBytesFromZipFile(String path, String zipFileName) {
		String fullPathToZip = path + "/" + zipFileName
		File zipFile = new File(fullPathToZip)
		return zipFile.getBytes()
	}
}
