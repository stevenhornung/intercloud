import com.intercloud.*

class BootStrap {
	
	def springSecurityService

    def init = { servletContext ->
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError: true)

		def steveAdmin = Account.findByEmail('steven.hornung@icloud.com') ?: new Account(
			email: 'steven.hornung@icloud.com',
			password: 'password',
			fullName: 'Steven Hornung',
			type: 'unlimited').save(failOnError: true)
			
		if (!steveAdmin.authorities.contains(adminRole)) {
			AccountRole.create steveAdmin, adminRole
			AccountRole.create steveAdmin, userRole
		}
			
		def shaderAdmin = Account.findByEmail('brandon.shader@uky.edu') ?: new Account(
			email: 'brandon.shader@uky.edu',
			password: 'password',
			fullName: 'BrandonShader',
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
	
    def destroy = {
    }
	
	private def createIntercloudCloudStore(Account newAccount) {
		CloudStore cloudStoreInstance = new CloudStore()
		
		cloudStoreInstance.account = newAccount
		cloudStoreInstance.storeName = 'intercloud'
		cloudStoreInstance.userId = newAccount.email
		cloudStoreInstance.spaceUsed = newAccount.spaceUsed
		cloudStoreInstance.totalSpace = newAccount.totalSpace
		
		cloudStoreInstance.save(flush: true)
	}
	
	private def createRootIntercloudFileResource(Account newAccount) {
		FileResource rootIntercloudFileResource = new FileResource()
		def storeName = 'intercloud'
		CloudStore cloudStore = CloudStore.findByStoreNameAndAccount(storeName, newAccount)
		
		rootIntercloudFileResource.cloudStore = cloudStore
		rootIntercloudFileResource.path = '/'
		rootIntercloudFileResource.isDir = true
		rootIntercloudFileResource.fileName = ''
		rootIntercloudFileResource.save()
		
		def fileResources = []
		fileResources.add(rootIntercloudFileResource)
		cloudStore.fileResources = fileResources
		cloudStore.save()
	}
}
