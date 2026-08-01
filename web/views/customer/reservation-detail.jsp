<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Chi tiết đơn đặt bàn</title>
        <style>
            body {
                margin: 0;
                font-family: Arial, sans-serif;
                background: #fff6e6;
                color: #2d160b;
            }
            .page {
                max-width: 1120px;
                margin: 0 auto;
                padding: 28px 20px 60px;
            }
            .top {
                display: flex;
                justify-content: space-between;
                gap: 16px;
                align-items: flex-start;
                margin-bottom: 24px;
            }
            h1 {
                margin: 0 0 6px;
                color: #5d2f1f;
                font-size: 34px;
                border-bottom: 2px solid #9a673e;
                padding-bottom: 6px;
            }
            .muted {
                color: #7b604d;
                margin: 0;
            }
            .grid {
                display: grid;
                grid-template-columns: 430px 1fr;
                gap: 24px;
            }
            .card {
                background: #fffdf8;
                border: 1px solid #e4d4bf;
                border-radius: 10px;
                padding: 24px;
                box-shadow: 0 4px 14px rgba(55, 32, 16, .08);
                margin-bottom: 24px;
            }
            .card h2 {
                margin: 0 0 14px;
                color: #3b1d0f;
                font-size: 22px;
            }
            .box {
                background: #f3eadc;
                border-radius: 8px;
                padding: 14px;
                margin-top: 12px;
            }
            .btn {
                display: inline-block;
                border: 0;
                border-radius: 7px;
                padding: 10px 16px;
                text-decoration: none;
                font-weight: 700;
                cursor: pointer;
            }
            .btn-primary {
                background: #76493b;
                color: white;
            }
            .btn-outline {
                border: 1px solid #7b604d;
                color: #5d2f1f;
                background: transparent;
            }
            .btn-danger {
                color: #d64242;
                background: transparent;
            }
            .actions {
                display: flex;
                gap: 12px;
                flex-wrap: wrap;
                align-items: center;
            }
            .alert {
                border-radius: 8px;
                padding: 12px 14px;
                margin-bottom: 16px;
            }
            .alert-success {
                background: #dff4e7;
                color: #0f5132;
                border: 1px solid #b7e3c7;
            }
            .alert-error {
                background: #fde2e2;
                color: #842029;
                border: 1px solid #f3b8b8;
            }
            .form-row {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 18px;
                margin: 18px 0 12px;
            }
            label {
                display: block;
                font-weight: 700;
                margin-bottom: 8px;
            }
            input, select {
                width: 100%;
                box-sizing: border-box;
                padding: 10px 12px;
                border: 1px solid #d8c7b4;
                border-radius: 6px;
                font-size: 15px;
                background: white;
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin: 16px 0;
                background: white;
            }
            th, td {
                text-align: left;
                padding: 12px 10px;
                border-bottom: 1px solid #eadbc9;
            }
            th {
                color: #3b1d0f;
                background: #fffaf2;
            }
            .badge {
                display: inline-block;
                padding: 4px 9px;
                border-radius: 999px;
                background: #169356;
                color: white;
                font-weight: 700;
                font-size: 13px;
            }
            @media (max-width: 900px) {
                .grid, .form-row {
                    grid-template-columns: 1fr;
                }
            }
        </style>
    </head>
    <body>
        <div class="page">
            <div class="top">
                <div>
                    <h1>Chi tiết đơn đặt bàn #${order.orderID}</h1>
                    <p class="muted">Xem thông tin đặt bàn, món đặt trước và chỉnh sửa nếu còn đủ điều kiện.</p>
                </div>
                <a class="btn btn-outline" href="${pageContext.request.contextPath}/reservation?action=history">
                    ← Quay lại lịch sử
                </a>
            </div>

            <c:if test="${param.updated == 'true'}">
                <div class="alert alert-success">Đã lưu thay đổi đặt bàn.</div>
            </c:if>
            <c:if test="${param.error == 'update_failed'}">
                <div class="alert alert-error">Không thể lưu thay đổi. Vui lòng thử lại.</div>
            </c:if>
            <c:if test="${not empty editError}">
                <div class="alert alert-error">${editError}</div>
            </c:if>

            <div class="grid">
                <div>
                    <div class="card">
                        <h2>Thông tin hiện tại</h2>
                        <p>Trạng thái: <strong>${order.orderStatus}</strong></p>
                        <p>
                            Ngày đến:
                            <strong><fmt:formatDate value="${order.orderTime}" pattern="HH:mm - dd/MM/yyyy"/></strong>
                        </p>
                        <p>
                            Tiền cọc bàn:
                            <strong><fmt:formatNumber value="${order.depositAmount}" type="number"/> VNĐ</strong>
                        </p>

                        <div class="box">
                            <strong>Chi tiết bàn</strong>
                            <ul>
                                <c:forEach var="detail" items="${orderDetails}">
                                    <li>
                                        <strong>${detail.quantity} bàn ${detail.capacity} chỗ</strong>
                                        - ${detail.areaType == 'private' ? 'Trong phòng' : 'Ngoài sảnh'}
                                    </li>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>

                    <div class="card">
                        <h2>Món đặt trước</h2>
                        <c:choose>
                            <c:when test="${not empty preorderItems}">
                                <ul>
                                    <c:forEach var="item" items="${preorderItems}" varStatus="loop">
                                        <li>
                                            <strong>${preorderMenus[loop.index].itemName}</strong>
                                            x ${item.quantity}
                                            -
                                            <fmt:formatNumber value="${item.price * item.quantity}" type="number"/> VNĐ
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:when>
                            <c:otherwise>
                                <p class="muted">Chưa có món đặt trước.</p>
                            </c:otherwise>
                        </c:choose>
                        <p>
                            <strong>Tổng tiền món dự kiến:</strong>
                            <fmt:formatNumber value="${preorderTotal}" type="number"/> VNĐ
                        </p>

                        <c:if test="${order.orderStatus == 'reserved'}">
                            <div class="actions">
                                <a class="btn btn-primary"
                                   href="${pageContext.request.contextPath}/reservation?action=preorder&orderID=${order.orderID}">
                                    Xem / chỉnh sửa món đặt trước
                                </a>
                                <a class="btn btn-danger"
                                   href="${pageContext.request.contextPath}/reservation?action=cancel&orderID=${order.orderID}"
                                   onclick="return confirm('Bạn chắc chắn muốn hủy giữ chỗ đơn #${order.orderID}?')">
                                    ✕ Hủy giữ chỗ
                                </a>
                            </div>
                        </c:if>
                    </div>
                </div>

                <div class="card">
                    <h2>Chỉnh sửa bàn / giờ đến</h2>
                    <p class="muted">Chỉ sửa được khi đơn còn giữ bàn và trước giờ đến ít nhất 2 tiếng.</p>

                    <c:choose>
                        <c:when test="${canEditReservation}">
                            <%-- [EDIT RESERVATION] Form cap nhat lai gio den/khu vuc/so luong ban da dat. --%>
                            <form method="post" action="${pageContext.request.contextPath}/reservation">
                                <input type="hidden" name="action" value="updateReservation">
                                <input type="hidden" name="orderID" value="${order.orderID}">

                                <div class="form-row">
                                    <div>
                                        <label for="orderTime">Ngày giờ đến</label>
                                        <input id="orderTime" name="orderTime" type="datetime-local"
                                               value="${editOrderTime}" required>
                                    </div>
                                    <div>
                                        <label for="areaType">Khu vực</label>
                                        <select id="areaType" name="areaType" required>
                                            <c:forEach var="area" items="${areaTypes}">
                                                <option value="${area}" ${area == editAreaType ? 'selected' : ''}>
                                                    ${area == 'private' ? 'Trong phòng' : 'Ngoài sảnh'}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>

                                <button type="button" class="btn btn-outline" onclick="checkAvailableTables()">
                                    Kiểm tra bàn trống
                                </button>

                                <table>
                                    <thead>
                                        <tr>
                                            <th>Loại bàn</th>
                                            <th>Còn trống</th>
                                            <th>Số lượng muốn đặt</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="g" items="${tableGroups}">
                                            <c:set var="selectionKey" value="${editAreaType}_${g.capacity}" />
                                            <c:set var="selectedQuantity" value="${selectedQuantities[selectionKey]}" />
                                            <tr>
                                                <td>Bàn ${g.capacity} chỗ</td>
                                                <td><span class="badge">${g.isActive}</span></td>
                                                <td>
                                                    <input type="number"
                                                           name="selection_${selectionKey}"
                                                           min="0"
                                                           max="${g.isActive}"
                                                           value="${empty selectedQuantity ? 0 : selectedQuantity}">
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>

                                <button type="submit" class="btn btn-primary" style="width:100%;">
                                    Lưu thay đổi đặt bàn
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <div class="alert alert-error">${editBlockReason}</div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <script>
            function checkAvailableTables() {
                const orderID = '${order.orderID}';
                const orderTime = document.getElementById('orderTime').value;
                const areaType = document.getElementById('areaType').value;
                let url = '${pageContext.request.contextPath}/reservation?action=detail&orderID='
                    + encodeURIComponent(orderID)
                    + '&orderTime=' + encodeURIComponent(orderTime)
                    + '&areaType=' + encodeURIComponent(areaType);
                window.location.href = url;
            }
        </script>
    </body>
</html>
