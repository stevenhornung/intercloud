import com.intercloud.cloudstore.DropboxCloudStore
import com.intercloud.cloudstore.GoogledriveCloudStore
import com.intercloud.cloudstore.AwsS3CloudStore
import com.intercloud.util.ZipUtilities
import com.intercloud.sync.SecurityEventListener

// Place your Spring DSL code here
beans = {
	securityEventListener(SecurityEventListener)
	
	userDetailsService(com.intercloud.accountdetails.AccountDetailsService)
	
	zipUtilities (ZipUtilities) {
		ZIP_TEMP_STORAGE_PATH = "storage/TemporaryZipStorage"
	}
	
	dropboxCloudStore (DropboxCloudStore) {
		STORE_NAME = 'dropbox'
		APP_KEY = "ujdofnwh516yrg0"
		APP_SECRET = "43itigcfb9y59dy"
		REDIRECT_URL = "http://localhost:8080/auth_redirect"
	}
	
	googledriveCloudStore (GoogledriveCloudStore) {
		STORE_NAME = 'googledrive'
		CLIENT_ID = "887098665005.apps.googleusercontent.com"
		CLIENT_SECRET = "OZQKsV0dGM04h-FtNt-VpGIF"
		REDIRECT_URL = "http://localhost:8080/auth_redirect"
	}
	
	awsS3CloudStore (AwsS3CloudStore) {
		STORE_NAME = 'awss3'
		AWS_CREDENTIAL_URL = "http://localhost:8080/awss3credentials"
		REDIRECT_URL = "http://localhost:8080/auth_redirect"
	}
}
