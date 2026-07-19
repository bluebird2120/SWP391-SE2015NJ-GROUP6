<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Giỏ hàng - Vị An Restaurant</title>
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
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
            }
            .container {
                width: 100%;
                max-width: 1100px;
                margin: 32px auto;
                padding: 0 16px;
                min-height: 60vh;
            }

            .cart-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 24px;
                border-bottom: 2px solid #e5dfd3;
                padding-bottom: 16px;
            }
            .cart-title h2 {
                font-size: 26px;
                color: #1c4332;
                font-weight: bold;
            }
            .cart-title p {
                font-size: 14px;
                color: #7a6e65;
                margin-top: 4px;
            }

            .cart-main-layout {
                display: flex;
                gap: 24px;
                align-items: flex-start;
            }
            .cart-items-column {
                flex: 1;
                display: flex;
                flex-direction: column;
                gap: 20px;
            }

            .table-group-box {
                background: #ffffff;
                border: 1px solid #eae5da;
                border-radius: 14px;
                box-shadow: 0 4px 16px rgba(44, 37, 32, 0.05);
                overflow: hidden;
            }
            .table-group-header {
                background: #D4A373;
                color: #ffffff;
                padding: 10px 18px;
                font-weight: bold;
                font-size: 15px;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }
            .table-group-header.db-header {
                background: #1c4332;
            }
            .table-group-header .badge {
                background: rgba(255,255,255,0.25);
                padding: 2px 10px;
                border-radius: 12px;
                font-size: 11px;
                text-transform: uppercase;
                letter-spacing: 0.5px;
            }

            .cart-item-card {
                background: #ffffff;
                padding: 20px;
                display: flex;
                align-items: center;
                gap: 16px;
                border-bottom: 1px solid #f0ebe0;
            }
            .cart-item-card:last-child {
                border-bottom: none;
            }
            .cart-item-card.disabled-item {
                opacity: 0.8;
                background: #fbf9f6;
            }

            input[type=checkbox] {
                width: 20px;
                height: 20px;
                cursor: pointer;
                accent-color: #1c4332;
                flex-shrink: 0;
            }

            .item-image-placeholder {
                width: 85px;
                height: 85px;
                border-radius: 8px;
                overflow: hidden;
                flex-shrink: 0;
                background: #fdfcfb;
                border: 1px solid #ede8de;
                display: flex;
                align-items: center;
                justify-content: center;
            }
            .item-details {
                flex: 1;
            }
            .item-name {
                font-size: 17px;
                font-weight: 700;
                color: #1c4332;
                margin-bottom: 4px;
            }
            .item-description {
                font-size: 13px;
                color: #6e655f;
                margin-bottom: 6px;
                line-height: 1.4;
            }
            .item-note {
                font-size: 12px;
                color: #bc945c;
                font-style: italic;
                font-weight: 500;
            }
            .item-unit-price {
                font-size: 13px;
                color: #8a7e75;
                margin-top: 2px;
            }
            .price-original {
                text-decoration: line-through;
                margin-right: 6px;
                color: #bfaea3;
            }

            .item-actions-wrap {
                display: flex;
                flex-direction: column;
                align-items: flex-end;
                gap: 12px;
                min-width: 140px;
            }
            .qty-control-box {
                display: flex;
                align-items: center;
                background: #f5f2eb;
                border: 1px solid #e0d9cd;
                border-radius: 6px;
                padding: 2px;
            }
            .qty-btn {
                background: transparent;
                border: none;
                color: #1c4332;
                width: 28px;
                height: 28px;
                font-size: 16px;
                font-weight: bold;
                cursor: pointer;
            }
            .qty-btn:hover {
                color: #bc945c;
            }
            .qty-control-box input[type=number] {
                width: 40px;
                background: transparent;
                border: none;
                color: #2c2520;
                text-align: center;
                font-size: 14px;
                font-weight: bold;
            }
            .qty-control-box input::-webkit-outer-spin-button, .qty-control-box input::-webkit-inner-spin-button {
                -webkit-appearance: none;
                margin: 0;
            }
            .item-line-total {
                font-size: 16px;
                font-weight: 700;
                color: #2c2520;
            }

            .btn-delete-icon {
                background: #fbf9f6;
                border: 1px solid #eadecf;
                border-radius: 6px;
                width: 32px;
                height: 32px;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                color: #a8988e;
                transition: all 0.2s;
            }
            .btn-delete-icon:hover {
                background: #fff0ee;
                border-color: #f5c2bc;
                color: #e74c3c;
            }
            .btn-delete-icon svg {
                width: 16px;
                height: 16px;
                fill: currentColor;
            }

            /* Sidebar */
            .cart-summary-sidebar {
                width: 340px;
                background: #ffffff;
                color: #2c2520;
                border-radius: 12px;
                padding: 24px;
                border: 1px solid #eae5da;
                box-shadow: 0 4px 12px rgba(44, 37, 32, 0.04);
                position: sticky;
                top: 32px;
            }
            .summary-title {
                font-size: 18px;
                font-weight: 700;
                margin-bottom: 20px;
                color: #1c4332;
                text-transform: uppercase;
                letter-spacing: 0.5px;
            }
            .summary-divider {
                border-top: 1px dashed #dcd5c5;
                margin: 16px 0;
                display: flex;
                justify-content: center;
                position: relative;
            }
            .summary-divider span {
                position: absolute;
                top: -10px;
                background: #ffffff;
                padding: 0 8px;
                font-size: 12px;
                color: #a8988e;
            }
            .summary-row {
                display: flex;
                justify-content: space-between;
                font-size: 14px;
                margin-bottom: 12px;
                color: #5e5550;
            }
            .summary-row.total-row {
                font-size: 16px;
                font-weight: 700;
                color: #1c4332;
                margin-top: 8px;
            }
            .summary-total-amount {
                font-size: 22px;
                color: #1c4332;
                font-weight: 800;
            }

            .btn-summary-action {
                width: 100%;
                padding: 14px;
                border-radius: 8px;
                font-size: 15px;
                font-weight: 700;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 8px;
                text-decoration: none;
                transition: all 0.2s;
                margin-top: 12px;
            }
            .btn-sidebar-kitchen {
                background: #D4A373;
                border: none;
                color: #ffffff;
                box-shadow: 0 4px 10px rgba(212, 163, 115, 0.2);
            }
            .btn-sidebar-kitchen:hover:not(:disabled) {
                background: #bc945c;
            }
            .btn-sidebar-kitchen:disabled {
                background: #eadecf;
                color: #ffffff;
                cursor: not-allowed;
                box-shadow: none;
            }

            .btn-sidebar-checkout {
                background: #1c4332;
                border: none;
                color: #ffffff;
                box-shadow: 0 4px 10px rgba(28, 67, 50, 0.2);
            }
            .btn-sidebar-checkout:hover {
                background: #275d46;
            }
            .btn-sidebar-checkout:disabled {
                background: #dcd9d2;
                color: #a19c95;
                cursor: not-allowed;
                box-shadow: none;
            }

            .empty-state {
                text-align: center;
                padding: 64px 0;
                background: #ffffff;
                border-radius: 12px;
                border: 1px solid #eae5da;
            }
            .empty-state p {
                font-size: 16px;
                color: #7a6e65;
                margin-bottom: 16px;
            }
            .empty-state a {
                color: #bc945c;
                text-decoration: none;
                font-weight: bold;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div class="container">

            <c:if test="${not empty sessionScope.successMsg}">
                <div style="background: #d1fae5; color: #065f46; padding: 12px 20px; border-radius: 8px; margin-bottom: 20px; font-weight: bold; border-left: 4px solid #10b981;">
                    ✅ ${sessionScope.successMsg}
                </div>
                <c:remove var="successMsg" scope="session"/>
            </c:if>
            
            <%-- HIỂN THỊ THÔNG BÁO LỖI (VÍ DỤ: HẾT MÓN) --%>
            <c:if test="${not empty sessionScope.errorMsg}">
                <div style="color:#D9534F; background:#FDE8E8; padding:12px 20px; border-radius:8px; margin-bottom:20px; border-left:4px solid #D9534F; font-weight:bold; line-height: 1.5;">
                    ⚠ ${sessionScope.errorMsg}
                </div>
                <c:remove var="errorMsg" scope="session"/>
            </c:if>
            
            <c:if test="${not empty param.error && param.error == 'invalid_quantity'}">
                <div style="color:#D9534F;background:#FDE8E8;padding:12px 20px;border-radius:8px;margin-bottom:20px;border-left:4px solid #D9534F;font-weight:bold;">
                    ⚠ Hệ thống cảnh báo: Số lượng món ăn không hợp lệ! Vui lòng chỉ nhập số lượng trong khoảng từ 1 đến 99 phần.
                </div>
            </c:if>

            <%-- Nút tiếp tục chọn món nằm ở cột bên trái --%>
            <div style="margin-top: 10px;">
                <a href="${pageContext.request.contextPath}/menu" 
                   style="display: inline-flex; align-items: center; gap: 8px; text-decoration: none; color: #76493b; font-weight: 600; padding: 10px 16px; border: 1px solid #dcd5c5; border-radius: 8px; background: #ffffff; transition: 0.2s;">
                    <i class="fas fa-arrow-left"></i> Tiếp tục chọn món
                </a>
            </div>

            <div class="cart-header">
                <div class="cart-title">
                    <h2>Danh sách gọi món</h2>
                    <p style="margin-bottom:5px;">Mã đơn hàng: #${orderID}</p>
                    <p style="color:#bc945c;font-weight:bold;font-size:15px;">
                        📍 Vị trí:
                        <c:forEach var="t" items="${assignedTables}" varStatus="loop">
                            ${t.tableName}${!loop.last ? ' + ' : ''}
                        </c:forEach>
                        <c:if test="${empty assignedTables}">Mang Về</c:if>
                        </p>
                    </div>
                </div>

            <%-- ===== KHI CHƯA CÓ GÌ CẢ ===== --%>
            <c:if test="${empty dbOrderItems && empty sessionCart}">
                <div class="empty-state">
                    <p>Bàn của bạn chưa gọi món nào.</p>
                    <a href="${pageContext.request.contextPath}/menu">Xem thực đơn ngay →</a>
                </div>
            </c:if>

            <c:if test="${not empty dbOrderItems || not empty sessionCart}">
                <div class="cart-main-layout">

                    <div class="cart-items-column">

                        <%-- =============================================== --%>
                        <%-- KHU VỰC 1: CÁC MÓN ĐÃ GỬI BẾP (DATABASE - READ ONLY) --%>
                        <%-- =============================================== --%>
                        <c:if test="${not empty dbOrderItems}">
                            <h3 style="color: #1c4332; font-size: 18px; margin-bottom: 5px;">🍲 Món đã gọi (Bếp đang làm)</h3>
                            <div class="table-group-box">
                                <div class="table-group-header db-header">
                                    <span>Danh sách chờ phục vụ</span>
                                </div>
                                <c:forEach var="oi" items="${dbOrderItems}" varStatus="loop">
                                    <c:set var="mi" value="${dbMenuItems[loop.index]}"/>
                                    <c:set var="unitPrice" value="${mi.discountedPrice > 0 ? mi.discountedPrice : mi.price}"/>

                                    <div class="cart-item-card disabled-item">
                                        <div class="item-image-placeholder">
                                            <c:if test="${not empty mi.image}"><img src="${mi.image}" style="width:100%;height:100%;object-fit:cover;"></c:if>
                                            </div>
                                            <div class="item-details">
                                                <div class="item-name">${mi.itemName}</div>
                                            <div class="item-unit-price">Đã gọi: <b>${oi.quantity}</b> phần x <fmt:formatNumber value="${unitPrice}" type="number"/> VNĐ</div>
                                            <div style="font-size: 12px; color: #10b981; font-weight: bold; margin-top: 4px;"><i class="fas fa-fire"></i> Đang chuẩn bị</div>
                                            <c:if test="${not empty oi.note}"><div class="item-note" style="margin-top: 4px;">✍️ Ghi chú: ${oi.note}</div></c:if>
                                            </div>
                                            <div class="item-actions-wrap">
                                                <div class="item-line-total"><fmt:formatNumber value="${unitPrice * oi.quantity}" type="number"/> VNĐ</div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:if>


                        <%-- =============================================== --%>
                        <%-- KHU VỰC 2: GIỎ HÀNG CHỜ GỬI (SESSION CART) --%>
                        <%-- =============================================== --%>
                        <c:if test="${not empty sessionCart}">
                            <h3 style="color: #D4A373; font-size: 18px; margin-top: 25px; margin-bottom: 5px;">🛒 Món mới (Chưa gửi bếp)</h3>
                            <div class="table-group-box">
                                <div class="table-group-header">
                                    <span>Vui lòng kiểm tra và Gửi Bếp</span>
                                    <span class="badge">Đợi xác nhận</span>
                                </div>
                                <c:forEach var="oi" items="${sessionCart}" varStatus="loop">
                                    <c:set var="mi" value="${sessionMenuItems[loop.index]}"/>
                                    <c:set var="unitPrice" value="${mi.discountedPrice > 0 ? mi.discountedPrice : mi.price}"/>
                                    <c:set var="lineTotal" value="${unitPrice * oi.quantity}"/>

                                    <div class="cart-item-card">
                                        <input type="checkbox"
                                               class="item-checkbox new-item-checkbox"
                                               value="${oi.orderItemID}"
                                               data-price="${lineTotal}"
                                               onchange="updateTotal()" checked>

                                        <div class="item-image-placeholder">
                                            <c:choose>
                                                <c:when test="${not empty mi.image}">
                                                    <img src="${mi.image}" alt="${mi.itemName}" style="width:100%;height:100%;object-fit:cover;">
                                                </c:when>
                                                <c:otherwise>
                                                    <svg viewBox="0 0 24 24" style="width:42px;height:42px;fill:#bc945c;">
                                                    <path d="M22,9C22,11 20.4,12.6 18.5,12.9L19.8,20.2C20,21.2 19.2,22 18.2,22H5.8C4.8,22 4,21.2 4.2,20.2L5.5,12.9C3.6,12.6 2,11 2,9H22M12,2A3,3 0 0,1 15,5V7H9V5A3,3 0 0,1 12,2M12,14A2,2 0 0,0 10,16V18A2,2 0 0,0 12,20A2,2 0 0,0 14,18V16A2,2 0 0,0 12,14Z"/>
                                                    </svg>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

                                        <div class="item-details">
                                            <div class="item-name">${mi.itemName}</div>
                                            <div class="item-unit-price">
                                                <c:if test="${mi.discountPercent > 0}">
                                                    <span class="price-original">
                                                        <fmt:formatNumber value="${mi.price}" type="number" maxFractionDigits="0"/> VNĐ
                                                    </span>
                                                </c:if>
                                                <fmt:formatNumber value="${unitPrice}" type="number"/> VNĐ / phần
                                            </div>
                                            <c:if test="${not empty oi.note}"><div class="item-note" style="margin-top: 4px;">✍️ Ghi chú: ${oi.note}</div></c:if>
                                            </div>

                                            <div class="item-actions-wrap">
                                                <form method="post" action="${pageContext.request.contextPath}/order">
                                                <input type="hidden" name="action" value="update">
                                                <input type="hidden" name="orderItemID" value="${oi.orderItemID}">
                                                <div class="qty-control-box">
                                                    <button type="button" class="qty-btn" onclick="changeQty(this, -1)">-</button>
                                                    <input type="number" name="quantity" value="${oi.quantity}" min="1" max="99" readonly>
                                                    <button type="button" class="qty-btn" onclick="changeQty(this, 1)">+</button>
                                                </div>
                                            </form>
                                            <div class="item-line-total"><fmt:formatNumber value="${lineTotal}" type="number"/> VNĐ</div>
                                            <form method="post" action="${pageContext.request.contextPath}/order">
                                                <input type="hidden" name="action" value="remove">
                                                <input type="hidden" name="orderItemID" value="${oi.orderItemID}">
                                                <button class="btn-delete-icon" type="submit" onclick="return confirm('Xóa món này khỏi giỏ?')">
                                                    <svg viewBox="0 0 24 24"><path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z"/></svg>
                                                </button>
                                            </form>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:if>
                    </div>

                    <%-- SIDEBAR TÓM TẮT --%>
                    <div class="cart-summary-sidebar">
                        <div class="summary-title">Tổng kết bữa ăn</div>

                        <div class="summary-row">
                            <span>Đã gọi (Bếp đang làm):</span>
                            <span><fmt:formatNumber value="${totalOrderedAmount}" type="number"/> đ</span>
                        </div>
                        <div class="summary-row">
                            <span style="color:#D4A373; font-weight:bold;">Món mới (Chưa gửi):</span>
                            <span id="totalNewDisplay" style="color:#D4A373; font-weight:bold;">0 đ</span>
                        </div>

                        <%-- HIỂN THỊ TIỀN CỌC --%>
                        <c:if test="${currentOrder != null && currentOrder.depositAmount > 0}">
                            <div class="summary-row" style="color: #10b981;">
                                <span>Đã đặt cọc:</span>
                                <span>- <fmt:formatNumber value="${currentOrder.depositAmount}" type="number"/> đ</span>
                            </div>
                        </c:if>

                        <div class="summary-divider"></div>
                        <div class="summary-row total-row">
                            <span>CÒN PHẢI THANH TOÁN:</span>
                            <div class="summary-total-amount" id="totalAllDisplay">0 đ</div>
                        </div>

                        <%-- ... (Giữ nguyên các NÚT GỬI BẾP và THANH TOÁN) ... --%>

                        <%-- NÚT: GỬI BẾP (Gọi bằng Javascript) --%>
                        <button class="btn-summary-action btn-sidebar-kitchen" type="button" id="btnKitchen" 
                                onclick="submitToKitchen()" ${empty sessionCart ? 'disabled' : ''}>
                            🔔 Gửi Bếp Thực Đơn Chọn ↗
                        </button>

                        <%-- NÚT: THANH TOÁN TỔNG --%>
                        <c:if test="${sessionScope.roleInTable == 'HOST'}">
                            <%-- 🌟 ĐÃ SỬA: Đổi action sang /checkout và method thành GET --%>
                            <form method="get" action="${pageContext.request.contextPath}/checkout" style="margin-top: 20px;">
                                <button class="btn-summary-action btn-sidebar-checkout" type="submit" ${empty dbOrderItems ? 'disabled' : ''}
                                        onclick="return confirm('Bạn xác nhận muốn tính tiền toàn bộ bữa ăn để ra về?')">
                                    🧾 YÊU CẦU TÍNH TIỀN
                                </button>
                                <p style="font-size:11px; text-align:center; color:#a8988e; margin-top:8px;">(Lưu ý: Không bao gồm các món chưa Gửi Bếp)</p>
                            </form>
                        </c:if>
                    </div>
                </div>
            </c:if>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>

        <script>
            // Hàm tính toán tổng tiền realtime khi tích/bỏ tích món
            function updateTotal() {
                var checked = document.querySelectorAll('.new-item-checkbox:checked');
                var totalNew = 0;
                checked.forEach(cb => totalNew += parseFloat(cb.getAttribute('data-price')));

                var totalOrdered = ${totalOrderedAmount != null ? totalOrderedAmount : 0};
                var deposit = ${currentOrder != null ? currentOrder.depositAmount : 0}; // Lấy tiền cọc

                var totalAll = totalOrdered + totalNew - deposit;
                if (totalAll < 0)
                    totalAll = 0; // Đảm bảo không bị âm tiền

                document.getElementById('totalNewDisplay').innerText = totalNew.toLocaleString('vi-VN') + ' đ';
                document.getElementById('totalAllDisplay').innerText = totalAll.toLocaleString('vi-VN') + ' đ';

                var btnKitchen = document.getElementById('btnKitchen');
                if (btnKitchen) {
                    btnKitchen.disabled = (checked.length === 0);
                }
            }

            // Gọi hàm tính tiền ngay khi load trang
            window.onload = function () {
                updateTotal();
            };

            // Hàm tăng giảm số lượng tự động submit form để update Session
            function changeQty(btn, amount) {
                var input = btn.parentElement.querySelector('input[type=number]');
                var currentVal = parseInt(input.value);
                if (!isNaN(currentVal)) {
                    var newVal = currentVal + amount;
                    if (newVal >= 1 && newVal <= 99) {
                        input.value = newVal;
                        input.form.submit();
                    }
                }
            }

            // Hàm gom các ô được tích để submit gửi bếp (Không làm rối nested forms)
            function submitToKitchen() {
                var checked = document.querySelectorAll('.new-item-checkbox:checked');
                if (checked.length === 0) {
                    alert('Vui lòng chọn ít nhất 1 món mới để gửi bếp!');
                    return;
                }

                var form = document.createElement('form');
                form.method = 'post';
                form.action = '${pageContext.request.contextPath}/order';

                // Input điều hướng vào hàm sendToKitchen của Servlet
                var inputAction = document.createElement('input');
                inputAction.type = 'hidden';
                inputAction.name = 'action';
                inputAction.value = 'sendToKitchen';
                form.appendChild(inputAction);

                // Gom từng ID món gửi đi
                checked.forEach(function (cb) {
                    var input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'selectedItems';
                    input.value = cb.value;
                    form.appendChild(input);
                });

                document.body.appendChild(form);
                form.submit();
            }
        </script>
    </body>
</html>