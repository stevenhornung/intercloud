class UrlMappings {

	static mappings = {
		"404" (controller: 'base', action: 'respondPageNotFound')
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
						POST: 'createAccount',
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
		
		"/dropbox" {
			controller = 'cloudStore'
			action = [GET: "listCloudStoreFiles",
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
			cloudStore = 'dropbox'
		}
		"/dropbox/$fileResourcePath**" {
			controller = 'cloudStore'
			action = [GET: "retrieveFileResource",
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
			cloudStore = 'dropbox'
		}
		
		"/auth_redirect" {
			controller = 'cloudStore'
			action = [GET: "authRedirect",
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
		}
		
		"/login/$action?"(controller: "login")
		"/logout/$action?"(controller: "logout")
		
		"/admin" (controller: 'admin')
	}
}
