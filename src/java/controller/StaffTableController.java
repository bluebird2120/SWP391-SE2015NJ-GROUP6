package controller;

import dal.StaffTableDAO;
import dal.DBContext; // Thêm dòng này để gọi được DBContext
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

    private static final String VIEW = "/views/staff/table-dashboard.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Employee employee = getLoggedInEmployee(request);
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/login?type=employee");
            return;
        }

        StaffTableDAO dao = new StaffTableDAO();
        request.setAttribute("physicalTables", dao.getPhysicalTables());
        request.setAttribute("tableSummary", dao.getSummaryByTableType());
        request.setAttribute("reservationRequirements",
                dao.getReservationsWaitingForTables());
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

        StaffTableDAO dao = new StaffTableDAO();
        String message;
        
        try {
            int orderID = Integer.parseInt(request.getParameter("orderID"));
            String action = request.getParameter("action");
            
            if ("assign".equals(action)) {
                int tableID = Integer.parseInt(request.getParameter("tableID"));
                String error = dao.assignTable(orderID, tableID, employee.getEmployeeID());
                message = error == null ? "assign_success" : error;
                
            } else if ("cleaned".equals(action)) {
                // [STAFF TABLE] Chi xu ly don da cleaning, khong sua thanh toan.
                message = dao.markCleaningCompleted(orderID)
                        ? "clean_success" : "Không thể hoàn tất dọn bàn.";
                        
            } else if ("checkin".equals(action) || "open_table".equals(action)) {
                
                // === THÊM MỚI: XỬ LÝ KHÁCH ĐẶT TRƯỚC ĐẾN VÀ KHÁCH VÃNG LAI MỞ BÀN ===
                // Chuyển trạng thái Order thành 'occupied' (Đang dùng bữa)
                String sql = "UPDATE `Order` SET tableStatus = 'occupied' WHERE orderID = ?";
                
                try (Connection conn = new DBContext().getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                     
                    ps.setInt(1, orderID);
                    int rowsAffected = ps.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        if ("checkin".equals(action)) {
                            // Sẽ được JSP dịch ra thành: "✅ Đã xác nhận khách đến nhận bàn..."
                            message = "checkin_success"; 
                        } else {
                            // Trả thẳng message vì JSP dùng thẻ <c:otherwise> để in text tự do
                            message = "✅ Đã mở bàn thành công! Khách hiện tại có thể xem Menu và gọi món.";
                        }
                    } else {
                        message = "Lỗi: Không tìm thấy đơn hàng để mở bàn.";
                    }
                    
                } catch (Exception e) {
                    System.err.println("Lỗi khi mở bàn/checkin: " + e.getMessage());
                    message = "Lỗi hệ thống: Không thể kết nối cơ sở dữ liệu.";
                }
                
            } else {
                message = "Thao tác không hợp lệ.";
            }
            
        } catch (NumberFormatException e) {
            message = "Mã đơn hoặc mã bàn không hợp lệ.";
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