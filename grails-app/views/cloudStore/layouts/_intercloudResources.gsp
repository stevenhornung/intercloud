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
				<td><a href="/download?storeName=intercloud&fileResourceId=${fileInstance.id}">Download</a></td>
				<td><a href="#">Move</a></td>
				<td><a href="/delete?storeName=intercloud&fileResourceId=${fileInstance.id}&targetUri=${request.forwardURI}">Delete</a></td>
			</p>
		</div>
	</tr>
</g:each>