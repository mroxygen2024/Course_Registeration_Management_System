package com.example.servlet;

import com.example.model.Student;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import javax.servlet.RequestDispatcher;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentServlet extends HttpServlet {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String jdbcDriver;

    @Override
    public void init() throws ServletException {
        Map<String, String> fileEnv = loadDotEnv();

        dbUrl = firstNonEmpty(
                getEnvValue("DB_URL", fileEnv),
                buildPostgresUrl(fileEnv));

        dbUser = firstNonEmpty(
                getEnvValue("DB_USER", fileEnv),
                getEnvValue("PGUSER", fileEnv));

        dbPassword = firstNonEmpty(
                getEnvValue("DB_PASSWORD", fileEnv),
                getEnvValue("PGPASSWORD", fileEnv));

        jdbcDriver = firstNonEmpty(
                getEnvValue("DB_DRIVER", fileEnv),
                inferDriver(dbUrl));

        if (dbUrl == null || dbUser == null || dbPassword == null) {
            throw new ServletException(
                    "Database configuration missing. Set DB_URL/DB_USER/DB_PASSWORD or PGHOST/PGDATABASE/PGUSER/PGPASSWORD in .env or environment.");
        }
        if (jdbcDriver == null) {
            jdbcDriver = "org.postgresql.Driver";
        }

        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException e) {
            throw new ServletException("JDBC Driver not found", e);
        }
    }

    private Map<String, String> loadDotEnv() {
        Map<String, String> values = new HashMap<>();

        // Prefer catalina.base/.env (Tomcat) then project working dir/.env
        mergeDotenv(values, System.getProperty("catalina.base"));
        mergeDotenv(values, System.getProperty("user.dir"));
        return values;
    }

    private void mergeDotenv(Map<String, String> target, String directory) {
        if (directory == null) {
            return;
        }
        Dotenv dotenv = Dotenv.configure()
                .directory(directory)
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        for (DotenvEntry entry : dotenv.entries()) {
            target.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    private String getEnvValue(String key, Map<String, String> fileEnv) {
        String sys = System.getenv(key);
        if (sys != null && !sys.isEmpty()) {
            return sys;
        }
        String fromFile = fileEnv.get(key);
        if (fromFile != null && !fromFile.isEmpty()) {
            return fromFile;
        }
        return null;
    }

    private String buildPostgresUrl(Map<String, String> fileEnv) {
        String host = getEnvValue("PGHOST", fileEnv);
        String database = getEnvValue("PGDATABASE", fileEnv);
        if (host == null || database == null) {
            return null;
        }
        String port = firstNonEmpty(getEnvValue("PGPORT", fileEnv), "5432");
        String sslmode = firstNonEmpty(getEnvValue("PGSSLMODE", fileEnv), "require");
        return "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=" + sslmode;
    }

    private String inferDriver(String url) {
        if (url != null && url.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        }
        return null;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
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
