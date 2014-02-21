<!DOCTYPE html>
<html>
	<head>
		<title>intercloud</title>
        <!-- <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">  -->
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="Login and Registration Form with HTML5 and CSS3" />
        <meta name="keywords" content="html5, css3, form, switch, animation, :target, pseudo-class" />
        <meta name="author" content="Codrops" />
        <link rel="shortcut icon" href="../favicon.ico">
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'demo.css')}" type="text/css" media="screen" />
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'style1.css')}" type="text/css" media="screen" />
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'animate-custom.css')}" type="text/css" media="screen" />
    </head>
    <body>
        <div class="container">
            <section>
                <div id="container_demo" >
                    <div id="wrapper">
                        <div id="credentials" class="animate form">
                            <form method="POST" action='/auth_redirect' autocomplete="on">
                                <h1>Please provide your Amazon AWS S3 Credentials</h1>
                                <h3>(Go to the <a href="https://console.aws.amazon.com/s3" target="_blank">S3 Management Console</a> ,
                                click the dropbown box with the account holder's name, and click 'Security Credentials'. Expand the 'Access Keys' box
                                and click 'Create New Access Key'. Copy the Access Key Id and Secret Access Key here.)</h3>
                                <p>
                                	<g:if test="${flash.invalidCredentials}">
										<div class="invalid_message" role="status">${flash.invalidCredentials}</div>
									</g:if>
								</p>
                                <p>
                                    <label for="accessKey" class="uname" data-icon="u" > AWS Access Key Id </label>
                                    <input class="field" type="text" name="accessKey" id="accessKey" value="" size="20" required="required" placeholder="XXXXXXXXXXXXXXXXXXXX"/>
                                </p>
                                <p>
                                    <label for="secretKey" class="secretKey" data-icon="p"> AWS Secret Key </label>
                                    <input class="field" type="text" name="secretKey" id="secretKey" size="40" required="required" placeholder="XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" />
                                </p>
                                <p class="login button">
                                    <input type="submit" value="Submit" />
								</p>
                            </form>
                        </div>

                    </div>
                </div>
            </section>
        </div>
    </body>
</html>
