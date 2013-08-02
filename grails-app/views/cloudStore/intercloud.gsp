<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>InterCloud - InterCloud</title>
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
		
		<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
		<script src="${resource(dir: 'js', file: 'jquery-1.9.1.js')}" type="text/javascript"></script>
		<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>
		
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'colorbox.css')}" type="text/css" media="screen" />
		<script src="${resource(dir: 'js', file: 'jquery.colorbox-min.js')}" type="text/javascript"></script>
		<script src="${resource(dir: 'js', file: 'jquery.colorbox.js')}" type="text/javascript"></script>
		<script>
			$(document).ready(function(){
				$(".colorbox").colorbox({rel:'colorbox', transition:"none", width:"75%", height:"75%"});
			});
		</script>
		
		<script>
		  $(function() {
		    $( "#accordion" ).accordion({
		      collapsible: true
		    });
		  });
		</script>
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
					<h2><b>InterCloud Files</b></h2>
					<div id="accordion">
						<g:each in="${fileInstanceList}" status="i" var="fileInstance">
							<tr>
								<h3>${fileInstance.fileName }</h3>
									<div>
										<p><g:if test="${fileInstance.isDir }">
												<td><a href="/intercloud${fileInstance.path.replaceAll(' ', '+')}">Open Folder</a></td>
											</g:if>
											<g:else>
												<td><a class="colorbox" href="/intercloud${fileInstance.path.replaceAll(' ', '+')}">Open</a></td>
											</g:else>
											<td><a href="/download?fileResourceId=${fileInstance.id}&storeName=intercloud">Download</a></td>
											<td><a href="#">Move</a></td>
											<td><a href="/delete?cloudStore=intercloud&fileResourceId=${fileInstance.id}&targetUri=${request.forwardURI}">Delete</a></td>
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