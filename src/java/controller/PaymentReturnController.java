package controller;

import dal.InvoicesDAO;
import dal.OrderDAOSon;
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
    private final OrderDAOSon orderDAOSon = new OrderDAOSon();

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
            // Chữ ký hợp lệ -> Kiểm tra trạng thái giao dịch
            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {

                // === THANH TOÁN THÀNH CÔNG ===
                HttpSession session = request.getSession();
                Integer invoiceID = (Integer) session.getAttribute("invoiceID");
                Integer orderID = (Integer) session.getAttribute("orderID"); // Bổ sung lấy orderID
                boolean isDepositPayment = false;

                // [FIX VNPAY] Phân biệt hóa đơn cọc và hóa đơn thanh toán cuối.
                // DEP-: chỉ xác nhận đã cọc; OrderDAOSon sẽ đồng bộ
                // INV-: thanh toán bữa ăn xong -> completed/cleaning.
                if (invoiceID != null) {
                    Invoices invoice = invoicesDAO.getInvoiceById(invoiceID);
                    if (invoice != null
                            && invoice.getInvoiceNumber() != null
                            && invoice.getInvoiceNumber().startsWith("DEP-")) {
                        // Ghi nhớ đây là thanh toán cọc
                        // để hiển thị đúng thông báo đặt bàn, không dùng câu chờ dọn bàn.
                        isDepositPayment = true;
                        invoicesDAO.updateInvoiceStatus(
                                invoiceID, "paid", "vnpay");
                        //  Sau khi cọc paid, đồng bộ ngay
                        // để đơn đặt bàn chuyển pending -> reserved trong lần return này.
                        orderDAOSon.synchronizeDepositStatus();
                    } else if (invoice != null && orderID != null) {
                        invoicesDAO.updatePaymentSuccessAndCleaningTable(
                                invoiceID, orderID, "vnpay");
                    }
                }

                // Dọn dẹp session để khách có thể quét mã gọi món lượt mới
                session.removeAttribute("orderID");
                session.removeAttribute("invoiceID");
                session.removeAttribute("tableID"); 
                session.removeAttribute("reservationOrderID");
                session.removeAttribute("reservationFlow");
                session.removeAttribute("depositAmount");
                session.removeAttribute("reservationHoldExpiresAt");

                
                // tách nội dung theo loại hóa đơn: DEP- là cọc, hóa đơn thường là thanh toán bữa ăn.
                out.println("<h2 style='color: green; text-align: center; margin-top: 50px;'>THANH TOÁN THÀNH CÔNG!</h2>");
                if (isDepositPayment) {
                    out.println("<p style='text-align: center;'>Đặt cọc thành công. Mã giao dịch của bạn là: <b>" + request.getParameter("vnp_TxnRef") + "</b></p>");
                    out.println("<p style='text-align: center; color: #bc945c;'><i>Đơn đặt bàn của bạn đã được giữ chỗ. Nhà hàng sẽ chuẩn bị theo thời gian bạn đã chọn.</i></p>");
                    out.println("<div style='text-align: center;'><a href='" + request.getContextPath() + "/reservation?action=history'>Xem lịch sử đặt bàn</a></div>");
                } else {
                    out.println("<p style='text-align: center;'>Cảm ơn bạn đã đặt món. Mã giao dịch của bạn là: <b>" + request.getParameter("vnp_TxnRef") + "</b></p>");
                    out.println("<p style='text-align: center; color: #bc945c;'><i>Bàn của bạn đang được đưa vào trạng thái chờ dọn dẹp.</i></p>");
                    out.println("<div style='text-align: center;'><a href='" + request.getContextPath() + "/menu'>Quay lại Menu</a></div>");
                }

            } else {
                // === GIAO DỊCH LỖI HOẶC BỊ HỦY ===
               // Ghi nhận thất bại để đơn cọc pending được
                // OrderDAOSon chuyển thành cancelled/available.
                HttpSession session = request.getSession(false);
                if (session != null) {
                    Integer invoiceID
                            = (Integer) session.getAttribute("invoiceID");
                    if (invoiceID != null) {
                        invoicesDAO.updateInvoiceStatus(
                                invoiceID, "failed", "vnpay");
                    }
                }
                out.println("<h2 style='color: red; text-align: center; margin-top: 50px;'>❌ THANH TOÁN THẤT BẠI HOẶC BỊ HỦY!</h2>");
                out.println("<div style='text-align: center;'><a href='" + request.getContextPath() + "/order?action=cart'>Quay lại Giỏ hàng</a></div>");
            }
        } else {
            // === CHỮ KÝ KHÔNG HỢP LỆ (CÓ THỂ DO HACKER SỬA URL) ===
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
