
<%@ page import="com.intercloud.FileResource" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'fileResource.label', default: 'FileResource')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-fileResource" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-fileResource" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list fileResource">
			
				<g:if test="${fileResourceInstance?.bytes}">
				<li class="fieldcontain">
					<span id="bytes-label" class="property-label"><g:message code="fileResource.bytes.label" default="Bytes" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${fileResourceInstance?.mimeType}">
				<li class="fieldcontain">
					<span id="mimeType-label" class="property-label"><g:message code="fileResource.mimeType.label" default="Mime Type" /></span>
					
						<span class="property-value" aria-labelledby="mimeType-label"><g:fieldValue bean="${fileResourceInstance}" field="mimeType"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${fileResourceInstance?.byteSize}">
				<li class="fieldcontain">
					<span id="byteSize-label" class="property-label"><g:message code="fileResource.byteSize.label" default="Byte Size" /></span>
					
						<span class="property-value" aria-labelledby="byteSize-label"><g:fieldValue bean="${fileResourceInstance}" field="byteSize"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${fileResourceInstance?.isDir}">
				<li class="fieldcontain">
					<span id="isDir-label" class="property-label"><g:message code="fileResource.isDir.label" default="Is Dir" /></span>
					
						<span class="property-value" aria-labelledby="isDir-label"><g:formatBoolean boolean="${fileResourceInstance?.isDir}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${fileResourceInstance?.modified}">
				<li class="fieldcontain">
					<span id="modified-label" class="property-label"><g:message code="fileResource.modified.label" default="Modified" /></span>
					
						<span class="property-value" aria-labelledby="modified-label"><g:fieldValue bean="${fileResourceInstance}" field="modified"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${fileResourceInstance?.path}">
				<li class="fieldcontain">
					<span id="path-label" class="property-label"><g:message code="fileResource.path.label" default="Path" /></span>
					
						<span class="property-value" aria-labelledby="path-label"><g:fieldValue bean="${fileResourceInstance}" field="path"/></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${fileResourceInstance?.id}" />
					<g:link class="edit" action="edit" id="${fileResourceInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
