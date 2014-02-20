import com.intercloud.*

class BootStrap {

	def springSecurityService

    def init = { servletContext ->
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError: true)

		Account steveAdmin = Account.findByEmail('steven.hornung@icloud.com') ?: new Account(
			email: 'steven.hornung@icloud.com',
			password: 'xxx',
			fullName: 'Steven Hornung',
			type: 'unlimited').save(failOnError: true)

		if (!steveAdmin.authorities.contains(adminRole)) {
			AccountRole.create steveAdmin, adminRole
			AccountRole.create steveAdmin, userRole
		}

		Account shaderAdmin = Account.findByEmail('brandon.shader@uky.edu') ?: new Account(
			email: 'brandon.shader@uky.edu',
			password: 'xxx',
			fullName: 'Brandon Shader',
			type: 'unlimited').save(failOnError: true)

		if (!shaderAdmin.authorities.contains(adminRole)) {
			AccountRole.create shaderAdmin, adminRole
			AccountRole.create shaderAdmin, userRole
		}

		createIntercloudCloudStore(steveAdmin)
		createRootIntercloudFileResource(steveAdmin)

		createIntercloudCloudStore(shaderAdmin)
		createRootIntercloudFileResource(shaderAdmin)
    }

	private def createIntercloudCloudStore(Account newAccount) {
		CloudStore cloudStoreInstance = new CloudStore()

		cloudStoreInstance.account = newAccount
		cloudStoreInstance.storeName = 'intercloud'
		cloudStoreInstance.userId = newAccount.email
		cloudStoreInstance.save()

		newAccount.addToCloudStores(cloudStoreInstance)
	}

	private def createRootIntercloudFileResource(Account newAccount) {
		FileResource rootIntercloudFileResource = new FileResource()
		def storeName = 'intercloud'
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, newAccount)

		rootIntercloudFileResource.cloudStore = cloudStore
		rootIntercloudFileResource.path = '/'

		String locationOnFileSystem = "storage/InterCloudStorage/" + newAccount.email + '/InterCloudRoot'
		//String locationOnFileSystem = "/home/stevenhornung/Development/intercloud/storage/InterCloudStorage/" + newAccount.email + '/InterCloudRoot'

		new File(locationOnFileSystem).mkdirs()
		rootIntercloudFileResource.locationOnFileSystem = locationOnFileSystem

		rootIntercloudFileResource.isDir = true
		rootIntercloudFileResource.fileName = 'InterCloudRoot'

		cloudStore.addToFileResources(rootIntercloudFileResource)
	}

	def destroy = {}

}
