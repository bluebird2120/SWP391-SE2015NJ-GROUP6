<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng ký – Vị An Restaurant</title>

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

            /* ── REGISTER BOX — bám sát login nhưng rộng hơn chút vì nhiều field ── */
            .register-box {
                width: min(900px, 100%);
                display: flex;
                border-radius: 18px;
                overflow: hidden;
                box-shadow: 0 20px 60px rgba(0,0,0,0.12);
            }

            /* ── LEFT — giống hệt login ── */
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
            .left h2  {
                font-family: 'Playfair Display', serif;
                margin: 0;
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

            /* ── RIGHT ── */
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

            /* ── FIELDS — giữ đúng format login ── */
            .field {
                margin-top: 16px;
            }
            .field input {
                width: 100%;
                padding: 12px;
                border: 1px solid #ddd;
                border-radius: 10px;
                font-family: 'Nunito', sans-serif;
                font-size: 14px;
                outline: none;
                transition: border-color 0.2s;
                box-sizing: border-box;
            }
            .field input:focus {
                border-color: #76493b;
            }
            .field input.input-error {
                border-color: #e74c3c;
            }

            .error {
                color: red;
                font-size: 13px;
                margin-top: 5px;
                min-height: 16px;
            }

            /* ── Lỗi tổng (registerError) ── */
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

            /* ── 2 cột cho userName + phoneNumber ── */
            .field-row {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 0 16px;
            }

            /* ── BUTTON — giống login ── */
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

            /* ── DIVIDER — giống login ── */
            .divider {
                display: flex;
                align-items: center;
                margin: 20px 0;
            }
            .divider-line {
                flex: 1;
                height: 1px;
                background: #ddd;
            }
            .divider span {
                padding: 0 12px;
                color: #999;
                font-size: 13px;
            }

            /* ── GOOGLE — giống login ── */
            .btn-google {
                width: 100%;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 10px;
                padding: 12px;
                border: 1px solid #ddd;
                border-radius: 10px;
                text-decoration: none;
                color: #333;
                font-weight: 600;
                font-size: 14px;
                box-sizing: border-box;
                transition: border-color 0.2s, background 0.2s;
            }
            .btn-google:hover {
                border-color: #76493b;
                background: #fdf6f0;
            }

            /* ── LINK VỀ LOGIN ── */
            .login-link {
                margin-top: 15px;
                text-align: center;
                font-size: 14px;
            }
            .login-link a {
                color: #76493b;
                font-weight: 700;
                text-decoration: none;
            }
            .login-link a:hover {
                text-decoration: underline;
            }

            /* ── RESPONSIVE ── */
            @media (max-width: 700px) {
                .register-box   {
                    flex-direction: column;
                }
                .left           {
                    padding: 28px 20px;
                }
                .left img       {
                    width: 110px;
                }
                .welcome-text   {
                    font-size: 20px;
                }
                .brand-text     {
                    font-size: 16px;
                }
                .right          {
                    padding: 28px 20px;
                }
                .field-row      {
                    grid-template-columns: 1fr;
                }
            }

            .field-label{
                display:block;
                margin-bottom:8px;
                font-size:14px;
                font-weight:700;
                color:#3d2318;
            }

            .field-label span{
                color:#c0392b;
            }

            .input-icon{
                position:relative;
            }

            .input-icon i{
                position:absolute;
                left:15px;
                top:50%;
                transform:translateY(-50%);
                color:#9a7060;
            }

            .input-icon input{
                padding-left:45px;
            }
        </style>
    </head>
    <body>

        <%@include file="/views/includes/header.jsp" %>

        <div class="page-body">
            <div class="register-box">

                <!-- LEFT — giống login -->
                <div class="left">
                    <a href="${pageContext.request.contextPath}/" class="logo-left">
                        <img src="${pageContext.request.contextPath}/images/logo.png" alt="Logo">
                    </a>
                    <h2 class="welcome-text">Chào mừng đến với</h2>
                    <h3 class="brand-text">Vị An Restaurant</h3>
                </div>

                <!-- RIGHT -->
                <div class="right">
                    <h2>Đăng ký tài khoản</h2>
                    <p>Điền thông tin bên dưới để tạo tài khoản</p>

                    <!--Lỗi Tổng-->
                    <c:if test="${not empty registerError}">
                        <div class="error-box">
                            <i class="fas fa-circle-exclamation"></i>${registerError}
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/register" method="post">

                        <div class="field-row">

                            <!--Tên đăng nhập-->
                            <div class="field">
                                <label class="field-label">
                                    TÊN ĐĂNG NHẬP <span>*</span>
                                </label>

                                <div class="input-icon">
                                    <i class="fas fa-user"></i>

                                    <input type="text"
                                           id="userName"
                                           name="userName"
                                           maxlength="30"
                                           value="${userName}"
                                           class="${not empty userNameError ? 'input-error' : ''}"
                                           required>
                                </div>

                                <div id="userNameError" class="error">${userNameError}</div>
                            </div>

                            <!--SĐT-->
                            <div class="field">
                                <label class="field-label">
                                    SỐ ĐIỆN THOẠI <span>*</span>
                                </label>
                                <div class="input-icon">
                                    <i class="fas fa-phone"></i>

                                    <input type="tel"
                                           id="phoneNumber"
                                           name="phoneNumber"
                                           placeholder="0912345678"
                                           value="${phoneNumber}"
                                           class="${not empty phoneNumberError ? 'input-error' : ''}"
                                           required>
                                </div>

                                <div id="phoneNumberError" class="error">${phoneNumberError}</div>
                            </div>

                        </div>

                        <!--Email-->
                        <div class="field">
                            <label class="field-label">
                                EMAIL <span>*</span>
                            </label>

                            <div class="input-icon">
                                <i class="fas fa-envelope"></i>

                                <input type="email"
                                       id="email"
                                       name="email"
                                       placeholder="example@gmail.com"
                                       value="${email}"
                                       class="${not empty emailError ? 'input-error' : ''}"
                                       required>
                            </div>

                            <div id="emailError" class="error">${emailError}</div>
                        </div>

                        <div class="field-row">

                            <!--Mật Khẩu-->
                            <div class="field">
                                <label class="field-label">
                                    MẬT KHẨU <span>*</span>
                                </label>

                                <div class="input-icon">
                                    <i class="fas fa-lock"></i>

                                    <input type="password"
                                           id="password"
                                           name="password"
                                           placeholder="Ít nhất 6 ký tự"
                                           class="${not empty passwordError ? 'input-error' : ''}"
                                           required>
                                </div>

                                <div id="passwordError" class="error">${passwordError}</div>
                            </div>

                            <!--Xác Nhận Mật Khẩu-->
                            <div class="field">
                                <label class="field-label">
                                    XÁC NHẬN MẬT KHẨU <span>*</span>
                                </label>

                                <div class="input-icon">
                                    <i class="fas fa-lock"></i>

                                    <input type="password"
                                           id="confirmPassword"
                                           name="confirmPassword"
                                           placeholder="Nhập lại mật khẩu"
                                           class="${not empty confirmError ? 'input-error' : ''}"
                                           required>
                                </div>

                                <div id="confirmError" class="error">${confirmError}</div>
                            </div>

                        </div>

                        <button class="btn" type="submit">
                            Đăng ký
                        </button>

                    </form>

                    <%-- Divider --%>
                    <div class="divider">
                        <div class="divider-line"></div>
                        <span>hoặc</span>
                        <div class="divider-line"></div>
                    </div>

                    <%-- Google --%>
                    <a href="${pageContext.request.contextPath}/login/google" class="btn-google">
                        <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg"
                             width="18" height="18" alt="Google">
                        Tiếp tục với Google
                    </a>

                    <div class="login-link">
                        Đã có tài khoản?
                        <a href="${pageContext.request.contextPath}/login">Đăng nhập ngay</a>
                    </div>

                </div>
            </div>
        </div>

        <%@include file="/views/includes/footer.jsp" %>
        <script>
            document.querySelector("form").addEventListener("submit", function (e) {

                let hasError = false;

                const userName = document.getElementById("userName");
                const phone = document.getElementById("phoneNumber");
                const email = document.getElementById("email");
                const password = document.getElementById("password");
                const confirmPassword = document.getElementById("confirmPassword");

                // xóa các lỗi cũ
                document.getElementById("userNameError").textContent = "";
                document.getElementById("phoneNumberError").textContent = "";
                document.getElementById("emailError").textContent = "";
                document.getElementById("passwordError").textContent = "";
                document.getElementById("confirmError").textContent = "";

                // USERNAME
                if (userName.value.trim() === "") {
                    document.getElementById("userNameError").textContent =
                            "Vui lòng nhập tên của bạn.";
                    hasError = true;
                } else if (userName.value.trim().length > 30) {
                    document.getElementById("userNameError").textContent =
                            "Tên trong khoảng từ 1-30 kí tự.";
                    hasError = true;
                }

                // PHONE
                if (phone.value.trim() === "") {
                    document.getElementById("phoneNumberError").textContent =
                            "Vui lòng nhập số điện thoại";
                    hasError = true;
                } else if (!/^\d{10,11}$/.test(phone.value.trim())) {
                    document.getElementById("phoneNumberError").textContent =
                            "Số điện thoại phải có đúng 10-11 chữ số.";
                    hasError = true;
                }

                // EMAIL
                const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,10}$/;

                if (email.value.trim() === "") {
                    document.getElementById("emailError").textContent =
                            "Vui lòng nhập email của bạn.";
                    hasError = true;
                } else if (!emailRegex.test(email.value.trim())) {
                    document.getElementById("emailError").textContent =
                            "Email không hợp lệ.";
                    hasError = true;
                }

                // PASSWORD
                if (password.value === "") {
                    document.getElementById("passwordError").textContent =
                            "Vui lòng nhập mật khẩu.";
                    hasError = true;
                } else if (password.value.length < 6) {
                    document.getElementById("passwordError").textContent =
                            "Mật khẩu phải có ít nhất 6 ký tự.";
                    hasError = true;
                }

                // CONFIRM PASSWORD
                if (confirmPassword.value === "") {
                    document.getElementById("confirmError").textContent =
                            "Vui lòng xác nhận mật khẩu.";
                    hasError = true;
                } else if (confirmPassword.value !== password.value) {
                    document.getElementById("confirmError").textContent =
                            "Mật khẩu không khớp!";
                    hasError = true;
                }

                if (hasError) {
                    e.preventDefault();
                }
            });
        </script>
    </body>
</html>