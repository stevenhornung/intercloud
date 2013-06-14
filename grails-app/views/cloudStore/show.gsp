
<%@ page import="com.intercloud.CloudStore" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'cloudStore.label', default: 'CloudStore')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-cloudStore" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-cloudStore" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list cloudStore">
			
				<g:if test="${cloudStoreInstance?.storeName}">
				<li class="fieldcontain">
					<span id="storeName-label" class="property-label"><g:message code="cloudStore.storeName.label" default="Store Name" /></span>
					
						<span class="property-value" aria-labelledby="storeName-label"><g:fieldValue bean="${cloudStoreInstance}" field="storeName"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${cloudStoreInstance?.fileResources}">
				<li class="fieldcontain">
					<span id="fileResources-label" class="property-label"><g:message code="cloudStore.fileResources.label" default="File Resources" /></span>
					
						<g:each in="${cloudStoreInstance.fileResources}" var="f">
						<span class="property-value" aria-labelledby="fileResources-label"><g:link controller="fileResource" action="show" id="${f.id}">${f?.encodeAsHTML()}</g:link></span>
						</g:each>
					
				</li>
				</g:if>
			
				<g:if test="${cloudStoreInstance?.fullName}">
				<li class="fieldcontain">
					<span id="fullName-label" class="property-label"><g:message code="cloudStore.fullName.label" default="Full Name" /></span>
					
						<span class="property-value" aria-labelledby="fullName-label"><g:fieldValue bean="${cloudStoreInstance}" field="fullName"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${cloudStoreInstance?.spaceUsed}">
				<li class="fieldcontain">
					<span id="spaceUsed-label" class="property-label"><g:message code="cloudStore.spaceUsed.label" default="Space Used" /></span>
					
						<span class="property-value" aria-labelledby="spaceUsed-label"><g:fieldValue bean="${cloudStoreInstance}" field="spaceUsed"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${cloudStoreInstance?.totalSpace}">
				<li class="fieldcontain">
					<span id="totalSpace-label" class="property-label"><g:message code="cloudStore.totalSpace.label" default="Total Space" /></span>
					
						<span class="property-value" aria-labelledby="totalSpace-label"><g:fieldValue bean="${cloudStoreInstance}" field="totalSpace"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${cloudStoreInstance?.uid}">
				<li class="fieldcontain">
					<span id="uid-label" class="property-label"><g:message code="cloudStore.uid.label" default="Uid" /></span>
					
						<span class="property-value" aria-labelledby="uid-label"><g:fieldValue bean="${cloudStoreInstance}" field="uid"/></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${cloudStoreInstance?.id}" />
					<g:link class="edit" action="edit" id="${cloudStoreInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
