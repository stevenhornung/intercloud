import grails.plugins.springsecurity.SecurityConfigType


// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = false // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

environments {
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.logging.jul.usebridge = false
        // TODO: grails.serverURL = "http://www.stevenhornung.com"
    }
}

// log4j configuration
log4j = {
    /*
    appenders {
        console name:'stdout',
            layout:pattern(conversionPattern: '%c{2} %m%n')
    }*/

	debug "com.intercloud"

    error  'org.codehaus.groovy.grails.web.servlet',        // controllers
           'org.codehaus.groovy.grails.web.pages',          // GSP
           'org.codehaus.groovy.grails.web.sitemesh',       // layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping',        // URL mapping
           'org.codehaus.groovy.grails.commons',            // core / classloading
           'org.codehaus.groovy.grails.plugins',            // plugins
           'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'
}

grails.plugins.springsecurity.userLookup.userDomainClassName = 'com.intercloud.Account'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'com.intercloud.AccountRole'
grails.plugins.springsecurity.authority.className = 'com.intercloud.Role'

grails.plugins.springsecurity.successHandler.defaultTargetUrl = '/home'
grails.plugins.springsecurity.auth.loginFormUrl = '/login'
grails.plugins.springsecurity.failureHandler.defaultFailureUrl = '/login/authfail'
grails.plugins.springsecurity.adh.errorPage = '/denied'
grails.plugins.springsecurity.logout.afterLogoutUrl = '/home'

grails.plugins.springsecurity.userLookup.usernamePropertyName='email'

grails.plugin.springsecurity.useSessionFixationPrevention = true

grails.plugins.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap
grails.plugins.springsecurity.interceptUrlMap = [
	'/account/**':         ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
    '/intercloud/**':      ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
	'/dropbox/**':         ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
	'/googledrive/**':     ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
	'/awss3/**':           ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
	'/awss3credentials':   ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
	'/cloudstore':         ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
	'/auth_redirect':      ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
	'/admin/**':		   ['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'],
	'/home':			   ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
    '/update':             ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
    '/upload':             ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
    '/delete':             ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
    '/download':           ['ROLE_USER', 'IS_AUTHENTICATED_REMEMBERED'],
	'/**':                 ['IS_AUTHENTICATED_ANONYMOUSLY']
]

grails.plugins.springsecurity.password.algorithm = 'bcrypt'

// Keep global list of all logged in users
loggedInUsers = []
grails.plugins.springsecurity.useSecurityEventListener = true

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
