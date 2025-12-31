package com.example.servlet;

import com.example.model.Student;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentServlet extends HttpServlet {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String jdbcDriver;

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        dbUrl = context.getInitParameter("dbUrl");
        dbUser = context.getInitParameter("dbUser");
        dbPassword = context.getInitParameter("dbPassword");
        jdbcDriver = context.getInitParameter("jdbcDriver");

        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new ServletException("JDBC Driver not found", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String action = request.getParameter("action");
        String message;
        List<Student> students = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            if (action == null) {
                message = "No action specified.";
            } else {
                switch (action) {
                    case "create":
                        message = createStudent(request, connection);
                        break;
                    case "read":
                        students = readStudents(request, connection);
                        message = students.isEmpty() ? "No student records found." : "Student records loaded.";
                        break;
                    case "update":
                        message = updateStudent(request, connection);
                        break;
                    case "delete":
                        message = deleteStudent(request, connection);
                        break;
                    default:
                        message = "Unsupported action.";
                        break;
                }
            }
        } catch (SQLException e) {
            message = "Database error: " + e.getMessage();
        } catch (NumberFormatException e) {
            message = "Invalid number format: " + e.getMessage();
        }

        request.setAttribute("students", students);
        request.setAttribute("message", message);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/studentResult.jsp");
        dispatcher.forward(request, response);
    }

    private String createStudent(HttpServletRequest request, Connection connection) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String department = request.getParameter("department");

        String sql = "INSERT INTO student (id, name, department) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, name);
            stmt.setString(3, department);
            int rows = stmt.executeUpdate();
            return rows > 0 ? "Student created successfully." : "Student creation failed.";
        }
    }

    private List<Student> readStudents(HttpServletRequest request, Connection connection) throws SQLException {
        List<Student> results = new ArrayList<>();
        String idParam = request.getParameter("id");

        if (idParam != null && !idParam.isEmpty()) {
            String sql = "SELECT id, name, department FROM student WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(idParam));
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(new Student(rs.getInt("id"), rs.getString("name"), rs.getString("department")));
                    }
                }
            }
        } else {
            String sql = "SELECT id, name, department FROM student";
            try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Student(rs.getInt("id"), rs.getString("name"), rs.getString("department")));
                }
            }
        }
        return results;
    }

    private String updateStudent(HttpServletRequest request, Connection connection) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String department = request.getParameter("department");

        String sql = "UPDATE student SET name = ?, department = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, department);
            stmt.setInt(3, id);
            int rows = stmt.executeUpdate();
            return rows > 0 ? "Student updated successfully." : "Student update failed.";
        }
    }

    private String deleteStudent(HttpServletRequest request, Connection connection) throws SQLException {
        int id = Integer.parseInt(request.getParameter("id"));
        String sql = "DELETE FROM student WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0 ? "Student deleted successfully." : "Student deletion failed.";
        }
    }
}
