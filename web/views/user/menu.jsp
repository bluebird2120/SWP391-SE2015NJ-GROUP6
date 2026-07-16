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
                width: 100%; 
                max-width: 95%; 
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
                justify-content: flex-start; 
                background: white;
                border: 1px solid #e2e8f0;
                color: #475569;
                padding: 16px;
                border-radius: 12px;
                margin-bottom: 20px;
                box-shadow: 0 4px 10px rgba(0,0,0,0.02);
                flex-wrap: wrap; 
            }

            .line2 {
                display: flex;
                align-items: center;
                gap: 12px;
                width: auto;
                white-space: nowrap;
            }

            .filter-input, .filter-select {
                padding: 8px 12px;
                border: 1px solid #cbd5e1;
                border-radius: 8px;
                font-size: 14px;
                color: #333333;
                background-color: #ffffff;
                outline: none;
                flex: 1; 
                min-width: 140px; 
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
                width: 100px; 
                min-width: 90px;
                flex: 0 0 auto;
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
                    <c:if test="${not empty sessionScope.successMsg}">
                        <div style="background: #d1fae5; color: #065f46; padding: 12px 20px; border-radius: 8px; margin-bottom: 20px; font-weight: bold; border-left: 4px solid #10b981; display: flex; justify-content: space-between; align-items: center;">
                            <span>✅ ${sessionScope.successMsg}</span>
                            <button onclick="this.parentElement.style.display = 'none'" style="background:none; border:none; color:#065f46; font-size:16px; cursor:pointer;">✖</button>
                        </div>
                        <c:remove var="successMsg" scope="session"/>
                    </c:if>

                    <c:if test="${not empty sessionScope.errorMsg}">
                        <div style="background: #fee2e2; color: #991b1b; padding: 12px 20px; border-radius: 8px; margin-bottom: 20px; font-weight: bold; border-left: 4px solid #ef4444; display: flex; justify-content: space-between; align-items: center;">
                            <span>⚠️ ${sessionScope.errorMsg}</span>
                            <button onclick="this.parentElement.style.display = 'none'" style="background:none; border:none; color:#991b1b; font-size:16px; cursor:pointer;">✖</button>
                        </div>
                        <c:remove var="errorMsg" scope="session"/>
                    </c:if>
                    
                    <div class="page-header">
                        <h2>Khám phá ẩm thực</h2>
                    </div>
                    
                    <div style="display: flex; gap: 12px; align-items: center;">
                        <a class="cart-btn" href="${pageContext.request.contextPath}/payment-info">
                            <i class="fas fa-file-invoice-dollar"></i> HÓA ĐƠN
                        </a>
                        <a class="cart-btn" href="${pageContext.request.contextPath}/order">
                            <i class="fas fa-shopping-cart"></i> GIỎ HÀNG
                        </a>
                    </div>
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

                    <select name="cookingMethod" class="filter-select">
                        <option value="">Tất cả phương thức</option>
                        <c:forEach var="method" items="${listMethod}">
                            <option value="${method.methodID}" ${param.cookingMethod == method.methodID ? "selected" : "" }>
                                ${method.methodName}
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

                            <div class="button-group" style="display: flex; flex-direction: column; gap: 10px; margin-top: 14px;">
                                <%-- 🌟 ĐẢM BẢO DÙNG CONTEXT PATH CHUẨN --%>
                                <form action="${pageContext.request.contextPath}/order" method="POST" style="flex: 1; margin: 0; display: flex; flex-direction: column; gap: 8px;">
                                    <input type="hidden" name="action" value="add">
                                    <input type="hidden" name="itemID" value="${item.itemID}">
                                    <input type="hidden" name="quantity" value="1">
                                    <input type="hidden" name="price" value="${item.discountPercent > 0 ? item.discountedPrice : item.price}">

                                    <button type="submit" class="btn" style="width: 100%; background-color: #76493b; color: white; margin-top: 0;">
                                        Thêm vào giỏ
                                    </button>
                                </form>
                                <a href="${pageContext.request.contextPath}/dish-detail?id=${item.itemID}" class="btn">
                                    Xem chi tiết
                                </a>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty listItem}">
                        <div style="text-align: center; padding: 60px 20px; color: #94a3b8;">
                            <span style="font-size: 50px; display: block; margin-bottom: 10px;">🔍</span>
                            <b style="font-size: 16px; color: #64748b; display: block;">Không tìm thấy món ăn nào khớp bộ lọc.</b>
                            <span style="font-size: 13px; display: block; margin-top: 5px;">Vui lòng thử tìm kiếm bằng từ khóa khác hoặc điều chỉnh lại khoảng giá.</span>
                        </div>
                    </c:if>
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

        <c:if test="${sessionScope.roleInTable == 'HOST'}">

            <button onclick="openQRScanner()" 
                    style="position: fixed; bottom: 30px; right: 30px; z-index: 9999; width: 56px; height: 56px; border-radius: 50%; background-color: #D4A373; color: white; border: none; box-shadow: 0 4px 15px rgba(212, 163, 115, 0.4); cursor: pointer; font-size: 22px; transition: transform 0.2s;" 
                    title="Quét mã QR gộp bàn">
                <i class="fas fa-qrcode"></i>
            </button>

            <div id="qr-modal-overlay" style="display: none; position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.8); z-index: 10000; justify-content: center; align-items: center; flex-direction: column;">

                <div style="background: white; padding: 20px; border-radius: 12px; width: 90%; max-width: 400px; text-align: center; position: relative; box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
                    <h3 style="color: #1c4332; margin-top: 0; margin-bottom: 10px;">📸 Quét QR Gộp Bàn</h3>
                    <p style="font-size: 13px; color: #666; margin-bottom: 16px;">Hướng camera về phía mã QR của bàn bên cạnh.</p>

                    <div id="qr-reader" style="width: 100%; border-radius: 8px; overflow: hidden; border: 2px dashed #D4A373;"></div>

                    <div style="margin-top: 15px; border-top: 1px solid #eee; padding-top: 15px; text-align: left;">
                        <label style="font-size: 12px; font-weight: bold; color: #333; margin-bottom: 5px; display: block;">Hoặc nhập thủ công để Test:</label>
                        <div style="display: flex; gap: 8px;">
                            <input type="text" id="manual-qr-input" placeholder="Dán link bàn hoặc mã Token..." 
                                   style="flex: 1; padding: 10px; border: 1px solid #ccc; border-radius: 6px; outline: none; font-size: 14px;"
                                   onkeypress="if (event.key === 'Enter')
                                               submitManualQR()">
                            <button onclick="submitManualQR()" 
                                    style="background: #1c4332; color: white; border: none; padding: 0 15px; border-radius: 6px; font-weight: bold; cursor: pointer; transition: 0.2s;">
                                Gộp
                            </button>
                        </div>
                    </div>
                    <button onclick="closeQRScanner()" style="margin-top: 15px; background: #e74c3c; color: white; border: none; padding: 10px 24px; border-radius: 6px; font-weight: bold; cursor: pointer; width: 100%;">
                        Hủy quét / Đóng
                    </button>
                </div>
            </div>

            <script src="https://unpkg.com/html5-qrcode"></script>
            <script>
                        let html5QrcodeScanner = null;

                        function submitManualQR() {
                            let inputVal = document.getElementById("manual-qr-input").value.trim();
                            if (inputVal === "")
                                return;

                            let targetUrl = "";
                            if (inputVal.includes("token=")) {
                                targetUrl = inputVal; 
                            } else {
                                targetUrl = "${pageContext.request.contextPath}/scan?token=" + inputVal;
                            }
                            
                            console.log("Đang chuyển hướng đến: " + targetUrl); 

                            closeQRScanner();
                            window.location.href = targetUrl;
                        }
                        
                        function openQRScanner() {
                            document.getElementById("qr-modal-overlay").style.display = "flex";
                            document.getElementById("manual-qr-input").value = "";

                            if (!html5QrcodeScanner) {
                                html5QrcodeScanner = new Html5Qrcode("qr-reader");
                            }

                            html5QrcodeScanner.start(
                                    {facingMode: "environment"},
                                    {
                                        fps: 10, 
                                        qrbox: {width: 250, height: 250} 
                                    },
                                    (decodedText, decodedResult) => {
                                closeQRScanner(); 
                                window.location.href = decodedText;
                            },
                                    (errorMessage) => {
                            }
                            ).catch((err) => {
                                console.log("Không có camera, sử dụng chế độ nhập tay.");
                            });
                        }

                        function closeQRScanner() {
                            document.getElementById("qr-modal-overlay").style.display = "none";
                            if (html5QrcodeScanner) {
                                html5QrcodeScanner.stop().then((ignore) => {
                                    html5QrcodeScanner.clear();
                                }).catch((err) => {
                                    console.log("Stop failed: ", err);
                                });
                            }
                        }
            </script>          
        </c:if>

        <c:if test="${sessionScope.roleInTable == 'HOST'}">

            <div id="host-widget" style="position: fixed; bottom: 30px; left: 30px; z-index: 9999;">

                <button id="btn-toggle-requests" onclick="toggleRequestPanel()" 
                        style="position: relative; width: 56px; height: 56px; border-radius: 50%; background-color: #1c4332; color: white; border: none; box-shadow: 0 4px 15px rgba(28, 67, 50, 0.4); cursor: pointer; font-size: 22px; transition: transform 0.2s;">
                    <i class="fas fa-users"></i>

                    <span id="request-badge" style="display: none; position: absolute; top: -4px; right: -4px; background: #e74c3c; color: white; font-size: 12px; font-weight: bold; width: 22px; height: 22px; border-radius: 50%; align-items: center; justify-content: center; box-shadow: 0 2px 5px rgba(0,0,0,0.2);">0</span>
                </button>

                <div id="request-panel" style="display: none; position: absolute; bottom: 70px; left: 0; width: 320px; background: white; border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,0.15); border: 1px solid #eae5da; overflow: hidden;">
                    <div style="background: #fdf6f0; padding: 14px 18px; border-bottom: 1px solid #eae5da; display: flex; justify-content: space-between; align-items: center;">
                        <h4 style="margin: 0; color: #1c4332; font-size: 15px;"><i class="fas fa-bell"></i> Yêu cầu vào bàn (<span id="panel-count">0</span>)</h4>
                        <i class="fas fa-times" onclick="toggleRequestPanel()" style="cursor: pointer; color: #a8988e;"></i>
                    </div>
                    <div id="request-list-content" style="padding: 15px; max-height: 300px; overflow-y: auto; background: #ffffff;">
                        <div style="text-align: center; color: #a8988e; font-size: 13px; padding: 20px 0;">Đang kiểm tra...</div>
                    </div>
                </div>

            </div>

            <script>
                const apiPath = "${pageContext.request.contextPath}/api/table-join";
                let isPanelOpen = false;

                function toggleRequestPanel() {
                    const panel = document.getElementById('request-panel');
                    isPanelOpen = !isPanelOpen;
                    panel.style.display = isPanelOpen ? 'block' : 'none';
                    if (isPanelOpen)
                        fetchPendingRequests(); 
                }

                setInterval(fetchPendingRequests, 3000);

                function fetchPendingRequests() {
                    fetch(apiPath + '?action=getPending')
                            .then(response => response.json())
                            .then(data => {
                                const badge = document.getElementById('request-badge');
                                const listDiv = document.getElementById('request-list-content');
                                const panelCount = document.getElementById('panel-count');

                                panelCount.innerText = data.length;
                                if (data.length > 0) {
                                    badge.style.display = 'flex';
                                    badge.innerText = data.length;
                                    document.getElementById('btn-toggle-requests').style.animation = "shake 0.5s"; 
                                    setTimeout(() => document.getElementById('btn-toggle-requests').style.animation = "", 500);
                                } else {
                                    badge.style.display = 'none';
                                }

                                if (data.length === 0) {
                                    listDiv.innerHTML = '<div style="text-align: center; color: #a8988e; font-size: 13px; padding: 20px 0;">Hiện không có ai xin vào bàn.</div>';
                                } else {
                                    listDiv.innerHTML = '';
                                    data.forEach(req => {
                                        listDiv.innerHTML += `
                                    <div style="background: #fbf9f6; border: 1px solid #eae5da; border-radius: 8px; padding: 12px; margin-bottom: 10px;">
                                        <div style="font-size: 14px; font-weight: bold; color: #2c2520; margin-bottom: 8px;">
                                            👤 ` + req.name + `
                                        </div>
                                        <div style="display: flex; gap: 8px;">
                                            <button onclick="handleRequest(` + req.id + `, 'approve')" style="flex: 1; background: #10b981; color: white; border: none; padding: 6px; border-radius: 6px; cursor: pointer; font-weight: bold; font-size: 12px;">Cho phép</button>
                                            <button onclick="handleRequest(` + req.id + `, 'reject')" style="flex: 1; background: #ef4444; color: white; border: none; padding: 6px; border-radius: 6px; cursor: pointer; font-weight: bold; font-size: 12px;">Từ chối</button>
                                        </div>
                                    </div>
                                    `;
                                    });
                                }
                            });
                }

                function handleRequest(reqID, actionType) {
                    fetch(apiPath, {
                        method: 'POST',
                        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                        body: 'action=' + actionType + '&requestID=' + reqID
                    })
                            .then(response => response.text())
                            .then(res => {
                                if (res === 'success') {
                                    fetchPendingRequests(); 
                                }
                            });
                }
            </script>
            <style>
                @keyframes shake {
                    0% { transform: rotate(0deg); }
                    25% { transform: rotate(-10deg); }
                    50% { transform: rotate(10deg); }
                    75% { transform: rotate(-10deg); }
                    100% { transform: rotate(0deg); }
                }
            </style>
        </c:if>
    </body>
</html>