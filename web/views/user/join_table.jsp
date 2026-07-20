<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xin vào bàn - Vị An Restaurant</title>
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css"/>
    <style>
        body { font-family: 'Nunito', sans-serif; background-color: #fdf6f0; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .join-card { background: white; padding: 40px; border-radius: 20px; box-shadow: 0 10px 25px rgba(118, 73, 59, 0.1); text-align: center; max-width: 400px; width: 90%; }
        h2 { color: #76493b; margin-top: 0; }
        p { color: #4b5563; margin-bottom: 25px; }
        input[type="text"] { width: 100%; padding: 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 16px; box-sizing: border-box; margin-bottom: 20px; outline: none; }
        input[type="text"]:focus { border-color: #76493b; }
        button { background-color: #76493b; color: white; border: none; padding: 12px 24px; border-radius: 8px; font-size: 16px; font-weight: bold; cursor: pointer; width: 100%; transition: background 0.2s; }
        button:hover { background-color: #5a3329; }
        .spinner { border: 4px solid #f3f3f3; border-top: 4px solid #76493b; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 20px auto; }
        @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
        #waitingScreen { display: none; }
        .error-msg { color: #dc2626; margin-top: 15px; font-weight: bold; display: none; }
    </style>
</head>
<body>
    <div class="join-card">
        <div id="inputScreen">
            <i class="fas fa-users" style="font-size: 40px; color: #76493b; margin-bottom: 15px;"></i>
            <h2>Bàn này đã có người ngồi!</h2>
            <p>Vui lòng nhập tên của bạn để chủ bàn nhận diện và cho phép bạn cùng gọi món.</p>
            <input type="text" id="guestName" placeholder="Ví dụ: Hoàng Anh..." required>
            
            <button onclick="requestJoin()">Gửi yêu cầu vào bàn</button>
            
            <%-- Nút mới dành cho Chủ bàn bị mất kết nối --%>
            <button onclick="requestReclaim()" style="background-color: #f59e0b; margin-top: 15px; box-shadow: 0 4px 6px rgba(245, 158, 11, 0.2);">
                <i class="fas fa-crown"></i> Tôi là Chủ bàn (Xin khôi phục quyền)
            </button>
            
            <div id="errorTxt" class="error-msg">Vui lòng nhập tên!</div>
        </div>

        <div id="waitingScreen">
            <div class="spinner"></div>
            <h2 id="waitingTitle">Đang chờ phê duyệt...</h2>
            <p id="waitingDesc">Hãy bảo chủ bàn kiểm tra điện thoại và bấm "Cho phép" nhé!</p>
            <div id="rejectMsg" class="error-msg">Chủ bàn đã từ chối yêu cầu của bạn!</div>
        </div>
    </div>

    <script>
        const contextPath = "${pageContext.request.contextPath}";
        let checkInterval;

        // Hành động 1: GUEST xin vào bàn
        function requestJoin() {
            const name = document.getElementById('guestName').value.trim();
            if(!name) {
                document.getElementById('errorTxt').style.display = 'block';
                return;
            }

            // Gửi dữ liệu xin vào bàn lên Server bằng AJAX
            fetch(contextPath + '/api/table-join', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'action=requestJoin&guestName=' + encodeURIComponent(name)
            })
            .then(response => response.text())
            .then(res => {
                if(res === 'success') {
                    // Chuyển sang giao diện chờ duyệt của Guest
                    document.getElementById('inputScreen').style.display = 'none';
                    document.getElementById('waitingScreen').style.display = 'block';
                    
                    // Cứ 3 giây hỏi Server 1 lần xem Host đã duyệt chưa
                    checkInterval = setInterval(checkStatus, 3000);
                }
            });
        }

        // Hành động 2: HOST xin khôi phục quyền (Mất Cookie)
        function requestReclaim() {
            // Đổi giao diện sang màn hình chờ Nhân viên
            document.getElementById('inputScreen').style.display = 'none';
            document.getElementById('waitingScreen').style.display = 'block';
            document.getElementById('waitingTitle').innerText = "Đang báo cho Nhân viên...";
            document.getElementById('waitingDesc').innerText = "Vui lòng đợi nhân viên nhà hàng kiểm tra và khôi phục quyền Chủ bàn cho bạn.";

            // Bắn API xin cấp lại quyền
            fetch(contextPath + '/api/table-join', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'action=requestReclaimHost'
            })
            .then(response => response.text())
            .then(res => {
                if(res === 'success') {
                    // Chờ nhân viên duyệt
                    checkInterval = setInterval(checkStatus, 3000);
                } else {
                    alert("Có lỗi xảy ra khi gửi yêu cầu!");
                    window.location.reload();
                }
            });
        }

        // Vòng lặp kiểm tra kết quả duyệt (Dùng chung cho cả Guest và Host)
        function checkStatus() {
            fetch(contextPath + '/api/table-join?action=checkStatus')
            .then(response => response.json())
            .then(data => {
                // Đã xử lý gộp chung logic cho cả Guest (approved) và Host khôi phục (approved_reclaim)
                if(data.status === 'approved' || data.status === 'approved_reclaim') {
                    clearInterval(checkInterval); // Ngừng hỏi
                    // Thành công -> Tự động chuyển hướng nhảy vào trang Menu chọn món
                    window.location.href = contextPath + '/menu';
                } else if(data.status === 'rejected') {
                    clearInterval(checkInterval);
                    document.querySelector('.spinner').style.display = 'none';
                    document.getElementById('rejectMsg').style.display = 'block';
                }
            });
        }
    </script>
</body>
</html>