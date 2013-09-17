import com.intercloud.*

// Place your Spring DSL code here
beans = {
	userDetailsService(com.intercloud.accountdetails.AccountDetailsService)
	
	"com.intercloud.cloudstore.DropboxCloudStore" (com.intercloud.cloudstore.DropboxCloudStore) { bean ->
		STORE_NAME = "dropbox"
		APP_KEY = "ujdofnwh516yrg0"
		APP_SECRET = "43itigcfb9y59dy"
		REDIRECT_URL = "http://localhost:8080/auth_redirect"
		ZIP_TEMP_STORAGE_PATH = "storage/TemporaryZipStorage"
	}
	
	"com.intercloud.CloudStoreController" (CloudStoreController) { bean ->
		INTERCLOUD_STORAGE_PATH = "storage/InterCloudStorage"
	}
}
