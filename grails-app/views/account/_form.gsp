<%@ page import="com.intercloud.Account" %>



<div class="fieldcontain ${hasErrors(bean: accountInstance, field: 'userName', 'error')} required">
	<label for="userName">
		<g:message code="account.userName.label" default="User Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="userName" required="" value="${accountInstance?.userName}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: accountInstance, field: 'password', 'error')} required">
	<label for="password">
		<g:message code="account.password.label" default="Password" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="password" required="" value="${accountInstance?.password}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: accountInstance, field: 'email', 'error')} required">
	<label for="email">
		<g:message code="account.email.label" default="Email" />
		<span class="required-indicator">*</span>
	</label>
	<g:field type="email" name="email" required="" value="${accountInstance?.email}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: accountInstance, field: 'fullName', 'error')} required">
	<label for="fullName">
		<g:message code="account.fullName.label" default="Full Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="fullName" required="" value="${accountInstance?.fullName}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: accountInstance, field: 'fileResources', 'error')} ">
	<label for="fileResources">
		<g:message code="account.fileResources.label" default="File Resources" />
		
	</label>
	<g:select name="fileResources" from="${com.intercloud.FileResource.list()}" multiple="multiple" optionKey="id" size="5" value="${accountInstance?.fileResources*.id}" class="many-to-many"/>
</div>

<div class="fieldcontain ${hasErrors(bean: accountInstance, field: 'cloudStores', 'error')} ">
	<label for="cloudStores">
		<g:message code="account.cloudStores.label" default="Cloud Stores" />
		
	</label>
	<g:select name="cloudStores" from="${com.intercloud.CloudStore.list()}" multiple="multiple" optionKey="id" size="5" value="${accountInstance?.cloudStores*.id}" class="many-to-many"/>
</div>

<div class="fieldcontain ${hasErrors(bean: accountInstance, field: 'spaceUsed', 'error')} required">
	<label for="spaceUsed">
		<g:message code="account.spaceUsed.label" default="Space Used" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="spaceUsed" type="number" value="${accountInstance.spaceUsed}" required=""/>
</div>

<div class="fieldcontain ${hasErrors(bean: accountInstance, field: 'totalSpace', 'error')} required">
	<label for="totalSpace">
		<g:message code="account.totalSpace.label" default="Total Space" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="totalSpace" type="number" value="${accountInstance.totalSpace}" required=""/>
</div>

<div class="fieldcontain ${hasErrors(bean: accountInstance, field: 'type', 'error')} ">
	<label for="type">
		<g:message code="account.type.label" default="Type" />
		
	</label>
	<g:textField name="type" value="${accountInstance?.type}"/>
</div>

