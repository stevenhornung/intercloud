<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<title><g:layoutTitle default="InterCloud"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'style.css')}" type="text/css" media="screen" />
  		<link rel="stylesheet" href="${resource(dir: 'css', file: 'slide.css')}" type="text/css" media="screen" />
  		<link rel="stylesheet" href="${resource(dir: 'css', file: 'header.css')}" type="text/css" media="screen" />
		<g:layoutHead/>
		<r:layoutResources />
		
		
		<script src="js/slide.js" type="text/javascript"></script>
		
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'colorbox.css')}" type="text/css" media="screen" />
		<script src="js/jquery.colorbox.js" type="text/javascript"></script>
		<script src="js/jquery.colorbox-min.js" type="text/javascript"></script>
	</head>
	<body>
		<header>
	      <div class="inner">
	        <a href="/home"><h1>InterCloud</h1></a>
	        <h2></h2>
	      </div>
	    </header>
		<g:layoutBody/>
			
		<!-- Panel -->
		<div id="toppanel">
		
			<!-- Display login/register for non logged in user -->
			<sec:ifNotLoggedIn>
				<!--  login -->
				<div id="panel">
					<div class="content clearfix">
						<div class="left">
							<h1>Welcome to InterCloud</h1>
							<h2>Access all of your cloud data in one location</h2>		
							<p class="grey">Add all of your cloud data services including Dropbox, Google Drive, Box, Azure Storage, Amazon AWS, SkyDrive and more!</p>
							</div>
						<div class="left">
							<!-- Login Form -->
							<form class="clearfix" action="${resource(file: 'j_spring_security_check')}" method="post">
								<h1>Member Login</h1>
								<label class="grey" for="log">Email:</label>
								<input class="field" type="email" name="j_username" id="log" value="" size="23" required="required"/>
								<label class="grey" for="pwd">Password:</label>
								<input class="field" type="password" name="j_password" id="pwd" size="23" required="required"/>
				            	<label><input name="_spring_security_remember_me" id="rememberme" type="checkbox" <g:if test='${hasCookie}'>checked='checked'</g:if> value="forever" /> &nbsp;Remember me</label>
			        			<div class="clear"></div>
								<input type="submit" name="submit" value="Login" class="bt_login" />
								<a class="lost-pwd" href="#">Lost your password?</a>
							</form>
						</div>
						<div class="left right">			
							<!-- Register Form -->
							<form action="#" method="post">
								<h1>Not a member yet? Sign Up!</h1>				
								<label class="grey" for="signup">Name:</label>
								<input class="field" type="text" name="name" id="name" value="" size="23" required="required"/>
								<label class="grey" for="email">Email:</label>
								<input class="field" type="email" name="email" id="email" size="23" required="required"/>
								<label class="grey" for="password">Password:</label>
								<input class="field" type="password" name="password" id="password" size="23" required="required"/>
								<label class="grey" for="confirmPass">Confirm Password:</label>
								<input class="field" type="password" name="confirmPass" id="confirmPass" size="23" required="required"/>
								<input type="submit" name="submit" value="Register" class="bt_register" />
							</form>
						</div>
					</div>
				</div> <!-- /login -->	
			
				<!-- The tab on top -->	
				<div class="tab">
					<ul class="login">
						<li class="left">&nbsp;</li>
						<li>Hello Guest!</li>
						<li class="sep">|</li>
						<li id="toggle">
							<a id="open" class="open" href="#">Log In | Register</a>
							<a id="close" style="display: none;" class="close" href="#">Close Panel</a>			
						</li>
						<li class="right">&nbsp;</li>
					</ul> 
				</div> <!-- / top -->
			</sec:ifNotLoggedIn>
			
			<sec:ifLoggedIn>
				<!--  loggged in user -->
				<div id="panel">
					<div class="content clearfix">
						<div class="left">
							<h1>Welcome to InterCloud</h1>
							<h2>Access all of your cloud data in one location</h2>		
							<p class="grey">Add all of your cloud data services including Dropbox, Google Drive, Box, Azure Storage, Amazon AWS, SkyDrive and more!</p>
							</div>
						<div class="left">
							<!-- Add Cloud Store -->
							<h1>Link Cloud Account</h1><br>
							<a href="cloudstore?storeName=dropbox">Link Dropbox Account</a><br>
							<g:link controller="cloudStore" action="index" params="[storeName:'googledrive']">Link Google Drive Account</g:link><br>
							<g:link controller="cloudStore" action="index" params="[storeName:'box']">Link Box Account</g:link><br>
							<g:link controller="cloudStore" action="index" params="[storeName:'skydrvie']">Link Microsoft SkyDrive Account</g:link><br>
							<g:link controller="cloudStore" action="index" params="[storeName:'azure']">Link Azure Storage Account</g:link><br>
							<g:link controller="cloudStore" action="index" params="[storeName:'amazonaws']">Link Amazon AWS Account</g:link><br>
						</div>
						<div class="left right">			
							<!-- Account Settings -->
							<h1>Account Settings</h1><br>
							<sec:access expression="hasRole('ROLE_ADMIN')">
								<g:link controller="admin" action="index">Admininstration</g:link><br>
							</sec:access>
							<g:link controller="account" action="index">Account Settings</g:link><br>
							<g:link controller="account" action="upgrade" params="">Upgrade Account</g:link><br>
						</div>
					</div>
				</div> <!-- /logged in user -->	
			
				<!-- The tab on top -->	
				<div class="tab">
		        	<ul class="login">
		            	<li class="left">&nbsp;</li>
		            	<li id="toggle">
		              		<a id="open" class="open" href="#"><sec:loggedInUserInfo field="fullName"/></a>
		              		<a id="close" style="display: none;" class="close" href="#">Close Panel</a>      
		            	</li>
		            	<li class="sep">|</li>
		            	<li><g:link controller="logout">&nbsp&nbspLogout</g:link></li>
		            	<li class="right">&nbsp;</li>
		          	</ul> 
		       	</div> <!-- / top -->
			</sec:ifLoggedIn>
		</div> <!--panel -->

		
		<div class="footer" role="contentinfo"></div>
		<div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
		<g:javascript library="application"/>
		<r:layoutResources />
	</body>
</html>
