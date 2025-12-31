package com.example.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;

public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", username);
            request.setAttribute("message", "Login successful. You may manage students now.");
        } else {
            request.setAttribute("message", "Invalid credentials. Please try again.");
        }
        request.setAttribute("students", Collections.emptyList());

        RequestDispatcher dispatcher = request.getRequestDispatcher("/studentResult.jsp");
        dispatcher.forward(request, response);
    }
}
