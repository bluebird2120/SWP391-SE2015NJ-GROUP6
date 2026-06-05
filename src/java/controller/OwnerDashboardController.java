package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Trang dashboard tổng quan cho Owner.
 * URL: /owner/dashboard (đã được AuthFilter bảo vệ).
 */
@WebServlet(name = "OwnerDashboardController", urlPatterns = {"/owner/dashboard"})
public class OwnerDashboardController extends HttpServlet {

    private static final String VIEW = "/views/owner/dashboard.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
