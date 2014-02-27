<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>intercloud</title>
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
				$(".colorbox").colorbox({rel:'colorbox', transition:"none", width:"75%", height:"75%"});
			});
		</script>

		<script>
		  $(function() {
		    $( "#accordion" ).accordion({
		      collapsible: true,
		      active: false
		    });
		  });
		</script>

		<script>
			Dropzone.options.dropzone = {
				parallelUploads: 1,
				maxFilesize: 3072 // 3 gb
					}
		</script>

	</head>
	<body>
		<a href="#page-body" class="skip"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<sec:ifLoggedIn>
			<g:if test="${fileInstanceList != null }">
				<div id="status" role="complementary">
					<p><a href="/download?storeName=intercloud">Download Entire intercloud</a></p>
					<form id="dropzone" action="/upload?storeName=intercloud&targetDir=${request.forwardURI}" class="dropzone">
					  <div class="fallback">
					    <input name="file" type="file" multiple />
					  </div>
					</form>

				</div>
			</g:if>
		</sec:ifLoggedIn>
		<div id="page-body" role="main">
			<g:if test="${flash.error }">
					<div class="errors">
							${flash.error}
					</div>
			</g:if>
			<sec:ifLoggedIn>
				<g:if test="${fileInstanceList != null }">
					<img src="${resource(dir: 'images', file: 'intercloud.jpeg')}" width=50 height=50>
					<div style="float:right">
				 		Space: ${spaceUsedList[0]} ${spaceUsedList[1]} of ${totalSpaceList[0]} ${totalSpaceList[1]}</b>
				 	</div>
					<g:if test="${params.fileResourcePath }">
						<g:set var="pathList" value="${params.fileResourcePath.split('/') }" scope="request" />
						<g:set var="backPath" value="/intercloud" scope="request" />
						&#65515; <a href="${backPath}">intercloud</a>
						<g:each in="${pathList }" status="i" var="pathPiece">
							<g:set var="backPath" value="${backPath + '/' + pathPiece}" scope="request" />
							&#65515; <a href="${backPath}">${pathPiece}</a>
						</g:each>
					</g:if>
					<br>
					<br>
					<hr>
					<br>
					<div id="accordion">
						<g:render template="layouts/cloudStoreResources" model="[fileInstanceList: fileInstanceList, cloudStore: 'intercloud']" />
					</div>
				</g:if>
			</sec:ifLoggedIn>
			<sec:ifNotLoggedIn>
				<h1>Signup to access all your files from one location</h1>
			</sec:ifNotLoggedIn>
		</div>

	</body>
</html>
