package controller;

import dal.InvoicesDAO;
import dal.ReservationDAO;
import dal.OrderDAO;
import dal.DBContext; // Thêm import DBContext để dùng cho hàm logPayment
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import model.Invoices;
import util.Config;

@WebServlet(name = "PaymentReturnController", urlPatterns = {"/vnpay_return"})
public class PaymentReturnController extends HttpServlet {

    private final InvoicesDAO invoicesDAO = new InvoicesDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. LẤY TẤT CẢ DỮ LIỆU VNPAY TRẢ VỀ TRÊN URL ĐƯA VÀO MAP
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
            String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        // 2. LẤY CHỮ KÝ BẢO MẬT CỦA VNPAY VÀ XÓA NÓ KHỎI MAP ĐỂ TỰ TÍNH TOÁN LẠI
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }

        // 3. BĂM LẠI DATA BẰNG SECRET KEY CỦA MÌNH ĐỂ KIỂM TRA TÍNH TOÀN VẸN
        String signValue = hashAllFields(fields);

        // Chuẩn bị response để in ra màn hình hoặc redirect
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (signValue.equals(vnp_SecureHash)) {
            // === LẤY THÔNG TIN GIAO DỊCH TỪ VNPAY ===
            String txnRef = request.getParameter("vnp_TxnRef"); // Mã giao dịch
            long vnpAmount = 0;
            try {
                vnpAmount = Long.parseLong(request.getParameter("vnp_Amount")) / 100;
            } catch (Exception e) {
                System.err.println("Lỗi parse vnp_Amount: " + e.getMessage());
            }

            HttpSession session = request.getSession();
            Integer invoiceID = (Integer) session.getAttribute("invoiceID");
            Integer orderID = (Integer) session.getAttribute("orderID");
            String expectedTxnRef =
                    (String) session.getAttribute("vnpPendingTxnRef");
            Integer expectedInvoiceID =
                    (Integer) session.getAttribute("vnpPendingInvoiceID");
            Long expectedAmount =
                    (Long) session.getAttribute("vnpPendingAmount");

            // [SECURITY FIX - VNPAY] Transaction, invoice và số tiền callback
            // phải khớp dữ liệu đã lưu trước khi chuyển sang VNPay.
            if (expectedTxnRef == null || !expectedTxnRef.equals(txnRef)
                    || expectedInvoiceID == null
                    || !expectedInvoiceID.equals(invoiceID)
                    || expectedAmount == null
                    || expectedAmount.longValue() != vnpAmount) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Thông tin giao dịch VNPay không khớp.");
                return;
            }

            // Chữ ký hợp lệ -> Kiểm tra trạng thái giao dịch
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                // ==========================================
                // THANH TOÁN THÀNH CÔNG
                // ==========================================
                boolean isDepositPayment = false;

                if (invoiceID != null) {
                    Invoices invoice = invoicesDAO.getInvoiceById(invoiceID);
                    if (invoice != null && invoice.getInvoiceNumber() != null && invoice.getInvoiceNumber().startsWith("DEP-")) {
                        // Nhánh 1: Cọc
                        isDepositPayment = true;
                        invoicesDAO.updateInvoiceStatus(invoiceID, "paid", "vnpay");
                        logPaymentRecord(invoiceID, txnRef, "vnpay", vnpAmount, "success");
                        reservationDAO.synchronizeDepositStatus();
                    } else if (invoice != null && orderID != null) {
                        // Nhánh 2: Thanh toán bữa ăn (Transaction gộp)
                        model.Order linkedOrder =
                                new OrderDAO().getOrderByInvoiceId(invoiceID);
                        if (linkedOrder == null
                                || linkedOrder.getOrderID() != orderID
                                || invoice.getFinalAmount() != vnpAmount) {
                            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                                    "Hóa đơn không khớp đơn hàng.");
                            return;
                        }
                        boolean updated =
                                invoicesDAO.updatePaymentSuccessAndCleaningTable(
                                        invoiceID, orderID, "vnpay",
                                        vnpAmount, txnRef);
                        if (!updated) {
                            response.sendError(HttpServletResponse.SC_CONFLICT,
                                    "Giao dịch đã được xử lý hoặc không còn hợp lệ.");
                            return;
                        }
                    }
                }

                // 1. Sao lưu ID hóa đơn ra một biến final để đẩy lên URL (Cực kỳ quan trọng)
                final Integer finalInvoiceID = invoiceID;
                if (finalInvoiceID != null) {
                    // [SECURITY FIX - INVOICE IDOR] Cho session hiện tại xem
                    // đúng hóa đơn vừa được VNPay trả kết quả, không mở IDOR.
                    session.setAttribute("paymentInfoInvoiceID", finalInvoiceID);
                }

                // 2. Dọn dẹp sạch sẽ Session (giải phóng bàn)
                session.removeAttribute("orderID");
                session.removeAttribute("invoiceID");
                session.removeAttribute("tableID");
                session.removeAttribute("reservationOrderID");
                session.removeAttribute("reservationFlow");
                session.removeAttribute("depositAmount");
                session.removeAttribute("reservationHoldExpiresAt");
                session.removeAttribute("vnpPendingTxnRef");
                session.removeAttribute("vnpPendingInvoiceID");
                session.removeAttribute("vnpPendingAmount");

                // 3. Thực hiện chuyển hướng (Bắt buộc dùng finalInvoiceID để tránh mất dấu)
                if (finalInvoiceID != null) {
                    response.sendRedirect(request.getContextPath() + "/payment-info?invoiceID=" + finalInvoiceID);
                    return;
                } else {
                    response.sendRedirect(request.getContextPath() + "/home?status=order_success");
                    return;
                }

            } else {
                // ==========================================
                // GIAO DỊCH LỖI HOẶC BỊ HỦY
                // ==========================================
                if (invoiceID != null) {
                    // Cập nhật hóa đơn và ghi log lỗi
                    invoicesDAO.updateInvoiceStatus(invoiceID, "failed", "vnpay");
                    logPaymentRecord(invoiceID, txnRef, "vnpay", vnpAmount, "failed");

                    // KHÔNG ĐƯỢC XÓA SESSION Ở ĐÂY: Vì nếu lỗi, khách phải còn Session để bấm "Thanh toán lại"
                    response.sendRedirect(request.getContextPath() + "/payment-info?invoiceID=" + invoiceID);
                    return;
                }

                // Backup nếu mất invoiceID
                out.println("<h2 style='color: red; text-align: center; margin-top: 50px;'>❌ THANH TOÁN THẤT BẠI HOẶC BỊ HỦY!</h2>");
                out.println("<div style='text-align: center;'><a href='" + request.getContextPath() + "/order?action=cart'>Quay lại Giỏ hàng</a></div>");
            }
        } else {
            // === CHỮ KÝ KHÔNG HỢP LỆ ===
            out.println("<h2 style='color: red; text-align: center; margin-top: 50px;'>⚠️ CẢNH BÁO: CHỮ KÝ BẢO MẬT KHÔNG HỢP LỆ!</h2>");
            out.println("<div style='text-align: center;'><a href='" + request.getContextPath() + "/menu'>Về trang chủ</a></div>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "VNPay Return Controller";
    }

    // =========================================================================
    // HÀM HELPER: Ghi trực tiếp lịch sử vào bảng Payments (Dành cho Cọc & Lỗi)
    // =========================================================================
    private void logPaymentRecord(int invoiceID, String transactionCode, String paymentGateway, long amount, String status) {
        String sql = "INSERT INTO Payments (invoiceID, transactionCode, paymentGateway, amount, status, paidAt) VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection conn = new DBContext().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, invoiceID);
            ps.setString(2, transactionCode);
            ps.setString(3, paymentGateway);
            ps.setLong(4, amount);
            ps.setString(5, status);
            ps.executeUpdate();

        } catch (Exception e) {
            System.err.println("[PaymentReturnController] Lỗi ghi log Payment: " + e.getMessage());
        }
    }

    // Hàm hỗ trợ băm lại các tham số nhận được
    private String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return Config.hmacSHA512(Config.vnp_HashSecret, sb.toString());
    }
}
