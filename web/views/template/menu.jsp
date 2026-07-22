<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Thực đơn – Vị An Restaurant</title>

        <!-- Import Font Playfair Display chuẩn tiếng Việt -->
        <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700;800;900&family=Be+Vietnam+Pro:wght@400;500;600;700&display=swap" rel="stylesheet">
        
        <!-- Import Thư viện Fancybox chuẩn web Vị An -->
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@fancyapps/ui@5.0/dist/fancybox/fancybox.css"/>

        <style>
            *, *::before, *::after {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }

            /* ── Background nhám giấy cổ điển chuẩn gốc Vị An ── */
            body {
                font-family: 'Be Vietnam Pro', sans-serif;
                background-color: #f2e3d5;
                background-image: url('https://nhahangvian.com/wp-content/themes/vian/images/bg-body.jpg');
                background-repeat: repeat;
                background-position: center top;
                display: flex;
                flex-direction: column;
                min-height: 100vh;
                padding-top: 78px;
            }
            
            main {
                flex: 1;
            }

            /* ── Banner Cầu Long Biên ── */
            .menu-header-banner {
                position: relative;
                width: 100%;
                height: 220px;
                background: linear-gradient(rgba(242, 227, 213, 0.45), rgba(242, 227, 213, 0.45)),
                            url('https://nhahangvian.com/wp-content/uploads/2023/12/cau-long-bien.jpg') center/cover no-repeat;
                display: flex;
                align-items: center;
                justify-content: center;
                box-shadow: 0 4px 10px rgba(0,0,0,0.05);
            }

            .title-bg-container {
                position: relative;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                padding: 15px 50px;
            }

            .title-bg-container .brush-image {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -12%);
                width: 380px;
                height: 50px;
                object-fit: fill;
                z-index: 1;
                pointer-events: none;
                opacity: 0.95;
            }

            .title-bg-container h1 {
                position: relative;
                z-index: 2;
                font-family: 'Playfair Display', serif;
                font-size: 2.2rem;
                color: #76493b;
                font-weight: 800;
                letter-spacing: 0.18em;
                text-transform: uppercase;
                margin: 0;
                line-height: 1.2;
            }

            /* ── Container chứa các trang Menu ── */
            .menu-album-wrap {
                max-width: 1100px;
                margin: 35px auto 60px;
                padding: 0 20px;
                width: 100%;
            }

            .menu-page-pair {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 20px;
                margin-bottom: 25px;
            }

            .menu-page-item {
                background: #fdf8f2;
                border-radius: 6px;
                overflow: hidden;
                box-shadow: 0 6px 16px rgba(70, 40, 20, 0.12);
                cursor: pointer;
                transition: transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1), box-shadow 0.3s ease;
                aspect-ratio: 3/4;
                display: flex;
                align-items: center;
                justify-content: center;
            }

            .menu-page-item:hover {
                transform: translateY(-6px) scale(1.01);
                box-shadow: 0 14px 28px rgba(70, 40, 20, 0.22);
            }

            .menu-page-item img {
                width: 100%;
                height: 100%;
                object-fit: cover;
                display: block;
            }

            /* ── ĐẨY Z-INDEX FANCYBOX LÊN CAO NHẤT ĐỂ ĐÈ LÊN HEADER ── */
            .fancybox__container {
                z-index: 999999 !important; /* Đè lên toàn bộ Header fixed */
            }

            /* Tùy chỉnh viền ảnh nhỏ active */
            .fancybox__thumb {
                border-radius: 3px;
            }
            .fancybox__thumb::after {
                border-color: #d32f2f !important;
                border-width: 2px !important;
            }
            .fancybox__toolbar {
                --fancybox-color: #e0e0e0;
            }

            @media (max-width: 768px) {
                .menu-page-pair { grid-template-columns: 1fr; }
            }
        </style>
    </head>
    <body>

        <%@ include file="/views/includes/header.jsp" %>

        <main>

            <!-- Banner Cầu Long Biên -->
            <div class="menu-header-banner">
                <div class="title-bg-container">
                    <img class="brush-image" src="${pageContext.request.contextPath}/images/bottom-title.jpg" alt="vệt cọ">
                    <h1>THỰC ĐƠN</h1>
                </div>
            </div>

            <!-- DANH SÁCH CÁC TRANG MENU -->
            <div class="menu-album-wrap">
                <div id="menuAlbum">

                    <!-- CẶP 1: TRANG 1 & TRANG 2 -->
                    <div class="menu-page-pair">
                        <a class="menu-page-item" href="${pageContext.request.contextPath}/images/menu1.jpg" data-fancybox="gallery" data-caption="Vị An">
                            <img src="${pageContext.request.contextPath}/images/menu1.jpg" alt="Thực đơn trang 1">
                        </a>
                        <a class="menu-page-item" href="${pageContext.request.contextPath}/images/menu2.jpg" data-fancybox="gallery" data-caption="Vị An">
                            <img src="${pageContext.request.contextPath}/images/menu2.jpg" alt="Thực đơn trang 2">
                        </a>
                    </div>

                    <!-- CẶP 2: TRANG 3 & TRANG 4 -->
                    <div class="menu-page-pair">
                        <a class="menu-page-item" href="${pageContext.request.contextPath}/images/menu3.jpg" data-fancybox="gallery" data-caption="Vị An">
                            <img src="${pageContext.request.contextPath}/images/menu3.jpg" alt="Thực đơn trang 3">
                        </a>
                        <a class="menu-page-item" href="${pageContext.request.contextPath}/images/menu4.jpg" data-fancybox="gallery" data-caption="Vị An">
                            <img src="${pageContext.request.contextPath}/images/menu4.jpg" alt="Thực đơn trang 4">
                        </a>
                    </div>

                    <!-- CẶP 3: TRANG 5 & TRANG 6 -->
                    <div class="menu-page-pair">
                        <a class="menu-page-item" href="${pageContext.request.contextPath}/images/menu5.jpg" data-fancybox="gallery" data-caption="Vị An">
                            <img src="${pageContext.request.contextPath}/images/menu5.jpg" alt="Thực đơn trang 5">
                        </a>
                        <a class="menu-page-item" href="${pageContext.request.contextPath}/images/menu6.jpg" data-fancybox="gallery" data-caption="Vị An">
                            <img src="${pageContext.request.contextPath}/images/menu6.jpg" alt="Thực đơn trang 6">
                        </a>
                    </div>

                    <!-- CẶP 4: TRANG 7 & TRANG 8 -->
                    <div class="menu-page-pair">
                        <a class="menu-page-item" href="${pageContext.request.contextPath}/images/menu7.jpg" data-fancybox="gallery" data-caption="Vị An">
                            <img src="${pageContext.request.contextPath}/images/menu7.jpg" alt="Thực đơn trang 7">
                        </a>
                        <a class="menu-page-item" href="${pageContext.request.contextPath}/images/menu8.jpg" data-fancybox="gallery" data-caption="Vị An">
                            <img src="${pageContext.request.contextPath}/images/menu8.jpg" alt="Thực đơn trang 8">
                        </a>
                    </div>

                </div>
            </div>

        </main>

        <%@ include file="/views/includes/footer.jsp" %>

        <!-- JS Fancybox -->
        <script src="https://cdn.jsdelivr.net/npm/@fancyapps/ui@5.0/dist/fancybox/fancybox.umd.js"></script>
        <script>
            Fancybox.bind('[data-fancybox="gallery"]', {
                Toolbar: {
                    display: {
                        left: ["infobar"],
                        middle: [],
                        right: ["zoom", "download", "close"],
                    },
                },
                Thumbs: {
                    autoStart: true,
                },
                Images: {
                    zoom: true,
                },
            });
        </script>

    </body>
</html>