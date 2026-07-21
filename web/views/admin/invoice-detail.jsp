<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%-- 🌟 BỔ SUNG THƯ VIỆN FN ĐỂ NHẬN DIỆN CHUỖI MÃ HÓA ĐƠN --%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- Kiểm tra xem đây có phải là Biên lai cọc hay không (Mã chứa chữ DEP) --%>
<c:set var="isDeposit" value="${fn:contains(invoice.invoiceNumber, 'DEP')}" />

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Chi tiết Hóa Đơn #${invoice.invoiceNumber} - Vị An Restaurant</title>
        <style>
            /* Tổng thể chung */
            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #FCF9F2; color: #4A3B32; margin: 0; }
            .vian-container { padding: 30px 50px; background-color: #FCF9F2; flex: 1; }
            
            /* Tiêu đề trang */
            .vian-title { color: #8B4513; font-family: 'Georgia', serif; border-bottom: 2px solid #D4A373; padding-bottom: 10px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: flex-end; }
            
            /* Khu vực Hóa Đơn (Bản in) */
            .invoice-card { background: #FFFFFF; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 1px solid #E6DEC9; padding: 40px; max-width: 800px; margin: 0 auto; }
            
            .invoice-header { display: flex; justify-content: space-between; border-bottom: 2px dashed #E6DEC9; padding-bottom: 20px; margin-bottom: 20px; }
            .invoice-header-left h1 { color: #1c4332; margin: 0 0 5px 0; font-size: 28px; }
            .invoice-header-left p { color: #7a6e65; margin: 0; font-size: 14px; }
            .invoice-header-right { text-align: right; }
            .invoice-header-right h3 { color: #8B4513; margin: 0 0 5px 0; font-size: 20px; text-transform: uppercase; }
            .invoice-header-right p { color: #5C4033; font-weight: bold; margin: 0 0 8px 0; font-size: 14px; }
            
            /* Lưới thông tin phụ */
            .info-grid { display: flex; gap: 40px; margin-bottom: 30px; }
            .info-box { flex: 1; }
            .info-box span { display: block; font-size: 12px; color: #7a6e65; text-transform: uppercase; margin-bottom: 5px; font-weight: bold;}
            .info-box strong { display: block; font-size: 15px; color: #2c2520; }
            
            /* Bảng Món Ăn */
            .vian-table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }
            .vian-table th { background-color: #D4A373; color: #FFFFFF; padding: 12px; text-align: left; font-size: 13px; text-transform: uppercase; }
            .vian-table td { padding: 12px; border-bottom: 1px solid #F0E6D2; color: #2c2520; font-size: 14px; }
            .vian-table tr:hover { background-color: #FAF4E8; }
            .text-right { text-align: right !important; }
            .text-center { text-align: center !important; }
            
            /* Tổng kết tiền bạc */
            .summary-box { width: 350px; margin-left: auto; border: 1px solid #E6DEC9; border-radius: 6px; padding: 15px; background: #fdfcfb; }
            .summary-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; color: #5C4033; }
            .summary-row.total { border-top: 2px dashed #D4A373; margin-top: 8px; padding-top: 12px; font-weight: bold; font-size: 18px; color: #d32f2f; }
            .summary-row.deposit { color: #1e8e3e; font-weight: 600; }
            
            /* Các nút điều hướng */
            .btn-group { display: flex; justify-content: center; gap: 15px; margin-top: 30px; }
            .btn-vian { padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; cursor: pointer; transition: 0.3s; border: none; font-size: 14px; }
            .btn-back { background-color: #E6DEC9; color: #4A3B32; }
            .btn-back:hover { background-color: #D4C9A8; }
            .btn-print { background-color: #1c4332; color: #ffffff; }
            .btn-print:hover { background-color: #275d46; }
            
            /* Nhãn trạng thái */
            .badge { padding: 5px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; display: inline-block; }
            .badge-paid { background: #e6f4ea; color: #1e8e3e; border: 1px solid #cce8d6; }
            .badge-unpaid { background: #fce8e6; color: #d93025; border: 1px solid #fad2cf; }

            /* ========================================= */
            /* CSS CHỈ CÓ TÁC DỤNG KHI BẤM IN (CTRL + P) */
            /* ========================================= */
            @media print {
                /* Xóa margin mặc định của giấy in để vô hiệu hóa URL và ngày giờ của trình duyệt */
                @page { margin: 0; }

                /* Căn chỉnh lại padding giấy in để nội dung không dính mép */
                body { 
                    background: #fff !important; 
                    margin: 0 !important;
                    padding: 1.5cm !important; 
                }

                /* Ẩn các thành phần không cần thiết */
                header, footer, nav, aside, .sidebar, 
                .vian-title, .btn-group, .dashboard-sidebar, .header-navbar { 
                    display: none !important; 
                }

                /* Đẩy layout tràn viền giấy */
                .layout, .vian-container { 
                    padding: 0 !important; 
                    margin: 0 !important; 
                    width: 100% !important; 
                    display: block !important; 
                }

                /* Bỏ viền và shadow của khối hóa đơn */
                .invoice-card { 
                    box-shadow: none !important; 
                    border: none !important; 
                    padding: 0 !important; 
                    max-width: 100% !important; 
                }
            }
        </style>
    </head>
    <body>

        <%@ include file="/views/includes/header.jsp" %>

        <div class="layout" style="display: flex; align-items: stretch; min-height: 80vh;">

            <%@ include file="/views/includes/dashboard.jsp" %>

            <div class="vian-container">

                <h2 class="vian-title">
                    Chi tiết Chứng từ
                    <a href="${pageContext.request.contextPath}/admin/invoices" style="font-size: 14px; color: #8B4513; text-decoration: none;">← Quay lại danh sách</a>
                </h2>

                <div class="invoice-card" id="printableInvoice">
                    
                    <%-- 1. Tiêu đề (Đổi tên dựa trên loại đơn) --%>
                    <div class="invoice-header">
                        <div class="invoice-header-left">
                            <h1>Vị An Restaurant</h1>
                            <p>145 Hoàng Cầu, Q. Đống Đa, Hà Nội</p>
                            <p>Điện thoại: 0123.456.789</p>
                        </div>
                        <div class="invoice-header-right">
                            <h3>
                                <c:choose>
                                    <c:when test="${isDeposit}">BIÊN LAI THU TIỀN CỌC</c:when>
                                    <c:otherwise>HÓA ĐƠN THANH TOÁN</c:otherwise>
                                </c:choose>
                            </h3>
                            <p>#${invoice.invoiceNumber}</p>
                            <div>
                                <c:choose>
                                    <c:when test="${invoice.status == 'paid'}"><span class="badge badge-paid">Đã thanh toán</span></c:when>
                                    <c:otherwise><span class="badge badge-unpaid">Chưa thanh toán</span></c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <%-- 2. Thông tin phụ --%>
                    <div class="info-grid">
                        <div class="info-box">
                            <span>Thời gian giao dịch:</span>
                            <strong><fmt:formatDate value="${invoice.issuedDate}" pattern="HH:mm - dd/MM/yyyy"/></strong>
                        </div>
                        <div class="info-box">
                            <span>Mã đơn gốc tham chiếu:</span>
                            <strong>
                                <c:choose>
                                    <c:when test="${not empty order}">#Order-${order.orderID}</c:when>
                                    <c:otherwise>Không xác định</c:otherwise>
                                </c:choose>
                            </strong>
                        </div>
                        <div class="info-box">
                            <span>Kênh thanh toán:</span>
                            <strong>
                                <c:choose>
                                    <c:when test="${invoice.paymentMethod == 'vnpay'}">Cổng chuyển khoản VNPay</c:when>
                                    <c:when test="${invoice.paymentMethod == 'cash'}">Tiền mặt tại quầy</c:when>
                                    <c:otherwise>${invoice.paymentMethod}</c:otherwise>
                                </c:choose>
                            </strong>
                        </div>
                    </div>

                    <%-- 3. Bảng dữ liệu (Thay đổi nội dung tùy loại đơn) --%>
                    <table class="vian-table">
                        <thead>
                            <tr>
                                <th style="width: 5%;">STT</th>
                                <th style="width: 45%;">Hạng mục / Tên Món</th>
                                <th class="text-center" style="width: 15%;">Số lượng</th>
                                <th class="text-right" style="width: 15%;">Đơn giá</th>
                                <th class="text-right" style="width: 20%;">Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <%-- KỊCH BẢN A: LÀ BIÊN LAI TIỀN CỌC --%>
                                <c:when test="${isDeposit}">
                                    <tr>
                                        <td>1</td>
                                        <td><strong>Khoản thu đặt cọc giữ bàn trước</strong></td>
                                        <td class="text-center">1</td>
                                        <td class="text-right"><fmt:formatNumber value="${invoice.subTotal}" pattern="#,###"/> đ</td>
                                        <td class="text-right" style="font-weight:bold; color:#1c4332;">
                                            <fmt:formatNumber value="${invoice.subTotal}" pattern="#,###"/> đ
                                        </td>
                                    </tr>
                                </c:when>

                                <%-- KỊCH BẢN B: LÀ HÓA ĐƠN ĂN UỐNG BÌNH THƯỜNG --%>
                                <c:otherwise>
                                    <c:forEach var="oi" items="${orderItems}" varStatus="loop">
                                        <c:set var="mi" value="${menuItems[loop.index]}" />
                                        <c:set var="unitPrice" value="${mi.discountedPrice > 0 ? mi.discountedPrice : mi.price}" />
                                        
                                        <tr>
                                            <td>${loop.index + 1}</td>
                                            <td><strong>${mi.itemName}</strong></td>
                                            <td class="text-center">${oi.quantity}</td>
                                            <td class="text-right"><fmt:formatNumber value="${unitPrice}" pattern="#,###"/> đ</td>
                                            <td class="text-right" style="font-weight:bold; color:#1c4332;">
                                                <fmt:formatNumber value="${unitPrice * oi.quantity}" pattern="#,###"/> đ
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    
                                    <c:if test="${empty orderItems}">
                                        <tr><td colspan="5" class="text-center" style="color: #7a6e65; padding: 20px;">Đơn hàng này chưa ghi nhận món ăn nào.</td></tr>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>

                    <%-- 4. Tổng kết tiền bạc (Tùy biến hiển thị) --%>
                    <div class="summary-box">
                        <c:choose>
                            <c:when test="${isDeposit}">
                                <div class="summary-row">
                                    <span>Tạm tính khoản cọc:</span>
                                    <span><fmt:formatNumber value="${invoice.subTotal}" pattern="#,###"/> đ</span>
                                </div>
                                <div class="summary-row total">
                                    <span>Khách đã thanh toán:</span>
                                    <span><fmt:formatNumber value="${invoice.finalAmount}" pattern="#,###"/> đ</span>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="summary-row">
                                    <span>Tạm tính món ăn:</span>
                                    <span><fmt:formatNumber value="${invoice.subTotal}" pattern="#,###"/> đ</span>
                                </div>
                                <div class="summary-row">
                                    <span>Phí dịch vụ:</span>
                                    <span><fmt:formatNumber value="${invoice.taxAmount}" pattern="#,###"/> đ</span>
                                </div>
                                
                                <c:if test="${invoice.depositDeducted > 0}">
                                    <div class="summary-row deposit">
                                        <span>Đã khấu trừ tiền cọc:</span>
                                        <span>-<fmt:formatNumber value="${invoice.depositDeducted}" pattern="#,###"/> đ</span>
                                    </div>
                                </c:if>
                                
                                <div class="summary-row total">
                                    <span>Khách phải trả:</span>
                                    <span><fmt:formatNumber value="${invoice.finalAmount}" pattern="#,###"/> đ</span>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    
                </div>

                <%-- Nút bấm --%>
                <div class="btn-group">
                    <button type="button" class="btn-vian btn-print" onclick="window.print();">🖨️ In Chứng Từ</button>
                </div>

            </div> 
        </div> 

        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>