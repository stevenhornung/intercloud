import com.intercloud.*

// Place your Spring DSL code here
beans = {
	userDetailsService(com.intercloud.accountdetails.AccountDetailsService)
	
	"com.intercloud.cloudstore.DropboxCloudStore" (com.intercloud.cloudstore.DropboxCloudStore) { bean ->
		APP_KEY = "ujdofnwh516yrg0"
		APP_SECRET = "43itigcfb9y59dy"
	}
}
