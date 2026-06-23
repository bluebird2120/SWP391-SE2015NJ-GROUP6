<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chờ nhân viên mở bàn - Vị An</title>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>
    <style>
        body { font-family: 'Nunito', sans-serif; background-color: #fdf6f0; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .waiting-card { background: white; padding: 40px; border-radius: 20px; box-shadow: 0 10px 25px rgba(118, 73, 59, 0.1); text-align: center; max-width: 400px; width: 90%; border-top: 6px solid #76493b; }
        h2 { color: #76493b; margin: 15px 0 10px 0; font-size: 24px; }
        p { color: #6b7280; font-size: 16px; line-height: 1.5; margin-bottom: 0; }
        .pulse-icon { font-size: 50px; color: #10b981; animation: pulse 2s infinite; }
        @keyframes pulse { 0% { transform: scale(0.95); opacity: 0.8; } 50% { transform: scale(1.1); opacity: 1; } 100% { transform: scale(0.95); opacity: 0.8; } }
    </style>
</head>
<body>
    <div class="waiting-card">
        <i class="fas fa-concierge-bell pulse-icon"></i>
        <h2>Đang chờ mở bàn...</h2>
        <p>Vui lòng chờ giây lát, nhân viên của chúng tôi đang tiến hành xác nhận mở bàn cho bạn.</p>
    </div>

    <script>
        const contextPath = "${pageContext.request.contextPath}";

        // Cứ 3 giây hỏi Server xem Nhân viên đã bấm duyệt chưa
        const checkInterval = setInterval(checkStaffApproval, 3000);

        function checkStaffApproval() {
            fetch(contextPath + '/api/table-join?action=checkStaffApproval')
            .then(response => response.json())
            .then(data => {
                if (data.status === 'approved') {
                    clearInterval(checkInterval);
                    // Nhân viên đã duyệt -> Load lại trang hiện tại (lúc này Controller sẽ cho thẳng vào Menu)
                    window.location.reload(); 
                }
            })
            .catch(err => console.log(err));
        }
    </script>
</body>
</html>