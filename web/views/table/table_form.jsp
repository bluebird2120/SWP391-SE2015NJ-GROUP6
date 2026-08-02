<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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
        margin-bottom: 30px;
    }
    .btn-cancel {
        color: #8B4513;
        text-decoration: none;
        margin-left: 15px;
        font-weight: bold;
    }
    .btn-cancel:hover { text-decoration: underline; }

    /* Layout cho Form Nhập Liệu (Add/Edit) */
    .vian-form-wrapper {
        background: white;
        padding: 30px;
        border-radius: 8px;
        box-shadow: 0 4px 10px rgba(0,0,0,0.05);
        max-width: 600px;
    }
    .vian-form-group { margin-bottom: 20px; }
    .vian-form-group label {
        display: block; font-weight: bold; margin-bottom: 8px; color: #5C4033;
    }
    .vian-input {
        width: 100%; padding: 10px 15px; border: 1px solid #D4A373;
        border-radius: 5px; font-size: 15px; background-color: #FAF4E8; box-sizing: border-box;
    }
    .vian-input:focus { outline: none; border-color: #8B4513; background-color: #FFF; }
    .btn-submit {
        background-color: #556B2F; color: white; padding: 10px 20px;
        border: none; border-radius: 5px; font-size: 16px; cursor: pointer; transition: 0.3s;
    }
    .btn-submit:hover { background-color: #3E4E22; }

    /* Layout cho Xem Chi Tiết (Detail) */
    .detail-grid { display: flex; gap: 30px; flex-wrap: wrap; }
    .info-card, .qr-card {
        background: white; padding: 25px; border-radius: 8px;
        box-shadow: 0 4px 10px rgba(0,0,0,0.05); flex: 1; min-width: 300px;
    }
    .info-card ul { list-style: none; padding: 0; }
    .info-card li { padding: 12px 0; border-bottom: 1px dashed #E0D3C1; font-size: 16px; }
    .info-card li b { color: #5C4033; display: inline-block; width: 140px; }
    .qr-card { text-align: center; border: 2px dashed #D4A373; background-color: #FFFDF9; }
    .qr-card h3 { color: #8B4513; margin-top: 0; }
    .qr-image {
        margin: 20px 0; padding: 10px; background: white;
        border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);
    }
</style>

<%@ include file="/views/includes/header.jsp" %>

<div style="display: flex; align-items: stretch; min-height: 80vh; background-color: #FCF9F2;">

    <%@ include file="/views/includes/dashboard.jsp" %>

    <div class="vian-container" style="flex: 1;">
        <h2 class="vian-title">
            <c:choose>
                <c:when test="${mode == 'add'}">Thêm Bàn Mới</c:when>
                <c:when test="${mode == 'edit'}">Cập nhật thông tin Bàn</c:when>
                <c:when test="${mode == 'detail'}">Thông tin: ${table.tableName}</c:when>
            </c:choose>
        </h2>

        <c:choose>
            <%-- ========================================== --%>
            <%-- PHẦN 1: HIỂN THỊ CHẾ ĐỘ XEM CHI TIẾT --%>
            <%-- ========================================== --%>
            <c:when test="${mode == 'detail'}">
                <div class="detail-grid">
                    <div class="info-card">
                        <ul>
                            <li><b>Mã hệ thống (ID):</b> #${table.tableID}</li>
                            <li><b>Sức chứa:</b> ${table.capacity} người</li>
                            <li><b>Khu vực:</b> 
                                <c:choose>
                                    <c:when test="${table.areaType == 'public'}">Ngoài Sảnh</c:when>
                                    <c:when test="${table.areaType == 'private'}">Trong Phòng</c:when>
                                    <c:otherwise>${table.areaType}</c:otherwise>
                                </c:choose>
                            </li>
                            <li><b>Trạng thái:</b> 
                                <span style="color: ${table.isActive == 1 ? '#556B2F' : '#D9534F'}; font-weight: bold;">
                                    ${table.isActive == 1 ? 'Đang hoạt động' : 'Tạm ngưng'}
                                </span>
                            </li>
                            <li><b>Mã Token nội bộ:</b> <br> <span style="font-size: 13px; color: #888;">${table.QRCodeToken}</span></li>
                        </ul>
                    </div>
                    <div class="qr-card">
                        <h3>Mã QR Đặt món tự động</h3>
                        <p style="color: #666; font-size: 14px;"><i>Vui lòng in hình này và đặt tại bàn để khách hàng quét camera gọi món.</i></p>
                        <img class="qr-image" src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&color=4A3B32&data=http://localhost:8080/Restaurant-Reservation-And-Table-Service-System/scan?token=${table.QRCodeToken}" alt="QR Code Bàn ${table.tableName}">
                    </div>
                </div>
                <br>
                <a href="${pageContext.request.contextPath}/owner/manage-table" class="btn-cancel" style="margin-left: 0;">← Quay lại danh sách</a>
            </c:when>

            <%-- ========================================== --%>
            <%-- PHẦN 2: HIỂN THỊ CHẾ ĐỘ FORM NHẬP LIỆU --%>
            <%-- ========================================== --%>
            <c:otherwise>
                <div class="vian-form-wrapper">
                    
                    <c:if test="${not empty errorMessage}">
                        <div class="alert-error" style="margin-bottom: 20px; border-left: 4px solid #D9534F;">
                            ⚠ <b>Lỗi:</b> ${errorMessage}
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/owner/manage-table" method="POST">
                        <input type="hidden" name="action" value="${mode}">
                        <input type="hidden" name="tableID" value="${table.tableID}">

                        <div class="vian-form-group">
                            <label>Tên Bàn (VD: Bàn 1, Bàn VIP 2):</label>
                            <input type="text" class="vian-input" name="tableName" value="${table.tableName}" 
                                   maxlength="30" placeholder="Tối đa 30 ký tự" required>
                        </div>

                        <div class="vian-form-group">
                            <label>Sức chứa (Số người):</label>
                            <%-- Frontend chỉ cho nhập số nguyên từ 1 đến 50; backend vẫn kiểm tra lại. --%>
                            <input type="number" class="vian-input" name="capacity"
                                   value="${table.capacity > 0 ? table.capacity : 2}"
                                   min="1" max="50" step="1" required
                                   placeholder="Nhập sức chứa từ 1 đến 50">
                        </div>

                        <div class="vian-form-group">
                            <label>Khu vực:</label>
                            <select class="vian-input" name="areaType">
                                <option value="public" ${table.areaType == 'public' ? 'selected' : ''}>Ngoài Sảnh (Public)</option>
                                <option value="private" ${table.areaType == 'private' ? 'selected' : ''}>Trong Phòng (Private)</option>                          
                            </select>
                        </div>

                        <div class="vian-form-group">
                            <label>Trạng thái hoạt động:</label>
                            <select class="vian-input" name="isActive">
                                <option value="1" ${table.isActive == null || table.isActive == 1 ? 'selected' : ''}>Mở bán (Đang hoạt động)</option>
                                <option value="0" ${table.isActive != null && table.isActive == 0 ? 'selected' : ''}>Tạm ngưng / Sửa chữa</option>
                            </select>
                        </div>

                        <button type="submit" class="btn-submit">${mode == 'edit' ? 'Lưu cập nhật' : 'Tạo bàn mới'}</button>
                        <a href="${pageContext.request.contextPath}/owner/manage-table" class="btn-cancel">Hủy bỏ</a>
                    </form>
                </div>
            </c:otherwise>
        </c:choose>

    </div> </div> <%@ include file="/views/includes/footer.jsp" %>
