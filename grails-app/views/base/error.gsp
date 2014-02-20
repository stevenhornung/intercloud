<!DOCTYPE html>
<html>
	<head>
		<title>Error</title>
		<meta name="layout" content="main">
	</head>
	<body>
		<ul class="errors">
			<li><g:if test="${flash.error }">
					${flash.error}
				</g:if>
			</li>
		</ul>
	</body>
</html>
