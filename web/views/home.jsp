<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                padding-top: 78px; /* đúng bằng height header fixed */
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
                height: 600px;
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
                background-size: cover;
                background-position: center;
                position: relative;
            }

            /* Dots */
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

            /* Prev / Next */
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

            /* About layout */
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
               3. ẨM THỰC
            ════════════════════════════ */
            .food-bg {
                background: #f5ede4;
            }

            .food-grid {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 0 40px;
                align-items: start;
                max-width: 1200px;
                margin: 0 auto;
            }

            /* Cột lẻ (1,3) ảnh trên, text dưới — cột chẵn (2,4) text trên, ảnh dưới */
            .food-item {
                display: flex;
                flex-direction: column;
            }
            .food-item.flip {
                flex-direction: column-reverse;
            }

            .food-img {
                position: relative;
                margin-bottom: 18px;
            }
            .food-item.flip .food-img {
                margin-bottom: 0;
                margin-top: 18px;
            }

            .food-img img {
                width: 100%;
                aspect-ratio: 3/4;
                object-fit: cover;
                display: block;
                border-radius: 8px;
            }

            /* Khung trang trí */
            .food-img::before {
                content: '';
                position: absolute;
                inset: -8px;
                border: 1.5px solid rgba(90,45,12,0.25);
                border-radius: 10px;
                pointer-events: none;
                z-index: 1;
            }

            .food-name {
                font-family: 'Playfair Display', serif;
                font-size: 0.8rem;
                font-weight: 700;
                color: #5a2d0c;
                text-transform: uppercase;
                letter-spacing: 0.1em;
                margin-bottom: 8px;
            }
            .food-desc {
                font-size: 0.85rem;
                line-height: 1.7;
                color: #7a5538;
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

            /* Khung trang trí giống ảnh gốc */
            .space-card::before {
                content: '';
                position: absolute;
                inset: 10px;
                border: 1.5px solid rgba(255,255,255,0.6);
                border-radius: 10px;
                pointer-events: none;
                z-index: 2;
            }

            .space-btn {
                display: block;
                width: fit-content;
                margin: 0 auto;
                padding: 12px 36px;
                background: #8b4513;
                color: #f0dcc2;
                font-family: 'Playfair Display', serif;
                font-size: 0.9rem;
                font-weight: 600;
                letter-spacing: 0.1em;
                text-transform: uppercase;
                text-decoration: none;
                border-radius: 30px;
                border: 1.5px solid #8b4513;
                transition: 0.3s;
            }
            .space-btn:hover {
                background: transparent;
                color: #8b4513;
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
            .video-caption {
                margin-top: 12px;
                font-size: 0.85rem;
                color: #9a6b4b;
                text-align: left;
            }

            /* ════════════════════════════
               RESPONSIVE
            ════════════════════════════ */
            @media (max-width: 900px) {
                .about-wrap {
                    grid-template-columns: 1fr;
                }
                .about-img {
                    order: -1;
                }
                .food-grid {
                    grid-template-columns: repeat(2, 1fr);
                }
                .space-grid {
                    flex-direction: column;
                }
                .space-card img {
                    height: 250px;
                }
            }
            @media (max-width: 600px) {
                .hero {
                    height: 320px;
                }
                .food-grid {
                    grid-template-columns: 1fr;
                }
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
                    <div class="slide" style="background-image:url('https://nhahangvian.com/wp-content/uploads/2025/12/Banner-Michelin88-01-min-scaled.jpg')"></div>
                    <div class="slide" style="background-image:url('https://nhahangvian.com/wp-content/uploads/2025/08/Banner-Com-ngon-scaled.jpg')"></div>
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
                        <%-- Thay URL ảnh nhà hàng của bạn vào đây --%>
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/04/HAN00052.jpg" alt="Không gian Vị An">
                    </div>
                </div>
            </section>

            <!-- ══════════════════════════════
                 3. ẨM THỰC
            ══════════════════════════════ -->
            <section class="section food-bg">
                <h2 class="section-title">Ẩm thực</h2>
                <span class="section-underline"></span>

                <div class="food-grid">

                    <%-- Cột 1: ảnh trên, text dưới --%>
                    <div class="food-item">
                        <div class="food-img">
                            <img src="https://nhahangvian.com/wp-content/uploads/2026/01/Anh-do-an-01-1.png" alt="Gà nướng thảo mộc">
                        </div>
                        <div class="food-name">Gà nướng thảo mộc</div>
                        <div class="food-desc">Gà được ướp thảo mộc vừa đủ, nướng chậm trên lửa để da vàng giòn, thịt mềm ngọt bên trong, hương lá thơm lan tỏa—mộc mạc mà tinh tế.</div>
                    </div>

                    <%-- Cột 2: text trên, ảnh dưới --%>
                    <div class="food-item flip">
                        <div class="food-img">
                            <img src="https://nhahangvian.com/wp-content/uploads/2026/01/Anh-do-an-02-1.png" alt="Cá quả nướng riềng mẻ">
                        </div>
                        <div style="padding-bottom:18px;">
                            <div class="food-name">Cá quả nướng riềng mẻ</div>
                            <div class="food-desc">Cá lăng được ướp giềng giã và mẻ chua dịu, nướng trên lửa vừa để dậy mùi thơm nồng, thịt cá săn chắc, béo ngọt, quyện vị chua thanh đặc trưng.</div>
                        </div>
                    </div>

                    <%-- Cột 3: ảnh trên, text dưới --%>
                    <div class="food-item">
                        <div class="food-img">
                            <img src="https://nhahangvian.com/wp-content/uploads/2026/01/Anh-do-an-03-1.png" alt="Nem rán">
                        </div>
                        <div class="food-name">Nem rán</div>
                        <div class="food-desc">Nem được cuốn khéo tay, rán ngập dầu đến khi vỏ ngoài vàng ruộm, giòn tan, bên trong nhân mềm ngọt, dậy mùi thịt và gia vị quen thuộc.</div>
                    </div>

                    <%-- Cột 4: text trên, ảnh dưới --%>
                    <div class="food-item flip">
                        <div class="food-img">
                            <img src="https://nhahangvian.com/wp-content/uploads/2026/01/Anh-do-an-04-1.png" alt="Gỏi cuốn tôm">
                        </div>
                        <div style="padding-bottom:18px;">
                            <div class="food-name">Gỏi cuốn tôm</div>
                            <div class="food-desc">Tôm luộc tươi ngọt cuốn cùng bún, rau xanh và đặc biệt là phải có hành trần, một món ngon cổ truyền đậm đà bản sắc dân tộc.</div>
                        </div>
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
                        <%-- Thay URL ảnh không gian của bạn --%>
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/12/khong-gian-quan-1-1.png" alt="Không gian 1">
                    </div>
                    <div class="space-card">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/12/khong-gian-quan-2.png" alt="Không gian 2">
                    </div>
                    <div class="space-card">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/12/khong-gian-quan-3.png" alt="Không gian 3">
                    </div>
                </div>

                <a href="#" class="space-btn">Xem thêm</a>
            </section>

            <!-- ══════════════════════════════
                 5. TRUYỀN THÔNG
            ══════════════════════════════ -->
            <section class="section">
                <h2 class="section-title">Truyền thông</h2>
                <span class="section-underline"></span>

                <div class="media-wrap">
                    <%-- Thay bằng URL thumbnail video và link YouTube của bạn --%>
                    <a href="https://www.youtube.com/watch?v=3zcLgiolz1E" target="_blank" class="video-thumb">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/10/SlideShow-scaled.jpg" alt="Video truyền thông">
                        <div class="play-btn"><i class="fas fa-play"></i></div>
                    </a>
                    <div class="video-caption">Hanoi Food Review</div>
                </div>
            </section>

        </main>


        <script>
            // ── HERO SLIDER ─────────────────────────────────────────────────
            let current = 0;
            const total = 3;
            const slides = document.getElementById('slides');
            const dots = document.querySelectorAll('.hero-dot');
            let autoTimer;

            function goSlide(n) {
                current = (n + total) % total;
                slides.style.transform = `translateX(-${current * 100}%)`; // ← đổi thành 100%, bỏ / total
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

            startAuto();
        </script>
        <%@ include file="/views/includes/footer.jsp" %>

    </body>
</html>
