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
				$('#accordion1').accordionza({
					captionDelay: 100,
					captionEasing: 'easeOutBounce',
					captionHeight: 40,
					captionHeightClosed: 10,
					navKey: true
				});
			});
		</script>

	</head>
	<body>
		<a href="#page-body" class="skip"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div>
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
				<ul id="accordion1">
					<g:each in="${homeResources}" status="i" var="cloudStore">
						<li class="${cloudStore.key}_slide">
							<div class="slide_handle">
								<g:if test="${cloudStore.key == 'dropbox' }">
									<img style="margin-top:4px;margin-left:4px" src="${resource(dir: 'images', file: 'dropbox.jpeg')}" width=30 height=30>
								</g:if>
								<g:elseif test="${cloudStore.key == 'googledrive' }">
									<img style="margin-top:4px;margin-left:4px" src="${resource(dir: 'images', file: 'googledrive.png')}" width=30 height=30>
								</g:elseif>
								<g:elseif test="${cloudStore.key == 'intercloud' }">
									<img style="margin-top:4px;margin-left:4px" src="${resource(dir: 'images', file: 'intercloud.jpeg')}" width=30 height=30>
								</g:elseif>
								<div></div>
							</div>
							<div class="slide_content">
								<a style="margin-top:4px;margin-left:4px;color:#fff" href="${cloudStore.key}">${cloudStore.key.capitalize()}</a>
								<g:if test="${cloudStore.key != 'intercloud' }">
										|	<g:remoteLink controller="cloudstore" action="update" update="${cloudStore.key}ResourceList" params="[storeName: cloudStore.key, targetDir: request.forwardURI]" style="color:#fff">Sync</g:remoteLink>
								</g:if>
								<g:if test="${cloudStore.value }">
									<div style="border:solid;border-width:1px;margin-bottom:20px;border-radius:5px;padding:3px">
										<div style="margin-left:10px;display:inline-block;">
											Name
										</div>
										<div style="margin-left:40%;display:inline-block;">
											Kind
										</div>
										<div style="float:right;margin-right:30px;display:inline-block;">
											Modified
										</div>
									</div>
									<div id="${cloudStore.key}ResourceList">
										<g:render template="layouts/${cloudStore.key}Resources" model="[fileInstanceList: cloudStore.value, cloudStore: cloudStore.key]" />
									</div>
								</g:if>
							</div>
						</li>
					</g:each>
				</ul>
			</sec:ifLoggedIn>
			<sec:ifNotLoggedIn>
				<h1>Display all features and capabilities for non logged in user</h1>
			</sec:ifNotLoggedIn>
		</div>
	</body>
</html>