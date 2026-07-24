<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Chi tiết đơn phục vụ</title>
        <style>
            * { box-sizing: border-box; }
            body { margin:0; font-family:Arial,sans-serif; background:#faf6f2; color:#3f3028; }
            .layout { display:flex; }
            .content { flex:1; padding:26px; min-width:0; }
            h1 { margin:0 0 6px; color:#76493b; }
            .sub { color:#8a6e5a; margin-bottom:18px; }
            .panel { background:#fff; border:1px solid #eaded6; border-radius:12px; overflow:hidden; margin-bottom:18px; }
            table { width:100%; border-collapse:collapse; }
            th,td { padding:12px; border-bottom:1px solid #eee3dc; text-align:left; }
            th { background:#fbf7f4; color:#76493b; }
            input[type=number] { width:90px; padding:7px; border:1px solid #d7c4b8; border-radius:6px; }
            button,.btn { border:0; border-radius:7px; padding:8px 12px; color:#fff; background:#76493b; cursor:pointer; text-decoration:none; display:inline-block; }
            .btn-green { background:#1c7c54; }
            .summary { display:grid; grid-template-columns: repeat(4, minmax(150px,1fr)); gap:12px; }
            .box { background:#fff; border:1px solid #eaded6; border-radius:10px; padding:14px; }
            .box span { display:block; color:#8a6e5a; font-size:13px; margin-bottom:5px; }
            .box strong { font-size:18px; color:#1c4332; }
            .message { padding:12px; margin-bottom:15px; background:#fff2cc; border-radius:8px; }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div class="layout">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <main class="content">
                <h1>Chi tiết đơn #${order.orderID}</h1>
                <p class="sub">Nhân viên kiểm tra món thực dùng, trừ số lượng hoàn trả nếu có, rồi xác nhận thanh toán.</p>

                <c:if test="${not empty sessionScope.staffTableMessage}">
                    <div class="message">${sessionScope.staffTableMessage}</div>
                    <c:remove var="staffTableMessage" scope="session"/>
                </c:if>

                <div class="panel">
                    <table>
                        <thead>
                            <tr>
                                <th>Món</th>
                                <th>Đơn giá</th>
                                <th>Số lượng tính tiền</th>
                                <th>Thành tiền</th>
                                <th>Ghi chú</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="oi" items="${orderItems}" varStatus="loop">
                                <c:set var="mi" value="${menuItems[loop.index]}"/>
                                <c:set var="unitPrice" value="${mi.discountedPrice > 0 ? mi.discountedPrice : mi.price}"/>
                                <tr>
                                    <td><strong>${mi.itemName}</strong></td>
                                    <td><fmt:formatNumber value="${unitPrice}" type="number"/> VNĐ</td>
                                    <td>
                                        <form method="post" action="${pageContext.request.contextPath}/staff/tables" style="display:flex;gap:8px;align-items:center;">
                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                            <input type="hidden" name="action" value="updateItem">
                                            <input type="hidden" name="orderID" value="${order.orderID}">
                                            <input type="hidden" name="orderItemID" value="${oi.orderItemID}">
                                            <input type="number" name="quantity" min="0" max="99" value="${oi.quantity}">
                                            <button type="submit">Cập nhật</button>
                                        </form>
                                    </td>
                                    <td><fmt:formatNumber value="${unitPrice * oi.quantity}" type="number"/> VNĐ</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty oi.note}">${oi.note}</c:when>
                                            <c:otherwise><span style="color:#8a6e5a;font-size:12px;">Nhập 0 nếu hoàn trả hết</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty orderItems}">
                                <tr><td colspan="5" style="text-align:center;color:#8a6e5a;">Đơn chưa có món.</td></tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <div class="summary">
                    <div class="box"><span>Tạm tính</span><strong><fmt:formatNumber value="${invoicePreview.subTotal}" type="number"/> VNĐ</strong></div>
                    <div class="box"><span>Phí dịch vụ</span><strong><fmt:formatNumber value="${invoicePreview.taxAmount}" type="number"/> VNĐ</strong></div>
                    <div class="box"><span>Đã đặt cọc</span><strong>- <fmt:formatNumber value="${invoicePreview.depositDeducted}" type="number"/> VNĐ</strong></div>
                    <div class="box"><span>Còn phải thanh toán</span><strong><fmt:formatNumber value="${invoicePreview.finalAmount}" type="number"/> VNĐ</strong></div>
                </div>

                <div style="margin-top:18px;display:flex;gap:10px;">
                    <form method="post" action="${pageContext.request.contextPath}/staff/tables">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="checkout">
                        <input type="hidden" name="orderID" value="${order.orderID}">
                        <button class="btn-green" type="submit" onclick="return confirm('Xác nhận số lượng đã đúng và chuyển sang thanh toán?')">
                            Xác nhận thanh toán
                        </button>
                    </form>
                    <a class="btn" href="${pageContext.request.contextPath}/staff/tables">Quay lại bàn phục vụ</a>
                </div>
            </main>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
