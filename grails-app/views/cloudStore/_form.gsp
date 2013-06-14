<%@ page import="com.intercloud.CloudStore" %>



<div class="fieldcontain ${hasErrors(bean: cloudStoreInstance, field: 'storeName', 'error')} required">
	<label for="storeName">
		<g:message code="cloudStore.storeName.label" default="Store Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="storeName" required="" value="${cloudStoreInstance?.storeName}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: cloudStoreInstance, field: 'fileResources', 'error')} ">
	<label for="fileResources">
		<g:message code="cloudStore.fileResources.label" default="File Resources" />
		
	</label>
	<g:select name="fileResources" from="${com.intercloud.FileResource.list()}" multiple="multiple" optionKey="id" size="5" value="${cloudStoreInstance?.fileResources*.id}" class="many-to-many"/>
</div>

<div class="fieldcontain ${hasErrors(bean: cloudStoreInstance, field: 'fullName', 'error')} ">
	<label for="fullName">
		<g:message code="cloudStore.fullName.label" default="Full Name" />
		
	</label>
	<g:textField name="fullName" value="${cloudStoreInstance?.fullName}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: cloudStoreInstance, field: 'spaceUsed', 'error')} required">
	<label for="spaceUsed">
		<g:message code="cloudStore.spaceUsed.label" default="Space Used" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="spaceUsed" type="number" value="${cloudStoreInstance.spaceUsed}" required=""/>
</div>

<div class="fieldcontain ${hasErrors(bean: cloudStoreInstance, field: 'totalSpace', 'error')} required">
	<label for="totalSpace">
		<g:message code="cloudStore.totalSpace.label" default="Total Space" />
		<span class="required-indicator">*</span>
	</label>
	<g:field name="totalSpace" type="number" value="${cloudStoreInstance.totalSpace}" required=""/>
</div>

<div class="fieldcontain ${hasErrors(bean: cloudStoreInstance, field: 'uid', 'error')} ">
	<label for="uid">
		<g:message code="cloudStore.uid.label" default="Uid" />
		
	</label>
	<g:textField name="uid" value="${cloudStoreInstance?.uid}"/>
</div>

