
<script>
  $(function() {
    $( "#accordion_intercloud" ).accordion({
      collapsible: true,
      active: false
    });
  });
</script>

<div id="accordion_intercloud">
	<g:each in="${fileInstanceList}" var="fileInstance">
		<h3 style="font-size:12px">
			<div style="margin-left:10px;display:inline-block;">
				${fileInstance.fileName}
			</div>
			<div style="margin-left:100px;display:inline-block;">
				${fileInstance.mimeType}
			</div>
			<div style="float:right;margin-right:30px;display:inline-block;">
				${fileInstance.modified}
			</div>
		</h3>
		<div>
			<p style="font-size:12px">
				<g:if test="${fileInstance.isDir }">
					<a href="/${cloudStore}${fileInstance.path.replaceAll(' ', '+')}">Open Folder</a>
				</g:if>
				<g:else>
					<td><a class="colorbox" href="/${cloudStore}${fileInstance.path.replaceAll(' ', '+')}">Open</a></td>
				</g:else>
				|
				<a href="/download?storeName=${cloudStore}&fileResourceId=${fileInstance.id}">Download</a>
				|
				<g:remoteLink controller="cloudstore" action="delete" update="resourceList" params="[storeName: cloudStore, fileResourceId: fileInstance.id, targetUri: request.forwardURI]">Delete</g:remoteLink>
			</p>
		</div>
	</g:each>
</div>