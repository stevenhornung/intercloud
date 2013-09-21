import com.intercloud.cloudstore.DropboxCloudStore
import com.intercloud.cloudstore.GoogledriveCloudStore

// Place your Spring DSL code here
beans = {
	userDetailsService(com.intercloud.accountdetails.AccountDetailsService)
	
	"com.intercloud.cloudstore.DropboxCloudStore" (DropboxCloudStore) { bean ->
		STORE_NAME = 'dropbox'
		APP_KEY = "ujdofnwh516yrg0"
		APP_SECRET = "43itigcfb9y59dy"
		REDIRECT_URL = "http://localhost:8080/auth_redirect"
		ZIP_TEMP_STORAGE_PATH = "storage/TemporaryZipStorage"
	}
	
	/*"com.intercloud.cloudstore.GoogledriveCloudStore" (GoogledriveCloudStore) { bean ->
		STORE_NAME = 'googledrive'
		CLIENT_ID = "887098665005.apps.googleusercontent.com"
		CLIENT_SECRET = "OZQKsV0dGM04h-FtNt-VpGIF"
		REDIRECT_URL = "http://localhost:8080/auth_redirect"
		ZIP_TEMP_STORAGE_PATH = "storage/TemporaryZipStorage"
	}*/
}
