
<%@ page import="com.intercloud.CloudStore" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'cloudStore.label', default: 'CloudStore')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-cloudStore" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-cloudStore" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
					
						<g:sortableColumn property="storeName" title="${message(code: 'cloudStore.storeName.label', default: 'Store Name')}" />
					
						<g:sortableColumn property="fullName" title="${message(code: 'cloudStore.fullName.label', default: 'Full Name')}" />
					
						<g:sortableColumn property="spaceUsed" title="${message(code: 'cloudStore.spaceUsed.label', default: 'Space Used')}" />
					
						<g:sortableColumn property="totalSpace" title="${message(code: 'cloudStore.totalSpace.label', default: 'Total Space')}" />
					
						<g:sortableColumn property="uid" title="${message(code: 'cloudStore.uid.label', default: 'Uid')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${cloudStoreInstanceList}" status="i" var="cloudStoreInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${cloudStoreInstance.id}">${fieldValue(bean: cloudStoreInstance, field: "storeName")}</g:link></td>
					
						<td>${fieldValue(bean: cloudStoreInstance, field: "fullName")}</td>
					
						<td>${fieldValue(bean: cloudStoreInstance, field: "spaceUsed")}</td>
					
						<td>${fieldValue(bean: cloudStoreInstance, field: "totalSpace")}</td>
					
						<td>${fieldValue(bean: cloudStoreInstance, field: "uid")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${cloudStoreInstanceTotal}" />
			</div>
		</div>
	</body>
</html>
