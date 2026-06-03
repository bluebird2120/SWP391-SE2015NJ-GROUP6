<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Đăng nhập – Lách Tách Restaurant</title>

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
                font-family: 'Nunito', sans-serif !important;
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
                width: 200px;
                margin-bottom: 20px;
            }

            .left h2 {
                font-family: 'Playfair Display', serif;
                margin: 0;
            }

            .right {
                flex: 1.2;
                background: white;
                padding: 50px;
            }

            .right h2 {
                margin-bottom: 5px;
            }

            .field {
                margin-top: 18px;
            }

            .field input {
                width: 100%;
                padding: 12px;
                border: 1px solid #ddd;
                border-radius: 10px;
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
        <%@include file="/includes/header.jsp" %>

        <!-- BODY -->
        <div class="page-body">

            <div class="login-box">

                <!-- LEFT -->
                <div class="left">
                    <img class="logo-left" src="${pageContext.request.contextPath}/images/logo.png">

                    <h2 class="welcome-text">Chào mừng đến với</h2>
                    <h3 class="brand-text">Lách Tách Restaurant</h3>
                </div>

                <!-- RIGHT -->
                <div class="right">

                    <h2>Đăng nhập</h2>
                    <p>Nhập số điện thoại và mật khẩu</p>

                    <form action="${pageContext.request.contextPath}/login" method="post">

                        <!-- SĐT -->
                        <div class="field">
                            <input type="text" name="identifier" placeholder="Số điện thoại">
                            <div class="error">${phoneError}</div>
                        </div>

                        <!-- Password -->
                        <div class="field">
                            <input type="password" name="password" placeholder="Mật khẩu">
                            <div class="error">${passwordError}</div>
                        </div>

                        <button class="btn" type="submit">
                            Đăng nhập
                        </button>

                    </form>

                    <div class="register">
                        Chưa có tài khoản?
                        <a href="${pageContext.request.contextPath}/register">Đăng ký</a>
                    </div>

                </div>

            </div>

        </div>
                  <%@include file="/includes/footer.jsp" %>  
    </body>
</html>