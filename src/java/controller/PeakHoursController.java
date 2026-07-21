package controller;

import dal.PeakHoursDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import model.HourStat;

@WebServlet(name = "PeakHoursController", urlPatterns = {"/owner/peak-hours-analysis"})
public class PeakHoursController extends HttpServlet {

    private final PeakHoursDAO dao = new PeakHoursDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String filterType = request.getParameter("filterType");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        if (filterType == null || filterType.isBlank()) {
            filterType = "today";
        }

        // Validate date range
        if ("custom".equals(filterType)) {
            if (startDate != null && endDate != null
                    && !startDate.isBlank() && !endDate.isBlank()
                    && startDate.compareTo(endDate) > 0) {
                request.setAttribute("errorMsg",
                        "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
                filterType = "today";
                startDate = null;
                endDate = null;
            }
        }

        try {
            List<HourStat> hourStats = dao.getOrderCountByHour(
                    filterType, startDate, endDate);
            int totalOrders = dao.getTotalOrders(filterType, startDate, endDate);

            // Tính các chỉ số
            int peakHour = -1;
            int peakCount = 0;
            int lowHour = -1;
            int lowCount = Integer.MAX_VALUE;
            int activeHours = 0;

            for (HourStat s : hourStats) {
                if (s.getOrderCount() > peakCount) {
                    peakCount = s.getOrderCount();
                    peakHour = s.getHour();
                }
                if (s.getOrderCount() < lowCount) {
                    lowCount = s.getOrderCount();
                    lowHour = s.getHour();
                }
                if (s.getOrderCount() > 0) {
                    activeHours++;
                }
            }

            int avgPerHour; // Khai báo biến trước

            if (activeHours > 0) {
                // Nếu có giờ hoạt động thì làm phép chia
                avgPerHour = totalOrders / activeHours; 
            } else {
                // Nếu không có giờ hoạt động thì gán bằng 0
                avgPerHour = 0; 
            }

            request.setAttribute("hourStats", hourStats);
            request.setAttribute("totalOrders", totalOrders);
            request.setAttribute("peakHour", peakHour);
            request.setAttribute("peakCount", peakCount);
            request.setAttribute("lowHour", lowHour);
            request.setAttribute("avgPerHour", avgPerHour);
            request.setAttribute("filterType", filterType);
            request.setAttribute("startDate", startDate != null ? startDate : "");
            request.setAttribute("endDate", endDate != null ? endDate : "");

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMsg", "Lỗi hệ thống, vui lòng thử lại.");
        }

        request.getRequestDispatcher("/views/owner/peak-hours-analysis.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}