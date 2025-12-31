package com.example.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class ConfirmationServlet extends HttpServlet {
    private String universityName;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = config.getServletContext();
        universityName = context.getInitParameter("universityName");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String studentName = session != null ? (String) session.getAttribute("studentName") : "";

        String studentId = (String) request.getAttribute("studentId");
        String courseCode = (String) request.getAttribute("courseCode");

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"en\">");
            out.println("<head><meta charset=\"UTF-8\"><title>Registration Confirmation</title></head>");
            out.println("<body>");
            out.println("<h1>Registration Successful</h1>");
            out.println("<p><strong>University:</strong> " + universityName + "</p>");
            out.println("<p><strong>Student ID:</strong> " + studentId + "</p>");
            out.println("<p><strong>Student Name:</strong> " + studentName + "</p>");
            out.println("<p><strong>Course Code:</strong> " + courseCode + "</p>");
            out.println("<p>Your registration has been recorded successfully.</p>");
            out.println("</body></html>");
        }
    }
}
