
<%@ page import="com.intercloud.Account" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'account.label', default: 'Account')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-account" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-account" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list account">
			
				<g:if test="${accountInstance?.userName}">
				<li class="fieldcontain">
					<span id="userName-label" class="property-label"><g:message code="account.userName.label" default="User Name" /></span>
					
						<span class="property-value" aria-labelledby="userName-label"><g:fieldValue bean="${accountInstance}" field="userName"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${accountInstance?.password}">
				<li class="fieldcontain">
					<span id="password-label" class="property-label"><g:message code="account.password.label" default="Password" /></span>
					
						<span class="property-value" aria-labelledby="password-label"><g:fieldValue bean="${accountInstance}" field="password"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${accountInstance?.email}">
				<li class="fieldcontain">
					<span id="email-label" class="property-label"><g:message code="account.email.label" default="Email" /></span>
					
						<span class="property-value" aria-labelledby="email-label"><g:fieldValue bean="${accountInstance}" field="email"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${accountInstance?.fullName}">
				<li class="fieldcontain">
					<span id="fullName-label" class="property-label"><g:message code="account.fullName.label" default="Full Name" /></span>
					
						<span class="property-value" aria-labelledby="fullName-label"><g:fieldValue bean="${accountInstance}" field="fullName"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${accountInstance?.fileResources}">
				<li class="fieldcontain">
					<span id="fileResources-label" class="property-label"><g:message code="account.fileResources.label" default="File Resources" /></span>
					
						<g:each in="${accountInstance.fileResources}" var="f">
						<span class="property-value" aria-labelledby="fileResources-label"><g:link controller="fileResource" action="show" id="${f.id}">${f?.encodeAsHTML()}</g:link></span>
						</g:each>
					
				</li>
				</g:if>
			
				<g:if test="${accountInstance?.cloudStores}">
				<li class="fieldcontain">
					<span id="cloudStores-label" class="property-label"><g:message code="account.cloudStores.label" default="Cloud Stores" /></span>
					
						<g:each in="${accountInstance.cloudStores}" var="c">
						<span class="property-value" aria-labelledby="cloudStores-label"><g:link controller="cloudStore" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></span>
						</g:each>
					
				</li>
				</g:if>
			
				<g:if test="${accountInstance?.spaceUsed}">
				<li class="fieldcontain">
					<span id="spaceUsed-label" class="property-label"><g:message code="account.spaceUsed.label" default="Space Used" /></span>
					
						<span class="property-value" aria-labelledby="spaceUsed-label"><g:fieldValue bean="${accountInstance}" field="spaceUsed"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${accountInstance?.totalSpace}">
				<li class="fieldcontain">
					<span id="totalSpace-label" class="property-label"><g:message code="account.totalSpace.label" default="Total Space" /></span>
					
						<span class="property-value" aria-labelledby="totalSpace-label"><g:fieldValue bean="${accountInstance}" field="totalSpace"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${accountInstance?.type}">
				<li class="fieldcontain">
					<span id="type-label" class="property-label"><g:message code="account.type.label" default="Type" /></span>
					
						<span class="property-value" aria-labelledby="type-label"><g:fieldValue bean="${accountInstance}" field="type"/></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${accountInstance?.id}" />
					<g:link class="edit" action="edit" id="${accountInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
