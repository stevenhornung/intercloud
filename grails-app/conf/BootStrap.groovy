import com.intercloud.*

class BootStrap {
	
	def springSecurityService

    def init = { servletContext ->
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError: true)

		def adminUser1 = Account.findByEmail('steven.hornung@icloud.com') ?: new Account(
			email: 'steven.hornung@icloud.com',
			password: 'password',
			fullName: 'Steven Hornung').save(failOnError: true)
			
		if (!adminUser1.authorities.contains(adminRole)) {
			AccountRole.create adminUser1, adminRole
			AccountRole.create adminUser1, userRole
		}
			
		def adminUser2 = Account.findByEmail('brandon.shader@uky.edu') ?: new Account(
			email: 'brandon.shader@uky.edu',
			password: 'password',
			fullName: 'BrandonShader').save(failOnError: true)

		if (!adminUser2.authorities.contains(adminRole)) {
			AccountRole.create adminUser2, adminRole
			AccountRole.create adminUser2, userRole
		}

    }
    def destroy = {
    }
}
