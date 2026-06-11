<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Giỏ hàng - Vị An Restaurant</title>
        <style>
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }
            body {
                font-family: 'Segoe UI', Arial, sans-serif;
                background: #f7f5f0; /* Nền kem sáng ấm áp, đặc trưng phong cách Vị An */
                color: #2c2520; /* Chữ màu nâu đen trầm charcoal dễ đọc */
                /* ĐÃ BỎ lệnh display: flex; và justify-content: center; ở đây */
            }
            .container {
                width: 100%;
                max-width: 1100px;
                margin: 32px auto; /* THÊM margin: auto để tự động căn giữa trang */
                padding: 0 16px;   /* Giữ khoảng cách an toàn 2 bên trên màn hình điện thoại */
                min-height: 60vh;  /* Đẩy footer xuống dưới nếu giỏ hàng trống/ngắn */
            }

            /* Header giỏ hàng */
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
                color: #1c4332; /* Màu xanh rêu đậm sang trọng */
                font-weight: bold;
            }
            .cart-title p {
                font-size: 14px;
                color: #7a6e65;
                margin-top: 4px;
            }
            .select-all-wrap {
                display: flex;
                align-items: center;
                gap: 8px;
                font-size: 15px;
                font-weight: 600;
                color: #1c4332;
                cursor: pointer;
            }

            /* Bố cục chính: Cột món ăn (Trái) & Tóm tắt đơn hàng (Phải) */
            .cart-main-layout {
                display: flex;
                gap: 24px;
                align-items: flex-start;
            }

            /* Cột bên trái: Danh sách các thẻ món ăn */
            .cart-items-column {
                flex: 1;
                display: flex;
                flex-direction: column;
                gap: 16px;
            }

            /* Thẻ món ăn (Card Item) - Nền trắng tinh khôi, đổ bóng nhẹ mềm mại */
            .cart-item-card {
                background: #ffffff;
                color: #2c2520;
                border-radius: 12px;
                padding: 20px;
                display: flex;
                align-items: center;
                gap: 16px;
                border: 1px solid #eae5da;
                box-shadow: 0 4px 12px rgba(44, 37, 32, 0.04);
                position: relative;
            }

            /* Checkbox tùy biến theo màu thương hiệu */
            input[type=checkbox] {
                width: 20px;
                height: 20px;
                cursor: pointer;
                accent-color: #1c4332; /* Tích chọn màu xanh rêu */
            }

            /* Khung ảnh món ăn sáng sủa */
            .item-image-placeholder {
                width: 85px;
                height: 85px;
                background: #fdfcfb;
                border: 1px solid #ede8de;
                border-radius: 8px;
                display: flex;
                align-items: center;
                justify-content: center;
                flex-shrink: 0;
            }
            .item-image-placeholder svg {
                width: 42px;
                height: 42px;
                fill: #bc945c; /* Icon màu vàng đồng quý phái */
            }

            /* Thông tin văn bản của món */
            .item-details {
                flex: 1;
            }
            .item-name {
                font-size: 18px;
                font-weight: 700;
                color: #1c4332; /* Tên món màu xanh rêu */
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
                color: #bc945c; /* Ghi chú màu vàng đồng */
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

            /* Khối điều khiển bên phải của Thẻ (Số lượng, thành tiền, nút xóa) */
            .item-actions-wrap {
                display: flex;
                flex-direction: column;
                align-items: flex-end;
                gap: 12px;
                min-width: 140px;
            }

            /* Cụm tăng giảm số lượng - Nền sáng kem tinh tế */
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
            .qty-control-box input::-webkit-outer-spin-button,
            .qty-control-box input::-webkit-inner-spin-button {
                -webkit-appearance: none;
                margin: 0;
            }

            /* Thành tiền của từng món - Nâu đậm nổi bật */
            .item-line-total {
                font-size: 16px;
                font-weight: 700;
                color: #2c2520;
            }

            /* Nút Thùng rác xóa món nhã nhặn */
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

            /* Cột bên phải: Khối Tóm tắt hóa đơn (Nền trắng đồng bộ Vị An) */
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
            .selected-count-note {
                font-size: 12px;
                color: #8a7e75;
                font-style: italic;
            }

            /* Các nút hành động chính ở Sidebar */
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
            /* Nút thanh toán chính: Màu xanh rêu, chữ trắng tinh tế */
            .btn-sidebar-checkout {
                background: #1c4332;
                border: none;
                color: #ffffff;
                box-shadow: 0 4px 10px rgba(28, 67, 50, 0.2);
            }
            .btn-sidebar-checkout:hover:not(:disabled) {
                background: #275d46;
                box-shadow: 0 4px 12px rgba(28, 67, 50, 0.3);
            }
            .btn-sidebar-checkout:disabled {
                background: #dcd9d2;
                color: #a19c95;
                box-shadow: none;
                cursor: not-allowed;
            }
            /* Nút chọn tiếp: Màu kem nhạt, viền mảnh mảnh quý phái */
            .btn-sidebar-continue {
                background: #fbf9f6;
                border: 1px solid #dcd5c5;
                color: #5e5550;
            }
            .btn-sidebar-continue:hover {
                background: #f5f2eb;
                color: #1c4332;
                border-color: #bc945c;
            }

            /* Trạng thái giỏ hàng trống */
            .empty-state {
                text-align: center;
                padding: 64px 0;
                background: #ffffff;
                border-radius: 12px;
                border: 1px solid #eae5da;
                box-shadow: 0 4px 12px rgba(44, 37, 32, 0.02);
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
            .empty-state a:hover {
                text-decoration: underline;
            }

            /* Thanh hiển thị số lượng chọn ở dưới cùng danh sách món */
            .bottom-count-bar {
                margin-top: 16px;
                font-size: 14px;
                color: #7a6e65;
                font-weight: 600;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div class="container">

            <div class="cart-header">
                <div class="cart-title">
                    <h2>Giỏ hàng của bạn</h2>
                </div>
                <c:if test="${not empty orderItems}">
                    <label class="select-all-wrap">
                        <input type="checkbox" id="checkAll" onclick="toggleAll(this)">
                        Chọn tất cả
                    </label>
                </c:if>
            </div>

            <%-- ===== TRƯỜNG HỢP GIỎ HÀNG TRỐNG ===== --%>
            <c:if test="${empty orderItems}">
                <div class="empty-state">
                    <p>Giỏ hàng của bạn đang trống.</p>
                    <a href="${pageContext.request.contextPath}/menu">Chọn món ngay →</a>
                </div>
            </c:if>

            <%-- ===== TRƯỜNG HỢP CÓ MÓN TRONG GIỎ ===== --%>
            <c:if test="${not empty orderItems}">
                <div class="cart-main-layout">

                    <div class="cart-items-column">
                        <c:forEach var="oi" items="${orderItems}" varStatus="loop">
                            <c:set var="mi" value="${menuItems[loop.index]}"/>

                            <%-- Tự động tính toán đơn giá phù hợp --%>
                            <c:choose>
                                <c:when test="${mi.discountedPrice > 0}">
                                    <c:set var="unitPrice" value="${mi.discountedPrice}"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="unitPrice" value="${mi.price}"/>
                                </c:otherwise>
                            </c:choose>
                            <c:set var="lineTotal" value="${unitPrice * oi.quantity}"/>

                            <div class="cart-item-card">
                                <input type="checkbox" 
                                       class="item-checkbox" 
                                       value="${oi.orderItemID}" 
                                       data-price="${lineTotal}" 
                                       onchange="updateTotal()">

                                <div class="item-image-placeholder">
                                    <svg viewBox="0 0 24 24">
                                    <path d="M22,9C22,11 20.4,12.6 18.5,12.9L19.8,20.2C20,21.2 19.2,22 18.2,22H5.8C4.8,22 4,21.2 4.2,20.2L5.5,12.9C3.6,12.6 2,11 2,9H22M12,2A3,3 0 0,1 15,5V7H9V5A3,3 0 0,1 12,2M12,14A2,2 0 0,0 10,16V18A2,2 0 0,0 12,20A2,2 0 0,0 14,18V16A2,2 0 0,0 12,14Z"/>
                                    </svg>
                                </div>

                                <div class="item-details">
                                    <div class="item-name">${mi.itemName}</div>
                                    <div class="item-description">${mi.description != null ? mi.description : 'Món ăn đặc sắc mang đậm phong vị quê hương.'}</div>

                                    <div class="item-unit-price">
                                        <c:if test="${mi.discountedPrice > 0}">
                                            <span class="price-original">
                                                <fmt:formatNumber value="${mi.price}" type="number" maxFractionDigits="0"/> VNĐ
                                            </span>
                                        </c:if>
                                        <fmt:formatNumber value="${unitPrice}" type="number" maxFractionDigits="0"/> VNĐ / phần
                                    </div>

                                    <c:if test="${not empty oi.note}">
                                        <div class="item-note">✍️ Ghi chú: ${oi.note}</div>
                                    </c:if>
                                </div>

                                <div class="item-actions-wrap">
                                    <form method="post" action="${pageContext.request.contextPath}/order">
                                        <input type="hidden" name="action" value="update">
                                        <input type="hidden" name="orderItemID" value="${oi.orderItemID}">

                                        <div class="qty-control-box">
                                            <button type="button" class="qty-btn" onclick="changeQty(this, -1)">-</button>
                                            <input type="number" name="quantity" value="${oi.quantity}" min="1" max="99"
                                                   onkeypress="return event.charCode >= 48 && event.charCode <= 57"
                                                   onchange="validateQty(this)">
                                            <button type="button" class="qty-btn" onclick="changeQty(this, 1)">+</button>
                                        </div>
                                        <span class="qty-error" style="display:none; color:#e74c3c; font-size:11px; margin-top:4px;">Từ 1-99</span>
                                    </form>

                                    <div class="item-line-total">
                                        <fmt:formatNumber value="${lineTotal}" type="number" maxFractionDigits="0"/> VNĐ
                                    </div>

                                    <form method="post" action="${pageContext.request.contextPath}/order">
                                        <input type="hidden" name="action" value="remove">
                                        <input type="hidden" name="orderItemID" value="${oi.orderItemID}">
                                        <button class="btn-delete-icon" type="submit" onclick="return confirm('Xóa món này khỏi giỏ?')">
                                            <svg viewBox="0 0 24 24">
                                            <path d="M19,4H15.5L14.5,3H9.5L8.5,4H5V6H19M6,19A2,2 0 0,0 8,21H16A2,2 0 0,0 18,19V7H6V19Z"/>
                                            </svg>
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </c:forEach>

                        <div class="bottom-count-bar" id="bottomCountDisplay">Đã chọn 0 món</div>
                    </div>

                    <div class="cart-summary-sidebar">
                        <div class="summary-title">Tóm tắt đơn hàng</div>

                        <div class="summary-divider"><span>Chi tiết</span></div>

                        <div class="summary-row">
                            <span>Tạm tính:</span>
                            <span id="totalDisplay">0 VNĐ</span>
                        </div>
                        <div class="summary-row">
                            <span>Phí dịch vụ:</span>
                            <span>0 VNĐ</span>
                        </div>

                        <div class="summary-row">
                            <span class="selected-count-note" id="selectedNote">Chưa chọn món nào</span>
                        </div>

                        <div class="summary-divider"></div>

                        <div class="summary-row total-row">
                            <span>Tổng cộng:</span>
                            <div class="summary-total-amount" id="totalDisplaySummary">0 VNĐ</div>
                        </div>

                        <button class="btn-summary-action btn-sidebar-checkout" id="btnCheckout"
                                type="button" disabled onclick="submitCheckout()">
                            💳 Tiến hành thanh toán ↗
                        </button>

                        <a href="${pageContext.request.contextPath}/menu" class="btn-summary-action btn-sidebar-continue">
                            ← Tiếp tục mua sắm
                        </a>
                    </div>

                </div>
            </c:if>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>
        <script>
            // Tích hoặc bỏ tích toàn bộ các checkbox món ăn
            function toggleAll(source) {
                document.querySelectorAll('.item-checkbox')
                        .forEach(cb => cb.checked = source.checked);
                updateTotal();
            }

            // Tính toán tổng số lượng và cập nhật tiền hiển thị theo thời gian thực
            function updateTotal() {
                var checked = document.querySelectorAll('.item-checkbox:checked');
                var total = 0;
                checked.forEach(cb => total += parseFloat(cb.getAttribute('data-price')));

                var formattedPrice = total.toLocaleString('vi-VN') + ' VNĐ';
                document.getElementById('totalDisplay').innerText = formattedPrice;
                document.getElementById('totalDisplaySummary').innerText = formattedPrice;

                var count = checked.length;
                document.getElementById('selectedNote').innerText =
                        count > 0 ? 'Tính theo ' + count + ' món đã chọn' : 'Chưa chọn món nào';
                document.getElementById('bottomCountDisplay').innerText = 'Đã chọn ' + count + ' món';

                document.getElementById('btnCheckout').disabled = (count === 0);

                var all = document.querySelectorAll('.item-checkbox');
                var checkAllBox = document.getElementById('checkAll');
                if (checkAllBox) {
                    checkAllBox.checked = (count === all.length && all.length > 0);
                }
            }

            // Thao tác bấm nút cộng trừ số lượng ngay trên giao diện
            function changeQty(btn, amount) {
                var input = btn.parentElement.querySelector('input[type=number]');
                var currentVal = parseInt(input.value);
                if (!isNaN(currentVal)) {
                    var newVal = currentVal + amount;
                    if (newVal >= 1 && newVal <= 99) {
                        input.value = newVal;
                        validateQty(input);
                    }
                }
            }

            // Kiểm tra tính hợp lệ dữ liệu số lượng ở phía Client-side trước khi Submit form
            function validateQty(input) {
                var errorSpan = input.parentElement.nextElementSibling;
                var val = parseInt(input.value);

                if (isNaN(val) || val < 1 || val > 99) {
                    errorSpan.style.display = 'block';
                    input.focus();
                    return;
                }

                errorSpan.style.display = 'none';
                input.form.submit();
            }

            // Tạo form ẩn độc lập bằng JavaScript để đóng gói các bản ghi được chọn đem sang /checkout
            function submitCheckout() {
                var checked = document.querySelectorAll('.item-checkbox:checked');
                if (checked.length === 0) {
                    alert('Vui lòng chọn ít nhất 1 món!');
                    return;
                }

                var form = document.createElement('form');
                form.method = 'post';
                form.action = '${pageContext.request.contextPath}/checkout';

                var inputOrderID = document.createElement('input');
                inputOrderID.type = 'hidden';
                inputOrderID.name = 'orderID';
                inputOrderID.value = '${orderID}';
                form.appendChild(inputOrderID);

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