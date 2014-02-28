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

		<script>
			$(document).ready(function(){
				$(".colorbox").colorbox({rel:'colorbox', transition:"none", maxWidth:"75%", maxHeight:"90%"});
			});
		</script>

		<script>
		  $(function() {
		    $( "#accordion_intercloud" ).accordion({
		      collapsible: true,
		      active: false
		    });
		  });
		  $(function() {
		    $( "#accordion_dropbox" ).accordion({
		      collapsible: true,
		      active: false
		    });
		  });
		  $(function() {
		    $( "#accordion_googledrive" ).accordion({
		      collapsible: true,
		      active: false
		    });
		  });
		</script>
	</head>
	<body>
		<a href="#page-body" class="skip"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<sec:ifLoggedIn>
			<div id="status" role="complementary">
				<ul>
					<li><a href="/update">Sync Files</a></li>
					<li><a href="/intercloud">Go to intercloud Files</a></li>
					<li><a href="/dropbox">Go to Dropbox Files</a></li>
					<li><a href="/googledrive">Go to Google Drive Files</a></li>
				</ul>
			</div>
		</sec:ifLoggedIn>
		<div id="page-body" role="main">
			<sec:ifLoggedIn>
				<g:if test="${flash.error }">
					<div class="errors">
							${flash.error}
					</div>
				</g:if>
				<g:if test="${flash.info }">
					<div class="message">
							${flash.info}
					</div>
				</g:if>
				<g:each in="${homeResources}" status="i" var="cloudStore">
					<br>
					<hr>
					<br>
					<g:if test="${cloudStore.key == 'dropbox' }">
						<a href="${cloudStore.key}"><img src="${resource(dir: 'images', file: 'dropbox.jpeg')}" width=50 height=50></a>
					</g:if>
					<g:elseif test="${cloudStore.key == 'googledrive' }">
						<a href="${cloudStore.key}"><img src="${resource(dir: 'images', file: 'googledrive.png')}" width=50 height=50></a>
					</g:elseif>
					<g:elseif test="${cloudStore.key == 'intercloud' }">
						<a href="${cloudStore.key}"><img src="${resource(dir: 'images', file: 'intercloud.jpeg')}" width=50 height=50></a>
					</g:elseif>
					<g:else>
						<h2><a href="${cloudStore.key}">${cloudStore.key.capitalize()} Files</a>
					</g:else>

					<g:if test="${cloudStore.key != 'intercloud' }">	|  <g:remoteLink controller="cloudstore" action="update" update="accordian_${cloudStore.key}" params="[storeName: cloudStore.key]">Sync</g:remoteLink> </g:if></h2>
					<g:if test="${cloudStore.value }">
						<div id="accordion_${cloudStore.key }">
							<g:render template="layouts/cloudStoreResources" model="[fileInstanceList: cloudStore.value, cloudStore: cloudStore.key]" />
						</div>
					</g:if>
				</g:each>
			</sec:ifLoggedIn>
			<sec:ifNotLoggedIn>
				<h1>Display all features and capabilities for non logged in user</h1>
			</sec:ifNotLoggedIn>
		</div>
	</body>
</html>