<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<html>
<head>
    <title>Add Employee Form</title>
</head>

<body>
<h2><spring:message code="lbl.page" text="Add New Employee" /></h2>
<br/>
<form:form method="post" modelAttribute="employee">
    <table>
        <tr>
            <td><spring:message code="lbl.firstName" text="First Name" /></td>
            <td><form:input path="firstName" /></td>
        </tr>
        <tr>
            <td><spring:message code="lbl.lastName" text="Last Name" /></td>
            <td><form:input path="lastName" /></td>
        </tr>
        <tr>
            <td><spring:message code="lbl.email" text="Email Id" /></td>
            <td><form:input path="email" /></td>
        </tr>

        <!-- DROPDOWN code -->

        <tr>
            <td><spring:message code="lbl.department" text="Department" /></td>
            <td><form:select path="department" items="${allDepartments}" itemValue="id" itemLabel="name" /></td>
            <td><form:errors path="department" cssClass="error" /></td>
        </tr>


        <tr>
            <td colspan="3"><input type="submit" value="Add Employee"/></td>
        </tr>
    </table>
</form:form>
</body>
</html>