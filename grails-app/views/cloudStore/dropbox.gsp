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
				$(".colorbox").colorbox({rel:'colorbox', transition:"none", maxWidth:"75%", maxHeight:"90%"});
			});
		</script>

		<script>
		    $(function() {
			    $( "#dialog-form" ).dialog({
			      autoOpen: false,
			      resizable: false,
			      height:200,
			      width: 350,
			      modal: true,
			      buttons: {
			        "Create Folder": function() {
			        	var storeName = $("#storeName").val();
			        	var targetDir = $("#targetDir").val();
			        	var folderName = $("#folderName").val();
			        	$.ajax({
			        		url: "/newfolder",
			        		type: "POST",
			        		data: {
			        			storeName: storeName,
			        			targetDir: targetDir,
			        			folderName: folderName},
			        		success: function(data) {
			        			$("#resourceList").html(data);
			        			$("#flashinfo").html("${flash.info}")
			        		}
			        	});

			            $( this ).dialog( "close" );
			        },
			        Cancel: function() {
			          $( this ).dialog( "close" );
			        }
			      }
			    });

				$( "#newFolder" ).click(function() {
			        $( "#dialog-form" ).dialog( "open" );
			      });

				Dropzone.options.dropboxDropzone = {
				  init: function() {
				    this.on("complete", function(file) {
				    	$("#flashinfo").html("File uploaded successfully.");
				    	setTimeout(function() {
				    		$("#flashinfo").html("");
				    	}, 3000);
			        });
				  },
				  parallelUploads: 5,
				  maxFilesize: 3072 // 3 gb
				};
			});
		</script>

	</head>
	<body>
		<a href="#page-body" class="skip"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<sec:ifLoggedIn>
			<g:if test="${fileInstanceList != null }">
				<div id="status" role="complementary">
					<p><a href="/download?storeName=dropbox">Download Entire Dropbox</a></p>
					<g:formRemote name="dropboxDropzone" url="[controller: 'cloudstore', action: 'upload', params:[storeName: 'dropbox', targetDir: request.forwardURI]]" update="resourceList" class="dropzone">
					  <div class="fallback">
					    <input name="file" type="file" multiple />
					  </div>
					</g:formRemote>

				</div>
			</g:if>
		</sec:ifLoggedIn>
		<div id="page-body" role="main">
			<g:if test="${flash.error }">
					<div id="flasherror" class="errors">
							${flash.error}
					</div>
			</g:if>
			<div id="flashinfo" class="message">
					${flash.info}
			</div>
			<sec:ifLoggedIn>
				<g:if test="${fileInstanceList != null }">
					<div style="margin-top:10px">
						<img style="display:inline-block" src="${resource(dir: 'images', file: 'dropbox.jpeg')}" width=50 height=50>
							 | <g:remoteLink controller="cloudstore" action="update" update="resourceList" params="[storeName:'dropbox']">Sync</g:remoteLink>
							 <div style="display:inline-block" id="newFolder">| <a href="#"><a href="#">New Folder</a></div>
							 	<div id="dialog-form">
							 		<form>
							 			<label for="folderName">Folder Name</label>
							 			<input type="text" id="folderName" placeholder="New Folder">
							 			<input type="hidden" id="storeName" value="dropbox">
							 			<input type="hidden" id="targetDir" value="${request.forwardURI}">
							 		</form>
							 	</div>
						<div style="float:right;display:inline-block">
					 		Space: ${spaceUsedList[0]} ${spaceUsedList[1]} of ${totalSpaceList[0]} ${totalSpaceList[1]}</b>
					 	</div>
						<br><br>
						<g:if test="${params.fileResourcePath }">
							<g:set var="pathList" value="${params.fileResourcePath.split('/') }" scope="request" />
							<g:set var="backPath" value="/dropbox" scope="request" />
							<a href="${backPath}">Dropbox</a>
							<g:each in="${pathList }" status="i" var="pathPiece">
								<g:set var="backPath" value="${backPath + '/' + pathPiece}" scope="request" />
								&#65515; <a href="${backPath}">${pathPiece}</a>
							</g:each>
						</g:if>
						<hr>
						<br>
					</div>
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
					<div id="resourceList">
						<g:render template="layouts/dropboxResources" model="[fileInstanceList: fileInstanceList, cloudStore: 'dropbox']" />
					</div>
				</g:if>
			</sec:ifLoggedIn>
			<sec:ifNotLoggedIn>
				<h1>Display what adding dropbox link can do</h1>
			</sec:ifNotLoggedIn>
		</div>

	</body>
</html>
