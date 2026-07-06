<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Xác thực email – Vị An Restaurant</title>

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

            .otp-box {
                width: min(900px, 100%);
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
                padding: 44px 50px;
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
            .right .email-highlight {
                font-weight: 700;
                color: #3d2318;
            }

            .field {
                margin-top: 20px;
            }
            .field-label {
                display: block;
                margin-bottom: 8px;
                font-size: 14px;
                font-weight: 700;
                color: #3d2318;
            }

            .otp-input {
                width: 100%;
                padding: 14px;
                border: 1px solid #ddd;
                border-radius: 10px;
                font-family: 'Nunito', sans-serif;
                font-size: 22px;
                font-weight: 700;
                letter-spacing: 8px;
                text-align: center;
                outline: none;
                transition: border-color 0.2s;
                box-sizing: border-box;
            }
            .otp-input:focus {
                border-color: #76493b;
            }
            .otp-input.input-error {
                border-color: #e74c3c;
            }

            .error {
                color: red;
                font-size: 13px;
                margin-top: 5px;
                min-height: 16px;
                text-align: center;
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

            .info-box {
                background: #eafaf1;
                border: 1px solid #b9e8cd;
                border-radius: 8px;
                padding: 10px 14px;
                color: #1d6b46;
                font-size: 13px;
                margin-top: 14px;
                display: flex;
                align-items: center;
                gap: 8px;
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

            .btn-link {
                width: 100%;
                margin-top: 12px;
                padding: 12px;
                background: transparent;
                color: #76493b;
                border: 1px solid #ddd;
                border-radius: 10px;
                cursor: pointer;
                font-weight: 700;
                font-size: 14px;
                font-family: 'Nunito', sans-serif;
                transition: border-color 0.2s, background 0.2s;
            }
            .btn-link:hover {
                border-color: #76493b;
                background: #fdf6f0;
            }

            .register-link {
                margin-top: 15px;
                text-align: center;
                font-size: 14px;
            }
            .register-link a {
                color: #76493b;
                font-weight: 700;
                text-decoration: none;
            }
            .register-link a:hover {
                text-decoration: underline;
            }

            @media (max-width: 700px) {
                .otp-box {
                    flex-direction: column;
                }
                .left {
                    padding: 28px 20px;
                }
                .left img {
                    width: 110px;
                }
                .welcome-text {
                    font-size: 20px;
                }
                .brand-text {
                    font-size: 16px;
                }
                .right {
                    padding: 28px 20px;
                }
            }
        </style>
    </head>
    <body>

        <div class="page-body">
            <div class="otp-box">

                <!-- LEFT -->
                <div class="left">
                    <a href="${pageContext.request.contextPath}/" class="logo-left">
                        <img src="${pageContext.request.contextPath}/images/logo.png" alt="Logo">
                    </a>
                    <h2 class="welcome-text">Chỉ còn một bước nữa</h2>
                    <h3 class="brand-text">Vị An Restaurant</h3>
                </div>

                <!-- RIGHT -->
                <div class="right">
                    <h2>Xác thực email</h2>
                    <p>
                        Mã OTP đã được gửi tới
                        <span class="email-highlight">${pendingEmail}</span>.
                        Vui lòng kiểm tra hộp thư (và mục Spam nếu chưa thấy).
                    </p>

                    <c:if test="${not empty otpError}">
                        <div class="error-box">
                            <i class="fas fa-circle-exclamation"></i>${otpError}
                        </div>
                    </c:if>

                    <c:if test="${not empty otpInfo}">
                        <div class="info-box">
                            <i class="fas fa-circle-check"></i>${otpInfo}
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/verify-otp" method="post" id="otpForm">
                        <input type="hidden" name="action" value="verify">

                        <div class="field">
                            <label class="field-label" for="otpCode">MÃ OTP</label>
                            <input type="text"
                                   id="otpCode"
                                   name="otpCode"
                                   class="otp-input"
                                   maxlength="6"
                                   inputmode="numeric"
                                   pattern="\d{6}"
                                   placeholder="******"
                                   autocomplete="one-time-code"
                                   required>
                            <div class="error" id="otpCodeError"></div>
                        </div>

                        <button class="btn" type="submit">Xác nhận</button>
                    </form>

                    <form action="${pageContext.request.contextPath}/verify-otp" method="post">
                        <input type="hidden" name="action" value="resend">
                        <button class="btn-link" type="submit">Gửi lại mã OTP</button>
                    </form>

                    <div class="register-link">
                        Nhập sai thông tin?
                        <a href="${pageContext.request.contextPath}/register">Đăng ký lại</a>
                    </div>

                </div>
            </div>
        </div>

        <script>
            document.getElementById("otpForm").addEventListener("submit", function (e) {
                const otpInput = document.getElementById("otpCode");
                const errorEl = document.getElementById("otpCodeError");
                errorEl.textContent = "";

                if (!/^\d{6}$/.test(otpInput.value.trim())) {
                    errorEl.textContent = "Vui lòng nhập OTP";
                    e.preventDefault();
                }
            });
        </script>
    </body>
</html>
