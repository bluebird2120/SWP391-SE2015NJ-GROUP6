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
            .container { width: 100%; max-width: 900px; }
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

            input[type=checkbox] { width: 18px; height: 18px;
                                   cursor: pointer; accent-color: #c0392b; }

            .price-original { text-decoration: line-through; color: #aaa; font-size: 12px; }
            .price-final    { color: #c0392b; font-weight: bold; }

            <%-- Form update số lượng: đứng độc lập, không lồng trong form nào --%>
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
            .total-wrap { display: flex; flex-direction: column; gap: 4px; }
            .selected-note { font-size: 13px; color: #888; }
            .total-amount  { font-size: 22px; font-weight: bold; color: #c0392b; }
            .btn-checkout  { padding: 12px 28px; background: #27ae60; color: #fff;
                             border: none; border-radius: 6px; font-size: 15px; cursor: pointer; }
            .btn-checkout:hover    { background: #1e8449; }
            .btn-checkout:disabled { background: #aaa; cursor: not-allowed; }

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

                <table>
                    <thead>
                        <tr>
                            <th>
                                <%-- Checkbox chọn tất cả --%>
                                <input type="checkbox" id="checkAll"
                                       title="Chọn tất cả" onclick="toggleAll(this)">
                            </th>
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
                            <c:set var="mi" value="${menuItems[loop.index]}"/>

                            <%-- Tính đơn giá hiệu dụng --%>
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
                                <%-- Checkbox chọn món --%>
                                <td>
                                    <input type="checkbox"
                                           class="item-checkbox"
                                           value="${oi.orderItemID}"
                                           data-price="${lineTotal}"
                                           onchange="updateTotal()">
                                </td>

                                <%-- STT --%>
                                <td>${loop.count}</td>

                                <%-- Tên món --%>
                                <td><strong>${mi.itemName}</strong></td>

                                <%-- Đơn giá --%>
                                <td>
                                    <c:if test="${mi.discountedPrice > 0}">
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

                                <%-- Số lượng: form độc lập, KHÔNG lồng trong form nào --%>
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

                                <%-- Xóa món: form độc lập --%>
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
                    <div class="total-wrap">
                        <span class="selected-note" id="selectedNote">Chưa chọn món nào</span>
                        <div>
                            <span style="font-size:16px; color:#333">Tổng cộng: </span>
                            <span class="total-amount" id="totalDisplay">0đ</span>
                        </div>
                    </div>
                    <button class="btn-checkout" id="btnCheckout"
                            type="button" disabled onclick="submitCheckout()">
                        Tiến hành thanh toán →
                    </button>
                </div>

            </c:if>

        </div>

        <script>
            // Tích/bỏ tích tất cả
            function toggleAll(source) {
                document.querySelectorAll('.item-checkbox')
                        .forEach(cb => cb.checked = source.checked);
                updateTotal();
            }

            // Cập nhật tổng tiền theo món được tích
            function updateTotal() {
                var checked = document.querySelectorAll('.item-checkbox:checked');
                var total   = 0;
                checked.forEach(cb => total += parseFloat(cb.getAttribute('data-price')));

                document.getElementById('totalDisplay').innerText =
                    total.toLocaleString('vi-VN') + 'đ';

                var count = checked.length;
                document.getElementById('selectedNote').innerText =
                    count > 0 ? 'Đã chọn ' + count + ' món' : 'Chưa chọn món nào';

                document.getElementById('btnCheckout').disabled = (count === 0);

                var all = document.querySelectorAll('.item-checkbox');
                document.getElementById('checkAll').checked = (count === all.length && all.length > 0);
            }

            // Tạo form động rồi submit → tránh hoàn toàn việc lồng form
            function submitCheckout() {
                var checked = document.querySelectorAll('.item-checkbox:checked');
                if (checked.length === 0) {
                    alert('Vui lòng chọn ít nhất 1 món!');
                    return;
                }

                // Tạo form mới hoàn toàn độc lập
                var form = document.createElement('form');
                form.method = 'post';
                form.action = '${pageContext.request.contextPath}/checkout';

                // Thêm orderID
                var inputOrderID = document.createElement('input');
                inputOrderID.type  = 'hidden';
                inputOrderID.name  = 'orderID';
                inputOrderID.value = '${orderID}';
                form.appendChild(inputOrderID);

                // Thêm từng orderItemID được tích
                checked.forEach(function(cb) {
                    var input = document.createElement('input');
                    input.type  = 'hidden';
                    input.name  = 'selectedItems';
                    input.value = cb.value;
                    form.appendChild(input);
                });

                // Gắn vào body rồi submit
                document.body.appendChild(form);
                form.submit();
            }
        </script>

    </body>
</html>
