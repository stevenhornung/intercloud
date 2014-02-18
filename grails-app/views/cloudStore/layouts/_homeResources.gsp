<g:each in="${cloudStore.value}" status = "i" var="fileInstance">
   <tr>
		<h3>${fileInstance.fileName }</h3>
		<div>
			<p>
				<g:if test="${fileInstance.isDir }">
					<td><a href="/${cloudStore.key}${fileInstance.path.replaceAll(' ', '+')}">Open Folder</a></td>
				</g:if>
				<g:else>
					<td><a class="colorbox" href="/${cloudStore.key}${fileInstance.path.replaceAll(' ', '+')}">Open</a></td>
				</g:else>
				<td><a href="/download?storeName=${cloudStore.key}&fileResourceId=${fileInstance.id}">Download</a></td>
				<td><a href="#">Move</a></td>
				<td><a href="/delete?storeName=${cloudStore.key}&fileResourceId=${fileInstance.id}">Delete</a></td>
			</p>
		</div>
	</tr>
</g:each>