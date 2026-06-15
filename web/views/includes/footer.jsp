<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>

<c:if test="${param.layout == 'dashboard'}">
    </main>
    </div>
</c:if>
    
<style>
    .footer {
        background: #76493b;
        color: #d7bfa4;
        padding: 60px 5% 20px;
    }
    .footer-container {
        display: flex;
        justify-content: space-between;
        flex-wrap: wrap;
        gap: 50px;
    }
    .footer-section {
        flex: 1;
        min-width: 200px;
    }
    .footer-desc {
        line-height: 1.8;
        color: #d7bfa4;
        margin-bottom: 25px;
    }
    .footer-title {
        font-size: 18px;
        font-weight: 600;
        margin-bottom: 20px;
        color: #e8cfae;
    }
    .footer-links {
        list-style: none;
        padding: 0;
    }
    .footer-links li {
        margin-bottom: 10px;
    }
    .footer-links a {
        text-decoration: none;
        color: #d7bfa4;
        transition: 0.3s;
    }
    .footer-links a:hover {
        color: #fff;
        padding-left: 5px;
    }
    .social-icons {
        display: flex;
        gap: 12px;
    }
    .social-icons a {
        width: 40px;
        height: 40px;
        background: rgba(215,191,164,0.15);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        text-decoration: none;
        color: #d7bfa4;
        transition: 0.3s;
    }
    .social-icons a:hover {
        background: #d7bfa4;
        color: #76493b;
    }
    .contact-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        margin-bottom: 12px;
    }
    .contact-item i {
        margin-top: 3px;
        color: #e8cfae;
    }
    .footer-bottom {
        border-top: 1px solid rgba(215,191,164,0.3);
        margin-top: 40px;
        padding-top: 20px;
        text-align: center;
        color: #d7bfa4;
    }
    .logoFooter img {
        height: 70px;
        width: auto;
        object-fit: contain;
        margin-bottom: 16px;
    }
</style>

<footer class="footer">
    <div class="footer-container">

        <div class="footer-section">
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

        <div class="footer-section">
            <h3 class="footer-title">Hỗ trợ</h3>
            <ul class="footer-links">
                <li><a href="#">Chính sách bảo mật</a></li>
                <li><a href="#">Điều khoản sử dụng</a></li>
                <li><a href="#">Câu hỏi thường gặp</a></li>
            </ul>
        </div>

        <div class="footer-section">
            <h3 class="footer-title">Liên hệ</h3>
            <div class="contact-item"><i class="fas fa-location-dot"></i><span>Đại học FPT, Hòa Lạc, Hà Nội</span></div>
            <div class="contact-item"><i class="fas fa-phone"></i><span>0123 456 789</span></div>
            <div class="contact-item"><i class="fas fa-envelope"></i><span>vianrestaurant@gmail.com</span></div>
        </div>

    </div>
    <div class="footer-bottom">© 2026 Vị An Restaurant. All rights reserved.</div>
</footer>
