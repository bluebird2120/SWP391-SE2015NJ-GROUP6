<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.OrderItem, java.util.List" %>
<%
    List<OrderItem> cartItems = (List<OrderItem>) request.getAttribute("cartItems");
    Integer orderID = (Integer) request.getAttribute("orderID");
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Giỏ hàng</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                max-width: 700px;
                margin: 30px auto;
                padding: 0 16px;
            }
            h2   {
                color: #c0392b;
            }
            table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 16px;
            }
            th, td {
                padding: 10px 12px;
                border: 1px solid #ddd;
                text-align: left;
            }
            th   {
                background: #f5f5f5;
            }
            .qty-form {
                display: flex;
                gap: 6px;
                align-items: center;
            }
            .qty-form input[type=number] {
                width: 55px;
                padding: 4px;
            }
            .btn       {
                padding: 6px 14px;
                border: none;
                border-radius: 4px;
                cursor: pointer;
            }
            .btn-update{
                background: #2980b9;
                color: #fff;
            }
            .btn-remove{
                background: #e74c3c;
                color: #fff;
            }
            .btn-checkout {
                display:block;
                margin: 20px 0;
                padding: 12px;
                background: #27ae60;
                color: #fff;
                text-align: center;
                border-radius: 6px;
                text-decoration: none;
                font-size: 16px;
            }
            .btn-back  {
                color: #2980b9;
                text-decoration: none;
            }
            .empty     {
                text-align: center;
                color: #888;
                margin-top: 40px;
            }
        </style>
    </head>
    <body>

        <h2>🛒 Giỏ hàng</h2>
        <a class="btn-back" href="<%= contextPath %>/menu">← Quay lại menu</a>

        <%-- ===== TRƯỜNG HỢP GIỎ HÀNG TRỐNG ===== --%>
        <% if (cartItems == null || cartItems.isEmpty()) { %>
        <p class="empty">Giỏ hàng của bạn đang trống.<br>
            <a href="<%= contextPath %>/menu">Chọn món ngay</a>
        </p>

        <%-- ===== TRƯỜNG HỢP CÓ MÓN TRONG GIỎ ===== --%>
        <% } else { %>
        <table>
            <thead>
                <tr>
                    <th>#</th>
                    <th>Tên món</th>
                    <th>Số lượng</th>
                    <th>Ghi chú</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <%
                    int stt = 1;
                    for (OrderItem item : cartItems) {
                %>
                <tr>
                    <td><%= stt++ %></td>
                    <td>Món #<%= item.getItemID() %></td>

                    <%-- Cập nhật số lượng --%>
                    <td>
                        <form class="qty-form" method="post" action="<%= contextPath %>/cart">
                            <input type="hidden" name="action"      value="update">
                            <input type="hidden" name="orderItemID" value="<%= item.getOrderItemID() %>">
                            <input type="number" name="quantity"    value="<%= item.getQuantity() %>" min="1" max="99">
                            <button class="btn btn-update" type="submit">Lưu</button>
                        </form>
                    </td>

                    <%-- Ghi chú --%>
                    <td><%= item.getNote() != null ? item.getNote() : "" %></td>

                    <%-- Xóa món --%>
                    <td>
                        <form method="post" action="<%= contextPath %>/cart">
                            <input type="hidden" name="action"      value="remove">
                            <input type="hidden" name="orderItemID" value="<%= item.getOrderItemID() %>">
                            <button class="btn btn-remove" type="submit"
                                    onclick="return confirm('Xóa món này khỏi giỏ?')">Xóa</button>
                        </form>
                    </td>
                </tr>
                <% } %>
            </tbody>
        </table>

        <%-- Nút thanh toán --%>
        <a class="btn-checkout" href="<%= contextPath %>/checkout?orderID=<%= orderID %>">
            Tiến hành thanh toán →
        </a>
        <% } %>

    </body>
</html>