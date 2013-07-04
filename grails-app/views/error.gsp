<!DOCTYPE html>
<html>
	<head>
		<title>Error</title>
		<meta name="layout" content="main">
	</head>
	<body>
		<ul class="errors">
			<li><g:if test="${flash.message }">
					${flash.message}
				</g:if>
			</li>
		</ul>
	</body>
</html>
