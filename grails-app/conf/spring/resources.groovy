import com.intercloud.CloudStoreController
import com.intercloud.AccountController
import com.intercloud.CloudStoreService
import com.intercloud.cloudstore.DropboxCloudStore
import com.intercloud.cloudstore.GoogledriveCloudStore
import com.intercloud.cloudstore.AwsS3CloudStore
import com.intercloud.util.ZipUtilities
import com.intercloud.sync.SecurityEventListener
import com.intercloud.accountdetails.AccountDetailsService

beans = {

	/*
	 *	Design to Prod:
	 *		-resources.groovy
	 *		-BootStrap.groovy
	 */


	securityEventListener(SecurityEventListener) {
		grailsApplication = ref('grailsApplication')
	}

	userDetailsService(AccountDetailsService)

	cloudStoreController(CloudStoreController) {
		ROOT_DIR = "/"
	}

	accountController(AccountController) {
		BASE_LOCATION = "storage/InterCloudStorage/"
		//BASE_LOCATION = "/home/stevenhornung/Development/intercloud/storage/InterCloudStorage/"
	}

	cloudStoreService(CloudStoreService) {
		ROOT_DIR = "/"

		INTERCLOUD = "intercloud"
		DROPBOX = "dropbox"
		GOOGLEDRIVE = "googledrive"
		AWSS3 = "awss3"

		INTERCLOUD_STORAGE_PATH = "storage/InterCloudStorage"
		//INTERCLOUD_STORAGE_PATH = "/home/stevenhornung/Development/intercloud/storage/InterCloudStorage"
	}

	zipUtilities (ZipUtilities) {
		ZIP_TEMP_STORAGE_PATH = "storage/TemporaryZipStorage"
		//ZIP_TEMP_STORAGE_PATH = "/home/stevenhornung/Development/intercloud/storage/TemporaryZipStorage"
	}

	dropboxCloudStore (DropboxCloudStore) {
		STORE_NAME = "dropbox"
		APP_KEY = "ujdofnwh516yrg0"
		APP_SECRET = "43itigcfb9y59dy"

		REDIRECT_URL = "http://localhost:8080/auth_redirect"
		//REDIRECT_URL = "https://www.stevenhornung.com:8443/auth_redirect"
	}

	googledriveCloudStore (GoogledriveCloudStore) {
		STORE_NAME = 'googledrive'

		CLIENT_ID = "887098665005.apps.googleusercontent.com"
		CLIENT_SECRET = "OZQKsV0dGM04h-FtNt-VpGIF"
		REDIRECT_URL = "http://localhost:8080/auth_redirect"

		//REDIRECT_URL = "https://www.stevenhornung.com:8443/auth_redirect"
		//CLIENT_ID = "887098665005-d5gvvl2l344k21cje461348djl6k4uvh.apps.googleusercontent.com"
		//CLIENT_SECRET = "0Clr25m78aADDV_Vk4bBpuAr"

		GOOGLEDRIVE_FOLDER_TYPE = "application/vnd.google-apps.folder"
	}

	awsS3CloudStore (AwsS3CloudStore) {
		STORE_NAME = 'awss3'

		//AWS_CREDENTIAL_URL = "http://localhost:8080/awss3credentials"
		//REDIRECT_URL = "http://localhost:8080/auth_redirect"

		AWS_CREDENTIAL_URL = "http://stevenhornung.com:8443/awss3credentials"
		REDIRECT_URL = "https://www.stevenhornung.com:8443/auth_redirect"
	}
}
