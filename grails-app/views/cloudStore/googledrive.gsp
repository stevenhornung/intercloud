<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>InterCloud</title>
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
	</head>
	<body>
		<a href="#page-body" class="skip"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div id="status" role="complementary">
			<p>side stuff</p>
		</div>
		<div id="page-body" role="main">
			<h1>Welcome to InterCloud</h1>
			<br>
			<hr>
			<sec:ifLoggedIn>
				<g:if test="${fileInstanceList != null }">
					<h2><b>Google Drive Files | <a href="/update?storeName=googledrive">Sync</a></b></h2>
					<div id="accordion">
						<g:each in="${fileInstanceList}" status="i" var="fileInstance">
							<tr>
								<h3>${fileInstance.fileName }</h3>
									<div>
										<p><g:if test="${fileInstance.isDir }">
												<td><a href="/googledrive${fileInstance.path.replaceAll(' ', '+')}">Open Folder</a></td>
											</g:if>
											<g:else>
												<td><a class="colorbox" href="/googledrive${fileInstance.path.replaceAll(' ', '+')}">Open</a></td>
											</g:else>
											<td><a href="/download?storeName=googledrive&fileResourceId=${fileInstance.id}">Download</a></td>
											<td><a href="#">Move</a></td>
											<td><a href="/delete?storeName=googledrive&fileResourceId=${fileInstance.id}&targetUri=${request.forwardURI}">Delete</a></td>
										</p>
									</div>
							</tr>
						</g:each>
					</div>
				</g:if>
			</sec:ifLoggedIn>
			<sec:ifNotLoggedIn>
				<h1>Signup to add your files to the cloud bre</h1>
			</sec:ifNotLoggedIn>
		</div>
	</body>
</html>
