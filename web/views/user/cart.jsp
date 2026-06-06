<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Giỏ hàng</title>
        <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body { font-family: Arial, sans-serif; background: #f7f7f7;
                   display: flex; justify-content: center; padding: 32px 16px; }
            .container { width: 100%; max-width: 860px; }
            h2  { color: #c0392b; font-size: 24px; margin-bottom: 8px; }
            .back { color: #2980b9; text-decoration: none; font-size: 14px; }
            .back:hover { text-decoration: underline; }

            table { width: 100%; border-collapse: collapse; margin-top: 16px;
                    background: #fff; border-radius: 8px; overflow: hidden;
                    box-shadow: 0 1px 4px rgba(0,0,0,.08); }
            th { background: #f5f5f5; padding: 12px 14px; text-align: left;
                 font-size: 14px; color: #555; border-bottom: 1px solid #e0e0e0; }
            td { padding: 10px 14px; border-bottom: 1px solid #f0f0f0;
                 vertical-align: middle; font-size: 14px; }
            tr:last-child td { border-bottom: none; }

            .price-original { text-decoration: line-through; color: #aaa; font-size: 12px; }
            .price-final    { color: #c0392b; font-weight: bold; }

            .qty-form { display: flex; gap: 6px; align-items: center; }
            .qty-form input[type=number] { width: 60px; padding: 5px 8px;
                border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
            .btn { padding: 5px 12px; border: none; border-radius: 4px;
                   cursor: pointer; font-size: 13px; }
            .btn-remove { background: #e74c3c; color: #fff; }
            .btn-remove:hover { background: #c0392b; }

            .footer { background: #fff; border-radius: 8px; margin-top: 12px;
                      padding: 16px 20px; box-shadow: 0 1px 4px rgba(0,0,0,.08);
                      display: flex; justify-content: space-between; align-items: center; }
            .total-label  { font-size: 16px; color: #333; }
            .total-amount { font-size: 22px; font-weight: bold; color: #c0392b; }
            .btn-checkout { padding: 12px 28px; background: #27ae60; color: #fff;
                            border: none; border-radius: 6px; font-size: 15px;
                            cursor: pointer; text-decoration: none; }
            .btn-checkout:hover { background: #1e8449; }
            .empty { text-align: center; color: #888; padding: 48px 0; }
            .empty a { color: #2980b9; }
        </style>
    </head>
    <body>
        <div class="container">

            <h2>🛒 Giỏ hàng</h2>
            <a class="back" href="${pageContext.request.contextPath}/menu">← Quay lại menu</a>

            <%-- ===== GIỎ HÀNG TRỐNG ===== --%>
            <c:if test="${empty orderItems}">
                <div class="empty">
                    <p>Giỏ hàng của bạn đang trống.</p>
                    <a href="${pageContext.request.contextPath}/menu">Chọn món ngay →</a>
                </div>
            </c:if>

            <%-- ===== CÓ MÓN TRONG GIỎ ===== --%>
            <c:if test="${not empty orderItems}">

                <%-- Tính tổng tiền --%>
                <c:set var="grandTotal" value="0"/>
                <c:forEach var="oi" items="${orderItems}" varStatus="loop">
                    <c:set var="mi" value="${menuItems[loop.index]}"/>
                    <c:choose>
                        <c:when test="${mi.discountedPrice != null}">
                            <c:set var="unitPrice" value="${mi.discountedPrice}"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="unitPrice" value="${mi.price}"/>
                        </c:otherwise>
                    </c:choose>
                    <c:set var="grandTotal" value="${grandTotal + unitPrice * oi.quantity}"/>
                </c:forEach>

                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Tên món</th>
                            <th>Đơn giá</th>
                            <th>Số lượng</th>
                            <th>Thành tiền</th>
                            <th>Ghi chú</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="oi" items="${orderItems}" varStatus="loop">
                            <%-- Lấy MenuItem cùng index --%>
                            <c:set var="mi" value="${menuItems[loop.index]}"/>

                            <%-- Tính đơn giá và thành tiền --%>
                            <c:choose>
                                <c:when test="${mi.discountedPrice != null}">
                                    <c:set var="unitPrice" value="${mi.discountedPrice}"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="unitPrice" value="${mi.price}"/>
                                </c:otherwise>
                            </c:choose>
                            <c:set var="lineTotal" value="${unitPrice * oi.quantity}"/>

                            <tr>
                                <%-- STT --%>
                                <td>${loop.count}</td>

                                <%-- Tên món --%>
                                <td><strong>${mi.itemName}</strong></td>

                                <%-- Đơn giá --%>
                                <td>
                                    <c:if test="${mi.discountedPrice != null}">
                                        <span class="price-original">
                                            <fmt:formatNumber value="${mi.price}"
                                                type="number" maxFractionDigits="0"/>đ
                                        </span><br>
                                    </c:if>
                                    <span class="price-final">
                                        <fmt:formatNumber value="${unitPrice}"
                                            type="number" maxFractionDigits="0"/>đ
                                    </span>
                                </td>

                                <%-- Số lượng: tự submit khi thay đổi --%>
                                <td>
                                    <form class="qty-form" method="post"
                                          action="${pageContext.request.contextPath}/cart">
                                        <input type="hidden" name="action"      value="update">
                                        <input type="hidden" name="orderItemID" value="${oi.orderItemID}">
                                        <input type="number" name="quantity"
                                               value="${oi.quantity}" min="1" max="99"
                                               onchange="this.form.submit()">
                                    </form>
                                </td>

                                <%-- Thành tiền --%>
                                <td>
                                    <strong>
                                        <fmt:formatNumber value="${lineTotal}"
                                            type="number" maxFractionDigits="0"/>đ
                                    </strong>
                                </td>

                                <%-- Ghi chú --%>
                                <td>${oi.note}</td>

                                <%-- Xóa món --%>
                                <td>
                                    <form method="post"
                                          action="${pageContext.request.contextPath}/cart">
                                        <input type="hidden" name="action"      value="remove">
                                        <input type="hidden" name="orderItemID" value="${oi.orderItemID}">
                                        <button class="btn btn-remove" type="submit"
                                                onclick="return confirm('Xóa món này khỏi giỏ?')">
                                            Xóa
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>

                <%-- Footer: tổng tiền + nút checkout --%>
                <div class="footer">
                    <div>
                        <span class="total-label">Tổng cộng: </span>
                        <span class="total-amount">
                            <fmt:formatNumber value="${grandTotal}"
                                type="number" maxFractionDigits="0"/>đ
                        </span>
                    </div>
                    <a class="btn-checkout"
                       href="${pageContext.request.contextPath}/checkout?orderID=${orderID}">
                        Tiến hành thanh toán →
                    </a>
                </div>

            </c:if>

        </div>
    </body>
</html>