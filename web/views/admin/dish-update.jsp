<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="model.MenuCategory" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <style>
            body{
                background-color: #f3f4f6;
            }
            .form-container{
                max-width: 100%;
                background-color: #ffffff;
                padding: 30px;
                border-radius: 8px;
            }
            h2{
                margin-top: 0;
                margin-bottom: 25px;
                color: #111827;
                font-size: 24px;
                font-weight: 600;
                border-bottom: 2px solid #e5e7eb;
                padding-bottom: 10px;
                text-align: center;
            }
            .form-layout{
                display: flex;
                gap: 30px;
            }
            .data-block{
                flex: 65;
            }
            .configuration-block{
                flex: 35;
            }
            .form-label{
                display: block;
                font-weight: 500;
                margin-bottom: 8px;
                color: #374151;
                font-size: 14px;
            }
            .form-input{
                width: 100%;
                padding: 10px 12px;
                font-size: 14px;
                border: 1px solid #d1d5db;
                border-radius: 6px;
                box-sizing: border-box;
            }
            .form-input:focus{
                border-color: #2563eb;
            }
            textArea.form-input{
                height: 80px;
                resize: vertical;
            }
            .form-input[readonly]{
                background-color: #f3f4f6;
                color: #6b7280;
                border-style: dashed;
            }
            .error-message {
                color: #dc2626;
                font-size: 12px;
                margin-top: 5px;
                font-weight: 500;
            }
            .form-image{
                text-align: center;
                background-color: #ffffff;
                padding: 15px;
                border: 1px solid #e5e7eb;
                border-radius: 6px;
                margin-bottom: 15px;
            }
            img{
                max-width: 100%;
                max-height: 200px;
                border-radius: 4px;
                border: 1px solid #d1d5db;
            }
            .form-changeImage{
                display: flex;
                align-items: center;
                gap: 10px;
                font-size: 14px;
                font-weight: 500;
            }
            .form-changeImage input{
                cursor: pointer;
            }
            .form-checkbox{
                display: flex;
                align-items: center;
                gap: 10px;
                font-size: 14px;
                font-weight: 500;
            }
            .form-checkbox input{
                width: 14px;
                height: 14px;
                cursor: pointer;
            }
            .form-submit{
                width: 100%;
                background-color: #2563eb;
                color: #ffffff;
                padding: 14px;
                font-size: 16px;
                font-weight: 600;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                margin-top: 10px;
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div style="display:flex; flex-wrap:nowrap; min-height:calc(100vh - 78px); align-items:flex-start;">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <div style="flex:1; padding:32px; background:#f3f4f6; min-width:0;">
                <div class="form-container">
                    <h2>${dish.itemID == 0 ? "THÊM MỚI MÓN ĂN" : "CẬP NHẬT MÓN ĂN"}</h2>
                    <form id="dishForm" action="${pageContext.request.contextPath}/update-menu" method="post" enctype="multipart/form-data">
                        <input type="hidden" value="${dish.itemID}" name="id"/>
                        <div class="form-layout">
                            <div class="data-block">
                                <div class="form-group">
                                    <label class="form-label">Nhập tên món ăn:</label>
                                    <input type="text" name="name" class="form-input" value="${param.name != null ? param.name : dish.itemName}" required/>
                                    <div class="error-message">${errorName}</div>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Lựa chọn loại món:</label>
                                    <select name="category" class="form-input">
                                        <c:forEach var="cat" items="${list}">
                                            <option value="${cat.categoryID}" ${(param.category != null ? param.category == cat.categoryID : dish.categoryID == cat.categoryID) ? "selected" : ""}>                    
                                                ${cat.categoryName}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Nhập mô tả món ăn:</label>
                                    <textarea class="form-input" name="description" required>${param.description != null ? param.description : dish.description}</textarea>
                                    <div class="error-message">${errorDescription}</div>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Ghi chú dị ứng:</label>
                                    <textarea class="form-input" name="allergyNotes" required>${param.allergyNotes != null ? param.allergyNotes : dish.allergyNotes}</textarea>
                                    <div class="error-message">${errorAllergyNotes}</div>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Nhập giá món ăn:</label>
                                    <input class="form-input" type="number" name="price" value="${param.price != null ? param.price : dish.price}" required/>
                                    <div class="error-message">${errorPrice}</div>
                                </div>
                                <div class="form-group"> 
                                    <label class="form-label">Nhập giảm giá món ăn:</label>
                                    <input class="form-input"class="form-input" type="number" name="discountPercent" value="${param.discountPercent != null ? param.discountPercent : dish.discountPercent}" required/>
                                    <div class="error-message">${errorDiscountPercent}</div>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Giá món ăn sau khi giảm giá:</label>
                                    <input class="form-input" type="number" value="${dish.discountedPrice}" readonly/>
                                </div>
                            </div>
                            <div class="configuration-block">
                                <div class="form-image">
                                    <label class="form-label">Ảnh hiện tại:</label>
                                    <img src="${dish.image}"/>
                                    <input type="hidden" value="${dish.image}" name="oldImage"/>  
                                </div>
                                <div class="form-changeImage">
                                    <label class="form-label">Đổi ảnh mới:</label>
                                    <input type="file" id="newImage" name="newImage"/>
                                </div>
                                <div class="error-message" id="errorImage">${errorImageFile}</div>

                                <div class="form-checkbox">
                                    <label class="form-label">Trạng thái món ăn:</label>
                                    <input type="checkbox" name="isAvailable" value="1" ${dish.isAvailable == 1 ? "checked" : ""}/>Hoạt Động
                                </div>

                                <input class="form-submit" type="submit" value="${dish.itemID == 0 ? "THÊM MỚI MÓN ĂN" : "CẬP NHẬT MÓN ĂN"}"/>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <script>
            const form = document.getElementById("dishForm");
            const imageInput = document.getElementById("newImage");
            const errorImage = document.getElementById("errorImage");

            imageInput.onchange = function () {
                let file = imageInput.files[0];
                if (file) {
                    if (file.size > 5 * 1024 * 1024) {
                        errorImage.innerHTML = "Ảnh quá lớn! Hệ thống chỉ cho phép ảnh dưới 5MB.";
                        imageInput.value = "";
                        return;
                    }

                    let fileName = file.name.toLowerCase();
                    if (!(fileName.endsWith('.jpg') || fileName.endsWith('.jpeg') || fileName.endsWith('.png') || fileName.endsWith('.webp'))) {
                        errorImage.innerHTML = "Vui lòng chọn file ảnh hợp lệ (.jpg, .png, .webp, .jpeg)";
                        imageInput.value = "";
                        return;
                    }
                    errorImage.innerHTML = "";
                }
            };

            form.onsubmit = function (event) {
                let isValid = true;

                const name = form.elements["name"].value.trim();
                const description = form.elements["description"].value.trim();
                const allergyNotes = form.elements["allergyNotes"].value.trim();
                const price = form.elements["price"].value.trim();
                const discountPercent = form.elements["discountPercent"].value.trim();
                const itemId = form.elements["id"].value;

                const errNameDiv = form.elements["name"].nextElementSibling;
                const errDescDiv = form.elements["description"].nextElementSibling;
                const errAllergyDiv = form.elements["allergyNotes"].nextElementSibling;
                const errPriceDiv = form.elements["price"].nextElementSibling;
                const errDiscountDiv = form.elements["discountPercent"].nextElementSibling;

                if (name === "") {
                    errNameDiv.innerHTML = "Tên món ăn không được để trống";
                    isValid = false;
                } else if (name.length > 150) {
                    errNameDiv.innerHTML = "Tên món ăn không được vượt quá 150 ký tự1";
                    isValid = false;
                } else {
                    errNameDiv.innerHTML = "";
                }

                if (description === "") {
                    errDescDiv.innerHTML = "Mô tả món ăn không được để trống";
                    isValid = false;
                } else if (description.length > 500) {
                    errDescDiv.innerHTML = "Mô tả món ăn không được vượt quá 500 ký tự";
                    isValid = false;
                } else {
                    errDescDiv.innerHTML = "";
                }

                if (allergyNotes === "") {
                    errAllergyDiv.innerHTML = "Mô tả dị ứng không được để trống";
                    isValid = false;
                } else if (allergyNotes.length > 500) {
                    errAllergyDiv.innerHTML = "Mô tả dị ứng không được vượt quá 500 ký tự";
                    isValid = false;
                } else {
                    errAllergyDiv.innerHTML = "";
                }

                if (price === "") {
                    errPriceDiv.innerHTML = "Giá món ăn không được để trống";
                    isValid = false;
                } else {
                    let pNum = parseInt(price);
                    if (isNaN(pNum) || pNum < 0 || pNum > 1000000000) {
                        errPriceDiv.innerHTML = "Giá món ăn từ 0-1000000000";
                        isValid = false;
                    } else {
                        errPriceDiv.innerHTML = "";
                    }
                }

                if (discountPercent === "") {
                    errDiscountDiv.innerHTML = "Giảm giá món ăn không được để trống";
                    isValid = false;
                } else {
                    let dNum = parseInt(discountPercent);
                    if (isNaN(dNum) || dNum < 0 || dNum > 1000000000) {
                        errDiscountDiv.innerHTML = "Giảm giá món ăn phải từ 0-1000000000";
                        isValid = false;
                    } else {
                        errDiscountDiv.innerHTML = "";
                    }
                }

                if (parseInt(itemId) === 0 && imageInput.files.length === 0) {
                    errorImage.innerHTML = "Vui lòng tải ảnh cho món ăn";
                    isValid = false;
                }

                if (!isValid) {
                    event.preventDefault();
                }
            };
        </script>
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
