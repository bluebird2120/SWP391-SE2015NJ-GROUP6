<%-- 
    Document   : dish-list
    Created on : May 30, 2026, 4:37:54 PM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="model.MenuCategory" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Quản lý Thực đơn - Lách Tách</title>
        <style>
            .menu-container{
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
                gap: 20px;
                padding: 20px;
            }
            .card{
                border: 1px solid #e0e0e0;
                border-radius: 16px; /* Bo góc 16px giống hệt ảnh mẫu */
                padding: 16px;
                background: #ffffff;
                font-family: Arial, sans-serif;
            }
            .card img {
                width: 100%;
                height: 180px;
                object-fit: cover; /* Giúp ảnh không bị méo khi co giãn */
                border-radius: 12px;
                margin-bottom: 12px;
            }
            .status {
                display: block;
                background-color: #edf7ed; /* Màu nền xanh lá cây nhạt */
                color: #1e4620; /* Màu chữ xanh lá cây đậm */
                padding: 4px 12px;
                border-radius: 20px;
                font-size: 14px;
                
            }
            .item-name {
                font-size: 18px;
                font-weight: bold;
                margin: 8px 0;
            }
            .category {
                color: #666666;
                font-size: 14px;
                margin: 4px 0;
            }
            .price-container{
                border-bottom: 1px solid #eeeeee; /* Thêm một đường gạch ngang mờ ở dưới giá */
                display: flex;
                align-items: center;
                gap: 10px;
                padding-bottom: 10px;

            }
            .price {
                font-size: 10px;
                color: #000000;
                text-decoration: line-through;
            }
            .discount-percent{
                font-size: 10px;
                color: red;
            }
            .discount-price{
                font-size: 18px;
                font-weight: bold;
                color: #000000;
            }
            .button-group {
                display: flex;
                gap: 12px; /* Khoảng cách giữa 2 nút */
            }
            .btn {
                flex: 1; /* Ép 2 nút tự chia đôi chiều rộng bằng nhau (50% - 50%) */
                padding: 10px 0;
                border: 1px solid #cccccc;
                border-radius: 12px; /* Nút bo góc tròn */
                background-color: #ffffff;
                cursor: pointer;
                font-size: 15px;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 6px; /* Khoảng cách giữa icon và chữ */
            }
            /* Hiệu ứng đổi màu nhẹ khi di chuột vào nút */
            .btn:hover {
                background-color: #f5f5f5;
            }
        </style>
    </head>
    <body>
        <!--Filter to search dish-->
        <form action="${pageContext.request.contextPath}/menu" method="post">
            <input type="text" name="search" value="${param.search}" placeholder="Tìm kiếm món ăn"/>

            <select name="category">
                <option value="">Tất cả các món</option>
                <c:forEach var="cat" items="${list}">
                    <option value="${cat.categoryID}" ${param.category == cat.categoryID ? "selected" : ""}>
                        ${cat.categoryName}
                    </option>
                </c:forEach>
            </select>

            <select name="status">
                <option value="1" ${param.status == 1 ? 'selected' : ''}>Đang Bán</option>
                <option value="0" ${param.status == 0 ? 'selected' : ''}>Tạm Ngưng</option>
            </select>
            <br/>
            Sắp xếp theo:
            <select name="price">
                <option value="price" ${param.price == 'price' ? 'selected' : ''}>Giá Gốc</option>
                <option value="discountedPrice" ${param.price == 'discountedPrice' ? 'selected' : ''}>Giá Giảm Giá</option>
            </select>
            <br/>
            Giá từ:
            <input type="number" name="minPrice" value="${param.minPrice}"/>
            đến:
            <input type="number" name="maxPrice" value="${param.maxPrice}"/>
            <br/>
            Sắp xếp theo thứ tự:
            <select name="sort">
                <option value="asc" ${param.sort == 'asc' ? 'selected' : ''}>Tăng Dần</option>
                <option value="desc" ${param.sort == 'desc' ? 'selected' : ''}>Giảm Dần</option>
            </select>
            <input type="submit" value="LỌC"/>
        </form>
        <!--Display dish-->
        <div class="menu-container">

            <c:forEach var="item" items="${listItem}">
                <div class="card">

                    <img src="${item.image}" alt="${item.itemName}">

                    <span class="status">${item.isAvailable == 1 ? 'Đang Bán' : 'Tạm Ngưng'}</span>

                    <h3 class="item-name">${item.itemName}</h3>

                    <p class="category">🏷️ ${item.categoryName}</p>

                    <div class="price-container">
                        <div class="discount-price">${item.discountedPrice}</div>

                        <div class="price">${item.price}</div>

                        <div class="discount-percent">${item.discountPercent}%</div>
                    </div>

                    <p>Chi tiết: ${item.allergyNotes}</p>

                    <div class="button-group">
                        <button class="btn">Detail</button>
                        <button class="btn">Update</button>
                    </div>

                </div>
            </c:forEach>

        </div>
    </body>
</html>
