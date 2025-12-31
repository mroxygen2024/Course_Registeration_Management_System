<%@ page import="java.util.List" %>
<%@ page import="com.example.model.Student" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Student Management</title>
</head>
<body>
<jsp:include page="message" />
<h1>Student Management Results</h1>
<p><strong>Status:</strong> ${message}</p>

<h2>Create or Update Student</h2>
<form action="student" method="post">
    <input type="hidden" name="action" value="create">
    <label for="id">ID:</label>
    <input type="number" id="id" name="id" required>
    <label for="name">Name:</label>
    <input type="text" id="name" name="name" required>
    <label for="department">Department:</label>
    <input type="text" id="department" name="department" required>
    <button type="submit">Create</button>
</form>

<form action="student" method="post" style="margin-top:12px;">
    <input type="hidden" name="action" value="update">
    <label for="uid">ID:</label>
    <input type="number" id="uid" name="id" required>
    <label for="uname">Name:</label>
    <input type="text" id="uname" name="name" required>
    <label for="udepartment">Department:</label>
    <input type="text" id="udepartment" name="department" required>
    <button type="submit">Update</button>
</form>

<h2>Read Students</h2>
<form action="student" method="post">
    <input type="hidden" name="action" value="read">
    <label for="rid">ID (optional for single record):</label>
    <input type="number" id="rid" name="id">
    <button type="submit">Fetch</button>
</form>

<h2>Delete Student</h2>
<form action="student" method="post">
    <input type="hidden" name="action" value="delete">
    <label for="did">ID:</label>
    <input type="number" id="did" name="id" required>
    <button type="submit">Delete</button>
</form>

<h2>Students</h2>
<table border="1" cellpadding="6" cellspacing="0">
    <tr><th>ID</th><th>Name</th><th>Department</th></tr>
    <%
        List<Student> students = (List<Student>) request.getAttribute("students");
        if (students != null) {
            for (Student student : students) {
    %>
    <tr>
        <td><%= student.getId() %></td>
        <td><%= student.getName() %></td>
        <td><%= student.getDepartment() %></td>
    </tr>
    <%
            }
        }
    %>
</table>
</body>
</html>
