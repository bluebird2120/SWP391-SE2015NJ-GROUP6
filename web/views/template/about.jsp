<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Giới thiệu – Vị An Restaurant</title>
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

            /* ════════ SHARED ════════ */
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

            /* ════════ 1. VỀ CHÚNG TÔI ════════ */
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
                font-size: 0.85rem;
                font-weight: 600;
                color: #9a6b4b;
                text-transform: uppercase;
                letter-spacing: 0.1em;
                margin-bottom: 20px;
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
                height: 420px;
                object-fit: cover;
                display: block;
            }

            /* ════════ 2. Ý NGHĨA TÊN ════════ */
            .meaning-bg {
                background: #f5ede4;
            }
            .meaning-wrap {
                max-width: 860px;
                margin: 0 auto;
                text-align: center;
            }
            .meaning-wrap p {
                font-size: 1rem;
                line-height: 2;
                color: #5a3d28;
                margin-bottom: 16px;
            }
            .meaning-wrap .brand-name {
                font-family: 'Playfair Display', serif;
                font-size: 2.2rem;
                color: #8b4513;
                font-style: italic;
                display: block;
                margin: 20px 0 10px;
            }
            .meaning-highlight {
                display: inline-block;
                background: #8b4513;
                color: #f0dcc2;
                font-family: 'Playfair Display', serif;
                font-size: 0.85rem;
                font-weight: 600;
                letter-spacing: 0.12em;
                text-transform: uppercase;
                padding: 6px 22px;
                border-radius: 30px;
                margin: 6px;
            }

            /* ════════ 3. VIDEO ════════ */
            .video-wrap {
                max-width: 860px;
                margin: 0 auto;
            }
            .video-container {
                position: relative;
                width: 100%;
                padding-bottom: 56.25%;
                border-radius: 16px;
                overflow: hidden;
                box-shadow: 0 12px 40px rgba(90,45,12,0.15);
            }
            .video-container iframe {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                border: none;
            }

            /* ════════ 4. ẢNH NHÀ HÀNG ════════ */
            .gallery-note {
                text-align: center;
                font-size: 0.82rem;
                color: #9a6b4b;
                font-style: italic;
                margin-top: 20px;
            }
            .gallery-grid {
                display: grid;
                grid-template-columns: repeat(3, 1fr);
                grid-template-rows: auto auto;
                gap: 16px;
                max-width: 1100px;
                margin: 0 auto;
            }
            .gallery-item:first-child {
                grid-column: 1 / 3;
            }
            .gallery-item {
                border-radius: 12px;
                overflow: hidden;
                box-shadow: 0 6px 24px rgba(90,45,12,0.12);
                cursor: pointer;
                position: relative;
            }
            .gallery-item::before {
                content: '';
                position: absolute;
                inset: 8px;
                border: 1.5px solid rgba(255,255,255,0.5);
                border-radius: 8px;
                pointer-events: none;
                z-index: 2;
            }
            .gallery-item img {
                width: 100%;
                height: 280px;
                object-fit: cover;
                display: block;
                transition: transform 0.4s ease;
            }
            .gallery-item:first-child img {
                height: 380px;
            }
            .gallery-item:hover img {
                transform: scale(1.04);
            }

            /* ════════ LIGHTBOX ════════ */
            .lightbox {
                display: none;
                position: fixed;
                inset: 0;
                background: rgba(0,0,0,0.85);
                z-index: 9999;
                align-items: center;
                justify-content: center;
            }
            .lightbox.open {
                display: flex;
            }
            .lightbox img {
                max-width: 90vw;
                max-height: 90vh;
                border-radius: 8px;
                box-shadow: 0 20px 60px rgba(0,0,0,0.4);
            }
            .lightbox-close {
                position: absolute;
                top: 20px;
                right: 28px;
                color: #fff;
                font-size: 2rem;
                cursor: pointer;
                line-height: 1;
            }

            /* ════════ RESPONSIVE ════════ */
            @media (max-width: 900px) {
                .about-wrap {
                    grid-template-columns: 1fr;
                }
                .about-img {
                    order: -1;
                }
                .gallery-grid {
                    grid-template-columns: 1fr 1fr;
                }
                .gallery-item:first-child {
                    grid-column: 1 / -1;
                }
            }
            @media (max-width: 600px) {
                .gallery-grid {
                    grid-template-columns: 1fr;
                }
                .gallery-item:first-child {
                    grid-column: auto;
                }
            }
        </style>
    </head>
    <body>

        <%@ include file="/views/includes/header.jsp" %>

        <main>

            <!-- ══ 1. VỀ CHÚNG TÔI ══ -->
            <section class="section">
                <h2 class="section-title">Về chúng tôi</h2>
                <span class="section-underline"></span>

                <div class="about-wrap">
                    <div class="about-text">
                        <h2>Vị An – Hương vị ẩm thực Việt</h2>
                        <h3>Nhà hàng cơm Việt, không gian ấm cúng tại Hà Nội.</h3>
                        <p>Tại <strong>Vị An</strong>, triết lý của chúng tôi rất đơn giản: mang đến hương vị và văn hóa thưởng thức cơm Việt thuần túy tới tất cả mọi người. Chúng tôi làm điều này bằng việc sử dụng nguồn nguyên liệu tươi sạch nhất, được chế biến qua đôi tay của những người đầu bếp tận tâm và giàu kinh nghiệm.</p>
                        <p>Không gian tại nhà hàng được lấy cảm hứng từ những giá trị truyền thống của Việt Nam kết hợp với nét hiện đại tinh tế, tạo nên cảm giác vừa quen thuộc vừa mới lạ khi bước vào.</p>
                        <p>Với tông màu ấm áp đặc trưng cùng cách bài trí gần gũi, <strong>Vị An</strong> luôn mong muốn mỗi bữa ăn tại đây là một trải nghiệm đáng nhớ với gia đình và bạn bè.</p>
                    </div>
                    <div class="about-img">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/04/HAN00052.jpg" alt="Không gian Vị An">
                    </div>
                </div>
            </section>

            <!-- ══ 2. Ý NGHĨA TÊN ══ -->
            <section class="section meaning-bg">
                <h2 class="section-title">Ý nghĩa tên gọi</h2>
                <span class="section-underline"></span>

                <div class="meaning-wrap">
                    <span class="brand-name">Vị An</span>

                    <p>
                        <span class="meaning-highlight">Vị</span>
                        trong tiếng Việt mang nghĩa hương vị, vị giác — gợi lên sự tinh tế của ẩm thực,
                        nơi từng món ăn được chăm chút đến từng chi tiết nhỏ nhất.
                    </p>
                    <p>
                        <span class="meaning-highlight">An</span>
                        là bình an, yên ổn — như cảm giác ấm lòng khi được ngồi lại cùng nhau
                        bên mâm cơm gia đình sau một ngày dài bận rộn.
                    </p>
                    <p>
                        <strong>Vị An</strong> — là nơi hội tụ của hương vị và sự bình yên. Chúng tôi mong muốn
                        mỗi thực khách khi bước vào đây đều cảm nhận được sự thư thái, thoải mái như đang
                        thưởng thức bữa cơm tại chính ngôi nhà của mình.
                    </p>
                    <p>Chúc Quý khách ngon miệng và mong rằng <strong>Vị An</strong> sẽ là điểm đến quen thuộc của bạn. Xin cảm ơn!</p>
                </div>
            </section>

            <!-- ══ 3. VIDEO GIỚI THIỆU ══ -->
            <section class="section">
                <h2 class="section-title">Video giới thiệu</h2>
                <span class="section-underline"></span>

                <div class="video-wrap">
                    <div class="video-container">
                        <iframe src="https://www.youtube.com/embed/egs99hEAzHE?si=N6VeHButOHeE-tCc"
                                title="Vị An Restaurant"
                                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                                allowfullscreen>
                        </iframe>
                    </div>
                </div>
            </section>

            <!-- ══ 4. ẢNH NHÀ HÀNG ══ -->
            <section class="section gallery-bg">
                <h2 class="section-title">Không gian nhà hàng</h2>
                <span class="section-underline"></span>

                <div class="gallery-grid">
                    <div class="gallery-item" onclick="openLightbox(this)">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/12/khong-gian-quan-1-1.png" alt="Không gian Vị An 1">
                    </div>
                    <div class="gallery-item" onclick="openLightbox(this)">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/12/khong-gian-quan-2.png" alt="Không gian Vị An 2">
                    </div>
                    <div class="gallery-item" onclick="openLightbox(this)">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/12/khong-gian-quan-3.png" alt="Không gian Vị An 3">
                    </div>
                    <div class="gallery-item" onclick="openLightbox(this)">
                        <img src="https://nhahangvian.com/wp-content/uploads/2023/04/HAN00052.jpg" alt="Không gian Vị An 4">
                    </div>
                    <div class="gallery-item" onclick="openLightbox(this)">
                        <img src="https://nhahangvian.com/wp-content/uploads/2025/08/Dam-da-huong-vi-viet-025-scaled.jpg" alt="Không gian Vị An 5">
                    </div>
                </div>
            </section>
        </main>

        <!-- Lightbox -->
        <div class="lightbox" id="lightbox" onclick="closeLightbox()">
            <span class="lightbox-close" onclick="closeLightbox()">&times;</span>
            <img id="lightbox-img" src="" alt="">
        </div>

        <%@ include file="/views/includes/footer.jsp" %>

        <script>
            function openLightbox(el) {
                document.getElementById('lightbox-img').src = el.querySelector('img').src;
                document.getElementById('lightbox').classList.add('open');
                document.body.style.overflow = 'hidden';
            }
            function closeLightbox() {
                document.getElementById('lightbox').classList.remove('open');
                document.body.style.overflow = '';
            }
            document.addEventListener('keydown', function (e) {
                if (e.key === 'Escape')
                    closeLightbox();
            });
        </script>

    </body>
</html>
