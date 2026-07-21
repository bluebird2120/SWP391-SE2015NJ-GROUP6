<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Quản lý Hóa Đơn - Vị An Restaurant</title>
        <style>
            /* Tổng thể chung */
            .vian-container {
                padding: 30px 50px;
                background-color: #FCF9F2;
                color: #4A3B32;
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            }
            .vian-title {
                color: #8B4513;
                font-family: 'Georgia', serif;
                border-bottom: 2px solid #D4A373;
                padding-bottom: 10px;
                margin-bottom: 20px;
                display: flex;
                justify-content: space-between;
                align-items: flex-end;
            }
            .total-stats {
                font-size: 14px;
                font-family: 'Segoe UI', sans-serif;
                color: #695640;
                font-weight: normal;
                background: #F0E6D2;
                padding: 5px 12px;
                border-radius: 15px;
            }
            .vian-table {
                width: 100%;
                border-collapse: collapse;
                background: #FFFFFF;
                box-shadow: 0 4px 10px rgba(0,0,0,0.05);
                border-radius: 8px;
                overflow: hidden;
            }
            .vian-table th {
                background-color: #D4A373;
                color: #FFFFFF;
                padding: 15px;
                text-transform: uppercase;
                font-size: 14px;
                letter-spacing: 0.5px;
            }
            .vian-table td {
                padding: 15px;
                border-bottom: 1px solid #F0E6D2;
                text-align: center;
                vertical-align: middle;
            }
            .vian-table tr:hover {
                background-color: #FAF4E8;
            }
            .btn-vian {
                padding: 8px 16px;
                text-decoration: none;
                border-radius: 5px;
                font-weight: 500;
                display: inline-block;
                transition: all 0.3s ease;
                border: none;
                cursor: pointer;
            }
            .btn-detail {
                background-color: #8B7355;
                color: white;
            }
            .btn-detail:hover {
                background-color: #695640;
            }
            
            /* Style trạng thái hóa đơn */
            .status-paid {
                color: #556B2F;
                font-weight: bold;
            }
            .status-unpaid {
                color: #D9534F;
                font-weight: bold;
            }

            /* Thanh bộ lọc phong cách Vị An */
            .vian-filter-bar {
                background-color: #FFFFFF;
                padding: 15px 20px;
                border-radius: 8px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.05);
                margin-bottom: 20px;
                border: 1px solid #E6DEC9;
            }
            .filter-form {
                display: flex;
                flex-wrap: wrap;
                gap: 15px;
                align-items: flex-end;
            }
            .filter-group {
                display: flex;
                flex-direction: column;
                flex: 1;
                min-width: 150px;
            }
            .filter-group label {
                font-size: 13px;
                font-weight: bold;
                color: #5C4033;
                margin-bottom: 5px;
            }
            .filter-input {
                width: 100%;
                padding: 8px 12px;
                border: 1px solid #D4A373;
                border-radius: 4px;
                background-color: #FCF9F2;
                color: #4A3B32;
                font-size: 14px;
                box-sizing: border-box;
            }
            .filter-input:focus {
                outline: none;
                border-color: #8B4513;
                background-color: #FFF;
            }
            .btn-filter {
                background-color: #8B7355;
                color: white;
                padding: 9px 20px;
                border: none;
                border-radius: 4px;
                font-weight: bold;
                cursor: pointer;
                transition: 0.3s;
            }
            .btn-filter:hover {
                background-color: #695640;
            }
            .btn-clear {
                background-color: #E6DEC9;
                color: #4A3B32;
                padding: 9px 15px;
                text-decoration: none;
                border-radius: 4px;
                font-size: 14px;
                text-align: center;
            }
            .btn-clear:hover {
                background-color: #D4C9A8;
            }
            
            /* Phân trang */
            .pagination {
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 5px;
                margin: 20px 0;
            }
            .pagination a, .pagination span {
                padding: 6px 14px;
                border: 1px solid #cbd5e1;
                border-radius: 6px;
                text-decoration: none;
                color: #334155;
                font-size: 14px;
                font-weight: 500;
                background-color: #ffffff;
                transition: all 0.2s ease;
            }
            .pagination a:hover {
                background-color: #76493b;
                border-color: #76493b;
                color: #ffffff;
            }
            .pagination .page-info {
                background-color: #f1f5f9;
                color: #1e293b;
            }
            .pagination .page-info b {
                color: #76493b;
            }
            .pagination .disabled {
                color: #94a3b8;
                background-color: #f8fafc;
                pointer-events: none;
            }
        </style>
    </head>
    <body>

        <%@ include file="/views/includes/header.jsp" %>

        <div style="display: flex; align-items: stretch; min-height: 80vh; background-color: #FCF9F2;">

            <%@ include file="/views/includes/dashboard.jsp" %>

            <div class="vian-container" style="flex: 1;">

                <h2 class="vian-title">
                    Danh sách Hóa Đơn
                    <span class="total-stats">Tìm thấy: <b>${totalRecords}</b> hóa đơn</span>
                </h2>

                <div class="vian-filter-bar">
                    <form action="${pageContext.request.contextPath}/owner/invoices" method="GET" class="filter-form">
                        
                        <div class="filter-group">
                            <label>Từ ngày:</label>
                            <input type="date" name="startDate" class="filter-input" value="${param.startDate}">
                        </div>

                        <div class="filter-group">
                            <label>Đến ngày:</label>
                            <input type="date" name="endDate" class="filter-input" value="${param.endDate}">
                        </div>

                        <div class="filter-group">
                            <label>Trạng thái thanh toán:</label>
                            <select name="status" class="filter-input">
                                <option value="all" ${param.status == 'all' || empty param.status ? 'selected' : ''}>-- Tất cả trạng thái --</option>
                                <option value="paid" ${param.status == 'paid' ? 'selected' : ''}>Đã thanh toán</option>
                                <option value="unpaid" ${param.status == 'unpaid' ? 'selected' : ''}>Chưa thanh toán</option>
                            </select>
                        </div>

                        <div style="display: flex; gap: 8px;">
                            <button type="submit" class="btn-filter">Tìm kiếm</button>
                            <a href="${pageContext.request.contextPath}/owner/invoices" class="btn-clear">Xóa bộ lọc</a>
                        </div>
                    </form>
                </div>

                <table class="vian-table">
                    <thead>
                        <tr>
                            <th>Mã Hóa Đơn</th>
                            <th>Thời gian xuất</th>
                            <th>Tạm tính</th>
                            <th>Đã cọc</th>
                            <th>Thực trả</th>
                            <th>Trạng thái</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${listInvoices}" var="inv">
                            <tr>
                                <td><strong>${inv.invoiceNumber}</strong></td>
                                <td><fmt:formatDate value="${inv.issuedDate}" pattern="HH:mm - dd/MM/yyyy"/></td>
                                <td><fmt:formatNumber value="${inv.subTotal}" pattern="#,###"/> đ</td>
                                <td style="color:#556B2F;">
                                    -<fmt:formatNumber value="${inv.depositDeducted}" pattern="#,###"/> đ
                                </td>
                                <td><strong style="color: #D9534F;"><fmt:formatNumber value="${inv.finalAmount}" pattern="#,###"/> đ</strong></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${inv.status == 'paid'}"><span class="status-paid">Đã thanh toán</span></c:when>
                                        <c:otherwise><span class="status-unpaid">Chưa thanh toán</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/admin/invoice-detail?id=${inv.invoiceID}" class="btn-vian btn-detail">Chi tiết</a>
                                </td>
                            </tr>
                        </c:forEach>
                        
                        <c:if test="${empty listInvoices}">
                            <tr>
                                <td colspan="7" style="padding: 40px; color: #8B4513;">📉 Không tìm thấy hóa đơn nào phù hợp với điều kiện lọc.</td>
                            </tr>
                        </c:if>
                    </tbody>          
                </table>
                
                <%-- Phân trang theo đúng cấu trúc CSS mẫu --%>
                <c:if test="${totalPages > 1}">
                    <div class="pagination">
                        <c:choose>
                            <c:when test="${currentPage > 1}">
                                <a href="${pageContext.request.contextPath}/admin/invoices?page=1&startDate=${param.startDate}&endDate=${param.endDate}&status=${param.status}" title="Về trang đầu">Đầu</a>
                                <a href="${pageContext.request.contextPath}/admin/invoices?page=${currentPage - 1}&startDate=${param.startDate}&endDate=${param.endDate}&status=${param.status}" title="Trang trước">Trước</a>
                            </c:when>
                            <c:otherwise>
                                <span class="disabled">Đầu</span>
                                <span class="disabled">Trước</span>
                            </c:otherwise>
                        </c:choose>

                        <span class="page-info">Trang <b>${currentPage}</b> / ${totalPages}</span>

                        <c:choose>
                            <c:when test="${currentPage < totalPages}">
                                <a href="${pageContext.request.contextPath}/admin/invoices?page=${currentPage + 1}&startDate=${param.startDate}&endDate=${param.endDate}&status=${param.status}" title="Trang sau">Sau</a>
                                <a href="${pageContext.request.contextPath}/admin/invoices?page=${totalPages}&startDate=${param.startDate}&endDate=${param.endDate}&status=${param.status}" title="Đến trang cuối">Cuối</a>
                            </c:when>
                            <c:otherwise>
                                <span class="disabled">Sau</span>
                                <span class="disabled">Cuối</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

            </div> 
        </div> 

        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>