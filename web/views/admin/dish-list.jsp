<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ page import="java.util.List" %>
        <%@ page import="model.MenuCategory" %>
            <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
                <%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
                    <!DOCTYPE html>
                    <html>

                    <head>
                        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                        <title>Quản lý Thực đơn - Lách Tách</title>
                        <style>
                            .page-header {
                                display: flex;
                                justify-content: space-between;
                                align-items: center;
                                border-bottom: 2px solid #e5e7eb;
                                padding-bottom: 10px;
                                margin-bottom: 25px;
                            }

                            .main-content h2 {
                                margin: 0;
                                border-bottom: none;
                                padding-bottom: 0;
                                font-size: 24px;
                                font-weight: 600;
                                color: #111827;
                                justify-content: center;
                            }

                            .btn-add-new {
                                background-color: #10b981;
                                /* Màu xanh lá cây hiện đại */
                                color: white;
                                text-decoration: none;
                                padding: 10px 18px;
                                border-radius: 8px;
                                font-weight: bold;
                                font-size: 12px;
                                display: flex;
                                align-items: center;
                                gap: 6px;
                            }

                            .admin-layout {
                                display: flex;
                                flex: 1;
                                flex-wrap: nowrap;
                                min-height: calc(100vh - 78px);
                                align-items: flex-start;
                            }

                            .line2 {
                                display: flex;
                                align-items: center;
                                gap: 12px;
                                width: auto;
                            }

                            .main-content {
                                flex: 1;
                                /* Tự động húp trọn phần khoảng trống còn lại bên phải */
                                padding: 24px;
                                background-color: #ffffff;
                                box-sizing: border-box;
                                /* Bảo hiểm giữ khung, chống tràn viền màn hình */
                            }

                            .filter-form {
                                display: flex;
                                gap: 12px;
                                align-items: center;
                                background: #f8fafc;
                                border: 1px solid #e2e8f0;
                                color: #475569;
                                padding: 12px;
                                flex-wrap: wrap;
                                /*tự động xuống dòng khi screen nhỏ */
                                border-radius: 12px;
                                margin-bottom: 20px;
                                font-family: Arial, sans-serif;
                            }

                            .filter-input,
                            .filter-select {
                                padding: 8px 8px;
                                border: 1px solid #cccccc;
                                border-radius: 8px;
                                font-size: 14px;
                                color: #333333;
                                background-color: #ffffff;
                            }

                            .filter-price {
                                display: flex;
                                align-items: center;
                                gap: 6px;
                                font-size: 14px;
                                color: #555555;
                            }

                            .filter-price input {
                                width: 80px;
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

                            .menu-container {
                                display: grid;
                                grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
                                gap: 20px;
                                padding: 20px;
                            }

                            .card {
                                border: 1px solid #e0e0e0;
                                border-radius: 16px;
                                padding: 16px;
                                background: #ffffff;
                                font-family: Arial, sans-serif;
                            }

                            .card img {
                                width: 100%;
                                height: 180px;
                                object-fit: cover;
                                /* Giúp ảnh không bị méo khi co giãn */
                                border-radius: 12px;
                                margin-bottom: 12px;
                            }

                            .status {
                                display: block;
                                padding: 4px 12px;
                                border-radius: 20px;
                                font-size: 14px;
                            }

                            .active {
                                background-color: #edf7ed;
                                color: #1e4620;
                            }

                            .inactive {
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

                            .price-container {
                                border-bottom: 1px solid #eeeeee;
                                /* Thêm một đường gạch ngang mờ ở dưới giá */
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

                            .discount-percent {
                                font-size: 10px;
                                color: red;
                            }

                            .discount-price {
                                font-size: 18px;
                                font-weight: bold;
                                color: #000000;
                            }

                            .button-group {
                                display: flex;
                                gap: 12px;
                                /* Khoảng cách giữa 2 nút */
                            }

                            .btn {
                                flex: 1;
                                /* Ép 2 nút tự chia đôi chiều rộng bằng nhau (50% - 50%) */
                                padding: 10px 0;
                                border: 1px solid #cccccc;
                                border-radius: 12px;
                                /* Nút bo góc tròn */
                                background-color: #ffffff;
                                cursor: pointer;
                                font-size: 15px;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                gap: 6px;
                                /* Khoảng cách giữa icon và chữ */
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

                            .pagination {
                                display: flex;
                                justify-content: center;
                                align-items: center;
                                gap: 5px;
                                margin: 30px 0;
                                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            }

                            .pagination a,
                            .pagination span {
                                padding: 6px 12px;
                                border: 1px solid #cbd5e1;
                                border-radius: 4px;
                                text-decoration: none;
                                color: #334155;
                                font-size: 14px;
                                font-weight: 500;
                                background-color: #ffffff;
                                transition: all 0.2s ease;
                            }

                            .pagination a:hover {
                                background-color: #f8fafc;
                                border-color: #94a3b8;
                                color: #0f172a;
                            }

                            /* Kiểu dáng cho ô hiển thị thông tin số trang ở giữa */
                            .pagination .page-info {
                                background-color: #f1f5f9;
                                border-color: #cbd5e1;
                                color: #1e293b;
                                cursor: default;
                            }

                            .pagination .page-info b {
                                color: #007bff;
                            }

                            /* Trạng thái nút bị vô hiệu hóa khi ở trang đầu hoặc trang cuối */
                            .pagination .disabled {
                                color: #94a3b8;
                                background-color: #f8fafc;
                                border-color: #e2e8f0;
                                pointer-events: none;
                                /* Khóa không cho click */
                                cursor: not-allowed;
                            }
                        </style>
                    </head>

                    <body>
                        <%@ include file="/views/includes/header.jsp" %>
                            <div class="admin-layout">
                                <%@ include file="/views/includes/dashboard.jsp" %>

                                    <div class="main-content">
                                        <div class="page-header">
                                            <h2>Danh sách món ăn</h2>
                                            <a href="${pageContext.request.contextPath}/update-menu"
                                                class="btn-add-new">
                                                Thêm món ăn mới
                                            </a>
                                        </div>
                                        <!--Filter to search dish-->
                                        <form action="${pageContext.request.contextPath}/menu-management" method="get"
                                            class="filter-form">
                                            <input type="text" name="search" value="${param.search}"
                                                placeholder="Tìm kiếm món ăn..." class="filter-input"
                                                style="width: 200px;" />

                                            <select name="category" class="filter-select">
                                                <option value="">Tất cả danh mục</option>
                                                <c:forEach var="cat" items="${list}">
                                                    <option value="${cat.categoryID}" ${param.category==cat.categoryID
                                                        ? "selected" : "" }>
                                                        ${cat.categoryName}
                                                    </option>
                                                </c:forEach>
                                            </select>

                                            <select name="status" class="filter-select">
                                                <option value="1" ${param.status==1 ? 'selected' : '' }>Đang Bán
                                                </option>
                                                <option value="0" ${param.status==0 ? 'selected' : '' }>Tạm Ngưng
                                                </option>
                                            </select>

                                            <div class="filter-price">
                                                <span>Giá từ:</span>
                                                <input type="number" name="minPrice" value="${param.minPrice}"
                                                    class="filter-input" />
                                                <span>đến:</span>
                                                <input type="number" name="maxPrice" value="${param.maxPrice}"
                                                    class="filter-input" />
                                            </div>

                                            <div class="line2">
                                                <span>Sắp xếp:</span>
                                                <select name="price" class="filter-select">
                                                    <option value="price" ${param.price=='price' ? 'selected' : '' }>Giá
                                                        Gốc</option>
                                                    <option value="discountedPrice" ${param.price=='discountedPrice'
                                                        ? 'selected' : '' }>Giá Thực Tế</option>
                                                </select>

                                                <select name="sort" class="filter-select">
                                                    <option value="asc" ${param.sort=='asc' ? 'selected' : '' }>Tăng Dần
                                                        ↑</option>
                                                    <option value="desc" ${param.sort=='desc' ? 'selected' : '' }>Giảm
                                                        Dần ↓</option>
                                                </select>

                                                <input type="submit" value="LỌC" class="btn-submit" />
                                            </div>
                                        </form>
                                        <c:if test="${not empty errorPrice}">
                                            <div class="error-msg">${errorPrice}</div>
                                        </c:if>
                                        <c:if test="${not empty errorSearch}">
                                            <div class="error-msg">${errorSearch}</div>
                                        </c:if>
                                        <!--Display dish-->
                                        <div class="menu-container">

                                            <c:forEach var="item" items="${listItem}">
                                                <div class="card">

                                                    <img src="${item.image}" alt="${item.itemName}">

                                                    <span
                                                        class="status ${item.isAvailable == 1 ? 'active' : 'inactive'}">
                                                        ${item.isAvailable == 1 ? 'Đang Bán' : 'Tạm Ngưng'}
                                                    </span>

                                                    <h3 class="item-name">${item.itemName}</h3>

                                                    <p class="category">🏷️ ${item.categoryName}</p>

                                                    <div class="price-container">
                                                        <div class="discount-price">
                                                            <fmt:formatNumber value="${item.discountedPrice}"
                                                                type="number" groupingUsed="true" />đ
                                                        </div>

                                                        <div class="price">
                                                            <fmt:formatNumber value="${item.price}" type="number"
                                                                groupingUsed="true" />đ
                                                        </div>

                                                        <div class="discount-percent">
                                                            <fmt:formatNumber value="${item.discountPercent}"
                                                                type="number" groupingUsed="true" />%
                                                        </div>
                                                    </div>

                                                    <p>Chi tiết: ${item.allergyNotes}</p>

                                                    <div class="button-group">
                                                        <a href="${pageContext.request.contextPath}/update-menu?id=${item.itemID}"
                                                            class="btn">UPDATE</a>
                                                    </div>

                                                </div>
                                            </c:forEach>
                                        </div>
                                    </div>
                            </div>
                            <c:if test="${totalPage > 1}">
                                <div class="pagination">

                                    <c:choose>
                                        <c:when test="${currentPage > 1}">
                                            <a href="${pageContext.request.contextPath}/menu-management?page=1&search=${param.search}&category=${param.category}&status=${empty param.status ? -1 : param.status}&minPrice=${param.minPrice}&maxPrice=${param.maxPrice}&price=${param.price}&sort=${param.sort}"
                                                title="Về trang đầu">Đầu</a>
                                            <a href="${pageContext.request.contextPath}/menu-management?page=${currentPage - 1}&search=${param.search}&category=${param.category}&status=${empty param.status ? -1 : param.status}&minPrice=${param.minPrice}&maxPrice=${param.maxPrice}&price=${param.price}&sort=${param.sort}"
                                                title="Trang trước">Trước</a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="disabled">Đầu</span>
                                            <span class="disabled">Trước</span>
                                        </c:otherwise>
                                    </c:choose>

                                    <span class="page-info">Trang <b>${currentPage}</b> / ${totalPage}</span>

                                    <c:choose>
                                        <c:when test="${currentPage < totalPage}">
                                            <a href="${pageContext.request.contextPath}/menu-management?page=${currentPage + 1}&search=${param.search}&category=${param.category}&status=${empty param.status ? -1 : param.status}&minPrice=${param.minPrice}&maxPrice=${param.maxPrice}&price=${param.price}&sort=${param.sort}"
                                                title="Trang sau">Sau</a>
                                            <a href="${pageContext.request.contextPath}/menu-management?page=${totalPage}&search=${param.search}&category=${param.category}&status=${empty param.status ? -1 : param.status}&minPrice=${param.minPrice}&maxPrice=${param.maxPrice}&price=${param.price}&sort=${param.sort}"
                                                title="Đến trang cuối">Cuối</a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="disabled">Sau</span>
                                            <span class="disabled">Cuối</span>
                                        </c:otherwise>
                                    </c:choose>

                                </div>
                            </c:if>
                            <%@ include file="/views/includes/footer.jsp" %>
                    </body>

                    </html>