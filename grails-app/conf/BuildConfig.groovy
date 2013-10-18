grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
grails.project.war.file = "target/${appName}-${appVersion}.war"


grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()
    }

    dependencies {
		compile 'com.dropbox.core:dropbox-core-sdk:[1.7,1.8)'
		compile 'com.google.apis:google-api-services-drive:v2-rev100-1.17.0-rc'
		compile 'com.google.api-client:google-api-client-servlet:1.17.0-rc'
		//compile 'com.amazonaws:aws-java-sdk:1.0.002'
		
		compile 'org.apache.tika:tika-core:0.7'
		
		build 'org.slf4j:slf4j-api:1.7.5'
		
		test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
		
		build 'org.apache.tika:tika-core:0.9'
    }

    plugins {
        runtime ":hibernate:$grailsVersion"
        runtime ":jquery:1.8.3"
        runtime ":resources:1.1.6"

        build ":tomcat:$grailsVersion"

        runtime ":database-migration:1.3.2"

        compile ':cache:1.0.1'
		
		compile ':spring-security-core:1.2.7.3'
		
		compile ":executor:0.3"
		compile ":quartz:1.0-RC9"
		
		compile ':webflow:2.0.8.1'
		
		test(":spock:0.7") {
			exclude "spock-grails-support"
		}
		test ":code-coverage:1.2.6"
    }
}
