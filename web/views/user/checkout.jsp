<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Xác nhận thanh toán</title>
        <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body { font-family: Arial, sans-serif; background: #f7f7f7;
                   display: flex; justify-content: center; padding: 32px 16px; }

            /* Khung hóa đơn giống receipt */
            .receipt { background: #fff; width: 100%; max-width: 520px;
                       border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,.1);
                       padding: 32px 28px; }

            /* Header */
            .receipt-header { text-align: center; margin-bottom: 24px; }
            .receipt-header h2 { font-size: 22px; color: #c0392b; }
            .receipt-header p  { font-size: 13px; color: #888; margin-top: 4px; }

            /* Divider */
            .divider { border: none; border-top: 1px dashed #ddd; margin: 16px 0; }

            /* Danh sách món */
            .item-list { width: 100%; border-collapse: collapse; }
            .item-list th { font-size: 13px; color: #888; font-weight: normal;
                            padding: 4px 0; text-align: left; }
            .item-list th:last-child { text-align: right; }
            .item-list td { font-size: 14px; padding: 6px 0;
                            border-bottom: 1px solid #f5f5f5; }
            .item-list td:last-child { text-align: right; font-weight: bold; }
            .item-name { font-weight: bold; }
            .item-qty  { font-size: 12px; color: #888; }

            /* Bảng phân rã chi phí */
            .cost-table { width: 100%; margin-top: 8px; }
            .cost-table td { padding: 5px 0; font-size: 14px; }
            .cost-table td:last-child { text-align: right; }
            .cost-table .label { color: #555; }

            /* Tổng tiền cuối */
            .total-row td { padding-top: 12px; font-size: 18px;
                            font-weight: bold; color: #c0392b; }

            /* Ghi chú */
            .note { font-size: 12px; color: #aaa; text-align: center;
                    margin-top: 16px; }

            /* Nút */
            .btn-payment { display: block; width: 100%; margin-top: 24px;
                           padding: 14px; background: #27ae60; color: #fff;
                           border: none; border-radius: 6px; font-size: 16px;
                           cursor: pointer; text-align: center; text-decoration: none; }
            .btn-payment:hover { background: #1e8449; }
            .btn-back { display: block; text-align: center; margin-top: 12px;
                        color: #2980b9; text-decoration: none; font-size: 13px; }
            .btn-back:hover { text-decoration: underline; }
        </style>
    </head>
    <body>
        <div class="receipt">

            <%-- ===== HEADER ===== --%>
            <div class="receipt-header">
                <h2>🧾 Hóa đơn tạm tính</h2>
                <p>Mã hóa đơn: <strong>${invoice.invoiceNumber}</strong></p>
                <p>Ngày: <fmt:formatDate value="${invoice.issuedDate}" pattern="dd/MM/yyyy"/></p>
            </div>

            <hr class="divider">

            <%-- ===== DANH SÁCH MÓN ===== --%>
            <table class="item-list">
                <thead>
                    <tr>
                        <th>Món</th>
                        <th style="text-align:center">SL</th>
                        <th>Thành tiền</th>
                    </tr>
                </thead>
                <tbody>
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

                        <tr>
                            <td>
                                <span class="item-name">${mi.itemName}</span><br>
                                <span class="item-qty">
                                    <fmt:formatNumber value="${unitPrice}" type="number" maxFractionDigits="0"/>đ
                                    × ${oi.quantity}
                                </span>
                            </td>
                            <td style="text-align:center">${oi.quantity}</td>
                            <td>
                                <fmt:formatNumber value="${lineTotal}"
                                    type="number" maxFractionDigits="0"/>đ
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <hr class="divider">

            <%-- ===== BẢNG PHÂN RÃ CHI PHÍ ===== --%>
            <table class="cost-table">
                <tr>
                    <td class="label">Tiền món ăn</td>
                    <td>
                        <fmt:formatNumber value="${invoice.subTotal}"
                            type="number" maxFractionDigits="0"/>đ
                    </td>
                </tr>
                <tr>
                    <td class="label">Thuế VAT (10%)</td>
                    <td>
                        <fmt:formatNumber value="${invoice.taxAmount}"
                            type="number" maxFractionDigits="0"/>đ
                    </td>
                </tr>

                <%-- Chỉ hiện dòng đặt cọc nếu có --%>
                <c:if test="${invoice.depositDeducted > 0}">
                    <tr>
                        <td class="label">Đã đặt cọc</td>
                        <td style="color: #27ae60">
                            - <fmt:formatNumber value="${invoice.depositDeducted}"
                                type="number" maxFractionDigits="0"/>đ
                        </td>
                    </tr>
                </c:if>

                <%-- Tổng cuối --%>
                <tr class="total-row">
                    <td>Tổng thanh toán</td>
                    <td>
                        <fmt:formatNumber value="${invoice.finalAmount}"
                            type="number" maxFractionDigits="0"/>đ
                    </td>
                </tr>
            </table>

            <hr class="divider">

            <p class="note">
                ⚠️ Hóa đơn này chỉ là tạm tính. Vui lòng không rời bàn trước khi nhân viên xác nhận.
            </p>

            <%-- ===== NÚT CHUYỂN SANG PAYMENT ===== --%>
            <a class="btn-payment"
               href="${pageContext.request.contextPath}/payment?invoiceID=${invoice.invoiceID}&orderID=${orderID}">
                Chọn phương thức thanh toán →
            </a>
            <a class="btn-back"
               href="${pageContext.request.contextPath}/order?action=cart">
                ← Quay lại giỏ hàng
            </a>

        </div>
    </body>
</html>