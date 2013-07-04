<!DOCTYPE html>
<html>
	<head>
		<title>Login - InterCloud</title>
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
                    <!-- hidden anchor to stop jump http://www.css3create.com/Astuce-Empecher-le-scroll-avec-l-utilisation-de-target#wrap4  -->
                    <a class="hiddenanchor" id="toregister"></a>
                    <a class="hiddenanchor" id="tologin"></a>
                    <div id="wrapper">
                        <div id="login" class="animate form">
                            <form method="POST" action='${postUrl}' autocomplete="on"> 
                                <h1>Log in</h1> 
                                <p>
                                	<g:if test="${flash.loginMessage}">
										<div class="login_message" role="status">${flash.loginMessage}</div>
									</g:if>	
								</p>	
                                <p> 
                                    <label for="email" class="uname" data-icon="u" > Email </label>
                                    <input class="field" type="email" name="j_username" id="email" value="" size="23" required="required" placeholder="email@intercloud.com"/>
                                </p>
                                <p> 
                                    <label for="password" class="youpasswd" data-icon="p"> Password </label>
                                    <input class="field" type="password" name="j_password" id="password" size="23" required="required" placeholder="eg. X8df!90EO" /> 
                                </p>
                                <p class="keeplogin">
									<label><input name='${rememberMeParameter}' id="rememberme" type="checkbox" <g:if test='${hasCookie}'>checked='checked'</g:if> value="forever" /> &nbsp;Remember me</label>
								</p>
                                <p class="login button"> 
                                    <input type="submit" value="Login" /> 
								</p>
                                <p class="change_link">
									Not a member yet ?
									<a href="#toregister" class="to_register">Register</a>
								</p>
                            </form>
                        </div>

                        <div id="register" class="animate form">
                            <form  action="/register" autocomplete="on" method="post"> 
                                <h1> Register </h1> 
                                <p>
                                	<g:if test="${flash.message}">
										<div class="message" role="status">${flash.message}</div>
									</g:if>	
								</p>
                                <p> 
                                	<label for="signup" class="uname" data-icon="u">Name:</label>
									<input type="text" name="name" id="name" value="" size="23" required="required" type="text" placeholder="Steven Hornung" />
                                </p>
                                <p> 
                                    <label for="email" class="youmail" data-icon="e" > Your email</label>
                                    <input id="email" name="email" required="required" type="email" placeholder="email@intercloud.com"/> 
                                </p>
                                <p> 
                                    <label for="password" class="youpasswd" data-icon="p">Your password </label>
                                    <input id="password" name="password" required="required" type="password" placeholder="eg. X8df!90EO"/>
                                </p>
                                <p> 
                                    <label for="confirmPass" class="youpasswd" data-icon="p">Please confirm your password </label>
                                    <input id="confirmPass" name="confirmPass" required="required" type="password" placeholder="eg. X8df!90EO"/>
                                </p>
                                <p class="signin button"> 
									<input type="submit" value="Register"/> 
								</p>
                                <p class="change_link">  
									Already a member ?
									<a href="#tologin" class="to_register"> Go and login </a>
								</p>
                            </form>
                        </div>
						
                    </div>
                </div>  
            </section>
        </div>
    </body>
</html>
