<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Vị An Restaurant</title>
        <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,600;0,700;1,400&family=Be+Vietnam+Pro:wght@300;400;500;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>
        <style>
            *, *::before, *::after {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }

            body {
                font-family: 'Be Vietnam Pro', sans-serif;
                background: #fdf6f0;
                display: flex;
                flex-direction: column;
                min-height: 100vh;
                padding-top: 78px;
            }

            main {
                flex: 1;
            }

            /* ════════════════════════════
               1. HERO SLIDER
            ════════════════════════════ */
            .hero {
                position: relative;
                width: 100%;
                height: auto;
                aspect-ratio: 100 / 38;
                overflow: hidden;
            }

            .slides {
                display: flex;
                height: 100%;
                transition: transform 0.8s cubic-bezier(0.77,0,0.18,1);
            }

            .slide {
                min-width: 100%;
                height: 100%;
                background-size: 100% 100%;
                background-position: center;
                background-repeat: no-repeat;
                position: relative;
            }

            .hero-dots {
                position: absolute;
                bottom: 22px;
                left: 50%;
                transform: translateX(-50%);
                display: flex;
                gap: 10px;
                z-index: 10;
            }
            .hero-dot {
                width: 10px;
                height: 10px;
                border-radius: 50%;
                background: rgba(255,255,255,0.5);
                cursor: pointer;
                transition: background 0.3s, transform 0.3s;
                border: none;
            }
            .hero-dot.active {
                background: #fff;
                transform: scale(1.3);
            }

            .hero-btn {
                position: absolute;
                top: 50%;
                transform: translateY(-50%);
                width: 44px;
                height: 44px;
                border-radius: 50%;
                background: rgba(255,255,255,0.2);
                border: 1.5px solid rgba(255,255,255,0.6);
                color: #fff;
                font-size: 18px;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                z-index: 10;
                transition: background 0.3s;
            }
            .hero-btn:hover {
                background: rgba(255,255,255,0.35);
            }
            .hero-btn.prev {
                left: 24px;
            }
            .hero-btn.next {
                right: 24px;
            }

            /* ════════════════════════════
               2. VỀ CHÚNG TÔI
            ════════════════════════════ */
            .section {
                padding: 80px 5%;
            }
            .section-title {
                font-family: 'Playfair Display', serif;
                font-size: clamp(1.8rem, 3vw, 2.6rem);
                color: #5a2d0c;
                text-align: center;
                margin-bottom: 8px;
                letter-spacing: 0.04em;
            }
            .section-underline {
                display: block;
                width: 80px;
                height: 3px;
                background: linear-gradient(90deg, #c8956c, #8b4513);
                margin: 0 auto 50px;
                border-radius: 2px;
            }

            .about-wrap {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 60px;
                align-items: center;
                max-width: 1100px;
                margin: 0 auto;
            }
            .about-text h2 {
                font-family: 'Playfair Display', serif;
                font-size: clamp(1.2rem, 2vw, 1.6rem);
                color: #5a2d0c;
                text-transform: uppercase;
                letter-spacing: 0.06em;
                margin-bottom: 10px;
            }
            .about-text h3 {
                font-size: 0.8rem;
                font-weight: 600;
                color: #9a6b4b;
                text-transform: uppercase;
                letter-spacing: 0.1em;
                margin-bottom: 18px;
            }
            .about-text p {
                font-size: 0.95rem;
                line-height: 1.9;
                color: #5a3d28;
                margin-bottom: 14px;
            }
            .about-img {
                border-radius: 12px;
                overflow: hidden;
                box-shadow: 0 12px 40px rgba(90,45,12,0.15);
            }
            .about-img img {
                width: 100%;
                height: 380px;
                object-fit: cover;
                display: block;
            }

            /* ════════════════════════════
               1.5 THỰC ĐƠN ĐỘNG + THANH LỌC CAO CẤP
            ════════════════════════════ */
            .menu-showcase {
                background: #f0e6d8;
            }

            .filter-form {
                display: flex;
                flex-wrap: wrap;
                gap: 15px;
                background: #fff;
                border: 1px solid #ebdcd0;
                padding: 20px;
                border-radius: 16px;
                max-width: 1200px;
                margin: 0 auto 40px;
                align-items: center;
                box-shadow: 0 4px 16px rgba(90,45,12,0.03);
            }

            .filter-input, .filter-select {
                padding: 10px 14px;
                border: 1px solid #e3d2c2;
                border-radius: 8px;
                font-size: 14px;
                color: #5a2d0c;
                min-width: 180px;
                flex: 1;
                outline: none;
                background-color: #fff;
            }
            .filter-input:focus, .filter-select:focus {
                border-color: #c8956c;
                box-shadow: 0 0 0 3px rgba(200,149,108,0.15);
            }
            .filter-form input[name="keyword"] {
                flex: 2;
            }

            .line2 {
                display: flex;
                align-items: center;
                gap: 10px;
                width: 100%;
                justify-content: flex-end;
                margin-top: 5px;
            }
            .line2 span {
                font-size: 14px;
                color: #5a2d0c;
                font-weight: 500;
            }

            .btn-submit {
                background: linear-gradient(135deg, #c8956c, #8b4513);
                color: white;
                border: none;
                padding: 10px 30px;
                border-radius: 8px;
                font-weight: bold;
                cursor: pointer;
                transition: opacity 0.2s;
            }
            .btn-submit:hover {
                opacity: 0.9;
            }

            .menu-grid {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 28px;
                max-width: 1200px;
                margin: 0 auto;
            }

            .menu-card {
                background: #fff;
                border-radius: 14px;
                overflow: hidden;
                box-shadow: 0 8px 24px rgba(90,45,12,0.08);
                transition: transform .25s, box-shadow .25s;
                display: flex;
                flex-direction: column;
            }
            .menu-card:hover {
                transform: translateY(-6px);
                box-shadow: 0 16px 36px rgba(90,45,12,0.16);
            }
            .menu-card-img {
                width: 100%;
                height: 170px;
                overflow: hidden;
                background: #f0e4d6;
            }
            .menu-card-img img {
                width: 100%;
                height: 100%;
                object-fit: cover;
                display: block;
                transition: transform .3s;
            }
            .menu-card:hover .menu-card-img img {
                transform: scale(1.06);
            }

            .menu-card-body {
                padding: 16px 18px 20px;
                display: flex;
                flex-direction: column;
                flex: 1;
            }
            .menu-card-category {
                font-size: 0.8rem;
                font-weight: 500;
                color: #7c7267;
                margin-bottom: 6px;
            }
            .menu-card-name {
                font-family: 'Playfair Display', serif;
                font-size: 1.1rem;
                font-weight: bold;
                color: #5a2d0c;
                margin-bottom: 12px;
                line-height: 1.35;
            }

            .menu-card-price {
                margin-top: auto;
                border-top: 1px solid #f1ece6;
                padding-top: 12px;
                display: flex;
                align-items: center;
                gap: 10px;
                flex-wrap: wrap;
            }
            .menu-card-price .current {
                font-size: 1.15rem;
                font-weight: 700;
                color: #de6b48;
            }
            .menu-card-price .original {
                font-size: 0.85rem;
                color: #9ca3af;
                text-decoration: line-through;
            }
            .menu-card-price .discount-percent {
                font-size: 0.85rem;
                color: #dc3545;
                font-weight: 600;
            }

            .menu-empty {
                text-align: center;
                color: #9a6b4b;
                font-size: 0.95rem;
                padding: 60px 0;
            }

            .menu-pagination {
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 8px;
                margin-top: 44px;
                flex-wrap: wrap;
            }
            .menu-pagination a,
            .menu-pagination span {
                min-width: 38px;
                height: 38px;
                padding: 0 10px;
                display: flex;
                align-items: center;
                justify-content: center;
                border-radius: 8px;
                border: 1px solid #e3d2c2;
                text-decoration: none;
                color: #5a2d0c;
                font-size: 0.88rem;
                font-weight: 500;
                background: #fff;
            }
            .menu-pagination a:hover {
                background: #f5ece4;
            }
            .menu-pagination .active {
                background: linear-gradient(135deg, #c8956c, #8b4513);
                color: #fff;
                border-color: transparent;
            }
            .menu-pagination .ellipsis {
                border: none;
                background: transparent;
                color: #b89c8a;
            }

            /* Hiệu ứng mờ nhẹ trong lúc dữ liệu đang tải ngầm (AJAX) */
            .ajax-loading {
                opacity: 0.4;
                pointer-events: none;
                transition: opacity 0.2s ease;
            }

            @media (max-width: 980px) {
                .menu-grid {
                    grid-template-columns: repeat(2, 1fr);
                }
            }
            @media (max-width: 560px) {
                .menu-grid {
                    grid-template-columns: 1fr;
                }
                .filter-form {
                    flex-direction: column;
                    align-items: stretch;
                }
            }

            /* ════════════════════════════
               4. KHÔNG GIAN NHÀ HÀNG
            ════════════════════════════ */
            .space-bg {
                background: #f0e6d8;
            }
            .space-grid {
                display: flex;
                gap: 20px;
                justify-content: center;
                align-items: stretch;
                max-width: 1100px;
                margin: 0 auto 36px;
            }
            .space-card {
                flex: 1;
                border-radius: 16px;
                overflow: hidden;
                position: relative;
                box-shadow: 0 8px 30px rgba(90,45,12,0.12);
            }
            .space-card img {
                width: 100%;
                height: 400px;
                object-fit: cover;
                display: block;
            }
            .space-card::before {
                content: '';
                position: absolute;
                inset: 10px;
                border: 1.5px solid rgba(255,255,255,0.6);
                border-radius: 10px;
                pointer-events: none;
                z-index: 2;
            }

            /* ════════════════════════════
               5. TRUYỀN THÔNG
            ════════════════════════════ */
            .media-wrap {
                max-width: 900px;
                margin: 0 auto;
            }
            .video-thumb {
                position: relative;
                border-radius: 12px;
                overflow: hidden;
                cursor: pointer;
                box-shadow: 0 12px 40px rgba(90,45,12,0.15);
            }
            .video-thumb img {
                width: 100%;
                display: block;
            }
            .play-btn {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                width: 70px;
                height: 70px;
                background: rgba(255,255,255,0.85);
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 28px;
                color: #8b4513;
                transition: transform 0.3s, background 0.3s;
            }
            .video-thumb:hover .play-btn {
                transform: translate(-50%, -50%) scale(1.1);
                background: #fff;
            }
        </style>
    </head>
    <body>

        <%@ include file="/views/includes/header.jsp" %>

        <main>

            <!-- ══════════════════════════════
                 1. HERO SLIDER
            ══════════════════════════════ -->
            <section class="hero">
                <div class="slides" id="slides">
                    <div class="slide" style="background-image:url('https://nhahangvian.com/wp-content/uploads/2026/06/Banner-Michelin-01-scaled.jpg')"></div>
                    <div class="slide" style="background-image:url('https://nhahangvian.com/wp-content/uploads/2026/06/Banner-Michelin-02-scaled.jpg')"></div>
                    <div class="slide" style="background-image:url('https://nhahangvian.com/wp-content/uploads/2025/08/Dam-da-huong-vi-viet-025-scaled.jpg')"></div>
                </div>

                <button class="hero-btn prev" onclick="moveSlide(-1)"><i class="fas fa-chevron-left"></i></button>
                <button class="hero-btn next" onclick="moveSlide(1)"><i class="fas fa-chevron-right"></i></button>

                <div class="hero-dots" id="heroDots">
                    <button class="hero-dot active" onclick="goSlide(0)"></button>
                    <button class="hero-dot" onclick="goSlide(1)"></button>
                    <button class="hero-dot" onclick="goSlide(2)"></button>
                </div>
            </section>

            <!-- ══════════════════════════════
                 1.5 THỰC ĐƠN ĐỘNG + THANH BỘ LỌC
            ══════════════════════════════ -->
            <section id="menu-section" class="section menu-showcase">
                <h2 class="section-title">Ẩm Thực</h2>
                <span class="section-underline"></span>

                <form id="filterForm" action="${pageContext.request.contextPath}/home" method="get" class="filter-form">
                    <input type="text" name="keyword" value="${keyword}" placeholder="Tìm kiếm món ăn..." class="filter-input"/>

                    <select name="category" class="filter-select">
                        <option value="">Tất cả danh mục</option>
                        <c:forEach var="cat" items="${listCategory}">
                            <option value="${cat.categoryID}" ${param.category == cat.categoryID ? "selected" : ""}>
                                ${cat.categoryName}
                            </option>
                        </c:forEach>
                    </select>

                    <select name="cookingMethod" class="filter-select">
                        <option value="">Tất cả phương thức</option>
                        <c:forEach var="method" items="${listMethod}">
                            <option value="${method.methodID}" ${param.cookingMethod == method.methodID ? "selected" : ""}>
                                ${method.methodName}
                            </option>
                        </c:forEach>
                    </select>

                    <div class="line2">
                        <span>Sắp xếp:</span>
                        <select name="price" class="filter-select">
                            <option value="price" ${param.price == 'price' ? 'selected' : ''}>Giá Gốc</option>
                            <option value="discountedPrice" ${param.price == 'discountedPrice' || empty param.price ? 'selected' : ''}>Giá Thực Tế</option>
                        </select>

                        <select name="sort" class="filter-select">
                            <option value="asc" ${param.sort == 'asc' || empty param.sort ? 'selected' : ''}>Tăng Dần ↑</option>
                            <option value="desc" ${param.sort == 'desc' ? 'selected' : ''}>Giảm Dần ↓</option>
                        </select>

                        <input type="submit" value="LỌC" class="btn-submit" />
                    </div>
                </form>

                <div id="menuContainerView">
                    <c:choose>
                        <c:when test="${empty menuItems}">
                            <div class="menu-empty">
                                <span style="font-size: 40px; display: block; margin-bottom: 10px;">🔍</span>
                                <b>Không tìm thấy món ăn phù hợp với bộ lọc hiện tại.</b>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="menu-grid">
                                <c:forEach var="item" items="${menuItems}">
                                    <a href="${pageContext.request.contextPath}/dish-detail?id=${item.itemID}" class="menu-card" style="text-decoration: none;">
                                        <div class="menu-card-img">
                                            <img src="${item.image}" alt="${item.itemName}">
                                        </div>
                                        <div class="menu-card-body">
                                            <div class="menu-card-category">🏷️ ${item.categoryName}</div>
                                            <div class="menu-card-name">${item.itemName}</div>

                                            <div class="menu-card-price">
                                                <span class="current">
                                                    <fmt:formatNumber value="${item.discountedPrice}" type="number" groupingUsed="true"/>đ
                                                </span>
                                                <span class="original">
                                                    <fmt:formatNumber value="${item.price}" type="number" groupingUsed="true"/>đ
                                                </span>
                                                <span class="discount-percent">
                                                    <fmt:formatNumber value="${item.discountPercent}" type="number" groupingUsed="true"/>%
                                                </span>
                                            </div>
                                        </div>
                                    </a>
                                </c:forEach>
                            </div>

                            <c:if test="${totalPages > 1}">
                                <div class="menu-pagination">
                                    <c:set var="kw" value="${keyword != null ? keyword : ''}"/>
                                    <c:set var="catParam" value="${param.category != null ? param.category : ''}"/>
                                    <c:set var="methodParam" value="${param.cookingMethod != null ? param.cookingMethod : ''}"/>
                                    <c:set var="priceParam" value="${param.price != null ? param.price : ''}"/>
                                    <c:set var="sortParam" value="${param.sort != null ? param.sort : ''}"/>
                                    <c:set var="baseLink" value="${pageContext.request.contextPath}/home?keyword=${kw}&category=${catParam}&cookingMethod=${methodParam}&price=${priceParam}&sort=${sortParam}"/>

                                    <c:if test="${currentPage > 1}">
                                        <a href="${baseLink}&page=${currentPage - 1}">
                                            <i class="fas fa-chevron-left"></i>
                                        </a>
                                    </c:if>

                                    <c:set var="windowSize" value="1"/>
                                    <c:set var="startPage" value="${currentPage - windowSize > 1 ? currentPage - windowSize : 1}"/>
                                    <c:set var="endPage" value="${currentPage + windowSize < totalPages ? currentPage + windowSize : totalPages}"/>

                                    <c:if test="${startPage > 1}">
                                        <a href="${baseLink}&page=1">1</a>
                                        <c:if test="${startPage > 2}">
                                            <span class="ellipsis">…</span>
                                        </c:if>
                                    </c:if>

                                    <c:forEach var="i" begin="${startPage}" end="${endPage}">
                                        <c:choose>
                                            <c:when test="${i == currentPage}">
                                                <span class="active">${i}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${baseLink}&page=${i}">${i}</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>

                                    <c:if test="${endPage < totalPages}">
                                        <c:if test="${endPage < totalPages - 1}">
                                            <span class="ellipsis">…</span>
                                        </c:if>
                                        <a href="${baseLink}&page=${totalPages}">${totalPages}</a>
                                    </c:if>

                                    <c:if test="${currentPage < totalPages}">
                                        <a href="${baseLink}&page=${currentPage + 1}">
                                            <i class="fas fa-chevron-right"></i>
                                        </a>
                                    </c:if>
                                </div>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>

            <!-- ══════════════════════════════
                 2. VỀ CHÚNG TÔI
            ══════════════════════════════ -->
            <section class="section">
                <h2 class="section-title">Về chúng tôi</h2>
                <span class="section-underline"></span>
                <div class="about-wrap">
                    <div class="about-text">
                        <h2>Vị An – Cơm ngon tròn vị</h2>
                        <h3>Nhà hàng cơm Việt, quán cơm gia đình ngon tại Hà Nội.</h3>
                        <p>Tại <strong>Vị An</strong>, triết lý của chúng tôi rất đơn giản: chia sẻ hương vị và văn hóa thưởng thức cơm Việt tới tất cả mọi người. Chúng tôi làm điều này bằng việc sử dụng nguồn nguyên liệu tươi sạch nhất, và chế biến chúng qua đôi tay của những người đầu bếp tận tâm.</p>
                        <p>Không gian tại nhà hàng được lấy cảm hứng từ những giá trị truyền thống của Việt Nam kết hợp với những thứ hiện đại để tạo nên một cảm giác xưa cũ kết hợp cùng những thứ mới mẻ.</p>
                        <p>Với chủ đạo là gỗ và cây, những thứ gắn liền nhất với thiên nhiên sẽ khiến trải nghiệm dùng bữa thực sự khác biệt!</p>
                    </div>
                    <div class="about-img">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/04/HAN00052.jpg" alt="Không gian Vị An">
                    </div>
                </div>
            </section>

            <!-- ══════════════════════════════
                 4. KHÔNG GIAN NHÀ HÀNG
            ══════════════════════════════ -->
            <section class="section space-bg">
                <h2 class="section-title">Không gian nhà hàng</h2>
                <span class="section-underline"></span>
                <div class="space-grid">
                    <div class="space-card">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/12/khong-gian-quan-1-1.png" alt="Không gian 1">
                    </div>
                    <div class="space-card">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/12/khong-gian-quan-2.png" alt="Không gian 2">
                    </div>
                    <div class="space-card">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/12/khong-gian-quan-3.png" alt="Không gian 3">
                    </div>
                </div>
            </section>

            <!-- ══════════════════════════════
                 5. TRUYỀN THÔNG
            ══════════════════════════════ -->
            <section class="section">
                <h2 class="section-title">Truyền thông</h2>
                <span class="section-underline"></span>
                <div class="media-wrap">
                    <a href="https://www.youtube.com/watch?v=3zcLgiolz1E" target="_blank" class="video-thumb">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/10/SlideShow-scaled.jpg" alt="Video truyền thông">
                        <div class="play-btn"><i class="fas fa-play"></i></div>
                    </a>
                </div>
            </section>

        </main>

        <script>
            /* ── 1. CORE HERO SLIDER CONTROL ── */
            (function () {
                let current = 0;
                const slides = document.getElementById('slides');
                const dots = document.querySelectorAll('.hero-dot');
                const total = slides ? slides.children.length : 0;
                let autoTimer;

                if (!slides || total === 0)
                    return;

                function goSlide(n) {
                    current = (n + total) % total;
                    slides.style.transform = 'translateX(-' + (current * 100) + '%)';
                    dots.forEach((d, i) => d.classList.toggle('active', i === current));
                }

                function moveSlide(dir) {
                    clearInterval(autoTimer);
                    goSlide(current + dir);
                    startAuto();
                }

                function startAuto() {
                    autoTimer = setInterval(() => goSlide(current + 1), 5000);
                }

                window.goSlide = goSlide;
                window.moveSlide = moveSlide;

                goSlide(0);
                startAuto();
            })();

            /* ── 2. CORE AJAX FETCH SYSTEM (NO RELOAD TRANG) ── */
            document.addEventListener("DOMContentLoaded", function () {
                const filterForm = document.getElementById('filterForm');
                const menuContainerView = document.getElementById('menuContainerView');

                if (!filterForm || !menuContainerView)
                    return;

                function fetchMenuDataAjax(url) {
                    menuContainerView.classList.add('ajax-loading');

                    fetch(url)
                        .then(response => {
                            if (!response.ok) throw new Error("Mạng lỗi");
                            return response.text();
                        })
                        .then(htmlText => {
                            const parser = new DOMParser();
                            const doc = parser.parseFromString(htmlText, 'text/html');
                            const incomingContent = doc.getElementById('menuContainerView');

                            if (incomingContent) {
                                menuContainerView.innerHTML = incomingContent.innerHTML;
                                window.history.pushState({}, '', url);
                            }
                            menuContainerView.classList.remove('ajax-loading');
                        })
                        .catch(error => {
                            console.error("Lỗi đồng bộ AJAX:", error);
                            menuContainerView.classList.remove('ajax-loading');
                        });
                }

                filterForm.addEventListener('submit', function (e) {
                    e.preventDefault();
                    
                    const formAction = filterForm.getAttribute('action') || window.location.pathname;
                    const urlSearchParams = new URLSearchParams(new FormData(filterForm)).toString();
                    const targetUrl = formAction + '?' + urlSearchParams;

                    fetchMenuDataAjax(targetUrl);
                });

                menuContainerView.addEventListener('click', function (e) {
                    const paginationLink = e.target.closest('.menu-pagination a');
                    if (paginationLink) {
                        e.preventDefault();
                        
                        const targetUrl = paginationLink.getAttribute('href');
                        fetchMenuDataAjax(targetUrl);
                    }
                });
            });
        </script>
        <%@ include file="/views/includes/footer.jsp" %>

    </body>
</html>