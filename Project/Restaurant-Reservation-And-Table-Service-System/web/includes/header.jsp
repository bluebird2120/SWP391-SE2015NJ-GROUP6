<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Restaurant Header</title>

        <!-- Google Font -->
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&family=Playfair+Display:wght@400;600;700&display=swap" rel="stylesheet">

        <!-- Font Awesome -->
        <link rel="stylesheet"
              href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>

        <style>

            *{
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body{
                font-family: 'Inter', sans-serif;
            }

            /* HEADER */
            .header{
                width: 100%;
                height: 78px;
                background: #76493b;
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 0 50px;
            }

            /* LOGO */
            .logo img{
                height: 58px;
                object-fit: contain;
            }

            /* MENU */
            .navbar{
                display: flex;
                gap: 50px;
            }

            .navbar a{
                text-decoration: none;
                color: #d7bfa4;
                font-size: 18px;
                font-weight: 600;
                text-transform: uppercase;
                transition: 0.3s;
            }

            .navbar a:hover{
                color: #e8cfae;
            }

            /* RIGHT SIDE */
            .right-header{
                display: flex;
                align-items: center;
                gap: 18px;
            }

            /* SEARCH */
            .search-box{
                position: relative;
            }

            .search-box input{
                width: 260px;
                height: 42px;
                border: none;
                outline: none;
                border-radius: 6px;
                background: #d7bfa4;
                padding-left: 15px;
                padding-right: 45px;
                font-size: 15px;
            }

            .search-box i{
                position: absolute;
                right: 15px;
                top: 50%;
                transform: translateY(-50%);
                color: #76493b;
                cursor: pointer;
            }

            /* USER DROPDOWN */
            .user-menu{
                position: relative;
            }

            /* ICON */
            .user-icon{
                width: 42px;
                height: 42px;
                border-radius: 50%;
                background: #d7bfa4;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                transition: 0.3s;
            }

            .user-icon:hover{
                background: #e8cfae;
            }

            .user-icon i{
                color: #76493b;
                font-size: 20px;
            }

            /* DROPDOWN */
            .dropdown{
                position: absolute;
                top: 55px;
                right: 0;
                width: 180px;
                background: white;
                border-radius: 8px;
                overflow: hidden;
                display: none;
                box-shadow: 0 5px 15px rgba(0,0,0,0.2);
            }

            .dropdown a{
                display: block;
                padding: 14px 18px;
                text-decoration: none;
                color: #333;
                font-size: 15px;
                transition: 0.3s;
            }

            .dropdown a:hover{
                background: #f2f2f2;
                color: #76493b;
            }

            .dropdown{
                position: absolute;
                top: 55px;
                right: 0;
                width: 180px;
                background: white;
                border-radius: 8px;
                overflow: hidden;
                display: none;
                box-shadow: 0 5px 15px rgba(0,0,0,0.2);
            }

            .dropdown.show{
                display: block;
            }

            .auth-buttons{
                display: flex;
                gap: 12px;
            }

            .auth-link{
                text-decoration: none;
                padding: 8px 14px;
                border: 1px solid #d7bfa4;
                color: #d7bfa4;
                border-radius: 6px;
                font-size: 14px;
                transition: 0.3s;
            }

            .auth-link:hover{
                background: #d7bfa4;
                color: #fff;
            }
        </style>
    </head>

    <body>

        <header class="header">

            <!-- LOGO -->
            <a href="#" class="logo">
                <img src="${pageContext.request.contextPath}/images/logo.png" alt="Logo">
            </a>

            <!-- MENU -->
            <nav class="navbar">
                <a href="#">Giới thiệu</a>
                <a href="#">Thực đơn</a>
                <a href="#">Đặt bàn</a>
                <a href="#">Album ảnh</a>
                <a href="#">Liên hệ</a>
            </nav>

            <!-- RIGHT -->
            <div class="right-header">

                <!-- SEARCH -->
                <div class="search-box">
                    <input type="text" placeholder="Tìm kiếm">
                    <i class="fa-solid fa-magnifying-glass"></i>
                </div>

                <!-- CHƯA LOGIN -->
                <c:if test="${sessionScope.user == null}">
                    <div class="auth-buttons">
                        <a href="login.jsp" class="auth-link">Đăng nhập</a>
                        <a href="register.jsp" class="auth-link">Đăng ký</a>
                    </div>
                </c:if>

                <!-- ĐÃ LOGIN -->
                <c:if test="${sessionScope.user != null}">
                    <div class="user-menu">

                        <div class="user-icon" onclick="toggleMenu()">
                            <i class="fa-solid fa-user"></i>
                        </div>

                        <div class="dropdown" id="dropdownMenu">
                            <a href="#">Hồ sơ của tôi</a>
                            <a href="logout">Đăng xuất</a>
                        </div>

                    </div>
                </c:if>

            </div>

        </header>
        <script>

            function toggleMenu() {
                document.getElementById("dropdownMenu")
                        .classList.toggle("show");
            }

            // click ra ngoài sẽ tự đóng
            window.onclick = function (event) {

                if (!event.target.closest('.user-menu')) {
                    document.getElementById("dropdownMenu")
                            .classList.remove("show");
                }

            }

        </script>
    </body>
</html>