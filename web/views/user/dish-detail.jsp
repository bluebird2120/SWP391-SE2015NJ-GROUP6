<%-- 
    Document   : dish-detail
    Created on : Jun 20, 2026, 6:21:47 PM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Chi tiết món ăn – Vị An Restaurant</title>

        <style>
            body {
                margin: 0;
                /* Sử dụng hệ font mặc định cao cấp của hệ điều hành, vừa đẹp vừa mượt */
                font-family: system-ui, -apple-system, "Segoe UI", Roboto, Helvetica, Arial, sans-serif !important;
                background-color: #fdf6f0;
                color: #111827;
            }

            /* THANH TOP BAR ĐỈNH MÀN HÌNH (CHO LUỒNG TẠI QUÁN) */
            .top-navigation-bar {
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 60px;
                background-color: #76493b;
                color: #ffffff;
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 0 40px;
                box-sizing: border-box;
                z-index: 10000;
                box-shadow: 0 2px 10px rgba(0,0,0,0.08);
            }

            .top-navigation-bar .back-link {
                color: #ffffff;
                text-decoration: none;
                font-size: 16px;
                font-weight: 600;
                display: inline-flex;
                align-items: center;
                gap: 8px;
            }

            .top-navigation-bar .restaurant-name {
                font-size: 20px;
                font-weight: 700;
                letter-spacing: 0.5px;
            }

            .top-navigation-bar .table-badge {
                background-color: rgba(255, 255, 255, 0.15);
                border: 1px solid rgba(255, 255, 255, 0.4);
                padding: 4px 14px;
                border-radius: 20px;
                font-size: 14px;
                font-weight: 700;
            }

            /* HỆ THỐNG KHUNG CHỐNG VỠ LAYOUT THEO FILE MẪU CỦA BẠN */
            .admin-layout {
                display: flex;
                flex: 1;
                flex-wrap: nowrap;
                min-height: calc(100vh - 78px);
                align-items: flex-start;
            }

            .main-content {
                flex: 1;
                padding: 24px;
                background-color: transparent;
                box-sizing: border-box;
            }

            .dine-in-padding {
                margin-top: 60px !important;
            }

            /* KHỐI CHI TIẾT MÓN ĂN CHIA ĐÔI 2 BÊN TRÊN DESKTOP */
            .dish-detail-card-box {
                background: #ffffff;
                border-radius: 20px;
                padding: 40px;
                box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
                border: 1px solid #e5e7eb;

                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 45px;
                align-items: flex-start;
            }

            /* KHỐI ALBUM ẢNH BÊN TRÁI */
            .image-left-zone {
                width: 100%;
            }

            .main-viewport {
                position: relative;
                width: 100%;
            }

            .large-display-img {
                width: 100%;
                height: 380px;
                object-fit: cover;
                border-radius: 16px;
                box-shadow: 0 4px 15px rgba(0,0,0,0.05);
            }

            .discount-tag {
                position: absolute;
                top: 15px;
                right: 15px;
                background-color: #dc2626;
                color: white;
                padding: 5px 12px;
                border-radius: 8px;
                font-size: 14px;
                font-weight: 700;
            }

            .thumbnails-row {
                display: flex;
                gap: 12px;
                margin-top: 15px;
                overflow-x: auto;
                padding-bottom: 5px;
            }

            .thumb-item-img {
                width: 75px;
                height: 75px;
                object-fit: cover;
                border-radius: 10px;
                cursor: pointer;
                border: 2px solid transparent;
                transition: all 0.2s;
                flex-shrink: 0;
            }

            .thumb-item-img:hover {
                border-color: #76493b;
                transform: scale(1.03);
            }

            /* KHỐI CHỮ BÊN PHẢI */
            .info-right-zone {
                width: 100%;
            }

            .dish-main-name {
                font-size: 34px;
                color: #76493b;
                margin: 0 0 15px 0;
                font-weight: 700;
                line-height: 1.2;
            }

            .dish-price-container {
                display: flex;
                align-items: center;
                gap: 15px;
                margin-bottom: 25px;
                padding-bottom: 20px;
                border-bottom: 1px dashed #e5e7eb;
            }

            .price-sale {
                font-size: 28px;
                font-weight: 700;
                color: #76493b;
            }

            .price-root {
                font-size: 16px;
                color: #9ca3af;
                text-decoration: line-through;
            }

            .percent-badge {
                font-size: 13px;
                color: #dc2626;
                font-weight: 700;
                background-color: #fef2f2;
                padding: 3px 8px;
                border-radius: 6px;
                border: 1px solid #fee2e2;
            }

            .dish-text-description {
                font-size: 16px;
                color: #4b5563;
                line-height: 1.7;
                margin: 0 0 25px 0;
            }

            .allergy-alert-box {
                display: flex;
                align-items: center;
                gap: 12px;
                background-color: #fff5f5;
                border: 1px solid #fed7d7;
                color: #c53030;
                padding: 14px 20px;
                border-radius: 12px;
                font-size: 15px;
                font-weight: 600;
            }

            /* ĐIỀU CHỈNH KHI CO GIAO DIỆN TRÊN ĐIỆN THOẠI */
            @media (max-width: 768px) {
                .admin-layout {
                    flex-direction: column;
                }
                .main-content {
                    padding: 15px;
                }
                .dish-detail-card-box {
                    grid-template-columns: 1fr;
                    gap: 25px;
                    padding: 20px;
                }
                .large-display-img {
                    height: 240px;
                }
                .dish-main-name {
                    font-size: 24px;
                }
            }
        </style>
    </head>

    <body>

        <c:choose>
            <c:when test="${currentTableID > 0}">
                <div class="top-navigation-bar">
                    <a href="${pageContext.request.contextPath}/menu" class="back-link">
                        <%-- Thay icon fa-chevron-left bằng ký tự Unicode mũi tên --%>
                        <span>❮ Quay lại Menu</span>
                    </a>
                    <div class="restaurant-name">Vị An Restaurant</div>
                    <div class="table-badge">
                        🪑 Bàn ${currentTableID}
                    </div>
                </div>
            </c:when>

            <c:otherwise>
                <%@ include file="/views/includes/header.jsp" %>
            </c:otherwise>
        </c:choose>


        <div class="admin-layout">

            <c:if test="${currentTableID == 0 || empty currentTableID}">
                <%@ include file="/views/includes/dashboard.jsp" %>
            </c:if>

            <div class="main-content ${currentTableID > 0 ? 'dine-in-padding' : ''}">

                <div class="dish-detail-card-box">

                    <div class="image-left-zone">
                        <div class="main-viewport">
                            <img id="mainDisplayImg" class="large-display-img" src="${dish.image}" alt="${dish.itemName}"/>

                            <c:if test="${dish.discountPercent > 0}">
                                <span class="discount-tag">-${dish.discountPercent}%</span>
                            </c:if>
                        </div>

                        <div class="thumbnails-row">
                            <img class="thumb-item-img" src="${dish.image}" onclick="changePreview(this.src)">

                            <c:forEach var="dishImg" items="${imageList}">
                                <img class="thumb-item-img" src="${dishImg.imagePath}" onclick="changePreview(this.src)"/>
                            </c:forEach>
                        </div>
                    </div>

                    <div class="info-right-zone">
                        <h1 class="dish-main-name">${dish.itemName}</h1>

                        <div class="dish-price-container">
                            <span class="price-sale">
                                <fmt:formatNumber value="${dish.discountedPrice}" type="number" groupingUsed="true"/>đ
                            </span>

                            <c:if test="${dish.discountPercent > 0}">
                                <span class="price-root">
                                    <fmt:formatNumber value="${dish.price}" type="number" groupingUsed="true"/>đ
                                </span>
                                <span class="percent-badge">Giảm ${dish.discountPercent}%</span>
                            </c:if>
                        </div>

                        <p class="dish-text-description">${dish.description}</p>

                        <c:if test="${not empty dish.allergyNotes}">
                            <div class="allergy-alert-box">
                                <span>⚠️ Thành phần cần lưu ý dị ứng: ${dish.allergyNotes}</span>
                            </div>
                        </c:if>
                    </div>
                </div> 
            </div> 
        </div> 
        <c:if test="${currentTableID == 0 || empty currentTableID}">
            <%@ include file="/views/includes/footer.jsp" %>
        </c:if>            

        <script>
            function changePreview(imgSrc) {
                document.getElementById('mainDisplayImg').src = imgSrc;
            }
        </script>
    </body>
</html>