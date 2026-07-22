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

@WebServlet(name = "RestaurantAnalyticsDashboardController", urlPatterns = {"/owner/restaurant-analytics-dashboard"})
public class RestaurantAnalyticsDashboardController extends HttpServlet {


    // Dashboard chỉ lấy top 5 món bán chạy nhất trong khoảng ngày đang lọc.
    private static final String VIEW = "/views/owner/restaurant-analytics-dashboard.jsp";
    private static final int TOP_DISH_LIMIT = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Chỉ Owner được xem dashboard này. Nếu chưa đăng nhập hoặc không phải Owner thì chuyển về login.
        if (!isOwner(request)) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        // Xác định khoảng ngày thống kê dựa trên filterType: today/week/month/year/custom.
        DateRange dateRange = resolveDateRange(request);

        // Gửi thông tin bộ lọc sang JSP để giữ trạng thái nút lọc và input ngày.
        request.setAttribute("filterType", dateRange.filterType);
        request.setAttribute("startDate", dateRange.startDate.toString());
        request.setAttribute("endDate", dateRange.endDate.toString());

        // Nếu người dùng nhập custom date sai, gửi message để JSP thông báo.
        if (dateRange.message != null) {
            request.setAttribute("dateMessage", dateRange.message);
        }

        // Hai DAO này lấy thống kê chính theo khoảng ngày: doanh thu, hóa đơn, đơn hàng.
        InvoicesDAO invoicesDAO = new InvoicesDAO();
        OrderDAO orderDAO = new OrderDAO();

        // DAO đang nhận ngày dạng String yyyy-MM-dd.
        String start = dateRange.startDate.toString();
        String end = dateRange.endDate.toString();

        // Tổng doanh thu từ các hóa đơn đã thanh toán trong khoảng ngày.
        request.setAttribute("totalRevenue", invoicesDAO.getPaidRevenueByDateRange(start, end));

        // Số hóa đơn đã thanh toán trong khoảng ngày.
        request.setAttribute("paidInvoices", invoicesDAO.countPaidInvoicesByDateRange(start, end));

        // Tổng số order hợp lệ, không tính order đã hủy.
        request.setAttribute("totalOrders", orderDAO.countOrdersByDateRange(start, end));

        // Số order đã hoàn tất.
        request.setAttribute("completedOrders", orderDAO.countCompletedOrdersByDateRange(start, end));

        // Các DAO này dùng connection kế thừa DBContext nên đặt trong try-with-resources để tự đóng.
        try (TableDAO tableDAO = new TableDAO();
             EmployeeDAO employeeDAO = new EmployeeDAO();
             ReviewDAO reviewDAO = new ReviewDAO();
             MenuItemDAO menuItemDAO = new MenuItemDAO()) {

            // Nếu một DAO mất kết nối database thì set dữ liệu rỗng để JSP không bị lỗi null.
            if (tableDAO.getConnection() == null || employeeDAO.getConnection() == null
                    || reviewDAO.getConnection() == null || menuItemDAO.getConnection() == null) {
                setEmptyDbStats(request);
            } else {
                // Tổng số bàn trong hệ thống.
                request.setAttribute("totalTables", tableDAO.countTables());

                // Số bàn đang hoạt động, tương đương isActive = 1.
                request.setAttribute("activeTables", tableDAO.countSearchTables(null, null, null, 1));

                // Số nhân viên phục vụ đang active.
                request.setAttribute("activeStaff", employeeDAO.countStaff(null, 1,
                        UserRole.RESTAURANT_STAFF.getRoleID()));

                // Lấy tổng review và số lượng review theo từng rating để tính điểm trung bình.
                int totalReviews = reviewDAO.countAllReviews();
                int[] ratingCounts = reviewDAO.countReviewsByRating();
                request.setAttribute("totalReviews", totalReviews);
                request.setAttribute("averageRating", calculateAverageRating(ratingCounts, totalReviews));

                // Lấy top món bán chạy theo số lượng trong các order completed của khoảng ngày.
                List<MenuItem> topDishes = menuItemDAO.getPerformanceDish("", 0, 0,
                        start, end, 0, TOP_DISH_LIMIT);
                request.setAttribute("topDishes", topDishes);
            }
        }

