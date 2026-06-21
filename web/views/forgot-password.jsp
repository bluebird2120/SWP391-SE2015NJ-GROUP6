<%-- 
    Document   : forgot-password
    Created on : Jun 20, 2026, 11:28:43 PM
    Author     : admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="${pageContext.request.contextPath}/forgot-password" method="post">
            <div>
                <label>Email</label>
                <input type="email"
                       name="email"
                       value="${email}">
            </div>

            <div style="color:red">
                ${emailError}
            </div>

            <div style="color:green">
                ${successMessage}
            </div>

            <button type="submit">
                Gửi mật khẩu tạm
            </button>
        </form>
        <div style="text-align:right; margin-top:8px;">
            <a href="${pageContext.request.contextPath}/change-password">
                Đổi mật khẩu?
            </a>
        </div>
    </body>
</html>
