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
        <title>JSP Page</title>
    </head>
    <body>
        <form action="${pageContext.request.contextPath}/menu" method="post">
            <input type="text" name="search" value="${param.search}" placeholder="Tìm kiếm món ăn"/>

            <select name="category">
                <option value="">Tất cả các món</option>
                <c:forEach var="cat" items="${list}">
                    <option value="${cat.categoryID}">${cat.categoryName}</option>
                </c:forEach>
            </select>

            <select name="status">
                <option value="1">Đang Bán</option>
                <option value="0">Tạm Ngưng</option>
            </select>
            <br/>
            Sắp xếp theo:
            <select name="price">
                <option value="price">Giá Gốc</option>
                <option value="discountedPrice">Giá Giảm Giá</option>
            </select>
            <br/>
            Giá từ:
            <input type="number" name="minPrice" value="${param.minPrice}"/>
            đến:
            <input type="number" name="maxPrice" value="${param.maxPrice}"/>
            <br/>
            Sắp xếp theo thứ tự:
            <select name="sort">
                <option value="asc">Tăng Dần</option>
                <option value="desc">Giảm Dần</option>
            </select>
            <input type="submit" value="LỌC"/>
        </form>

        <div class="menu-container">

            <c:forEach var="item" items="${listItem}">
                <div class="card">

                    <img src="${item.image}" alt="${item.itemName}">

                    <span class="status">Đang bán</span>

                    <h3>${item.itemName}</h3>

                    <p>${item.categoryID}</p>

                    <p class="price">${item.discountedPrice}</p>

                    <div class="button-group">
                        <button>Detail</button>
                        <button>Update</button>
                    </div>

                </div>
            </c:forEach>

        </div>
    </body>
</html>
