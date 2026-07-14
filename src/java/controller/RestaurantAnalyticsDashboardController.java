package controller;

import dal.EmployeeDAO;
import dal.InvoicesDAO;
import dal.MenuItemDAO;
import dal.OrderDAO;
import dal.ReviewDAO;
import dal.TableDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import model.Employee;
import model.MenuItem;
import util.UserRole;

@WebServlet(name = "RestaurantAnalyticsDashboardController", urlPatterns = {"/restaurant-analytics-dashboard"})
public class RestaurantAnalyticsDashboardController extends HttpServlet {

    private static final String VIEW = "/views/admin/restaurant-analytics-dashboard.jsp";
    private static final int TOP_DISH_LIMIT = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isOwner(request)) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        DateRange dateRange = resolveDateRange(request);
        request.setAttribute("filterType", dateRange.filterType);
        request.setAttribute("startDate", dateRange.startDate.toString());
        request.setAttribute("endDate", dateRange.endDate.toString());
        if (dateRange.message != null) {
            request.setAttribute("dateMessage", dateRange.message);
        }

        InvoicesDAO invoicesDAO = new InvoicesDAO();
        OrderDAO orderDAO = new OrderDAO();

        String start = dateRange.startDate.toString();
        String end = dateRange.endDate.toString();

        request.setAttribute("totalRevenue", invoicesDAO.getPaidRevenueByDateRange(start, end));
        request.setAttribute("paidInvoices", invoicesDAO.countPaidInvoicesByDateRange(start, end));
        request.setAttribute("totalOrders", orderDAO.countOrdersByDateRange(start, end));
        request.setAttribute("completedOrders", orderDAO.countCompletedOrdersByDateRange(start, end));

        try (TableDAO tableDAO = new TableDAO();
             EmployeeDAO employeeDAO = new EmployeeDAO();
             ReviewDAO reviewDAO = new ReviewDAO();
             MenuItemDAO menuItemDAO = new MenuItemDAO()) {

            if (tableDAO.getConnection() == null || employeeDAO.getConnection() == null
                    || reviewDAO.getConnection() == null || menuItemDAO.getConnection() == null) {
                setEmptyDbStats(request);
            } else {
                request.setAttribute("totalTables", tableDAO.countTables());
                request.setAttribute("activeTables", tableDAO.countSearchTables(null, null, null, 1));
                request.setAttribute("activeStaff", employeeDAO.countStaff(null, 1,
                        UserRole.RESTAURANT_STAFF.getRoleID()));

                int totalReviews = reviewDAO.countAllReviews();
                int[] ratingCounts = reviewDAO.countReviewsByRating();
                request.setAttribute("totalReviews", totalReviews);
                request.setAttribute("averageRating", calculateAverageRating(ratingCounts, totalReviews));

                List<MenuItem> topDishes = menuItemDAO.getPerformanceDish("", 0, 0,
                        start, end, 0, TOP_DISH_LIMIT);
                request.setAttribute("topDishes", topDishes);
            }
        }

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private boolean isOwner(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object employee = session.getAttribute("employee");
        return employee instanceof Employee
                && ((Employee) employee).getRoleID() == UserRole.RESTAURANT_OWNER.getRoleID();
    }

    private DateRange resolveDateRange(HttpServletRequest request) {
        LocalDate today = LocalDate.now();
        String filterType = request.getParameter("filterType");
        String startRaw = request.getParameter("startDate");
        String endRaw = request.getParameter("endDate");

        if (filterType == null || filterType.isBlank()) {
            filterType = "month";
        }

        switch (filterType) {
            case "today":
                return new DateRange(today, today, filterType, null);
            case "week":
                return new DateRange(today.with(DayOfWeek.MONDAY), today, filterType, null);
            case "year":
                return new DateRange(today.withDayOfYear(1), today, filterType, null);
            case "custom":
                return parseCustomRange(startRaw, endRaw, today);
            case "month":
            default:
                return new DateRange(today.withDayOfMonth(1), today, "month", null);
        }
    }

    private DateRange parseCustomRange(String startRaw, String endRaw, LocalDate today) {
        try {
            LocalDate start = LocalDate.parse(startRaw);
            LocalDate end = LocalDate.parse(endRaw);
            if (start.isAfter(end)) {
                LocalDate temp = start;
                start = end;
                end = temp;
            }
            return new DateRange(start, end, "custom", null);
        } catch (Exception ex) {
            return new DateRange(today.withDayOfMonth(1), today, "month",
                    "Khoảng ngày không hợp lệ, hệ thống đang hiển thị dữ liệu tháng này.");
        }
    }

    private double calculateAverageRating(int[] ratingCounts, int totalReviews) {
        if (ratingCounts == null || totalReviews <= 0) {
            return 0;
        }
        int totalPoints = 0;
        for (int rating = 1; rating <= 5 && rating < ratingCounts.length; rating++) {
            totalPoints += rating * ratingCounts[rating];
        }
        return totalPoints / (double) totalReviews;
    }

    private void setEmptyDbStats(HttpServletRequest request) {
        request.setAttribute("totalTables", 0);
        request.setAttribute("activeTables", 0);
        request.setAttribute("activeStaff", 0);
        request.setAttribute("totalReviews", 0);
        request.setAttribute("averageRating", 0);
        request.setAttribute("topDishes", Collections.emptyList());
        request.setAttribute("dashboardError", "Không thể tải một số thống kê do mất kết nối database.");
    }

    private static class DateRange {

        private final LocalDate startDate;
        private final LocalDate endDate;
        private final String filterType;
        private final String message;

        private DateRange(LocalDate startDate, LocalDate endDate, String filterType, String message) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.filterType = filterType;
            this.message = message;
        }
    }
}
