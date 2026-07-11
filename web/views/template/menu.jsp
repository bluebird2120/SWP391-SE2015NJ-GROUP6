<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Thực đơn – Vị An Restaurant</title>
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
                /* Nền be/kem giống trang gốc */
                background: #f0e6da;
                display: flex;
                flex-direction: column;
                min-height: 100vh;
                padding-top: 78px;
            }
            main {
                flex: 1;
            }

            /* ── Tiêu đề "Thực đơn" căn giữa ── */
            .page-heading {
                text-align: center;
                padding: 48px 5% 32px;
            }
            .page-heading h1 {
                font-family: 'Playfair Display', serif;
                font-size: clamp(1.8rem, 3.5vw, 2.8rem);
                color: #5a2d0c;
                font-weight: 700;
                letter-spacing: 0.05em;
            }

            /* ── Wrapper toàn bộ grid ── */
            .menu-wrap {
                max-width: 1200px;
                margin: 0 auto;
                padding: 0 24px 64px;
                width: 100%;
            }

            /* ── Grid 2 cột ── */
            .menu-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 10px;
            }

            /* ── Mỗi ô ảnh ── */
            .menu-item {
                background: #e8d8c4;
                overflow: hidden;
                cursor: zoom-in;
                position: relative;
            }
            .menu-item img {
                width: 100%;
                height: 100%;
                object-fit: cover;
                display: block;
                transition: transform 0.3s ease;
            }
            .menu-item:hover img {
                transform: scale(1.02);
            }

            /* Placeholder khi chưa có ảnh */
            .menu-item-placeholder {
                width: 100%;
                aspect-ratio: 3/4;
                background: #ddc9b0;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                gap: 10px;
                color: #9a6b4b;
            }
            .menu-item-placeholder i {
                font-size: 2rem;
                opacity: 0.5;
            }
            .menu-item-placeholder span {
                font-size: 0.82rem;
                opacity: 0.6;
            }

            /* ── Lightbox ── */
            .lightbox {
                display: none;
                position: fixed;
                inset: 0;
                background: rgba(0,0,0,0.92);
                z-index: 9999;
                align-items: center;
                justify-content: center;
            }
            .lightbox.open {
                display: flex;
            }
            .lightbox img {
                max-width: 92vw;
                max-height: 94vh;
                display: block;
            }
            .lb-close {
                position: absolute;
                top: 14px;
                right: 22px;
                color: #fff;
                font-size: 2.4rem;
                cursor: pointer;
                opacity: 0.8;
                line-height: 1;
            }
            .lb-close:hover {
                opacity: 1;
            }
            .lb-nav {
                position: absolute;
                top: 50%;
                transform: translateY(-50%);
                color: #fff;
                font-size: 3rem;
                cursor: pointer;
                opacity: 0.65;
                padding: 10px;
                user-select: none;
                line-height: 1;
            }
            .lb-nav:hover {
                opacity: 1;
            }
            .lb-prev {
                left: 12px;
            }
            .lb-next {
                right: 12px;
            }
            .lb-counter {
                position: absolute;
                bottom: 16px;
                left: 50%;
                transform: translateX(-50%);
                color: rgba(255,255,255,0.7);
                font-size: 0.85rem;
            }

            @media (max-width: 640px) {
                .menu-grid {
                    grid-template-columns: 1fr;
                }
            }
        </style>
    </head>
    <body>

        <%@ include file="/views/includes/header.jsp" %>

        <main>

            <!-- Tiêu đề -->
            <div class="page-heading">
                <h1>Thực đơn</h1>
            </div>

            <!-- Grid 2 cột ảnh tờ menu -->
            <div class="menu-wrap">
                <div class="menu-grid" id="menuGrid">

                    <%--
                        HƯỚNG DẪN CHÈN ẢNH:
                        Thay phần <div class="menu-item-placeholder">...</div>
                        bằng: <img src="${pageContext.request.contextPath}/images/menu/menu-1.jpg" alt="Thực đơn trang 1">
                        Upload ảnh vào thư mục: web/images/menu/
                    --%>

                    <div class="menu-item" onclick="openLightbox(0)">
                        <%-- Trang 1: thay bằng <img src="..." alt="..."> --%>
                        <div class="menu-item-placeholder">
                            <i class="fas fa-image"></i>
                            <span>Ảnh thực đơn trang 1</span>
                        </div>
                    </div>

                    <div class="menu-item" onclick="openLightbox(1)">
                        <%-- Trang 2 --%>
                        <div class="menu-item-placeholder">
                            <i class="fas fa-image"></i>
                            <span>Ảnh thực đơn trang 2</span>
                        </div>
                    </div>

                    <div class="menu-item" onclick="openLightbox(2)">
                        <%-- Trang 3 --%>
                        <div class="menu-item-placeholder">
                            <i class="fas fa-image"></i>
                            <span>Ảnh thực đơn trang 3</span>
                        </div>
                    </div>

                    <div class="menu-item" onclick="openLightbox(3)">
                        <%-- Trang 4 --%>
                        <div class="menu-item-placeholder">
                            <i class="fas fa-image"></i>
                            <span>Ảnh thực đơn trang 4</span>
                        </div>
                    </div>

                    <div class="menu-item" onclick="openLightbox(4)">
                        <%-- Trang 5 --%>
                        <div class="menu-item-placeholder">
                            <i class="fas fa-image"></i>
                            <span>Ảnh thực đơn trang 5</span>
                        </div>
                    </div>

                    <div class="menu-item" onclick="openLightbox(5)">
                        <%-- Trang 6 --%>
                        <div class="menu-item-placeholder">
                            <i class="fas fa-image"></i>
                            <span>Ảnh thực đơn trang 6</span>
                        </div>
                    </div>

                    <div class="menu-item" onclick="openLightbox(6)">
                        <%-- Trang 7 --%>
                        <div class="menu-item-placeholder">
                            <i class="fas fa-image"></i>
                            <span>Ảnh thực đơn trang 7</span>
                        </div>
                    </div>

                    <div class="menu-item" onclick="openLightbox(7)">
                        <%-- Trang 8 --%>
                        <div class="menu-item-placeholder">
                            <i class="fas fa-image"></i>
                            <span>Ảnh thực đơn trang 8</span>
                        </div>
                    </div>

                </div>
            </div>

        </main>

        <!-- Lightbox -->
        <div class="lightbox" id="lightbox">
            <span class="lb-close" onclick="closeLightbox()">&times;</span>
            <span class="lb-nav lb-prev" onclick="changeImg(-1)">&#8249;</span>
            <img id="lb-img" src="" alt="">
            <span class="lb-nav lb-next" onclick="changeImg(1)">&#8250;</span>
            <span class="lb-counter" id="lb-counter"></span>
        </div>

        <%@ include file="/views/includes/footer.jsp" %>

        <script>
            (function () {
                var items = Array.from(document.querySelectorAll('#menuGrid .menu-item'));
                var srcs = items.map(function (el) {
                    var img = el.querySelector('img');
                    return img ? img.src : '';
                });
                var current = 0;

                window.openLightbox = function (idx) {
                    if (!srcs[idx])
                        return; // chưa có ảnh thì không mở
                    current = idx;
                    document.getElementById('lb-img').src = srcs[current];
                    document.getElementById('lb-counter').textContent =
                            (current + 1) + ' / ' + srcs.filter(Boolean).length;
                    document.getElementById('lightbox').classList.add('open');
                    document.body.style.overflow = 'hidden';
                };

                window.closeLightbox = function () {
                    document.getElementById('lightbox').classList.remove('open');
                    document.body.style.overflow = '';
                };

                window.changeImg = function (dir) {
                    var total = srcs.length;
                    current = (current + dir + total) % total;
                    document.getElementById('lb-img').src = srcs[current];
                    document.getElementById('lb-counter').textContent =
                            (current + 1) + ' / ' + srcs.filter(Boolean).length;
                };

                document.getElementById('lightbox').addEventListener('click', function (e) {
                    if (e.target === this)
                        closeLightbox();
                });

                document.addEventListener('keydown', function (e) {
                    if (!document.getElementById('lightbox').classList.contains('open'))
                        return;
                    if (e.key === 'Escape')
                        closeLightbox();
                    if (e.key === 'ArrowLeft')
                        changeImg(-1);
                    if (e.key === 'ArrowRight')
                        changeImg(1);
                });
            })();
        </script>

    </body>
</html>