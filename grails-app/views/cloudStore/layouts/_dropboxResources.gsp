<g:each in="${fileInstanceList}" status="i" var="fileInstance">
	<tr>
		<h3>${fileInstance.fileName }</h3>
		<div>
			<p><g:if test="${fileInstance.isDir }">
					<td><a href="/dropbox${fileInstance.path.replaceAll(' ', '+')}">Open Folder</a></td>
				</g:if>
				<g:else>
					<td><a class="colorbox" href="/dropbox${fileInstance.path.replaceAll(' ', '+')}">Open</a></td>
				</g:else>
				<td><a href="/download?storeName=dropbox&fileResourceId=${fileInstance.id}">Download</a></td>
				<td><a href="#">Move</a></td>
				<td><a href="/delete?storeName=dropbox&fileResourceId=${fileInstance.id}&targetUri=${request.forwardURI}">Delete</a></td>
			</p>
		</div>
	</tr>
</g:each>