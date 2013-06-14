class UrlMappings {

	static mappings = {
		"404" (controller: 'base', action: 'respondPageNotFound')
		"500"(controller: 'base', action: 'respondServerError')
		// temp
		"/" {
			controller = 'cloudStore'
			action = [GET: "index",
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
		
		"/home" {
			controller = 'home'
			action = [GET: "list",
						POST: 'respondInvalidAction',
						PUT: 'respondInvalidAction',
						DELETE: 'respondInvalidAction']
		}
		
		"/files" {
			controller = 'fileResource'
			action = [GET: "retrieveAllResources",
						POST: 'uploadResources',
						PUT: 'updateResources',
						DELETE: 'deleteResources']
		}
		
		"/files/$file_id" {
			controller = 'fileResource'
			action = [GET: "retrieveSingleResource",
						POST: 'respondInvalidAction',
						PUT: 'updateResources',
						DELETE: 'deleteResources']
		}
		
		"/account" {
			controller = 'account'
			action = [GET: 'info',
						POST: 'createAccount',
						PUT: 'updateAccount',
						DELETE: 'deleteAccount']
		}
		
		"/cs" {
			controller = 'cloudStore'
			action = [GET: 'list',
						POST: 'addAccountCloudStore',
						PUT: 'updateAccountCloudStore',
						DELETE: 'deleteAccountCloudStore']
		}
	}
}
