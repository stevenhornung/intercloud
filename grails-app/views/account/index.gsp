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
			InterCloud Space Used: ${ (float)accountInstance.spaceUsed / (float)(1024*1024*1024)} gb<br>
			InterCloud Total Space: ${accountInstance.totalSpace / (1024*1024*1024) } gb
		</sec:access>
	</body>
</html>