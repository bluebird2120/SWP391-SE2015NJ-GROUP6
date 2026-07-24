<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Đăng nhập – Vị An Restaurant</title>

        <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;600;700&family=Nunito:wght@300;400;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>

        <style>
            body {
                margin: 0;
                font-family: 'Nunito', sans-serif !important;
                background: #fdf6f0;
                display: flex;
                flex-direction: column;
                min-height: 100vh;
            }

            .navbar a {
                color: #d7bfa4;
                margin-left: 30px;
                text-decoration: none;
                font-weight: 600;
            }

            .page-body {
                flex: 1;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 40px 20px;
                box-sizing: border-box;
            }

            .login-box {
                width: 850px;
                display: flex;
                border-radius: 18px;
                overflow: hidden;
                box-shadow: 0 20px 60px rgba(0,0,0,0.12);
            }

            .left {
                flex: 1;
                background: #76493b;
                color: #f0dcc2;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                padding: 40px;
                text-align: center;
            }

            .left img {
                width: 250px;
            }

            .left h2 {
                font-family: 'Playfair Display', serif;
                margin: 0;
            }

            .right {
                flex: 1.2;
                background: white;
                padding: 50px;
                box-sizing: border-box;
            }

            .right h2 {
                margin-bottom: 5px;
            }

            .field {
                margin-top: 18px;
                position: relative;
            }

            .field input {
                width: 100%;
                padding: 12px 40px 12px 12px;
                border: 1px solid #ddd;
                border-radius: 10px;
                font-family: 'Nunito', sans-serif;
                font-size: 14px;
                outline: none;
                box-sizing: border-box;
            }

            /* CSS Icon con mắt dùng chung từ trang Đổi mật khẩu */
            /* CSS chỉnh lại vị trí con mắt chính xác giữa ô input */
.toggle-eye {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    cursor: pointer;
    color: #9a7060;
    font-size: 14px;
    z-index: 2; /* Đảm bảo con mắt nổi lên trên */
}

            .btn {
                width: 100%;
                margin-top: 20px;
                padding: 12px;
                background: #76493b;
                color: white;
                border: none;
                border-radius: 10px;
                cursor: pointer;
                font-weight: 700;
                font-size: 15px;
            }

            .btn:hover {
                background: #5a3329;
            }

            .register {
                margin-top: 15px;
                text-align: center;
                font-size: 14px;
            }

            .register a {
                color: #76493b;
                font-weight: 700;
                text-decoration: none;
            }

            .error {
                color: red;
                font-size: 13px;
                margin-top: 5px;
            }

            .welcome-text {
                font-family: 'Playfair Display', serif;
                font-size: 28px;
                margin: 0;
                font-weight: 700;
                color: #f0dcc2;
            }

            .brand-text {
                font-family: 'Playfair Display', serif;
                font-size: 22px;
                margin-top: 6px;
                font-weight: 600;
                color: #f0dcc2;
            }
        </style>
    </head>

    <body>

        <!-- BODY -->
        <div class="page-body">

            <div class="login-box">

                <!-- LEFT -->
                <div class="left">
                    <a href="${pageContext.request.contextPath}/" class="logo-left">
                        <img src="${pageContext.request.contextPath}/images/logo.png" alt="Logo">
                    </a>
                    <h2 class="welcome-text">Chào mừng đến với</h2>
                    <h3 class="brand-text">Vị An Restaurant</h3>
                </div>

                <!-- RIGHT -->
                <div class="right">

                    <h2>Đăng nhập</h2>
                    <p>Nhập số điện thoại và mật khẩu</p>

                    <!-- Thông báo lỗi Google -->
                    <c:if test="${param.error == 'google_denied'}">
                        <div class="error" style="margin-bottom:15px;">
                            Bạn đã từ chối đăng nhập bằng Google.
                        </div>
                    </c:if>

                    <c:if test="${param.error == 'state_mismatch'}">
                        <div class="error" style="margin-bottom:15px;">
                            Phiên đăng nhập Google không hợp lệ.
                        </div>
                    </c:if>

                    <c:if test="${param.error == 'no_code'}">
                        <div class="error" style="margin-bottom:15px;">
                            Không nhận được mã xác thực từ Google.
                        </div>
                    </c:if>

                    <!-- Lỗi tổng -->
                    <c:if test="${not empty loginError}">
                        <div class="error" style="margin-bottom:15px;">
                            ${loginError}
                        </div>
                    </c:if>

                    <c:if test="${not empty successMessage}">
                        <div style="color: green; font-size: 14px; margin-bottom: 15px; font-weight: 600;">
                            <i class="fas fa-circle-check"></i> ${successMessage}
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/login" method="post">
                        <!-- SĐT -->
                        <div class="field">
                            <input
                                type="tel"
                                name="identifier"
                                placeholder="Số điện thoại"
                                value="${identifier}" required>
                            <div class="error">${phoneError}</div>
                        </div>

                        <!-- Password (Đồng bộ nút mắt với Change Password) -->
                        <div class="field">
    <div style="position: relative; width: 100%;">
        <input
            id="password"
            type="password"
            name="password"
            placeholder="Mật khẩu"
            value="${prefillPassword}" required>
        <i class="fas fa-eye toggle-eye" onclick="togglePassword('password', this)"></i>
    </div>
    <div class="error">${passwordError}</div>

    <!-- Quên mật khẩu -->
    <div style="text-align:right; margin-top:8px;">
        <a href="${pageContext.request.contextPath}/forgot-password"
           style="
           color:#76493b;
           font-size:13px;
           font-weight:600;
           text-decoration:none;">
            Quên mật khẩu?
        </a>
    </div>
</div>

                        <!-- Button Login -->
                        <button class="btn" type="submit">
                            Đăng nhập
                        </button>
                    </form>

                    <!-- Divider -->
                    <div style="
                         display:flex;
                         align-items:center;
                         margin:20px 0;">
                        <div style="flex:1;height:1px;background:#ddd;"></div>
                        <span style="
                              padding:0 12px;
                              color:#999;
                              font-size:13px;">
                            hoặc
                        </span>
                        <div style="flex:1;height:1px;background:#ddd;"></div>
                    </div>

                    <!-- Login Google -->
                    <a href="${pageContext.request.contextPath}/login/google"
                       style="
                       width:100%;
                       display:flex;
                       align-items:center;
                       justify-content:center;
                       gap:10px;
                       padding:12px;
                       border:1px solid #ddd;
                       border-radius:10px;
                       text-decoration:none;
                       color:#333;
                       font-weight:600;
                       font-size:14px;
                       box-sizing:border-box;">

                        <img
                            src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg"
                            width="18"
                            height="18" alt="Google Logo">
                        <span>Tiếp tục với Google</span>
                    </a>

                    <!-- Register -->
                    <div class="register">
                        Chưa có tài khoản?
                        <a href="${pageContext.request.contextPath}/register">
                            Đăng ký ngay
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <!-- Script togglePassword giống hệt trang Đổi mật khẩu -->
        <script>
            function togglePassword(inputId, icon) {
                const input = document.getElementById(inputId);
                if (input.type === "password") {
                    input.type = "text";
                    icon.classList.replace("fa-eye", "fa-eye-slash");
                } else {
                    input.type = "password";
                    icon.classList.replace("fa-eye-slash", "fa-eye");
                }
            }
        </script>
    </body>
</html>