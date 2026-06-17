<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Owner Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: 'Inter', sans-serif; background: #faf6f2; }
        .main { flex: 1; padding: 28px 36px; }
        .page-title {
            font-family: 'Playfair Display', serif;
            color: #76493b;
            font-size: 1.8rem;
            margin: 0 0 6px;
        }
        .page-sub { color: #a0714f; font-size: 0.95rem; margin-bottom: 24px; }
        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
            gap: 18px;
        }
        .card {
            background: #fff;
            border: 1px solid #ede0d8;
            border-radius: 12px;
            padding: 20px;
            text-decoration: none;
            color: inherit;
            transition: all 0.2s;
            display: block;
        }
        .card:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(118,73,59,0.12);
            border-color: #d7bfa4;
        }
        .card-icon {
            width: 44px;
            height: 44px;
            border-radius: 10px;
            background: rgba(118,73,59,0.1);
            color: #76493b;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.2rem;
            margin-bottom: 12px;
        }
        .card-title {
            font-weight: 700;
            color: #76493b;
            font-size: 1rem;
            margin-bottom: 4px;
        }
        .card-desc { color: #8a6e5a; font-size: 0.85rem; }
        .welcome {
            background: linear-gradient(135deg, #76493b, #a0714f);
            color: #fff;
            padding: 24px 28px;
            border-radius: 12px;
            margin-bottom: 24px;
        }
        .welcome h2 {
            font-family: 'Playfair Display', serif;
            margin: 0 0 6px;
            font-size: 1.4rem;
        }
        .welcome p { margin: 0; opacity: 0.9; }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>
    <div style="display: flex;">
        <%@ include file="/views/includes/dashboard.jsp" %>
        <main class="main">
            <h1 class="page-title">Owner Dashboard</h1>
            <p class="page-sub">Restaurant operations overview</p>

            <div class="welcome">
                <h2>Welcome, ${sessionScope.employee.fullName}!</h2>
                <p>Quick access to management modules from here.</p>
            </div>

            <div class="grid">
                <a class="card" href="${pageContext.request.contextPath}/owner/staff?action=list">
                    <div class="card-icon"><i class="fas fa-user-tie"></i></div>
                    <div class="card-title">Manage Staff</div>
                    <div class="card-desc">Add, edit, deactivate staff</div>
                </a>
                <a class="card" href="${pageContext.request.contextPath}/owner/shift-templates">
                    <div class="card-icon"><i class="fas fa-calendar-alt"></i></div>
                    <div class="card-title">Shift Templates</div>
                    <div class="card-desc">Quản lý ca làm việc cố định</div>
                </a>
                <a class="card" href="${pageContext.request.contextPath}/owner/shift-roster">
                    <div class="card-icon"><i class="fas fa-user-clock"></i></div>
                    <div class="card-title">Shift Roster</div>
                    <div class="card-desc">Phân ca theo ngày cho nhân viên</div>
                </a>
                <a class="card" href="${pageContext.request.contextPath}/owner/attendance">
                    <div class="card-icon"><i class="fas fa-clipboard-check"></i></div>
                    <div class="card-title">Attendance</div>
                    <div class="card-desc">Điểm danh ca làm trong ngày</div>
                </a>
                <a class="card" href="${pageContext.request.contextPath}/owner/order-history">
                    <div class="card-icon"><i class="fas fa-history"></i></div>
                    <div class="card-title">Order History</div>
                    <div class="card-desc">View completed orders</div>
                </a>
                <a class="card" href="${pageContext.request.contextPath}/manage-table">
                    <div class="card-icon"><i class="fas fa-chair"></i></div>
                    <div class="card-title">Restaurant Tables</div>
                    <div class="card-desc">Table layout and status</div>
                </a>
                <a class="card" href="${pageContext.request.contextPath}/restaurant-analytics-dashboard">
                    <div class="card-icon"><i class="fas fa-chart-line"></i></div>
                    <div class="card-title">Revenue Reports</div>
                    <div class="card-desc">Business analytics</div>
                </a>
            </div>
        </main>
    </div>
    <%@ include file="/views/includes/footer.jsp" %>
</body>
</html>
