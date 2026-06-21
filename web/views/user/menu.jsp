<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.MenuCategory" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Quản lý Thực đơn - Lách Tách</title>

        <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@600;700&family=Nunito:wght@400;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>

        <style>
            body {
                margin: 0;
                font-family: 'Nunito', sans-serif !important;
                background-color: #fdf6f0; 
                color: #111827;
            }

            .admin-layout {
                display: flex;
                flex: 1;
                flex-wrap: nowrap;
                min-height: calc(100vh - 78px);
                align-items: flex-start;
                justify-content: center;
            }

            .main-content {
                flex: 1;
                max-width: 1200px; 
                padding: 24px;
                box-sizing: border-box;
            }

            .restaurant-banner {
                background: linear-gradient(135deg, #76493b 0%, #5a3329 100%);
                color: #f0dcc2;
                padding: 25px;
                border-radius: 16px;
                text-align: center;
                margin-bottom: 25px;
                box-shadow: 0 8px 24px rgba(118, 73, 59, 0.12);
            }

            .restaurant-banner h1 {
                font-family: 'Playfair Display', serif;
                margin: 0 0 6px 0;
                font-size: 30px;
                font-weight: 700;
            }

            .table-badge {
                display: inline-flex;
                align-items: center;
                gap: 8px;
                background-color: rgba(240, 220, 194, 0.15);
                border: 1px solid #f0dcc2;
                padding: 5px 16px;
                border-radius: 30px;
                font-size: 15px;
                font-weight: 700;
            }

            .border-cart {
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 2px solid #76493b;
                padding-bottom: 8px;
                margin-bottom: 25px;
            }

            .border-cart .page-header {
                margin: 0;
                padding: 0;
            }

            .border-cart h2 {
                font-family: 'Playfair Display', serif;
                margin: 0;
                font-size: 24px;
                font-weight: 700;
                color: #76493b;
            }

            .btn-add-new {
                background-color: #10b981;
                color: white;
                text-decoration: none;
                padding: 8px 14px;
                border-radius: 8px;
                font-weight: bold;
                font-size: 12px;
                display: flex;
                align-items: center;
                gap: 6px;
            }

            .cart-btn {
                display: inline-flex;
                align-items: center;
                gap: 8px;
                background-color: #76493b;
                color: #ffffff !important;
                text-decoration: none;
                padding: 10px 18px;
                border-radius: 20px;
                font-weight: 700;
                font-size: 14px;
                box-shadow: 0 4px 12px rgba(118, 73, 59, 0.2);
                transition: all 0.2s ease-in-out;
            }

            .cart-btn:hover {
                background-color: #5a3329;
                transform: translateY(-2px);
            }

            .filter-form {
                display: flex;
                gap: 12px;
                align-items: center;
                justify-content: space-between;
                background: white;
                border: 1px solid #e2e8f0;
                color: #475569;
                padding: 16px;
                border-radius: 12px;
                margin-bottom: 20px;
                box-shadow: 0 4px 10px rgba(0,0,0,0.02);
                flex-wrap: nowrap !important;
                overflow-x: auto; 
            }

            .line2 {
                display: flex;
                align-items: center;
                gap: 12px;
                width: auto;
                white-space: nowrap;
            }

            .filter-input,
            .filter-select {
                padding: 8px 12px;
                border: 1px solid #cbd5e1;
                border-radius: 8px;
                font-size: 14px;
                color: #333333;
                background-color: #ffffff;
                outline: none;
            }

            .filter-input:focus,
            .filter-select:focus {
                border-color: #76493b;
            }

            .filter-price {
                display: flex;
                align-items: center;
                gap: 6px;
                font-size: 14px;
                color: #555555;
                white-space: nowrap; 
            }

            .filter-price input {
                width: 90px;
                border-radius: 8px;
            }

            .btn-submit {
                background-color: #76493b; 
                color: white;
                border: none;
                padding: 8px 22px;
                border-radius: 8px;
                font-weight: bold;
                cursor: pointer;
                font-size: 14px;
                transition: background 0.2s;
            }

            .btn-submit:hover {
                background-color: #5a3329;
            }

            .menu-container {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
                gap: 25px;
                padding: 10px 0;
            }

            .card {
                border: 1px solid #e5e7eb;
                border-radius: 16px;
                padding: 16px;
                background: #ffffff;
                box-shadow: 0 6px 18px rgba(0,0,0,0.02);
                transition: transform 0.2s, box-shadow 0.2s;
                display: flex;
                flex-direction: column;
                justify-content: space-between;
            }

            .card:hover {
                transform: translateY(-4px);
                box-shadow: 0 12px 24px rgba(118, 73, 59, 0.06);
            }

            .card img {
                width: 100%;
                height: 180px;
                object-fit: cover;
                border-radius: 12px;
                margin-bottom: 12px;
            }

            .item-name {
                font-size: 18px;
                font-weight: 700;
                margin: 6px 0;
                color: #1f2937;
            }

            .category {
                color: #6b7280;
                font-size: 13px;
                margin: 4px 0 12px 0;
            }

            .price-container {
                border-top: 1px dashed #e5e7eb;
                display: flex;
                align-items: baseline;
                gap: 8px;
                padding-top: 10px;
                margin-top: auto;
            }

            .discount-price {
                font-size: 19px;
                font-weight: 700;
                color: #76493b;
            }

            .price {
                font-size: 12px;
                color: #9ca3af;
                text-decoration: line-through;
            }

            .discount-percent {
                font-size: 11px;
                color: #dc2626;
                font-weight: 600;
                background: #fef2f2;
                padding: 1px 5px;
                border-radius: 4px;
            }

            .button-group {
                display: flex;
                gap: 10px;
                margin-top: 14px;
            }

            .btn {
                flex: 1;
                margin-top: 0;
                padding: 10px 0;
                border: 1px solid #76493b;
                border-radius: 10px;
                background-color: #ffffff;
                cursor: pointer;
                font-size: 14px;
                font-weight: 600;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                gap: 6px;
                text-decoration: none;
                color: #76493b;
                transition: all 0.2s;
            }

            .btn:hover {
                background-color: #fdf6f0;
            }

            .error-msg {
                color: #b91c1c;
                background-color: #fef2f2;
                border: 1px solid #fee2e2;
                padding: 8px 14px;
                font-size: 13px;
                margin-top: 6px;
                font-weight: 500;
                border-radius: 8px;
                display: flex;
                align-items: center;
                gap: 6px;
            }

            .pagination {
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 5px;
                margin: 35px 0;
            }

            .pagination a,
            .pagination span {
                padding: 6px 14px;
                border: 1px solid #cbd5e1;
                border-radius: 6px;
                text-decoration: none;
                color: #334155;
                font-size: 14px;
                font-weight: 500;
                background-color: #ffffff;
                transition: all 0.2s ease;
            }

            .pagination a:hover {
                background-color: #76493b;
                border-color: #76493b;
                color: #ffffff;
            }

            .pagination .page-info {
                background-color: #f1f5f9;
                border-color: #cbd5e1;
                color: #1e293b;
                cursor: default;
            }

            .pagination .page-info b {
                color: #76493b;
            }

            .pagination .disabled {
                color: #94a3b8;
                background-color: #f8fafc;
                border-color: #e2e8f0;
                pointer-events: none;
                cursor: not-allowed;
            }
        </style>
    </head>

    <body>
        <div class="admin-layout">

            <div class="main-content">

                <div class="restaurant-banner">
                    <h1>Vị An Restaurant</h1>
                    <div class="table-badge">
                        <i class="fas fa-chair"></i>
                        <span>Vị trí: ${not empty sessionScope.currentTableID ? 'Bàn số '.concat(sessionScope.currentTableID) : 'Menu Trực Tuyến'}</span>
                    </div>
                </div>

                <div class="border-cart"> 
                    <div class="page-header">
                        <h2>Khám phá ẩm thực</h2>
                    </div>
                    <a class="cart-btn" href="${pageContext.request.contextPath}/order">
                        <i class="fas fa-shopping-cart"></i> GIỎ HÀNG
                    </a>
                </div>

                <form id="filterForm" action="${pageContext.request.contextPath}/menu" method="get" class="filter-form">
                    <input type="text" name="search" value="${param.search}"
                           placeholder="Tìm kiếm món ăn..." class="filter-input"/>

                    <select name="category" class="filter-select">
                        <option value="">Tất cả danh mục</option>
                        <c:forEach var="cat" items="${list}">
                            <option value="${cat.categoryID}" ${param.category==cat.categoryID ? "selected" : "" }>
                                ${cat.categoryName}
                            </option>
                        </c:forEach>
                    </select>

                    <div class="filter-price">
                        <span>Giá từ:</span>
                        <input type="number" name="minPrice" value="${param.minPrice}" class="filter-input" />
                        <span>đến:</span>
                        <input type="number" name="maxPrice" value="${param.maxPrice}" class="filter-input" />
                    </div>

                    <div class="line2">
                        <span>Sắp xếp:</span>
                        <select name="price" class="filter-select">
                            <option value="price" ${param.price=='price' ? 'selected' : '' }>Giá Gốc</option>
                            <option value="discountedPrice" ${param.price=='discountedPrice' ? 'selected' : '' }>Giá Thực Tế</option>
                        </select>

                        <select name="sort" class="filter-select">
                            <option value="asc" ${param.sort=='asc' ? 'selected' : '' }>Tăng Dần ↑</option>
                            <option value="desc" ${param.sort=='desc' ? 'selected' : '' }>Giảm Dần ↓</option>
                        </select>

                        <input type="submit" value="LỌC" class="btn-submit" />
                    </div>
                </form>

                <%-- Các vùng thông báo lỗi cũ của Giang --%>
                <c:if test="${not empty errorPrice}">
                    <div id="jsErrorPrice" class="error-msg">${errorPrice}</div>
                </c:if>
                <c:if test="${empty errorPrice}">
                    <div id="jsErrorPrice" class="error-msg" style="display: none;"></div>
                </c:if>

                <c:if test="${not empty errorSearch}">
                    <div id="jsErrorSearch" class="error-msg">${errorSearch}</div>
                </c:if>
                <c:if test="${empty errorSearch}">
                    <div id="jsErrorSearch" class="error-msg" style="display: none;"></div>
                </c:if>

                <div class="menu-container">
                    <c:forEach var="item" items="${listItem}">
                        <div class="card">
                            <img src="${item.image}" alt="${item.itemName}">
                            <h3 class="item-name">${item.itemName}</h3>
                            <p class="category">🏷️ ${item.categoryName}</p>

                            <div class="price-container">
                                <div class="discount-price">
                                    <fmt:formatNumber value="${item.discountedPrice}" type="number" groupingUsed="true" />đ
                                </div>
                                <div class="price">
                                    <fmt:formatNumber value="${item.price}" type="number" groupingUsed="true" />đ
                                </div>
                                <div class="discount-percent">
                                    <fmt:formatNumber value="${item.discountPercent}" type="number" groupingUsed="true" />%
                                </div>
                            </div>

                            <div class="button-group">
                                <a href="${pageContext.request.contextPath}/add-to-cart?id=${item.itemID}" class="btn" style="background-color: #76493b; color: white;">
                                    Thêm vào giỏ
                                </a>
                                <a href="${pageContext.request.contextPath}/dish-detail?id=${item.itemID}" class="btn">
                                    Xem chi tiết
                                </a>
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
                        <a href="${pageContext.request.contextPath}/menu?page=1&search=${param.search}&category=${param.category}&status=${empty param.status ? -1 : param.status}&minPrice=${param.minPrice}&maxPrice=${param.maxPrice}&price=${param.price}&sort=${param.sort}" title="Về trang đầu">Đầu</a>
                        <a href="${pageContext.request.contextPath}/menu?page=${currentPage - 1}&search=${param.search}&category=${param.category}&status=${empty param.status ? -1 : param.status}&minPrice=${param.minPrice}&maxPrice=${param.maxPrice}&price=${param.price}&sort=${param.sort}" title="Trang trước">Trước</a>
                    </c:when>
                    <c:otherwise>
                        <span class="disabled">Đầu</span>
                        <span class="disabled">Trước</span>
                    </c:otherwise>
                </c:choose>

                <span class="page-info">Trang <b>${currentPage}</b> / ${totalPage}</span>

                <c:choose>
                    <c:when test="${currentPage < totalPage}">
                        <a href="${pageContext.request.contextPath}/menu?page=${currentPage + 1}&search=${param.search}&category=${param.category}&status=${empty param.status ? -1 : param.status}&minPrice=${param.minPrice}&maxPrice=${param.maxPrice}&price=${param.price}&sort=${param.sort}" title="Trang sau">Sau</a>
                        <a href="${pageContext.request.contextPath}/menu?page=${totalPage}&search=${param.search}&category=${param.category}&status=${empty param.status ? -1 : param.status}&minPrice=${param.minPrice}&maxPrice=${param.maxPrice}&price=${param.price}&sort=${param.sort}" title="Đến trang cuối">Cuối</a>
                    </c:when>
                    <c:otherwise>
                        <span class="disabled">Sau</span>
                        <span class="disabled">Cuối</span>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <script>
            const filterForm = document.getElementById('filterForm');
            const jsErrorSearch = document.getElementById("jsErrorSearch");
            const jsErrorPrice = document.getElementById("jsErrorPrice");

            filterForm.onsubmit = function (event) {
                let isValid = true;

                const searchInput = filterForm.elements["search"].value.trim();
                const minPriceInput = filterForm.elements["minPrice"].value.trim();
                const maxPriceInput = filterForm.elements["maxPrice"].value.trim();

                if (jsErrorSearch) {
                    if (searchInput.length > 100) {
                        jsErrorSearch.innerHTML = "Tìm kiếm không vượt quá 100 kí tự";
                        jsErrorSearch.style.display = "flex";
                        isValid = false;
                    } else {
                        jsErrorSearch.innerHTML = "";
                        jsErrorSearch.style.display = "none";
                    }
                }

                if (jsErrorPrice) {
                    let minPrice = minPriceInput !== "" ? parseInt(minPriceInput) : 0;
                    let maxPrice = maxPriceInput !== "" ? parseInt(maxPriceInput) : Infinity;

                    if (minPrice < 0 || maxPrice < 0) {
                        jsErrorPrice.innerHTML = "Giá món ăn không được là số âm";
                        jsErrorPrice.style.display = "flex";
                        isValid = false;
                    } else if (minPriceInput !== "" && maxPriceInput !== "" && minPrice > maxPrice) {
                        jsErrorPrice.innerHTML = "Giá max phải lớn hơn giá Min";
                        jsErrorPrice.style.display = "flex";
                        isValid = false;
                    } else {
                        jsErrorPrice.innerHTML = "";
                        jsErrorPrice.style.display = "none";
                    }
                }

                if (!isValid) {
                    event.preventDefault();
                }
            };
        </script>
    </body>
</html>