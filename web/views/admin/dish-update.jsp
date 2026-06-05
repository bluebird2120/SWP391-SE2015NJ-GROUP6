<%-- 
    Document   : dish-update
    Created on : Jun 3, 2026, 9:11:40 PM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="${pageContext.request.contextPath}/update-menu">
            <div>
                Nhập tên món ăn:
                <input type="text" name="name" value="${dish.itemName}"/>
            </div>
            <div>
                Nhập mô tả món ăn:
                <textarea name="discription">${dish.description}</textarea>
            </div>
            <div>
                Nhập giá món ăn:
                <input type="number" name="price" value="${dish.price}"/>
            </div>
            <div>
                Nhập giảm giá món ăn:
                <input type="number" name="price" value="${dish.discountPercent}"/>
            </div>
            <div>
                Giá món ăn sau khi giảm giá:
                <input type="number" name="price" value="${dish.discountedPrice}" readonly/>
            </div>
            <div>
                Ảnh hiện tại:
                <image src="${dish.image}"/>
            </div>
            <div>
                Đổi ảnh mới:
                <input type="file" name="image"/>
            </div>
            
            <div>
                Ghi chú dị ứng:
                <textarea name="allergyNotes">${dish.allergyNotes}</textarea>
            </div>
        </form>
    </body>
</html>
