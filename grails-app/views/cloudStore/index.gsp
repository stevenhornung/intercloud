<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Home - intercloud</title>
		<style type="text/css" media="screen">
			#status {
				background-color: #eee;
				border: .2em solid #fff;
				margin: 2em 2em 1em;
				padding: 1em;
				width: 12em;
				float: left;
				-moz-box-shadow: 0px 0px 1.25em #ccc;
				-webkit-box-shadow: 0px 0px 1.25em #ccc;
				box-shadow: 0px 0px 1.25em #ccc;
				-moz-border-radius: 0.6em;
				-webkit-border-radius: 0.6em;
				border-radius: 0.6em;
			}

			.ie6 #status {
				display: inline; /* float double margin fix http://www.positioniseverything.net/explorer/doubled-margin.html */
			}

			#status ul {
				font-size: 0.9em;
				list-style-type: none;
				margin-bottom: 0.6em;
				padding: 0;
			}

			#status li {
				line-height: 1.3;
			}

			#status h1 {
				text-transform: uppercase;
				font-size: 1.1em;
				margin: 0 0 0.3em;
			}

			#page-body {
				margin: 2em 1em 1.25em 18em;
			}

			h2 {
				margin-top: 1em;
				margin-bottom: 0.3em;
				font-size: 1em;
			}

			p {
				line-height: 1.5;
				margin: 0.25em 0;
			}

			#controller-list ul {
				list-style-position: inside;
			}

			#controller-list li {
				line-height: 1.3;
				list-style-position: inside;
				margin: 0.25em 0;
			}

			@media screen and (max-width: 480px) {
				#status {
					display: none;
				}

				#page-body {
					margin: 0 1em 1em;
				}

				#page-body h1 {
					margin-top: 0;
				}
			}
		</style>

		<script src="${resource(dir: 'js', file: 'jquery-1.10.2.js')}" type="text/javascript"></script>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'jquery-ui-1.10.4.custom.css')}" type="text/css" media="screen" />
		<script src="${resource(dir: 'js', file: 'jquery-ui-1.10.4.custom.js')}" type="text/javascript"></script>

		<link rel="stylesheet" href="${resource(dir: 'css', file: 'accordianza1-style.css')}" type="text/css" media="screen" />
		<script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery-easing/1.3/jquery.easing.min.js"></script><!-- optional -->
		<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.accordionza.min.js')}"></script>

		<script>
			$(document).ready(function(){
				$(".colorbox").colorbox({rel:'colorbox', transition:"none", maxWidth:"75%", maxHeight:"90%"});
			});
		</script>

		<script>
			$(document).ready(function() {

				// On page load, remove any
				setTimeout(function() {
		    		$("#flashinfo").html("");
		    		$("#flasherror").html("");
		    	}, 3000);
			});
		</script>

		<script>
			$(document).ready(function() {
				// On page load, begin polling if client needs resource update
				pollServerForUpdates();

			});

			function pollServerForUpdates() {
				$.ajax({
	        		url: "/cloudstore/needsupdate",
	        		type: "POST",
	        		complete: function(response) {
	        			var resp = $.parseJSON(response.responseText);

	        			// check if client needs update
	        			if(resp.isUpdated) {
        					updateResources();
	        			}

        				// Poll every 5 seconds
        				setTimeout(function() {
        					pollServerForUpdates();
        				}, 5000);
	        		}
		        });
			}

			function updateResources() {
				var targetDir = $(location).attr('href');
				$.ajax({
	        		url: "/cloudstore/update",
	        		type: "POST",
	        		data: {
			        	targetDir: targetDir,
			        	sync: false
			        },
	        		success: function(data) {
	        			$("#homeResources").html(data);
	        			$("#flashinfo").html("Cloud store linked successfully.")
	        			setUpdated();
	        		}
	        	});
			}

			function setUpdated() {
				$.ajax({
	        		url: "/cloudstore/setupdated",
	        		type: "POST",
	        		success: function(response) {}
	        	});
			}
		</script>

	</head>
	<body>
		<a href="#page-body" class="skip"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div>
			<sec:ifLoggedIn>
				<div id="flasherror" class="errors">
					${flash.error}
				</div>
				<div id="flashinfo" class="message">
					${flash.info}
				</div>
				<div id="homeResources">
					<g:render template="layouts/homeResources" model="[homeResources: homeResources]" />
				</div>
			</sec:ifLoggedIn>
			<sec:ifNotLoggedIn>
				<h1>Display all features and capabilities for non logged in user</h1>
			</sec:ifNotLoggedIn>
		</div>
	</body>
</html>