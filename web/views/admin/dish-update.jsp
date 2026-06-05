<%-- 
    Document   : dish-update
    Created on : Jun 3, 2026, 9:11:40 PM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.MenuCategory" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="${pageContext.request.contextPath}/update-menu" method="post" enctype="multipart/form-data">
            <input type="hidden" value="${dish.itemID}" name="id"/>
            <div>
                Nhập tên món ăn:
                <input type="text" name="name" value="${dish.itemName}"/>
                <div style="color: red">${errorName}</div>
            </div>
            <div>
                Lựa chọn loại món:
                <select name="category">
                    <c:forEach var="cat" items="${list}">
                        <option value="${cat.categoryID}" ${param.categoryID == cat.categoryID ? "selected" : ""}>
                            ${cat.categoryName}
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div>
                Nhập mô tả món ăn:
                <textarea name="description">${dish.description}</textarea>
                <div style="color: red">${errorDescription}</div>
            </div>
            <div>
                Nhập giá món ăn:
                <input type="number" name="price" value="${dish.price}"/>
                <div style="color: red">${errorDescription}</div>
            </div>
            <div>
                Nhập giảm giá món ăn:
                <input type="number" name="discountPercent" value="${dish.discountPercent}"/>
            </div>
            <div>
                Giá món ăn sau khi giảm giá:
                <input type="number" value="${dish.discountedPrice}" readonly/>
            </div>
            <div>
                Ảnh hiện tại:
                <img src="${dish.image}"/>
                <input type="hidden" value="${dish.image}" name="image"/>
            </div>
            <div>
                Đổi ảnh mới:
                <input type="file" name="image"/>
            </div>
            <div>
                Trạng thái món ăn:
                <input type="checkbox" name="isAvailable" value="1" ${dish.isAvailable == 1 ? "checked" : ""}/>Hoạt Động
            </div>
            <div>
                Ghi chú dị ứng:
                <textarea name="allergyNotes">${dish.allergyNotes}</textarea>
            </div>
            <input type="submit" value="CẬP NHẬT"/>
        </form>
    </body>
</html>
