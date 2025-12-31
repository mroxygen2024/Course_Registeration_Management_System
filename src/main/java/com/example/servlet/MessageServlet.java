package com.example.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class MessageServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        // Do not close the writer on include; container manages it.
        out.println("<div style=\"border-bottom:1px solid #ccc; padding:8px 0; margin-bottom:12px;\">");
        out.println("<strong>Student Management Portal</strong> - " + LocalDateTime.now());
        out.println("</div>");
    }
}
