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
            body {
                background-color: #fdfbf7;
                color: #333;
                margin: 0;
            }
            .page-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 2px solid #f1ece6;
                padding-bottom: 10px;
                margin-bottom: 25px;
            }

            .main-content h2 {
                margin: 0;
                font-size: 24px;
                font-weight: 600;
                color: #78493b;
            }

            .btn-add-new {
                background-color: #4b6b40;
                color: white;
                text-decoration: none;
                padding: 10px 18px;
                border-radius: 8px;
                font-weight: bold;
                font-size: 12px;
                display: flex;
                align-items: center;
                gap: 6px;
                transition: background-color 0.2s;
            }
            .btn-add-new:hover {
                background-color: #395231;
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
                gap: 10px;
                width: max-content;
                flex-shrink: 0;
            }

            .main-content {
                flex: 1;
                padding: 24px;
                background-color: #ffffff;
                box-sizing: border-box;
            }

            .filter-form {
                display: flex;
                flex-wrap: wrap;
                gap: 15px;
                background: #fdfaf7;
                border: 1px solid #ebdcd0;
                padding: 16px;
                border-radius: 12px;
                margin-bottom: 20px;
                font-family: Arial, sans-serif;
                align-items: center;
            }

            .filter-input, .filter-select {
                padding: 8px 12px;
                border: 1px solid #cbd5e1;
                border-radius: 8px;
                font-size: 14px;
                min-width: 160px;
                flex: 1;
            }
            .filter-input:focus, .filter-select:focus {
                border-color: #78493b;
                outline: none;
            }
            .filter-form input[name="search"] {
                flex: 2;
            }

            .filter-price {
                display: flex;
                align-items: center;
                gap: 8px;
                font-size: 14px;
            }
            .filter-price input {
                width: 90px;
                padding: 8px;
                border: 1px solid #cbd5e1;
                border-radius: 8px;
            }

            .btn-submit {
                background-color: #78493b;
                color: white;
                border: none;
                padding: 9px 24px;
                border-radius: 8px;
                font-weight: bold;
                cursor: pointer;
                margin-left: auto;
                transition: background-color 0.2s;
            }

            .btn-submit:hover {
                background-color: #5c352d;
            }

            .menu-container {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
                gap: 20px;
                padding: 20px;
            }

            .card {
                border: 1px solid #f1ece6;
                border-radius: 16px;
                padding: 16px;
                background: #ffffff;
                box-shadow: 0 2px 8px rgba(120,73,59,0.03);
            }

            .card img {
                width: 100%;
                height: 180px;
                object-fit: cover;
                border-radius: 12px;
                margin-bottom: 12px;
            }

            .status {
                display: block;
                padding: 4px 12px;
                border-radius: 20px;
                font-size: 14px;
                font-weight: 500;
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
                color: #1a1512;
                height: 2.4em;
                line-height: 1.2em;
                overflow: hidden;
                display: -webkit-box;
                -webkit-line-clamp: 2;
            }

            .category {
                color: #7c7267;
                font-size: 14px;
                margin: 4px 0;
            }

            .price-container {
                border-bottom: 1px solid #f1ece6;
                display: flex;
                align-items: center;
                gap: 10px;
                padding-bottom: 10px;
            }

            .price {
                font-size: 11px;
                color: #9ca3af;
                text-decoration: line-through;
            }

            .discount-percent {
                font-size: 11px;
                color: #dc3545;
                font-weight: 600;
            }

            .discount-price {
                font-size: 18px;
                font-weight: bold;
                color: #de6b48;
            }

            .button-group {
                display: flex;
                gap: 12px;
            }

            .btn {
                flex: 1;
                margin-top: 10px;
                padding: 10px 0;
                border: 1px solid #ebdcd0;
                border-radius: 12px;
                background-color: #ffffff;
                cursor: pointer;
                font-size: 14px;
                font-weight: 600;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 6px;
                text-decoration: none;
                color: #78493b;
                transition: all 0.2s;
            }

            .btn:hover {
                background-color: #fdfbf7;
                border-color: #78493b;
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
            }

            .pagination a, .pagination span {
                padding: 6px 12px;
                border: 1px solid #ebdcd0;
                border-radius: 4px;
                text-decoration: none;
                color: #4a3f35;
                font-size: 14px;
                font-weight: 500;
                background-color: #ffffff;
                transition: all 0.2s ease;
            }

            .pagination a:hover {
                background-color: #78493b;
                border-color: #78493b;
                color: #ffffff;
            }

            .pagination .page-info {
                background-color: #fdfaf7;
                border-color: #ebdcd0;
                color: #4a3f35;
                cursor: default;
            }

            .pagination .page-info b {
                color: #de6b48;
            }

            .pagination .disabled {
                color: #cbd5e1;
                background-color: #f8fafc;
                border-color: #e2e8f0;
                pointer-events: none;
                cursor: not-allowed;
            }
        </style>
    </head>

    <body>
        <%@ include file="/views/includes/header.jsp" %>
        <div class="admin-layout">
            <c:if test="${not empty sessionScope.employee.roleID}">
                <%@ include file="/views/includes/dashboard.jsp" %>
            </c:if>

            <div class="main-content">
                <div class="page-header">
                    <h2>Danh sách món ăn</h2>
                    <c:if test="${sessionScope.employee.roleID == 1}">
                        <a href="${pageContext.request.contextPath}/update-menu" class="btn-add-new">
                            Thêm Món Ăn Mới
                        </a>
                    </c:if>
                    <c:if test="${sessionScope.customer != null}">
                        <a href="${pageContext.request.contextPath}/reservation?action=preorderCart" class="btn-add-new">
                            🛒 Xem Giỏ Hàng
                        </a>
                    </c:if>
                </div>

                <form id="filterForm" action="${pageContext.request.contextPath}/menu" method="get" class="filter-form">
                    <input type="text" name="search" value="${currentSearch}" placeholder="Tìm kiếm món ăn..." class="filter-input"/>

                    <select name="category" class="filter-select">
                        <option value="0">Tất cả danh mục</option>
                        <c:forEach var="cat" items="${list}">
                            <option value="${cat.categoryID}" ${currentCategory == cat.categoryID ? "selected" : "" }>
                                ${cat.categoryName}
                            </option>
                        </c:forEach>
                    </select>

                    <select name="cookingMethod" class="filter-select">
                        <option value="0">Tất cả phương thức</option>
                        <c:forEach var="method" items="${listMethod}">
                            <option value="${method.methodID}" ${currentMethod == method.methodID ? "selected" : "" }>
                                ${method.methodName}
                            </option>
                        </c:forEach>
                    </select>

                    <c:if test="${sessionScope.employee.roleID == 1}">
                        <select name="status" class="filter-select">
                            <option value="-1" ${currentStatus == -1 ? 'selected' : '' }>Tất Cả Trạng Thái</option>
                            <option value="1" ${currentStatus == 1 ? 'selected' : '' }>Đang Bán</option>
                            <option value="0" ${currentStatus == 0 ? 'selected' : '' }>Tạm Ngưng</option>
                        </select>
                    </c:if>

                    <div class="filter-price">
                        <span>Giá từ:</span>
                        <input type="number" name="minPrice" value="${currentMinPrice}" class="filter-input" />
                        <span>đến:</span>
                        <input type="number" name="maxPrice" value="${currentMaxPrice}" class="filter-input" />
                    </div>

                    <div class="line2">
                        <span>Sắp xếp:</span>
                        <select name="price" class="filter-select">
                            <option value="price" ${currentPriceType == 'price' ? 'selected' : '' }>Giá Gốc</option>
                            <option value="discountedPrice" ${currentPriceType == 'discountedPrice' ? 'selected' : '' }>Giá Thực Tế</option>
                        </select>

                        <select name="sort" class="filter-select">
                            <option value="asc" ${currentSort == 'asc' ? 'selected' : '' }>Tăng Dần ↑</option>
                            <option value="desc" ${currentSort == 'desc' ? 'selected' : '' }>Giảm Dần ↓</option>
                        </select>

                        <input type="submit" value="LỌC" class="btn-submit" />
                    </div>
                </form>

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
                            <span class="status ${item.isAvailable == 1 ? 'active' : 'inactive'}">
                                ${item.isAvailable == 1 ? 'Đang Bán' : 'Tạm Ngưng'}
                            </span>
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
                                <c:choose>
                                    <c:when test="${sessionScope.customer != null}">
                                        <form action="${pageContext.request.contextPath}/reservation" method="post" style="flex:1; margin:0; display:flex;">
                                            <input type="hidden" name="action" value="addPreorderItem">
                                            <input type="hidden" name="itemID" value="${item.itemID}">
                                            <input type="hidden" name="quantity" value="1">
                                            <button type="submit" class="btn" style="width:100%; background-color:#76493b; color:white;">
                                                Thêm vào giỏ
                                            </button>
                                        </form>
                                        <a href="${pageContext.request.contextPath}/dish-detail?id=${item.itemID}" class="btn">Xem chi tiết</a>
                                    </c:when>
                                    <c:when test="${sessionScope.employee.roleID == 1}">
                                        <a href="${pageContext.request.contextPath}/update-menu?id=${item.itemID}" class="btn">Chỉnh sửa</a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${pageContext.request.contextPath}/dish-detail?id=${item.itemID}" class="btn">Xem chi tiết</a>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:forEach>
                </div> 

                <c:if test="${empty listItem}">
                    <div style="text-align: center; padding: 60px 20px; color: #94a3b8;">
                        <span style="font-size: 50px; display: block; margin-bottom: 10px;">🔍</span>
                        <b style="font-size: 16px; color: #64748b; display: block;">Không tìm thấy món ăn nào khớp bộ lọc.</b>
                        <span style="font-size: 13px; display: block; margin-top: 5px;">Vui lòng thử tìm kiếm bằng từ khóa khác hoặc điều chỉnh lại khoảng giá.</span>
                    </div>
                </c:if>
            </div>
        </div>

        <c:if test="${totalPage > 1}">
            <div class="pagination">
                <c:choose>
                    <c:when test="${currentPage > 1}">
                        <a href="${pageContext.request.contextPath}/menu?page=1&search=${currentSearch}&category=${currentCategory}&cookingMethod=${currentMethod}&status=${currentStatus}&minPrice=${currentMinPrice}&maxPrice=${currentMaxPrice}&price=${currentPriceType}&sort=${currentSort}" title="Về trang đầu">Đầu</a>
                        <a href="${pageContext.request.contextPath}/menu?page=${currentPage - 1}&search=${currentSearch}&category=${currentCategory}&cookingMethod=${currentMethod}&status=${currentStatus}&minPrice=${currentMinPrice}&maxPrice=${currentMaxPrice}&price=${currentPriceType}&sort=${currentSort}" title="Trang trước">Trước</a>
                    </c:when>
                    <c:otherwise>
                        <span class="disabled">Đầu</span>
                        <span class="disabled">Trước</span>
                    </c:otherwise>
                </c:choose>

                <span class="page-info">Trang <b>${currentPage}</b> / ${totalPage}</span>

                <c:choose>
                    <c:when test="${currentPage < totalPage}">
                        <a href="${pageContext.request.contextPath}/menu?page=${currentPage + 1}&search=${currentSearch}&category=${currentCategory}&cookingMethod=${currentMethod}&status=${currentStatus}&minPrice=${currentMinPrice}&maxPrice=${currentMaxPrice}&price=${currentPriceType}&sort=${currentSort}" title="Trang sau">Sau</a>
                        <a href="${pageContext.request.contextPath}/menu?page=${totalPage}&search=${currentSearch}&category=${currentCategory}&cookingMethod=${currentMethod}&status=${currentStatus}&minPrice=${currentMinPrice}&maxPrice=${currentMaxPrice}&price=${currentPriceType}&sort=${currentSort}" title="Đến trang cuối">Cuối</a>
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
                    let minPrice = minPriceInput !== "" ? parseFloat(minPriceInput) : 0;
                    let maxPrice = maxPriceInput !== "" ? parseFloat(maxPriceInput) : Infinity;
                    const INT_MAX = 2147483647;

                    if (minPrice < 0 || maxPrice < 0) {
                        jsErrorPrice.innerHTML = "Giá món ăn không được là số âm!";
                        jsErrorPrice.style.display = "flex";
                        isValid = false;
                    } else if (minPrice > INT_MAX || (maxPriceInput !== "" && maxPrice > INT_MAX)) {
                        jsErrorPrice.innerHTML = "Giá tiền nhập vào vượt quá giới hạn cho phép (Tối đa 2 tỷ)!";
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
        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>