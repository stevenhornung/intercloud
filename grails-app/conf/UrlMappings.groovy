class UrlMappings {

	static mappings = {
		"401" (controller: 'base', action: 'respondUnauthorized')
		"404" (controller: 'base', action: 'respondPageNotFound')
		"405" (controller: 'base', action: 'respondInvalidAction')
		"500"(controller: 'base', action: 'respondServerError')

		"/" {
			controller = 'home'
			action = [GET: "redirectHome",
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
		}
		
		"/home" {
			controller = 'home'
			action = [GET: "index",
						POST: 'loginOrRegister',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
		}
		
		"/account" {
			controller = 'account'
			action = [GET: 'index',
						POST: 'register',
						PUT: 'updateAccount',
						DELETE: 'deleteAccount']
		}
		
		"/account/logout" {
			controller = 'account'
			action = [GET: 'logout',
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
		}
		
		"/cloudstore" {
			controller = 'cloudStore'
			action = [GET: 'index',
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
		}
		
		"/intercloud" {
			controller = 'cloudStore'
			action = [GET: "getCloudStoreResources",
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
			storeName = 'intercloud'
		}
		
		"/intercloud/$fileResourcePath**" {
			controller = 'cloudStore'
			action = [GET: "getSpecificCloudStoreResources",
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
			storeName = 'intercloud'
		}
		
		"/dropbox" {
			controller = 'cloudStore'
			action = [GET: "getCloudStoreResources",
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
			storeName = 'dropbox'
		}
		
		"/dropbox/$fileResourcePath**" {
			controller = 'cloudStore'
			action = [GET: "getSpecificCloudStoreResources",
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
			storeName = 'dropbox'
		}
		
		"/delete" {
			controller = 'cloudStore'
			action = [GET: 'deleteResource',
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
		}
		
		"/auth_redirect" {
			controller = 'cloudStore'
			action = [GET: "authRedirect",
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
		}
		
		"/register" (controller: 'account', action: 'register')
		"/login" (controller: 'login', action: 'auth')
		"/login/authfail" (controller: 'login', action: 'authfail')
		"/denied" (controller: 'login', action: 'denied')
		"/logout"(controller: "logout")
		
		"/admin" (controller: 'admin')
		
		"/download" {
			controller = 'cloudStore'
			action = [GET: 'showDownloadDialog',
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
		}
		
		"/update" {
			controller = "cloudStore"
			action = [GET: 'updateResources',
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
		}
		
		"/upload" {
			controller = "cloudStore"
			action = [GET: 'respondInvalidAction',
						POST: 'uploadResources',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
		}
	}
}
