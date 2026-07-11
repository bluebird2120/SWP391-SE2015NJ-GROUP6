package controller;

import dal.StaffTableDAO;
import dal.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import model.Employee;

@WebServlet(name = "StaffTableController", urlPatterns = {"/staff/tables"})
public class StaffTableController extends HttpServlet {

    private static final String VIEW = "/views/staff/my-tables.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Employee employee = getLoggedInEmployee(request);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        // [PHAN QUYEN PHUC VU] Staff chi nhin thay don va ban cua chinh minh.
        request.setAttribute("assignedTables",
                new StaffTableDAO().getTablesForEmployee(employee.getEmployeeID()));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Employee employee = getLoggedInEmployee(request);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        String message = "Thao tác không hợp lệ.";
        try {
            int orderID = Integer.parseInt(request.getParameter("orderID"));
            String action = request.getParameter("action");

            if ("cleaned".equals(action)) {
                // [PHAN QUYEN PHUC VU] DAO kiem tra order phai thuoc staff nay.
                message = new StaffTableDAO().markCleaningCompleted(
                        orderID, employee.getEmployeeID())
                        ? "clean_success" : "Không thể hoàn tất dọn bàn này.";
                        
            } else if ("checkin".equals(action) || "open_table".equals(action)) {
                
                // Khách đặt trước đến -> 'arrived' (chờ quét). Khách vãng lai -> 'occupied' luôn (vì đã quét rồi)
                String newStatus = "checkin".equals(action) ? "arrived" : "occupied";
                String sql = "UPDATE `Order` SET tableStatus = ? WHERE orderID = ?";
                
                try (Connection conn = new DBContext().getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                     
                    ps.setString(1, newStatus);
                    ps.setInt(2, orderID);
                    int rowsAffected = ps.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        if ("checkin".equals(action)) {
                            message = "checkin_success"; 
                        } else {
                            message = "✅ Đã mở bàn thành công! Khách hiện tại có thể xem Menu và gọi món.";
                        }
                    } else {
                        message = "Lỗi: Không tìm thấy đơn hàng để thao tác.";
                    }
                    
                } catch (Exception e) {
                    System.err.println("Lỗi khi mở bàn/checkin: " + e.getMessage());
                    message = "Lỗi hệ thống: Không thể kết nối cơ sở dữ liệu.";
                }
            }
        } catch (NumberFormatException e) {
            message = "Mã đơn không hợp lệ.";
        }

        request.getSession().setAttribute("staffTableMessage", message);
        response.sendRedirect(request.getContextPath() + "/staff/tables");
    }

    private Employee getLoggedInEmployee(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null
                : (Employee) session.getAttribute("employee");
    }
}