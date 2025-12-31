package com.example.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String studentId = request.getParameter("studentId");
        String studentName = request.getParameter("studentName");
        String courseCode = request.getParameter("courseCode");

        HttpSession session = request.getSession(true);
        session.setAttribute("studentName", studentName);

        request.setAttribute("studentId", studentId);
        request.setAttribute("courseCode", courseCode);

        RequestDispatcher dispatcher = request.getRequestDispatcher("confirmation");
        dispatcher.forward(request, response);
    }
}
