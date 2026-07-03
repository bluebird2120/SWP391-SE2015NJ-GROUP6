<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Giỏ món đặt trước</title>
        <style>
            body {
                margin:0;
                background:#faf7f2;
                color:#173f32;
                font-family:Arial,sans-serif;
            }
            .preorder-wrap {
                max-width:1050px;
                margin:36px auto;
                padding:0 20px;
            }
            .preorder-card {
                background:#fff;
                border:1px solid #eadfd1;
                border-radius:14px;
                padding:20px;
                margin-bottom:14px;
                box-shadow:0 4px 14px rgba(0,0,0,.04);
            }
            .item-row {
                display:grid;
                grid-template-columns:90px 1fr auto;
                gap:18px;
                align-items:center;
            }
            .item-row img {
                width:90px;
                height:75px;
                border-radius:10px;
                object-fit:cover;
            }
            .item-name {
                font-size:18px;
                font-weight:700;
                margin-bottom:6px;
            }
            .actions {
                display:flex;
                align-items:center;
                gap:8px;
            }
            .qty {
                width:64px;
                padding:8px;
                border:1px solid #d8c8b7;
                border-radius:7px;
            }
            .btn {
                border:0;
                border-radius:8px;
                padding:10px 15px;
                cursor:pointer;
                text-decoration:none;
                display:inline-block;
                font-weight:700;
            }
            .btn-main {
                background:#174f3d;
                color:#fff;
            }
            .btn-light {
                background:#f3eadf;
                color:#5d4032;
            }
            .btn-danger {
                background:#fee2e2;
                color:#b91c1c;
            }
            .summary {
                display:flex;
                justify-content:space-between;
                align-items:center;
                background:#fff;
                border-radius:14px;
                padding:20px;
                border:1px solid #eadfd1;
            }
            .empty {
                text-align:center;
                padding:55px 20px;
                background:#fff;
                border-radius:14px;
            }
            .error-message {
                margin:0 0 14px;
                padding:12px 14px;
                border-radius:10px;
                background:#fee2e2;
                color:#b91c1c;
                font-weight:700;
            }
            @media (max-width:700px) {
                .item-row {
                    grid-template-columns:70px 1fr;
                }
                .item-row img {
                    width:70px;
                    height:65px;
                }
                .actions {
                    grid-column:1 / -1;
                    justify-content:flex-end;
                }
                .summary {
                    flex-direction:column;
                    gap:15px;
                    align-items:stretch;
                }
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div class="preorder-wrap">
            <h2>Danh sách món đặt trước </h2>
            <c:if test="${not empty cartError}">
                <div class="error-message">${cartError}</div>
            </c:if>

            <c:choose>
                <c:when test="${empty orderItems}">
                    <div class="empty">
                        <p>Bạn chưa chọn món nào.</p>
                        <a class="btn btn-main"
                           href="${pageContext.request.contextPath}/menu?reservation=true&orderID=${orderID}">
                            Chọn món ngay
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:set var="total" value="0"/>
                    <c:forEach var="oi" items="${orderItems}" varStatus="loop">
                        <c:set var="mi" value="${menuItems[loop.index]}"/>
                        <c:set var="lineTotal" value="${oi.price * oi.quantity}"/>
                        <c:set var="total" value="${total + lineTotal}"/>

                        <div class="preorder-card item-row">
                            <img src="${mi.image}" alt="${mi.itemName}">
                            <div>
                                <div class="item-name">${mi.itemName}</div>
                                <div>
                                    <fmt:formatNumber value="${oi.price}" type="number"/> VNĐ/phần
                                </div>
                                <strong>
                                    Thành tiền:
                                    <fmt:formatNumber value="${lineTotal}" type="number"/> VNĐ
                                </strong>
                            </div>
                            <div class="actions">
                                <form method="post"
                                      action="${pageContext.request.contextPath}/reservation">
                                    <input type="hidden" name="action"
                                           value="updatePreorderItem">
                                    <input type="hidden" name="orderItemID"
                                           value="${oi.orderItemID}">
                                    <%--    
                                        Validate nhập số lượng 
                                         để bắt được cả chữ/ký tự đặc biệt trước khi submit. --%>
                                    <input class="qty" type="text" name="quantity"
                                           inputmode="numeric" maxlength="2"
                                           value="${oi.quantity}"
                                           data-old-value="${oi.quantity}"
                                           onchange="validateQuantityAndSubmit(this)">

                                </form>
                                <form method="post"
                                      action="${pageContext.request.contextPath}/reservation">
                                    <input type="hidden" name="action"
                                           value="removePreorderItem">
                                    <input type="hidden" name="orderItemID"
                                           value="${oi.orderItemID}">
                                    <button class="btn btn-danger" type="submit"
                                            onclick="return confirm('Xóa món này khỏi giỏ?')">
                                        Xóa
                                    </button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>

                    <div class="summary">
                        <div>
                            <div>Tổng tiền món dự kiến</div>
                            <h2><fmt:formatNumber value="${total}" type="number"/> VNĐ</h2>
                            <div>
                                Cọc bàn đã thanh toán:
                                <strong>
                                    <fmt:formatNumber value="${depositAmount}"
                                                      type="number"/> VNĐ
                                </strong>
                            </div>
                            <small>Tiền món được thanh toán khi dùng bữa.</small>
                        </div>
                        <div>
                            <a class="btn btn-light"
                               href="${pageContext.request.contextPath}/menu?reservation=true&orderID=${orderID}">
                                ← Chọn thêm món
                            </a>
                            <form method="post"
                                  action="${pageContext.request.contextPath}/reservation"
                                  style="display:inline;">
                                <input type="hidden" name="action"
                                       value="confirmPreorder">
                                <button class="btn btn-main" type="submit"
                                        onclick="return confirmSavePreorder();">
                                    Lưu món đặt trước
                                </button>
                            </form>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>
        <script>
            
            
            // Validate số lượng ở phía giao diện:
            // chỉ nhận số nguyên dương từ 1 đến 99, không nhận chữ/ký tự/số âm.
            function validateQuantityAndSubmit(input) {
                const value = input.value.trim();
                if (!/^\d+$/.test(value)) {
                    alert("Số lượng món phải là số nguyên dương từ 1 đến 99.");
                    input.value = input.dataset.oldValue || "1";
                    input.focus();
                    return false;
                }

                const quantity = Number(value);
                if (!Number.isInteger(quantity) || quantity < 1 || quantity > 99) {
                    alert("Số lượng món phải là số nguyên dương từ 1 đến 99.");
                    input.value = input.dataset.oldValue || "1";
                    input.focus();
                    return false;
                }

                input.form.submit();
                return true;
            }

            function confirmSavePreorder() {
                const quantityInputs = document.querySelectorAll(".qty");
                for (const input of quantityInputs) {
                    const value = input.value.trim();
                    const quantity = Number(value);
                    if (!/^\d+$/.test(value)
                            || !Number.isInteger(quantity)
                            || quantity < 1 || quantity > 99) {
                        alert("Vui lòng nhập số lượng món là số nguyên dương từ 1 đến 99 trước khi lưu.");
                        input.focus();
                        return false;
                    }
                }
                return confirm("Bạn có chắc chắn muốn lưu các món đặt trước này không?");
            }
        </script>
    </body>
</html>
