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
import model.Employee;

@WebServlet(name = "TableJoinController", urlPatterns = {"/api/table-join"})
public class TableJoinController extends HttpServlet {

    private final TableJoinRequestDAO requestDAO = new TableJoinRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        util.CsrfUtil.ensureToken(session);

        if ("checkStatus".equals(action)) {
            Integer orderID = (Integer) session.getAttribute("pendingOrderID");
            String sessionID = session.getId();

            if (orderID != null) {
                String status = requestDAO.checkRequestStatus(sessionID, orderID);
                
                if ("approved".equals(status)) {
                    session.setAttribute("orderID", orderID);
                    session.setAttribute("roleInTable", "GUEST");
                    session.setAttribute("tableID", session.getAttribute("pendingTableID"));
                    session.setAttribute("currentTableID", session.getAttribute("pendingTableID"));
                    session.setAttribute("areaType", session.getAttribute("pendingAreaType"));
                } 
                else if ("approved_reclaim".equals(status)) {
                    // Xử lý khi nhân viên đã xác nhận cấp lại quyền HOST
                    dal.OrderDAO orderDAO = new dal.OrderDAO();
                    model.Order o = orderDAO.getOrderById(orderID);
                    
                    if (o != null && o.getHostToken() != null) {
                        // Cập nhật Cookie mới cho khách
                        jakarta.servlet.http.Cookie hostCookie = new jakarta.servlet.http.Cookie("HOST_OF_TABLE_" + session.getAttribute("pendingTableID"), o.getHostToken());
                        hostCookie.setMaxAge(24 * 60 * 60);
                        hostCookie.setPath("/");
                        // [COOKIE SECURITY] Host token không được phép đọc bằng JavaScript.
                        hostCookie.setHttpOnly(true);
                        hostCookie.setSecure(request.isSecure());
                        response.addCookie(hostCookie);
                        
                        // Phục hồi quyền HOST
                        session.setAttribute("orderID", orderID);
                        session.setAttribute("roleInTable", "HOST");
                        session.setAttribute("tableID", session.getAttribute("pendingTableID"));
                        session.setAttribute("currentTableID", session.getAttribute("pendingTableID"));
                        session.setAttribute("areaType", session.getAttribute("pendingAreaType"));
                    }
                }
                out.print("{\"status\": \"" + (status != null ? status : "unknown") + "\"}");
            } else {
                out.print("{\"status\": \"error\"}");
            }
        
        } else if ("checkStaffApproval".equals(action)) {
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
            Integer orderID = (Integer) session.getAttribute("orderID");
            String role = (String) session.getAttribute("roleInTable");

            if (orderID != null && "HOST".equals(role)) {
                List<TableJoinRequest> list = requestDAO.getPendingRequestsByOrderId(orderID);
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    TableJoinRequest r = list.get(i);
                    json.append("{\"id\":").append(r.getRequestID())
                            // [JSON FIX] Escape tên khách trước khi ghép JSON.
                            .append(",\"name\":\"").append(escapeJson(r.getGuestName())).append("\"}");
                    if (i < list.size() - 1) json.append(",");
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
        String action = request.getParameter("action");
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        // [CSRF FIX] Bảo vệ request join/approve/reclaim làm thay đổi DB.
        if (!util.CsrfUtil.isValid(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("csrf_invalid");
            return;
        }

        if ("requestJoin".equals(action)) {
            Integer orderID = (Integer) session.getAttribute("pendingOrderID");
            String guestName = request.getParameter("guestName");
            String sessionID = session.getId();

            if (orderID != null && guestName != null
                    && !guestName.trim().isEmpty()
                    && guestName.trim().length() <= 100) {
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
            Integer hostOrderID = (Integer) session.getAttribute("orderID");
            if ("HOST".equals(session.getAttribute("roleInTable")) && hostOrderID != null) {
                try {
                    int requestID = Integer.parseInt(request.getParameter("requestID"));
                    String newStatus = "approve".equals(action) ? "approved" : "rejected";
                    // [SECURITY FIX] HOST chỉ được duyệt request thuộc đúng order của mình.
                    boolean success = requestDAO.updateRequestStatusForOrder(
                            requestID, hostOrderID, newStatus);
                    out.print(success ? "success" : "fail");
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("invalid");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("unauthorized");
            }
            
        } else if ("requestReclaimHost".equals(action)) {
            // Khách gửi yêu cầu xin khôi phục quyền
            Integer orderID = (Integer) session.getAttribute("pendingOrderID");
            String sessionID = session.getId();

            if (orderID != null) {
                boolean success = requestDAO.createReclaimRequest(orderID, sessionID);
                out.print(success ? "success" : "fail");
            } else {
                out.print("invalid");
            }

        } else if ("staffApproveReclaim".equals(action)) {
            // [SECURITY FIX] Endpoint /api không đi qua AuthenticationFilter:
            // chỉ Owner(1) hoặc Receptionist(3) được cấp lại quyền HOST.
            Employee employee = (Employee) session.getAttribute("employee");
            if (employee == null
                    || (employee.getRoleID() != 1 && employee.getRoleID() != 3)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("unauthorized");
                return;
            }

            try {
                int orderID = Integer.parseInt(request.getParameter("orderID"));
                if (!requestDAO.hasPendingReclaim(orderID)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("invalid");
                    return;
                }

                String newHostToken = java.util.UUID.randomUUID().toString();
                dal.OrderDAO orderDAO = new dal.OrderDAO();
                boolean tokenUpdated = orderDAO.updateHostToken(orderID, newHostToken);
                boolean success = tokenUpdated
                        && requestDAO.updateReclaimStatusByOrder(
                                orderID, "approved_reclaim");
                out.print(success ? "success" : "fail");
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("invalid");
            }
        }
    }

    // [JSON FIX] Không cho dấu nháy/xuống dòng trong tên khách phá cấu trúc JSON.
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
