
<%@ page import="com.intercloud.Account" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'account.label', default: 'Account')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-account" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-account" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
					
						<g:sortableColumn property="userName" title="${message(code: 'account.userName.label', default: 'User Name')}" />
					
						<g:sortableColumn property="password" title="${message(code: 'account.password.label', default: 'Password')}" />
					
						<g:sortableColumn property="email" title="${message(code: 'account.email.label', default: 'Email')}" />
					
						<g:sortableColumn property="fullName" title="${message(code: 'account.fullName.label', default: 'Full Name')}" />
					
						<g:sortableColumn property="spaceUsed" title="${message(code: 'account.spaceUsed.label', default: 'Space Used')}" />
					
						<g:sortableColumn property="totalSpace" title="${message(code: 'account.totalSpace.label', default: 'Total Space')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${accountInstanceList}" status="i" var="accountInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${accountInstance.id}">${fieldValue(bean: accountInstance, field: "userName")}</g:link></td>
					
						<td>${fieldValue(bean: accountInstance, field: "password")}</td>
					
						<td>${fieldValue(bean: accountInstance, field: "email")}</td>
					
						<td>${fieldValue(bean: accountInstance, field: "fullName")}</td>
					
						<td>${fieldValue(bean: accountInstance, field: "spaceUsed")}</td>
					
						<td>${fieldValue(bean: accountInstance, field: "totalSpace")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${accountInstanceTotal}" />
			</div>
		</div>
	</body>
</html>
