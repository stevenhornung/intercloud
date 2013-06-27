import com.intercloud.*

class BootStrap {
	
	def springSecurityService

    def init = { servletContext ->
		def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER').save(failOnError: true)
		def adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: 'ROLE_ADMIN').save(failOnError: true)

		def adminUser = Account.findByEmail('steven.hornung@icloud.com') ?: new Account(
			email: 'steven.hornung@icloud.com',
			password: 'vie11eicht',
			fullName: 'Steven Hornung').save(failOnError: true)

		if (!adminUser.authorities.contains(adminRole)) {
			AccountRole.create adminUser, adminRole
		}

    }
    def destroy = {
    }
}
