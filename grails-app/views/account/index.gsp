<!DOCTYPE html>
<html>
	<head>
		<title>Account - InterCloud</title>
		<meta name="layout" content="main">
	</head>
	<body>
		<sec:access expression="hasRole('ROLE_USER')">
			Email: ${accountInstance.email }<br>
			Name: ${accountInstance.fullName }<br>
			Account Type: ${accountInstance.type }<br>
			InterCloud Space Used: ${accountInstance.spaceUsed }<br>
			InterCloud Total Space: ${accountInstance.totalSpace }
		</sec:access>
	</body>
</html>