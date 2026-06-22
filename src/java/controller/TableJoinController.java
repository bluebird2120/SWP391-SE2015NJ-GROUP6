package controller;

import dal.TableJoinRequestDAO;
import model.TableJoinRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "TableJoinController", urlPatterns = {"/api/table-join"})
public class TableJoinController extends HttpServlet {

    private final TableJoinRequestDAO requestDAO = new TableJoinRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // GET dùng để điện thoại tự động hỏi Server liên tục (Polling)
        String action = request.getParameter("action");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        if ("checkStatus".equals(action)) {
            // GUEST HỎI: "Chủ bàn đã duyệt cho tôi chưa?"
            Integer orderID = (Integer) session.getAttribute("pendingOrderID");
            String sessionID = session.getId();

            if (orderID != null) {
                String status = requestDAO.checkRequestStatus(sessionID, orderID);
                if ("approved".equals(status)) {
                    // Nếu được duyệt -> Cấp quyền gọi món chính thức
                    session.setAttribute("orderID", orderID);
                    session.setAttribute("roleInTable", "GUEST");
                }
                out.print("{\"status\": \"" + (status != null ? status : "unknown") + "\"}");
            } else {
                out.print("{\"status\": \"error\"}");
            }

        } else if ("checkStaffApproval".equals(action)) {
            // MÀN HÌNH CHỜ HỎI: "Nhân viên đã duyệt mở bàn chưa?"
            Integer orderID = (Integer) session.getAttribute("pendingOrderID");
            if (orderID != null) {
                dal.OrderDAO orderDAO = new dal.OrderDAO();
                model.Order order = orderDAO.getOrderById(orderID);

                if (order != null && order.getIsStaffConfirmed() == 1) {
                    out.print("{\"status\": \"approved\"}");
                } else {
                    out.print("{\"status\": \"waiting\"}");
                }
            } else {
                out.print("{\"status\": \"error\"}");
            }

        } else if ("getPending".equals(action)) {
            // HOST HỎI: "Có ai đang chờ xin vào bàn không?"
            Integer orderID = (Integer) session.getAttribute("orderID");
            String role = (String) session.getAttribute("roleInTable");

            if (orderID != null && "HOST".equals(role)) {
                List<TableJoinRequest> list = requestDAO.getPendingRequestsByOrderId(orderID);

                // Trả về một mảng JSON tự build
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    TableJoinRequest r = list.get(i);
                    json.append("{\"id\":").append(r.getRequestID())
                            .append(",\"name\":\"").append(r.getGuestName()).append("\"}");
                    if (i < list.size() - 1) {
                        json.append(",");
                    }
                }
                json.append("]");
                out.print(json.toString());
            } else {
                out.print("[]");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // POST dùng để thực hiện các hành động bấm nút
        String action = request.getParameter("action");
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        if ("requestJoin".equals(action)) {
            // GUEST NỘP ĐƠN: Xin vào bàn
            Integer orderID = (Integer) session.getAttribute("pendingOrderID");
            String guestName = request.getParameter("guestName");
            String sessionID = session.getId();

            if (orderID != null && guestName != null && !guestName.trim().isEmpty()) {
                TableJoinRequest req = new TableJoinRequest();
                req.setOrderID(orderID);
                req.setGuestSessionID(sessionID);
                req.setGuestName(guestName.trim());

                boolean success = requestDAO.createJoinRequest(req);
                out.print(success ? "success" : "fail");
            } else {
                out.print("invalid");
            }

        } else if ("approve".equals(action) || "reject".equals(action)) {
            // HOST DUYỆT ĐƠN: Cho phép hoặc Từ chối
            if ("HOST".equals(session.getAttribute("roleInTable"))) {
                int requestID = Integer.parseInt(request.getParameter("requestID"));
                String newStatus = "approve".equals(action) ? "approved" : "rejected";

                boolean success = requestDAO.updateRequestStatus(requestID, newStatus);
                out.print(success ? "success" : "fail");
            } else {
                out.print("unauthorized");
            }
        }
    }
}
