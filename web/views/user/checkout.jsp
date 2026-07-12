<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Xác nhận thanh toán - Vị An Restaurant</title>
        <style>
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }
            body {
                font-family: 'Segoe UI', Arial, sans-serif;
                background: #f7f5f0;
                color: #2c2520;
                margin: 0;
                padding: 0;
            }
            .container {
                width: 100%;
                max-width: 1100px;
                margin: 32px auto;
                padding: 0 16px;
                min-height: 65vh;
            }

            /* Header tiêu đề */
            .checkout-header {
                margin-bottom: 24px;
                border-bottom: 2px solid #e5dfd3;
                padding-bottom: 16px;
            }
            .checkout-header h2 {
                font-size: 26px;
                color: #1c4332;
                font-weight: bold;
            }
            .checkout-header p {
                font-size: 14px;
                color: #7a6e65;
                margin-top: 4px;
            }

            /* Bố cục lưới */
            .checkout-layout {
                display: flex;
                gap: 24px;
                align-items: flex-start;
            }

            /* Cột trái */
            .checkout-info-column {
                flex: 1;
                display: flex;
                flex-direction: column;
                gap: 20px;
            }

            /* Các khối bo góc */
            .checkout-box {
                background: #ffffff;
                border: 1px solid #eae5da;
                border-radius: 12px;
                padding: 24px;
                box-shadow: 0 4px 12px rgba(44, 37, 32, 0.04);
            }
            .box-title {
                font-size: 18px;
                font-weight: 700;
                color: #1c4332;
                margin-bottom: 16px;
                display: flex;
                align-items: center;
                gap: 8px;
                border-bottom: 1px solid #f5f2eb;
                padding-bottom: 10px;
            }

            /* CSS Form thông tin */
            .form-group {
                margin-bottom: 14px;
            }
            .form-group label {
                display: block;
                font-size: 13px;
                font-weight: 600;
                color: #5e5550;
                margin-bottom: 6px;
            }
            .form-control-static {
                padding: 10px 12px;
                background: #fbf9f6;
                border: 1px solid #eadecf;
                border-radius: 6px;
                font-size: 14px;
                font-weight: 600;
                color: #2c2520;
            }

            /* Khu vực chọn Phương thức thanh toán */
            .payment-methods-grid {
                display: flex;
                flex-direction: column;
                gap: 12px;
            }
            .payment-method-card {
                border: 1px solid #eae5da;
                border-radius: 8px;
                padding: 16px;
                display: flex;
                align-items: center;
                gap: 14px;
                cursor: pointer;
                transition: all 0.2s ease;
                background: #fbf9f6;
            }
            .payment-method-card:hover {
                border-color: #bc945c;
                background: #fdfcfb;
            }
            .payment-method-card.active {
                border-color: #1c4332;
                background: #f4f5f1;
                box-shadow: 0 2px 8px rgba(28, 67, 50, 0.06);
            }
            .payment-method-card input[type="radio"] {
                width: 18px;
                height: 18px;
                accent-color: #1c4332;
                cursor: pointer;
            }
            .payment-method-info {
                flex: 1;
            }
            .payment-method-title {
                font-size: 15px;
                font-weight: 700;
                color: #2c2520;
            }
            .payment-method-desc {
                font-size: 12px;
                color: #7a6e65;
                margin-top: 2px;
            }
            .payment-logo {
                width: 40px;
                height: 40px;
                object-fit: contain;
                border-radius: 4px;
            }

            /* Cột phải: Tóm tắt món */
            .checkout-summary-sidebar {
                width: 380px;
                background: #ffffff;
                border: 1px solid #eae5da;
                border-radius: 12px;
                padding: 24px;
                box-shadow: 0 4px 12px rgba(44, 37, 32, 0.04);
                position: sticky;
                top: 32px;
            }
            .checkout-items-list {
                max-height: 280px;
                overflow-y: auto;
                margin-bottom: 16px;
                padding-right: 4px;
            }
            .checkout-items-list::-webkit-scrollbar {
                width: 4px;
            }
            .checkout-items-list::-webkit-scrollbar-thumb {
                background: #eadecf;
                border-radius: 4px;
            }
            .checkout-item-row {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 10px 0;
                border-bottom: 1px solid #f5f2eb;
                font-size: 14px;
            }
            .checkout-item-name {
                font-weight: 600;
                color: #2c2520;
            }
            .checkout-item-qty {
                font-size: 12px;
                color: #8a7e75;
                margin-top: 2px;
            }
            .checkout-item-price {
                font-weight: 700;
                color: #1c4332;
            }

            /* Khối tính tiền */
            .summary-divider {
                border-top: 1px dashed #dcd5c5;
                margin: 16px 0;
            }
            .summary-row {
                display: flex;
                justify-content: space-between;
                font-size: 14px;
                margin-bottom: 10px;
                color: #5e5550;
            }
            .summary-row.total-row {
                font-size: 16px;
                font-weight: 700;
                color: #1c4332;
                margin-top: 6px;
            }
            .summary-total-amount {
                font-size: 22px;
                color: #1c4332;
                font-weight: 800;
            }

            /* Nút hành động */
            .btn-checkout-submit {
                width: 100%;
                padding: 15px;
                background: #1c4332;
                border: none;
                border-radius: 8px;
                color: #ffffff;
                font-size: 16px;
                font-weight: 700;
                cursor: pointer;
                box-shadow: 0 4px 10px rgba(28, 67, 50, 0.2);
                transition: all 0.2s;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
                margin-top: 16px;
            }
            .btn-checkout-submit:hover {
                background: #275d46;
                box-shadow: 0 4px 12px rgba(28, 67, 50, 0.3);
            }
            .btn-checkout-back {
                width: 100%;
                padding: 12px;
                background: #fbf9f6;
                border: 1px solid #dcd5c5;
                border-radius: 8px;
                color: #5e5550;
                font-size: 14px;
                font-weight: 600;
                text-align: center;
                text-decoration: none;
                display: block;
                margin-top: 12px;
                transition: all 0.2s;
            }
            .btn-checkout-back:hover {
                background: #f5f2eb;
                color: #1c4332;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>

        <div class="container">
            <div class="checkout-header">
                <h2>Xác nhận đơn hàng</h2>
                <p>Vui lòng rà soát lại danh sách món và chọn phương thức thanh toán phù hợp</p>
            </div>

            <form id="checkoutForm" method="post">
                <input type="hidden" name="action" value="confirm">
                <input type="hidden" name="orderID" value="${orderID}">

                <div class="checkout-layout">

                    <div class="checkout-info-column">
                        <div class="checkout-box">
                            <div class="box-title">📍 Thông tin phục vụ</div>
                            <div class="form-group">
                                <label>Mã đơn hàng xử lý</label>
                                <div class="form-control-static">#Order-${orderID}</div>
                            </div>
                            <div class="form-group">
                                <label>Vị trí phục vụ</label>
                                <div class="form-control-static">
                                    <c:choose>
                                        <c:when test="${not empty sessionScope.tableID && sessionScope.tableID > 0}">
                                            Bàn số ${sessionScope.tableID} (Phục vụ tại chỗ) 
                                            <c:if test="${not empty sessionScope.areaType}">
                                                - Khu vực: ${sessionScope.areaType}
                                            </c:if>
                                            <c:if test="${not empty sessionScope.capacity && sessionScope.capacity > 0}">
                                                (${sessionScope.capacity} chỗ)
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            Đơn hàng mang về (Take-away)
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                        <div class="checkout-box">
                            <div class="box-title">💳 Phương thức thanh toán</div>
                            <div class="payment-methods-grid">

                                <label class="payment-method-card active" onclick="selectPayment(this)">
                                    <input type="radio" name="paymentGateway" value="cash" checked>
                                    <div class="payment-method-info">
                                        <div class="payment-method-title">Thanh toán tại quầy</div>
                                        <div class="payment-method-desc">Quý khách vui lòng thanh toán bằng tiền mặt hoặc quẹt thẻ tại quầy sau khi dùng bữa</div>
                                    </div>
                                    <svg class="payment-logo" viewBox="0 0 24 24" style="fill: #bc945c; width:32px; height:32px;">
                                    <path d="M21,18H3V6H21M21,4H3C1.89,4 1,4.89 1,6V18C1,19.1 1.89,20 3,20H21C22.1,20 23,19.1 23,18V6C23,4.89 22.1,4 21,4M12,10A2,2 0 0,0 10,12A2,2 0 0,0 12,14A2,2 0 0,0 14,12A2,2 0 0,0 12,10Z"/>
                                    </svg>
                                </label>

                                <label class="payment-method-card" onclick="selectPayment(this)">
                                    <input type="radio" name="paymentGateway" value="vnpay">
                                    <div class="payment-method-info">
                                        <div class="payment-method-title">Thanh toán Online qua VNPay</div>
                                        <div class="payment-method-desc">Chuyển hướng an toàn qua cổng VNPay hỗ trợ ATM, Visa, Master hoặc quét mã QR Code</div>
                                    </div>
                                    <img class="payment-logo" src="https://sandbox.vnpayment.vn/paymentv2/Images/brands/logo.svg" alt="VNPay Logo" style="width: 55px;">
                                </label>

                            </div>
                        </div>
                    </div>

                    <div class="checkout-summary-sidebar">
                        <div class="box-title" style="border-bottom:none; margin-bottom:0; padding-bottom:0;">
                            🍽️ Món ăn đã chọn
                        </div>
                        <div class="summary-divider"></div>

                        <div class="checkout-items-list">
                            <c:forEach var="oi" items="${orderItems}" varStatus="loop">
                                <c:set var="mi" value="${menuItems[loop.index]}"/>

                                <c:choose>
                                    <c:when test="${mi.discountedPrice > 0}">
                                        <c:set var="unitPrice" value="${mi.discountedPrice}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="unitPrice" value="${mi.price}"/>
                                    </c:otherwise>
                                </c:choose>
                                <c:set var="lineTotal" value="${unitPrice * oi.quantity}"/>

                                <div class="checkout-item-row">
                                    <div>
                                        <div class="checkout-item-name">${mi.itemName}</div>
                                        <div class="checkout-item-qty">Số lượng: ${oi.quantity} x <fmt:formatNumber value="${unitPrice}" type="number"/> VNĐ</div>
                                    </div>
                                    <div class="checkout-item-price">
                                        <fmt:formatNumber value="${lineTotal}" type="number" maxFractionDigits="0"/> VNĐ
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <%-- 🌟 ĐÃ SỬA: Hiển thị tiền theo dữ liệu từ Controller (đối tượng invoice) --%>
                        <div class="summary-row">
                            <span>Tạm tính đơn chọn:</span>
                            <span><fmt:formatNumber value="${invoice.subTotal}" type="number"/> VNĐ</span>
                        </div>
                        
                        <div class="summary-row">
                            <span>Phí dịch vụ nhà hàng:</span>
                            <span><fmt:formatNumber value="${invoice.taxAmount}" type="number"/> VNĐ</span>
                        </div>

                        <%-- Kiểm tra và hiển thị tiền đặt cọc (Nếu có) --%>
                        <c:if test="${not empty invoice && invoice.depositDeducted > 0}">
                            <div class="summary-row" style="color: #10b981; font-weight: 500;">
                                <span>Đã đặt cọc trước:</span>
                                <span>- <fmt:formatNumber value="${invoice.depositDeducted}" type="number"/> VNĐ</span>
                            </div>
                        </c:if>

                        <div class="summary-divider"></div>

                        <div class="summary-row total-row">
                            <span>Tổng thanh toán:</span>
                            <div class="summary-total-amount">
                                <fmt:formatNumber value="${invoice.finalAmount}" type="number" maxFractionDigits="0"/> VNĐ
                            </div>
                        </div>

                        <button type="button" class="btn-checkout-submit" onclick="processCheckoutRouting()">
                            🔔 Xác nhận & Hoàn tất đơn ↗
                        </button>

                        <a href="${pageContext.request.contextPath}/order?action=cart" class="btn-checkout-back">
                            ← Quay lại sửa giỏ hàng
                        </a>
                    </div>

                </div>
            </form>
        </div>

        <%@ include file="/views/includes/footer.jsp" %>

        <script>
            // Đổi hiệu ứng màu sắc khi chọn thẻ thanh toán
            function selectPayment(element) {
                document.querySelectorAll('.payment-method-card').forEach(function (card) {
                    card.classList.remove('active');
                });
                element.classList.add('active');
                var radio = element.querySelector('input[type="radio"]');
                if (radio) {
                    radio.checked = true;
                }
            }

            // XỬ LÝ ĐỊNH TUYẾN FORM DỰA VÀO PHƯƠNG THỨC ĐƯỢC CHỌN
            function processCheckoutRouting() {
                var paymentMethod = document.querySelector('input[name="paymentGateway"]:checked').value;
                var form = document.getElementById('checkoutForm');

                // LUÔN LUÔN GỬI FORM VỀ CHECKOUT ĐỂ JAVA XỬ LÝ DATABASE TRƯỚC
                form.action = '${pageContext.request.contextPath}/checkout';

                if (paymentMethod === 'cash') {
                    alert("🎉 Hoàn tất đơn hàng! Vui lòng thanh toán tại quầy sau khi dùng bữa.");
                }

                // Gửi form đi
                form.submit();
            }
        </script>
    </body>
</html>