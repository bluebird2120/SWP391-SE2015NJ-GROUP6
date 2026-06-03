<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login - Restaurant System</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: 'Inter', sans-serif;
            background: linear-gradient(135deg, #d7bfa4 0%, #76493b 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .login-card {
            background: #fff;
            border-radius: 16px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            width: 100%;
            max-width: 460px;
            overflow: hidden;
        }
        .login-header {
            padding: 32px 32px 16px;
            text-align: center;
        }
        .login-header h1 {
            font-family: 'Playfair Display', serif;
            color: #76493b;
            font-size: 1.8rem;
            margin-bottom: 6px;
        }
        .login-header p {
            color: #a0714f;
            font-size: 0.9rem;
        }
        .tabs {
            display: flex;
            border-bottom: 1px solid #ede0d8;
        }
        .tab {
            flex: 1;
            padding: 14px;
            text-align: center;
            cursor: pointer;
            font-weight: 600;
            color: #a0714f;
            text-decoration: none;
            transition: all 0.2s;
            font-size: 0.9rem;
            background: #faf6f2;
        }
        .tab:hover { background: #f5ece4; }
        .tab.active {
            color: #76493b;
            background: #fff;
            border-bottom: 3px solid #76493b;
        }
        .login-body { padding: 28px 32px 32px; }
        .form-group { margin-bottom: 16px; }
        .form-group label {
            display: block;
            font-size: 0.85rem;
            color: #6b4c3b;
            margin-bottom: 6px;
            font-weight: 500;
        }
        .form-group input {
            width: 100%;
            padding: 11px 14px;
            border: 1px solid #d7bfa4;
            border-radius: 8px;
            font-size: 0.95rem;
            font-family: inherit;
            transition: all 0.2s;
        }
        .form-group input:focus {
            outline: none;
            border-color: #76493b;
            box-shadow: 0 0 0 3px rgba(118, 73, 59, 0.12);
        }
        .btn-login {
            width: 100%;
            padding: 12px;
            background: #76493b;
            color: #fff;
            border: none;
            border-radius: 8px;
            font-size: 0.95rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
            margin-top: 8px;
        }
        .btn-login:hover { background: #5d3a2e; }
        .alert {
            padding: 11px 14px;
            border-radius: 8px;
            margin-bottom: 16px;
            font-size: 0.88rem;
        }
        .alert-error {
            background: #fde7e9;
            color: #b00020;
            border: 1px solid #f5c2c7;
        }
        .login-footer {
            text-align: center;
            margin-top: 14px;
            font-size: 0.85rem;
            color: #a0714f;
        }
        .login-footer a {
            color: #76493b;
            font-weight: 600;
            text-decoration: none;
        }
    </style>
</head>
<body>
    <div class="login-card">
        <div class="login-header">
            <h1><i class="fas fa-utensils"></i> Restaurant System</h1>
            <p>Sign in to continue</p>
        </div>

        <div class="tabs">
            <a href="${pageContext.request.contextPath}/login?type=customer"
               class="tab ${loginType != 'employee' ? 'active' : ''}">
                <i class="fas fa-user"></i> Customer
            </a>
            <a href="${pageContext.request.contextPath}/login?type=employee"
               class="tab ${loginType == 'employee' ? 'active' : ''}">
                <i class="fas fa-user-tie"></i> Employee
            </a>
        </div>

        <div class="login-body">
            <c:if test="${not empty error}">
                <div class="alert alert-error">
                    <i class="fas fa-exclamation-circle"></i> ${error}
                </div>
            </c:if>

            <c:choose>
                <c:when test="${loginType == 'employee'}">
                    <form method="post" action="${pageContext.request.contextPath}/login">
                        <input type="hidden" name="loginType" value="employee">
                        <div class="form-group">
                            <label>Email</label>
                            <input type="email" name="email" value="${email}" placeholder="staff@restaurant.local" required>
                        </div>
                        <div class="form-group">
                            <label>Password</label>
                            <input type="password" name="password" placeholder="Enter your password" required>
                        </div>
                        <button type="submit" class="btn-login">
                            <i class="fas fa-sign-in-alt"></i> Sign in
                        </button>
                    </form>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${pageContext.request.contextPath}/login">
                        <input type="hidden" name="loginType" value="customer">
                        <div class="form-group">
                            <label>Username / Email</label>
                            <input type="text" name="identifier" value="${identifier}" placeholder="Enter username or email" required>
                        </div>
                        <div class="form-group">
                            <label>Password</label>
                            <input type="password" name="password" placeholder="Enter your password" required>
                        </div>
                        <button type="submit" class="btn-login">
                            <i class="fas fa-sign-in-alt"></i> Sign in
                        </button>
                    </form>
                    <div class="login-footer">
                        Don't have an account? <a href="${pageContext.request.contextPath}/register">Register now</a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>
