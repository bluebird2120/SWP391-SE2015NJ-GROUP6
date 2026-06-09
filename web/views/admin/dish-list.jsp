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
            .admin-layout{
                display: flex;
                flex: 1;
                flex-wrap: wrap;
            }
            .line2 {
                display: flex;
                align-items: center;
                gap: 12px;
                width: auto;
            }
            .main-content{
                flex: 1;            /* Tự động húp trọn phần khoảng trống còn lại bên phải */
                padding: 24px;
                background-color: #ffffff;
                box-sizing: border-box; /* Bảo hiểm giữ khung, chống tràn viền màn hình */
            }
            .filter-form{
                display: flex;
                gap: 12px;
                align-items: center;
                background: #f8fafc;
                border: 1px solid #e2e8f0;
                color: #475569;
                padding: 12px;
                flex-wrap: wrap; /*tự động xuống dòng khi screen nhỏ */
                border-radius: 12px;
                margin-bottom: 20px;
                font-family: Arial, sans-serif;
            }
            .filter-input, .filter-select{
                padding: 8px 12px;
                border: 1px solid #cccccc;
                border-radius: 8px;
                font-size: 14px;
                color: #333333;
                background-color: #ffffff;
            }
            .filter-price{
                display: flex;
                align-items: center;
                gap: 6px;
                font-size: 14px;
                color: #555555;
            }
            .filter-price input {
                width: 100px;
                border-radius: 6px;
            }
            .btn-submit {
                background-color: #007bff;
                color: white;
                border: none;
                padding: 8px 20px;
                border-radius: 8px;
                font-weight: bold;
                cursor: pointer;
            }
            .btn-submit:hover {
                background-color: #0056b3;
            }
            .menu-container{
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
                gap: 20px;
                padding: 20px;
            }
            .card{
                border: 1px solid #e0e0e0;
                border-radius: 16px;
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
                padding: 4px 12px;
                border-radius: 20px;
                font-size: 14px;
            }
            .active{
                background-color: #edf7ed;
                color: #1e4620;
            }
            .inactive{
                background-color: #fdeaea;
                color: #c62828;
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
                text-decoration: none;
                color: black;
            }
            /* Hiệu ứng đổi màu nhẹ khi di chuột vào nút */
            .btn:hover {
                background-color: #f5f5f5;
            }
            .error-msg {
                color: #b91c1c;
                background-color: #fef2f2; 
                border: 1px solid #fee2e2; 
                padding: 6px 12px;
                font-size: 13px;
                margin-top: 6px;
                font-weight: 500;
                border-radius: 6px;
                display: flex;
                align-items: center;
                gap: 6px;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div class="admin-layout">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <div class="main-content">
                <!--Filter to search dish-->
                <form action="${pageContext.request.contextPath}/menu" method="post" class="filter-form">
                    <input type="text" name="search" value="${param.search}" placeholder="Tìm kiếm món ăn..." class="filter-input" style="width: 200px;"/>

                    <select name="category" class="filter-select">
                        <option value="">Tất cả danh mục</option>
                        <c:forEach var="cat" items="${list}">
                            <option value="${cat.categoryID}" ${param.category == cat.categoryID ? "selected" : ""}>
                                ${cat.categoryName}
                            </option>
                        </c:forEach>
                    </select>

                    <select name="status" class="filter-select">
                        <option value="1" ${param.status == 1 ? 'selected' : ''}>Đang Bán</option>
                        <option value="0" ${param.status == 0 ? 'selected' : ''}>Tạm Ngưng</option>
                    </select>

                    <div class="filter-price">
                        <span>Giá từ:</span>
                        <input type="number" name="minPrice" value="${param.minPrice}" class="filter-input"/>
                        <span>đến:</span>
                        <input type="number" name="maxPrice" value="${param.maxPrice}" class="filter-input"/>
                    </div>

                    <div class="line2">
                        <span>Sắp xếp:</span>
                        <select name="price" class="filter-select">
                            <option value="price" ${param.price == 'price' ? 'selected' : ''}>Giá Gốc</option>
                            <option value="discountedPrice" ${param.price == 'discountedPrice' ? 'selected' : ''}>Giá Thực Tế</option>
                        </select>

                        <select name="sort" class="filter-select">
                            <option value="asc" ${param.sort == 'asc' ? 'selected' : ''}>Tăng Dần ↑</option>
                            <option value="desc" ${param.sort == 'desc' ? 'selected' : ''}>Giảm Dần ↓</option>
                        </select>

                        <input type="submit" value="LỌC" class="btn-submit"/>
                    </div>
                </form>
                        <c:if test="${not empty error}">
                        <div class="error-msg">${error}</div>
                    </c:if>
                <!--Display dish-->
                <div class="menu-container">

                    <c:forEach var="item" items="${listItem}">
                        <div class="card">

                            <img src="${item.image}" alt="${item.itemName}">

                            <span class="status ${item.isAvailable == 1 ? 'active' : 'inactive'}">
                                ${item.isAvailable == 1 ? 'Đang Bán' : 'Tạm Ngưng'}
                            </span>

                            <h3 class="item-name">${item.itemName}</h3>

                            <p class="category">🏷️ ${item.categoryName}</p>

                            <div class="price-container">
                                <div class="discount-price">${item.discountedPrice}</div>

                                <div class="price">${item.price}</div>

                                <div class="discount-percent">${item.discountPercent}%</div>
                            </div>

                            <p>Chi tiết: ${item.allergyNotes}</p>

                            <div class="button-group">                       
                                <a href="${pageContext.request.contextPath}/update-menu?id=${item.itemID}" class="btn">UPDATE</a>
                            </div>

                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
