/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dal.InvoicesDAO;
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
import util.Config;

/**
 *
 * @author taduc
 */
@WebServlet(name = "PaymentReturnController", urlPatterns = {"/vnpay_return"})
public class PaymentReturnController extends HttpServlet {

    private final InvoicesDAO invoicesDAO = new InvoicesDAO();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        // Ở đây ta không in HTML mặc định nữa, mà sẽ xử lý logic VNPay
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

                // CHÍNH THỨC CẬP NHẬT DATABASE TẠI ĐÂY
                if (invoiceID != null) {
                    invoicesDAO.updateInvoiceStatus(invoiceID, "paid", "vnpay");
                }

                // Dọn dẹp session để khách có thể đặt đơn mới
                session.removeAttribute("orderID");
                session.removeAttribute("invoiceID");

                // In ra thông báo thành công (Bạn có thể sửa thành response.sendRedirect sang 1 trang JSP đẹp hơn)
                out.println("<h2 style='color: green; text-align: center; margin-top: 50px;'>🎉 THANH TOÁN THÀNH CÔNG!</h2>");
                out.println("<p style='text-align: center;'>Cảm ơn bạn đã đặt món. Mã giao dịch của bạn là: <b>" + request.getParameter("vnp_TxnRef") + "</b></p>");
                out.println("<div style='text-align: center;'><a href='" + request.getContextPath() + "/menu'>Quay lại Menu</a></div>");

            } else {
                // === GIAO DỊCH LỖI HOẶC BỊ HỦY ===
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
        return "Short description";
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
