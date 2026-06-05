<%-- 
    Document   : footer
    Created on : May 30, 2026, 4:45:50 PM
    Author     : admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<link rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>
<style>
    html, body {
        margin: 0;
        overflow-x: hidden;
    }

    .footer{
        background: #76493b;
        color: #d7bfa4;
        padding: 60px 80px 20px;
    }

    .footer-container{
        display: flex;
        justify-content: space-between;
        flex-wrap: wrap;
        gap: 50px;
    }

    .footer-section{
        flex: 1;
        min-width: 220px;
    }

    .footer-logo{
        font-family: 'Playfair Display', serif;
        font-size: 32px;
        font-weight: 700;
        margin-bottom: 20px;
        color: #e8cfae;
    }

    .footer-desc{
        line-height: 1.8;
        color: #d7bfa4;
        margin-bottom: 25px;
    }

    .footer-title{
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 25px;
        color: #e8cfae;
    }

    .footer-links li{
        margin-bottom: 12px;
    }

    .footer-links a{
        text-decoration: none;
        color: #d7bfa4;
        transition: .3s;
    }

    .footer-links{
        list-style: none;
        padding: 0;
    }

    .social-icons{
        display: flex;
        gap: 15px;
    }

    .social-icons a{
        width: 45px;
        height: 45px;
        background: rgba(215,191,164,0.15);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        text-decoration: none;
        color: #d7bfa4;
        transition: .3s;
    }

    .social-icons a:hover{
        background: #d7bfa4;
        color: #76493b;
    }

    .contact-item{
        display: flex;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 15px;
    }

    .contact-item i{
        margin-top: 4px;
        color: #e8cfae;
    }

    .footer-bottom{
        border-top: 1px solid rgba(215,191,164,0.3);
        margin-top: 40px;
        padding-top: 20px;
        text-align: center;
        color: #d7bfa4;
    }

    .logoFooter img{
        height: 70px;
        width: auto;
        object-fit: contain;
        margin-bottom: 20px;
    }

    .footer-links a:hover{
        color: white;
        padding-left: 5px;
    }
</style>
<footer class="footer">

    <div class="footer-container">

        <!-- Cột 1 -->
        <div class="footer-section">
            <!-- LOGO -->
            <a href="${pageContext.request.contextPath}/" class="logoFooter">
                <img src="${pageContext.request.contextPath}/images/logo.png" alt="Logo">
            </a>

            <p class="footer-desc">
                Mang đến trải nghiệm ẩm thực tuyệt vời với những món ăn
                được chế biến từ nguyên liệu tươi ngon và chất lượng.
            </p>

            <div class="social-icons">
                <a href="https://www.facebook.com/#"><i class="fab fa-facebook-f"></i></a>
                <a href="https://www.instagram.com/"><i class="fab fa-instagram"></i></a>
                <a href="https://www.tiktok.com/"><i class="fab fa-tiktok"></i></a>
                <a href="http://youtube.com/"><i class="fab fa-youtube"></i></a>
            </div>
        </div>

        <!-- Cột 2 -->
        <div class="footer-section">
            <h3 class="footer-title">Khám phá</h3>

            <ul class="footer-links">
                <li><a href="#">Giới thiệu</a></li>
                <li><a href="#">Thực đơn</a></li>
                <li><a href="#">Đặt bàn</a></li>
                <li><a href="#">Album ảnh</a></li>
                <li><a href="#">Liên hệ</a></li>
            </ul>
        </div>

        <!-- Cột 3 -->
        <div class="footer-section">
            <h3 class="footer-title">Hỗ trợ</h3>

            <ul class="footer-links">
                <li><a href="#">Chính sách bảo mật</a></li>
                <li><a href="#">Điều khoản sử dụng</a></li>
                <li><a href="#">Câu hỏi thường gặp</a></li>
            </ul>
        </div>

        <!-- Cột 4 -->
        <div class="footer-section">
            <h3 class="footer-title">Liên hệ</h3>

            <div class="contact-item">
                <i class="fas fa-location-dot"></i>
                <span>Đại học FPT, Hòa Lạc, Hà Nội</span>
            </div>

            <div class="contact-item">
                <i class="fas fa-phone"></i>
                <span>0123 456 789</span>
            </div>

            <div class="contact-item">
                <i class="fas fa-envelope"></i>
                <span>lachtachrestaurant@gmail.com</span>
            </div>
        </div>

    </div>

    <div class="footer-bottom">
        © 2026 Lách Tách Restaurant. All rights reserved.
    </div>

</footer>
