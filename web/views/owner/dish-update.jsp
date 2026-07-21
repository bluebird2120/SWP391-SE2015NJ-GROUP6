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
                background-color: #fdfbf7; /* 🌟 ĐÃ SỬA: Nền kem nhạt chuẩn Vị An */
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            }
            .form-container{
                max-width: 100%;
                background-color: #ffffff;
                padding: 30px;
                border-radius: 12px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.03); /* Đổ bóng nhẹ sang trọng */
            }
            h2{
                margin-top: 0;
                margin-bottom: 25px;
                color: #78493b; /* 🌟 ĐÃ SỬA: Chữ màu nâu trầm chủ đạo */
                font-size: 24px;
                font-weight: 600;
                border-bottom: 2px solid #f1ece6;
                padding-bottom: 10px;
                text-align: center;
            }

            /* Hộp thông báo Flash Attribute */
            .alert-success-box {
                background-color: #edf7ed;
                border-left: 4px solid #28a745;
                padding: 12px 15px;
                border-radius: 6px;
                margin-bottom: 20px;
                font-size: 14px;
                color: #1e4620;
                font-weight: 600;
            }
            .alert-danger-box {
                background-color: #fdeaea;
                border-left: 4px solid #dc3545;
                padding: 12px 15px;
                border-radius: 6px;
                margin-bottom: 20px;
                font-size: 14px;
                color: #c62828;
                font-weight: 600;
            }
            .btn-back {
                display: inline-flex;
                align-items: center;
                padding: 8px 16px;
                background-color: #f1ece6; /* Màu nền kem nhạt */
                color: #78493b;            /* Màu chữ nâu chủ đạo */
                border-radius: 8px;
                text-decoration: none;
                font-size: 13px;
                font-weight: 600;
                transition: all 0.2s;
                border: 1px solid #ebdcd0;
                margin-right: 15px;
            }
            .btn-back:hover {
                background-color: #ebdcd0; /* Hover nhẹ nhàng */
                color: #5c352d;
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
                color: #4a3f35; /* 🌟 ĐÃ SỬA: Chữ label nâu xám đậm */
                font-size: 14px;
            }
            .form-input{
                width: 100%;
                padding: 10px 12px;
                font-size: 14px;
                border: 1px solid #d1d5db;
                border-radius: 8px;
                box-sizing: border-box;
                transition: all 0.2s ease;
            }
            .form-input:focus{
                border-color: #78493b; /* 🌟 ĐÃ SỬA: Viền khi focus đổi sang nâu chủ đạo */
                outline: none;
                box-shadow: 0 0 0 3px rgba(120, 73, 59, 0.1);
            }
            textArea.form-input{
                height: 80px;
                resize: vertical;
            }
            .form-input[readonly]{
                background-color: #f8f6f2;
                color: #7c7267;
                border-style: dashed;
            }
            .error-message {
                color: #dc3545;
                font-size: 12px;
                margin-top: 5px;
                font-weight: 500;
            }
            .form-image{
                text-align: center;
                background-color: #ffffff;
                padding: 15px;
                border: 1px solid #f1ece6;
                border-radius: 8px;
                margin-bottom: 15px;
            }
            img{
                max-width: 100px;
                max-height: 100px;
                border-radius: 6px;
                border: 1px solid #d1d5db;
                margin: 2px;
            }

            .form-changeImage{
                display: flex;
                align-items: center;
                gap: 10px;
                font-size: 14px;
                font-weight: 500;
                margin-bottom: 5px;
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
                margin-top: 15px;
            }
            .form-checkbox input{
                width: 16px;
                height: 16px;
                cursor: pointer;
                accent-color: #78493b; /* Đổi màu checkbox sang tông nâu */
            }
            .form-submit{
                width: 100%;
                background-color: #de6b48; /* 🌟 ĐÃ SỬA: Nút xác nhận đổi sang cam đất nổi bật giống nút của Vị An */
                color: #ffffff;
                padding: 14px;
                font-size: 16px;
                font-weight: 600;
                border: none;
                border-radius: 8px;
                cursor: pointer;
                margin-top: 20px;
                transition: background-color 0.2s;
            }
            .form-submit:hover{
                background-color: #c95938; /* Hover cam đậm hơn */
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div style="display:flex; flex-wrap:nowrap; min-height:calc(100vh - 78px); align-items:flex-start;">
            <%@ include file="/views/includes/dashboard.jsp" %>
            <div style="flex:1; padding:32px; background:#f3f4f6; min-width:0;">
                <div class="form-container">
                    <a href="${backUrl}" class="btn-back">← Quay lại</a>
                    <h2>${dish.itemID == 0 ? "THÊM MỚI MÓN ĂN" : "CẬP NHẬT MÓN ĂN"}</h2>
                    <c:if test="${not empty updateSuccess}">
                        <div class="alert-success-box">
                            ✅ <b>Thành công:</b> ${updateSuccess}
                        </div>
                    </c:if>

                    <c:if test="${not empty updateFail}">
                        <div class="alert-danger-box">
                            ❌ <b>Lỗi hệ thống:</b> ${updateFail}
                        </div>
                    </c:if>

                    <form id="dishForm" action="${pageContext.request.contextPath}/owner/update-menu" method="post" enctype="multipart/form-data">
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
                                    <label class="form-label">Lựa chọn phương thức chế biến:</label>
                                    <select name="methodCooking" class="form-input">
                                        <c:forEach var="method" items="${listMethod}">
                                            <option value="${method.methodID}" ${(param.methodCooking != null ? param.methodCooking == method.methodID : dish.methodID == method.methodID) ? "selected" : ""}>                    
                                                ${method.methodName}
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
                                    <input class="form-input" type="number" name="discountPercent" value="${param.discountPercent != null ? param.discountPercent : dish.discountPercent}" required/>
                                    <div class="error-message">${errorDiscountPercent}</div>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Giá món ăn sau khi giảm giá:</label>
                                    <input class="form-input" type="number" value="${dish.discountedPrice}" readonly/>
                                </div>
                            </div>
                            <div class="configuration-block">
                                <div class="form-image">
                                    <label class="form-label">Ảnh chính hiện tại:</label>
                                    <img src="${dish.image}"/>
                                    <input type="hidden" value="${dish.image}" name="oldImage"/>  
                                </div>
                                <div class="form-changeImage">
                                    <label class="form-label">Đổi ảnh chính mới:</label>
                                    <input class="button-image" type="file" id="newMainImage" name="newMainImage"/>
                                </div>

                                <div class="error-message" id="errorMainImage">${errorMainImage}</div>

                                <div class="form-image">
                                    <label class="form-label">Ảnh phụ hiện tại:</label>
                                    <c:forEach var="subImg" items="${subImages}">
                                        <img src="${subImg.imagePath}"/>
                                    </c:forEach>
                                </div>
                                <div class="form-changeImage">
                                    <label class="form-label">Đổi ảnh phụ mới:</label>
                                    <input class="button-image" type="file" id="newSubImage" name="newSubImage" multiple/>
                                </div>

                                <div class="error-message" id="errorSubImage">${errorSubImage}</div>

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
            const mainImageInput = document.getElementById("newMainImage");
            const subImageInput = document.getElementById("newSubImage");
            const errorMainImage = document.getElementById("errorMainImage");
            const errorSubImage = document.getElementById("errorSubImage");

            const MAX_SIZE = 5 * 1024 * 1024;

            function checkFileValid(file) {
                if (!file)
                    return "";
                if (file.size > MAX_SIZE) {
                    return "Dung lượng ảnh vượt quá 5MB! Vui lòng chọn ảnh nhỏ hơn.";
                }
                let fName = file.name.toLowerCase();
                if (!(fName.endsWith('.jpg') || fName.endsWith('.jpeg') || fName.endsWith('.png') || fName.endsWith('.webp'))) {
                    return "Vui lòng chọn file định dạng ảnh hợp lệ (.jpg, .png, .webp, .jpeg)";
                }
                return "";
            }

            mainImageInput.onchange = function () {
                let file = mainImageInput.files[0];
                errorMainImage.innerHTML = checkFileValid(file);
            };

            subImageInput.onchange = function () {
                let files = subImageInput.files;
                if (files.length > 3) {
                    errorSubImage.innerHTML = "Hệ thống chỉ cho phép tải lên tối đa 3 ảnh phụ!";
                    subImageInput.value = "";
                    return;
                }

                for (let file of files) {
                    let errorMsg = checkFileValid(file);
                    if (errorMsg !== "") {
                        errorSubImage.innerHTML = errorMsg;
                        subImageInput.value = "";
                        return;
                    }
                }
                errorSubImage.innerHTML = "";
            };

            form.onsubmit = function (event) {
                let isValid = true;

                const name = form.elements["name"].value.trim();
                const description = form.elements["description"].value.trim();
                const allergyNotes = form.elements["allergyNotes"].value.trim();
                const price = form.elements["price"].value.trim();
                const discountPercent = form.elements["discountPercent"].value.trim();
                const itemId = parseInt(form.elements["id"].value);

                const errNameDiv = form.elements["name"].nextElementSibling;
                const errDescDiv = form.elements["description"].nextElementSibling;
                const errAllergyDiv = form.elements["allergyNotes"].nextElementSibling;
                const errPriceDiv = form.elements["price"].nextElementSibling;
                const errDiscountDiv = form.elements["discountPercent"].nextElementSibling;

                if (name === "") {
                    errNameDiv.innerHTML = "Tên món ăn không được để trống";
                    isValid = false;
                } else if (name.length > 150) {
                    errNameDiv.innerHTML = "Tên món ăn không được vượt quá 150 ký tự";
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
                    if (isNaN(dNum) || dNum < 0 || dNum > 100) {
                        errDiscountDiv.innerHTML = "Giảm giá món ăn phải từ 0-100%";
                        isValid = false;
                    } else {
                        errDiscountDiv.innerHTML = "";
                    }
                }

                if (itemId === 0) {
                    if (mainImageInput.files.length === 0) {
                        errorMainImage.innerHTML = "Vui lòng tải ảnh chính đại diện cho món ăn mới!";
                        isValid = false;
                    }
                    if (subImageInput.files.length === 0) {
                        errorSubImage.innerHTML = "Món ăn mới bắt buộc phải có từ 1 đến 3 ảnh phụ!";
                        isValid = false;
                    }
                }

                if (!isValid) {
                    event.preventDefault();
                }
            };
        </script>
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
