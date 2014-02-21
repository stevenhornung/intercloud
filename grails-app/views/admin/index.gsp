<!DOCTYPE html>
<html>
	<head>
		<title>Administration - intercloud</title>
		<meta name="layout" content="main">
	</head>
	<body>
		<sec:access expression="hasRole('ROLE_ADMIN')">
			<g:each in="${accountList }" var="account">
				<p>${account.email }</p>
			</g:each>
		</sec:access>
	</body>
</html>