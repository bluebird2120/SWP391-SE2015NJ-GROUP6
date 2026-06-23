<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>403 - Truy cập bị từ chối</title>

        <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@600;700&family=Nunito:wght@400;600;700&display=swap" rel="stylesheet">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>

        <style>
            body {
                margin: 0;
                padding: 0;
                font-family: 'Nunito', sans-serif;
                background: #fdf6f0;
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
            }

            .container {
                text-align: center;
                background: white;
                padding: 50px 45px;
                border-radius: 18px;
                box-shadow: 0 20px 60px rgba(0,0,0,0.12);
                max-width: 420px;
            }

            .icon {
                font-size: 48px;
                color: #76493b;
                margin-bottom: 10px;
            }

            h1 {
                font-family: 'Playfair Display', serif;
                font-size: 60px;
                color: #76493b;
                margin: 0 0 6px;
                font-weight: 700;
            }

            h2 {
                font-family: 'Playfair Display', serif;
                font-size: 22px;
                color: #3d2318;
                margin: 0 0 14px;
                font-weight: 600;
            }

            p {
                color: #9a7060;
                font-size: 15px;
                margin-bottom: 28px;
            }

            .btn {
                display: inline-block;
                padding: 12px 28px;
                background: #76493b;
                color: white;
                text-decoration: none;
                border-radius: 10px;
                font-weight: 700;
                font-size: 14px;
                transition: background 0.2s;
            }

            .btn:hover {
                background: #5a3329;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="icon"><i class="fas fa-lock"></i></div>
            <h1>403</h1>
            <h2>Truy cập bị từ chối</h2>
            <p>Bạn không có quyền truy cập chức năng này.</p>
            <a class="btn" href="${pageContext.request.contextPath}/home">
                <i class="fas fa-arrow-left"></i> Quay về trang chủ
            </a>
        </div>
    </body>
</html>
