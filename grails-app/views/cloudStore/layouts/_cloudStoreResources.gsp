<g:each in="${fileInstanceList}" status = "i" var="fileInstance">
   <tr>
		<h3>${fileInstance.fileName }</h3>
		<div>
			<p>
				<g:if test="${fileInstance.isDir }">
					<td><a href="/${cloudStore}${fileInstance.path.replaceAll(' ', '+')}">Open Folder</a></td>
				</g:if>
				<g:else>
					<td><a class="colorbox" href="/${cloudStore}${fileInstance.path.replaceAll(' ', '+')}">Open</a></td>
				</g:else>
				<td><a href="/download?storeName=${cloudStore}&fileResourceId=${fileInstance.id}">Download</a></td>
				<td><a href="/delete?storeName=${cloudStore}&fileResourceId=${fileInstance.id}&targetUri=${request.forwardURI}">Delete</a></td>
			</p>
		</div>
	</tr>
</g:each>