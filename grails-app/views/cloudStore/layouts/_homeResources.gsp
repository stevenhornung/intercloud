<script>
	$(document).ready(function() {
		$('#accordion1').accordionza({
			captionDelay: 100,
			captionEasing: 'easeOutBounce',
			captionHeight: 40,
			captionHeightClosed: 10,
			navKey: true
		});
	});
</script>

<ul id="accordion1">
	<g:each in="${homeResources}" status="i" var="cloudStore">
		<li class="${cloudStore.key}_slide">
			<div class="slide_handle">
				<g:if test="${cloudStore.key == 'dropbox' }">
					<img style="margin-top:4px;margin-left:4px" src="${resource(dir: 'images', file: 'dropbox.jpeg')}" width=30 height=30>
				</g:if>
				<g:elseif test="${cloudStore.key == 'googledrive' }">
					<img style="margin-top:4px;margin-left:4px" src="${resource(dir: 'images', file: 'googledrive.png')}" width=30 height=30>
				</g:elseif>
				<g:elseif test="${cloudStore.key == 'intercloud' }">
					<img style="margin-top:4px;margin-left:4px" src="${resource(dir: 'images', file: 'intercloud.jpeg')}" width=30 height=30>
				</g:elseif>
				<div></div>
			</div>
			<div class="slide_content">
				<a style="margin-top:4px;margin-left:4px;color:#fff" href="${cloudStore.key}">${cloudStore.key.capitalize()}</a>
				<g:if test="${cloudStore.key != 'intercloud' }">
						|	<g:remoteLink controller="cloudstore" action="update" update="${cloudStore.key}ResourceList" params="[storeName: cloudStore.key, targetDir: request.forwardURI, sync: true]" style="color:#fff">Sync</g:remoteLink>
				</g:if>
				<g:if test="${cloudStore.value }">
					<div style="border:solid;border-width:1px;margin-bottom:20px;border-radius:5px;padding:3px">
						<div style="margin-left:10px;display:inline-block;">
							Name
						</div>
						<div style="margin-left:40%;display:inline-block;">
							Kind
						</div>
						<div style="float:right;margin-right:30px;display:inline-block;">
							Modified
						</div>
					</div>
					<div id="${cloudStore.key}ResourceList">
						<g:render template="layouts/${cloudStore.key}Resources" model="[fileInstanceList: cloudStore.value, cloudStore: cloudStore.key]" />
					</div>
				</g:if>
			</div>
		</li>
	</g:each>
</ul>