        // Sau khi chuẩn bị đủ dữ liệu, forward sang JSP để render dashboard.
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private boolean isOwner(HttpServletRequest request) {
        // Không tạo session mới; chỉ kiểm tra session hiện có.
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        // Employee được lưu trong session sau khi đăng nhập.
        Object employee = session.getAttribute("employee");

        // Chỉ role Restaurant Owner mới được truy cập dashboard phân tích.
        return employee instanceof Employee
                && ((Employee) employee).getRoleID() == UserRole.RESTAURANT_OWNER.getRoleID();
    }

    private DateRange resolveDateRange(HttpServletRequest request) {
        LocalDate today = LocalDate.now();

        // filterType quyết định khoảng ngày: today, week, month, year hoặc custom.
        String filterType = request.getParameter("filterType");
        String startRaw = request.getParameter("startDate");
        String endRaw = request.getParameter("endDate");

        // Nếu người dùng chưa chọn filter thì mặc định xem dữ liệu tháng hiện tại.
        if (filterType == null || filterType.isBlank()) {
            filterType = "month";
        }

        switch (filterType) {
            case "today":
                // Thống kê riêng ngày hôm nay.
                return new DateRange(today, today, filterType, null);
            case "week":
                // Thống kê từ thứ Hai của tuần hiện tại đến hôm nay.
                return new DateRange(today.with(DayOfWeek.MONDAY), today, filterType, null);
            case "year":
                // Thống kê từ ngày đầu năm đến hôm nay.
                return new DateRange(today.withDayOfYear(1), today, filterType, null);
            case "custom":
                // Thống kê theo khoảng ngày người dùng nhập.
                return parseCustomRange(startRaw, endRaw, today);
            case "month":
            default:
                // Thống kê từ ngày đầu tháng đến hôm nay.
                return new DateRange(today.withDayOfMonth(1), today, "month", null);
        }
    }

    private DateRange parseCustomRange(String startRaw, String endRaw, LocalDate today) {
        try {
            // Input date từ HTML date có dạng yyyy-MM-dd nên LocalDate.parse đọc trực tiếp được.
            LocalDate start = LocalDate.parse(startRaw);
            LocalDate end = LocalDate.parse(endRaw);

            // Nếu người dùng nhập ngược ngày bắt đầu và kết thúc thì tự đảo lại cho hợp lệ.
            if (start.isAfter(end)) {
                LocalDate temp = start;
                start = end;
                end = temp;
            }
            return new DateRange(start, end, "custom", null);
        } catch (Exception ex) {
            // Nếu custom date sai định dạng/rỗng thì quay về tháng hiện tại và gửi message lên JSP.
            return new DateRange(today.withDayOfMonth(1), today, "month",
                    "Khoảng ngày không hợp lệ, hệ thống đang hiển thị dữ liệu tháng này.");
        }
    }

    private double calculateAverageRating(int[] ratingCounts, int totalReviews) {
        // Không có review thì điểm trung bình là 0, tránh chia cho 0.
        if (ratingCounts == null || totalReviews <= 0) {
            return 0;
        }

        // Tổng điểm = 1*số review 1 sao + 2*số review 2 sao + ... + 5*số review 5 sao.
        int totalPoints = 0;
        for (int rating = 1; rating <= 5 && rating < ratingCounts.length; rating++) {
            totalPoints += rating * ratingCounts[rating];
        }

        // Ép sang double để kết quả có phần thập phân.
        return totalPoints / (double) totalReviews;
    }

    private void setEmptyDbStats(HttpServletRequest request) {
        // Set giá trị mặc định khi DB lỗi để giao diện vẫn render được.
        request.setAttribute("totalTables", 0);
        request.setAttribute("activeTables", 0);
        request.setAttribute("activeStaff", 0);
        request.setAttribute("totalReviews", 0);
        request.setAttribute("averageRating", 0);
        request.setAttribute("topDishes", Collections.emptyList());

        // Message này dùng để JSP hiển thị cảnh báo mất kết nối database.
        request.setAttribute("dashboardError", "Không thể tải một số thống kê do mất kết nối database.");
    }

    // Object nhỏ để gom startDate/endDate/filterType/message, giúp code doGet không phải truyền nhiều biến rời rạc.
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