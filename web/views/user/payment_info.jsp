<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Chi tiết thanh toán - Vị An Restaurant</title>
        <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
                font-family: 'Segoe UI', Arial, sans-serif;
                background: #f7f5f0;
                color: #2c2520;
            }
            .receipt-wrapper {
                width: 100%;
                max-width: 600px;
                margin: 40px auto;
                padding: 0 16px;
            }
            .receipt-card {
                background: #ffffff;
                border-radius: 16px;
                box-shadow: 0 8px 24px rgba(44, 37, 32, 0.08);
                overflow: hidden;
                border: 1px solid #eae5da;
            }
            .receipt-header {
                background: #1c4332;
                color: #ffffff;
                padding: 30px 24px;
                text-align: center;
                position: relative;
            }
            .receipt-header h2 {
                font-size: 24px;
                margin-bottom: 8px;
                font-weight: 700;
                letter-spacing: 1px;
            }
            .status-badge {
                display: inline-block;
                padding: 6px 16px;
                border-radius: 20px;
                font-size: 14px;
                font-weight: bold;
                text-transform: uppercase;
                margin-top: 10px;
            }
            .status-paid { background: #d1fae5; color: #065f46; }
            .status-unpaid { background: #fef3c7; color: #92400e; }
            .status-failed { background: #fee2e2; color: #991b1b; }

            .receipt-body {
                padding: 24px;
            }
            .info-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 16px;
                margin-bottom: 24px;
                padding-bottom: 20px;
                border-bottom: 1px dashed #dcd5c5;
            }
            .info-item label {
                display: block;
                font-size: 12px;
                color: #8a7e75;
                margin-bottom: 4px;
                text-transform: uppercase;
            }
            .info-item span {
                font-size: 15px;
                font-weight: 600;
                color: #2c2520;
            }

            .item-list {
                margin-bottom: 24px;
            }
            .item-row {
                display: flex;
                justify-content: space-between;
                padding: 10px 0;
                border-bottom: 1px solid #f5f2eb;
            }
            .item-row:last-child { border-bottom: none; }
            .item-name { font-size: 15px; font-weight: 600; }
            .item-qty { font-size: 13px; color: #8a7e75; margin-top: 2px;}
            .item-price { font-size: 15px; font-weight: bold; color: #1c4332; }

            .summary-box {
                background: #fbf9f6;
                padding: 20px;
                border-radius: 12px;
                border: 1px solid #eae5da;
            }
            .summary-row {
                display: flex;
                justify-content: space-between;
                font-size: 14px;
                color: #5e5550;
                margin-bottom: 10px;
            }
            .summary-row.total {
                font-size: 18px;
                font-weight: 800;
                color: #1c4332;
                border-top: 1px dashed #dcd5c5;
                padding-top: 12px;
                margin-top: 4px;
                margin-bottom: 0;
            }
            .btn-action {
                display: block;
                width: 100%;
                text-align: center;
                padding: 14px;
                border-radius: 8px;
                font-size: 16px;
                font-weight: bold;
                text-decoration: none;
                margin-top: 24px;
                transition: 0.2s;
            }
            .btn-home { background: #f5f2eb; color: #1c4332; border: 1px solid #dcd5c5; }
            .btn-home:hover { background: #eadecf; }
            .btn-pay { background: #1c4332; color: #ffffff; border: none; margin-bottom: 12px;}
            .btn-pay:hover { background: #275d46; }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>

        <div class="receipt-wrapper">
            <div class="receipt-card">
                <div class="receipt-header">
                    <h2>VỊ AN RESTAURANT</h2>
                    <p>Chi Tiết Giao Dịch</p>
                    
                    <c:choose>
                        <c:when test="${invoice.status == 'paid'}">
                            <div class="status-badge status-paid">✔ Đã Thanh Toán</div>
                        </c:when>
                        <c:when test="${invoice.status == 'failed'}">
                            <div class="status-badge status-failed">✖ Giao Dịch Lỗi</div>
                        </c:when>
                        <c:otherwise>
                            <div class="status-badge status-unpaid">⏳ Chờ Thanh Toán</div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="receipt-body">
                    <div class="info-grid">
                        <div class="info-item">
                            <label>Mã Hóa Đơn</label>
                            <span>${invoice.invoiceNumber}</span>
                        </div>
                        <div class="info-item">
                            <label>Ngày Tạo</label>
                            <span><fmt:formatDate value="${invoice.issuedDate}" pattern="dd/MM/yyyy"/></span>
                        </div>
                        <div class="info-item">
                            <label>Mã Đơn Hàng</label>
                            <span>#${order.orderID}</span>
                        </div>
                        <div class="info-item">
                            <label>Phương Thức</label>
                            <span>
                                <c:choose>
                                    <c:when test="${invoice.paymentMethod == 'cash'}">Tiền mặt tại quầy</c:when>
                                    <c:when test="${invoice.paymentMethod == 'vnpay'}">Chuyển khoản VNPay</c:when>
                                    <c:otherwise>Chưa xác định</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>

                    <h4 style="margin-bottom: 12px; color: #1c4332;">Chi tiết món ăn</h4>
                    <div class="item-list">
                        <c:forEach var="oi" items="${orderItems}" varStatus="loop">
                            <c:set var="mi" value="${menuItems[loop.index]}"/>
                            <c:set var="unitPrice" value="${mi.discountedPrice > 0 ? mi.discountedPrice : mi.price}"/>
                            
                            <div class="item-row">
                                <div>
                                    <div class="item-name">${mi.itemName}</div>
                                    <div class="item-qty">${oi.quantity} x <fmt:formatNumber value="${unitPrice}" type="number"/> đ</div>
                                </div>
                                <div class="item-price">
                                    <fmt:formatNumber value="${oi.quantity * unitPrice}" type="number"/> đ
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty orderItems}">
                            <div style="text-align: center; color: #8a7e75; font-size: 14px; padding: 10px;">(Hóa đơn dịch vụ / Không có món ăn cụ thể)</div>
                        </c:if>
                    </div>

                    <div class="summary-box">
                        <div class="summary-row">
                            <span>Tạm tính:</span>
                            <span><fmt:formatNumber value="${invoice.subTotal}" type="number"/> đ</span>
                        </div>
                        <div class="summary-row">
                            <span>VAT (10%):</span>
                            <span><fmt:formatNumber value="${invoice.taxAmount}" type="number"/> đ</span>
                        </div>
                        <div class="summary-row">
                            <span>Đã cọc (Trừ đi):</span>
                            <span>- <fmt:formatNumber value="${invoice.depositDeducted}" type="number"/> đ</span>
                        </div>
                        <div class="summary-row total">
                            <span>Thành Tiền:</span>
                            <span><fmt:formatNumber value="${invoice.finalAmount}" type="number"/> đ</span>
                        </div>
                    </div>

                    <c:if test="${invoice.status == 'unpaid' || invoice.status == 'failed'}">
                        <form action="${pageContext.request.contextPath}/checkout" method="get">
                            <button type="submit" class="btn-action btn-pay">Thử Thanh Toán Lại</button>
                        </form>
                    </c:if>

                    <c:if test="${isDepositPayment
                                  && invoice.status == 'paid'
                                  && not empty order
                                  && order.orderStatus == 'reserved'}">
                        <a href="${pageContext.request.contextPath}/reservation?action=preorder&orderID=${order.orderID}"
                           class="btn-action btn-pay">
                            🍲 Đặt món trước
                        </a>
                    </c:if>
                    
                    <a href="${pageContext.request.contextPath}/home" class="btn-action btn-home">Về Trang Chủ</a>
                </div>
            </div>
        </div>

        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
