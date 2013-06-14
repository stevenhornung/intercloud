<%@ page import="com.intercloud.FileResource" %>



<div class="fieldcontain ${hasErrors(bean: fileResourceInstance, field: 'bytes', 'error')} ">
	<label for="bytes">
		<g:message code="fileResource.bytes.label" default="Bytes" />
		
	</label>
	<input type="file" id="bytes" name="bytes" />
</div>

<div class="fieldcontain ${hasErrors(bean: fileResourceInstance, field: 'mimeType', 'error')} ">
	<label for="mimeType">
		<g:message code="fileResource.mimeType.label" default="Mime Type" />
		
	</label>
	<g:textField name="mimeType" value="${fileResourceInstance?.mimeType}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: fileResourceInstance, field: 'byteSize', 'error')} ">
	<label for="byteSize">
		<g:message code="fileResource.byteSize.label" default="Byte Size" />
		
	</label>
	<g:textField name="byteSize" value="${fileResourceInstance?.byteSize}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: fileResourceInstance, field: 'isDir', 'error')} ">
	<label for="isDir">
		<g:message code="fileResource.isDir.label" default="Is Dir" />
		
	</label>
	<g:checkBox name="isDir" value="${fileResourceInstance?.isDir}" />
</div>

<div class="fieldcontain ${hasErrors(bean: fileResourceInstance, field: 'modified', 'error')} ">
	<label for="modified">
		<g:message code="fileResource.modified.label" default="Modified" />
		
	</label>
	<g:textField name="modified" value="${fileResourceInstance?.modified}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: fileResourceInstance, field: 'path', 'error')} ">
	<label for="path">
		<g:message code="fileResource.path.label" default="Path" />
		
	</label>
	<g:textField name="path" value="${fileResourceInstance?.path}"/>
</div>

