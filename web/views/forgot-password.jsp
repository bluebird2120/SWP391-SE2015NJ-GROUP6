<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Quên mật khẩu – Vị An Restaurant</title>

        <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;600;700&family=Nunito:wght@300;400;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>

        <style>
            body {
                margin: 0;
                font-family: 'Nunito', sans-serif;
                background: #fdf6f0;
                display: flex;
                flex-direction: column;
                min-height: 100vh;
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

            .right {
                flex: 1.2;
                background: white;
                padding: 50px;
            }

            .right h2 {
                margin-bottom: 5px;
                color: #3d2318;
            }

            .right > p {
                color: #9a7060;
                font-size: 14px;
                margin-bottom: 4px;
            }

            .field {
                margin-top: 18px;
            }

            .field input {
                width: 100%;
                padding: 12px;
                border: 1px solid #ddd;
                border-radius: 10px;
                font-family: 'Nunito', sans-serif;
                font-size: 14px;
                outline: none;
                box-sizing: border-box;
                transition: border-color 0.2s;
            }

            .field input:focus {
                border-color: #76493b;
            }

            .field input.input-error {
                border-color: #e74c3c;
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
                font-family: 'Nunito', sans-serif;
                transition: background 0.2s;
            }

            .btn:hover {
                background: #5a3329;
            }

            .error {
                color: red;
                font-size: 13px;
                margin-top: 5px;
                min-height: 16px;
            }

            .success-box {
                background: #eafaf1;
                border: 1px solid #b7e4c7;
                border-radius: 8px;
                padding: 12px 14px;
                color: #1e6b3a;
                font-size: 14px;
                margin-top: 16px;
                display: flex;
                align-items: flex-start;
                gap: 8px;
            }

            .error-box {
                background: #fcebeb;
                border: 1px solid #f5c1c1;
                border-radius: 8px;
                padding: 10px 14px;
                color: #791f1f;
                font-size: 13px;
                margin-top: 14px;
                display: flex;
                align-items: center;
                gap: 8px;
            }

            .back-link {
                margin-top: 18px;
                text-align: center;
                font-size: 14px;
            }

            .back-link a {
                color: #76493b;
                font-weight: 700;
                text-decoration: none;
            }
        </style>
    </head>

    <body>
        <%@include file="/views/includes/header.jsp" %>

        <div class="page-body">
            <div class="login-box">

                <!-- LEFT -->
                <div class="left">
                    <a href="${pageContext.request.contextPath}/" class="logo-left">
                        <img src="${pageContext.request.contextPath}/images/logo.png" alt="Logo">
                    </a>
                    <h2 class="welcome-text">Khôi phục mật khẩu</h2>
                    <h3 class="brand-text">Vị An Restaurant</h3>
                </div>

                <!-- RIGHT -->
                <div class="right">

                    <h2>Quên mật khẩu</h2>
                    <p>Nhập email đã đăng ký, hệ thống sẽ gửi mật khẩu mới về email của bạn.</p>

                    <c:if test="${not empty generalError}">
                        <div class="error-box">
                            <i class="fas fa-circle-exclamation"></i> ${generalError}
                        </div>
                    </c:if>

                    <c:if test="${not empty successMessage}">
                        <div class="success-box">
                            <i class="fas fa-circle-check"></i> ${successMessage}
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/forgot-password" method="post">

                        <div class="field">
                            <input type="email"
                                   id="email"
                                   name="email"
                                   placeholder="example@gmail.com"
                                   value="${email}"
                                   class="${not empty emailError ? 'input-error' : ''}"
                                   required>
                            <div id="emailError" class="error">${emailError}</div>
                        </div>

                        <button class="btn" type="submit">
                            Gửi mật khẩu mới
                        </button>

                    </form>

                    <div class="back-link">
                        <a href="${pageContext.request.contextPath}/login">
                            <i class="fas fa-arrow-left"></i> Quay lại đăng nhập
                        </a>
                    </div>

                </div>
            </div>
        </div>

        <%@include file="/views/includes/footer.jsp" %>
    </body>
</html>
