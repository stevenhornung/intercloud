
<%@ page import="com.intercloud.FileResource" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'fileResource.label', default: 'FileResource')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-fileResource" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-fileResource" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
					
						<g:sortableColumn property="bytes" title="${message(code: 'fileResource.bytes.label', default: 'Bytes')}" />
					
						<g:sortableColumn property="mimeType" title="${message(code: 'fileResource.mimeType.label', default: 'Mime Type')}" />
					
						<g:sortableColumn property="byteSize" title="${message(code: 'fileResource.byteSize.label', default: 'Byte Size')}" />
					
						<g:sortableColumn property="isDir" title="${message(code: 'fileResource.isDir.label', default: 'Is Dir')}" />
					
						<g:sortableColumn property="modified" title="${message(code: 'fileResource.modified.label', default: 'Modified')}" />
					
						<g:sortableColumn property="path" title="${message(code: 'fileResource.path.label', default: 'Path')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${fileResourceInstanceList}" status="i" var="fileResourceInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${fileResourceInstance.id}">${fieldValue(bean: fileResourceInstance, field: "bytes")}</g:link></td>
					
						<td>${fieldValue(bean: fileResourceInstance, field: "mimeType")}</td>
					
						<td>${fieldValue(bean: fileResourceInstance, field: "byteSize")}</td>
					
						<td><g:formatBoolean boolean="${fileResourceInstance.isDir}" /></td>
					
						<td>${fieldValue(bean: fileResourceInstance, field: "modified")}</td>
					
						<td>${fieldValue(bean: fileResourceInstance, field: "path")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${fileResourceInstanceTotal}" />
			</div>
		</div>
	</body>
</html>
