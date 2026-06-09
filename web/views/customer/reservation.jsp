<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt Bàn Trọn Gói Trong Một Trang</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background: #f8f5f0; }
        .card-wrap {
            max-width: 650px; margin: 40px auto;
            background: #fff; border-radius: 16px;
            box-shadow: 0 4px 24px rgba(0,0,0,.08);
            padding: 35px 30px;
        }
        .area-btn {
            border: 2px solid #dee2e6; border-radius: 12px;
            padding: 14px; cursor: pointer; text-align: center;
            background: #fff; width: 100%; transition: all .2s;
        }
        .area-btn:hover, .area-btn.selected { border-color: #c0392b; background: #fff5f5; }
        .table-card {
            border: 2px solid #dee2e6; border-radius: 12px; padding: 15px;
            text-align: center; cursor: pointer; transition: all .2s; background: #fff;
        }
        .table-card:hover, .table-card.selected { border-color: #27ae60; background: #f1f9f5; }
        .btn-main {
            background: #c0392b; color: #fff; border: none; padding: 12px 30px;
            border-radius: 30px; font-weight: bold; width: 100%; transition: all .2s;
        }
        .btn-main:hover { background: #a83226; color: #fff; }
        .btn-check-tables {
            background: #2d3748; color: #fff; border: none; padding: 10px 20px;
            border-radius: 8px; font-weight: 600; width: 100%;
        }
        .btn-check-tables:hover { background: #1a202c; }
        .menu-placeholder {
            padding: 1.5rem; border: 2px dashed #cbd5e1;
            background-color: #f8fafc; border-radius: 12px; text-align: center;
        }
        .section-title { font-size: 1.1rem; font-weight: 700; color: #2d3748; margin-bottom: 12px; padding-bottom: 6px; border-bottom: 2px solid #edf2f7; }
    </style>
</head>
<body>

<div class="container">

    <%-- TRANG THÀNH CÔNG --%>
    <c:if test="${step == 'success'}">
        <div class="card-wrap text-center py-4">
            <div class="display-3 text-success mb-3">🎉</div>
            <h3 class="fw-bold text-success">Đặt bàn thành công!</h3>
            <p class="text-muted">Mã đơn hàng của bạn: <strong>#${order.orderID}</strong></p>
            <div class="bg-light rounded-3 p-3 text-start my-4" style="font-size: .95rem;">
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-secondary">Loại quy mô đặt giữ chỗ:</span>
                    <span class="fw-bold text-dark">Nhóm bàn ${table.capacity} chỗ (${areaType == 'public' ? 'Ngoài sảnh' : 'Trong phòng'})</span>
                </div>
                <div class="d-flex justify-content-between mb-2">
                    <span class="text-secondary">Thời gian đến:</span>
                    <span class="fw-bold text-dark"><fmt:formatDate value="${order.orderTime}" pattern="HH:mm - dd/MM/yyyy"/></span>
                </div>
                <div class="d-flex justify-content-between">
                    <span class="text-secondary">Trạng thái giữ đơn:</span>
                    <span class="badge bg-warning text-dark">Chờ xác nhận</span>
                </div>
            </div>
            <div class="d-grid gap-2">
                <a href="${pageContext.request.contextPath}/" class="btn btn-dark rounded-pill">Trở về Trang chủ</a>
                <a href="${pageContext.request.contextPath}/reservation?action=history" class="btn btn-outline-secondary rounded-pill btn-sm">Xem lịch sử đặt của tôi</a>
            </div>
        </div>
    </c:if>

    <%-- TRANG LỊCH SỬ ĐẶT BÀN --%>
    <c:if test="${step == 'history'}">
        <div class="card-wrap">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h4 class="fw-bold m-0">Lịch sử đặt bàn</h4>
                <a href="${pageContext.request.contextPath}/reservation" class="btn btn-danger btn-sm rounded-pill px-3">Đặt bàn mới</a>
            </div>
            <c:choose>
                <c:when test="${not empty orders}">
                    <c:forEach var="o" items="${orders}">
                        <div class="card mb-3 shadow-sm border-0 bg-light">
                            <div class="card-body p-3">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="fw-bold small text-secondary">Mã đơn #${o.orderID}</span>
                                    <span class="badge ${o.orderStatus == 'pending' ? 'bg-warning text-dark' : (o.orderStatus == 'reserved' ? 'bg-success' : 'bg-danger')}">${o.orderStatus}</span>
                                </div>
                                <p class="mb-1 small">📅 Thời gian: <strong><fmt:formatDate value="${o.orderTime}" pattern="HH:mm - dd/MM/yyyy"/></strong></p>
                                <c:if test="${o.orderStatus == 'pending' or o.orderStatus == 'reserved'}">
                                    <div class="text-end mt-2">
                                        <a href="${pageContext.request.contextPath}/reservation?action=cancel&orderID=${o.orderID}" class="btn btn-link btn-sm text-danger p-0 text-decoration-none" onclick="return confirm('Bạn muốn hủy đơn này?')">Huỷ đặt bàn</a>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="text-center py-5 text-muted"><p>Bạn chưa có đơn đặt bàn nào.</p></div>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <%-- FORM ĐẶT BÀN TRỌN GÓI MỘT TRANG DUY NHẤT --%>
    <c:if test="${step != 'success' && step != 'history'}">
        <div class="card-wrap">
            <h3 class="fw-bold text-center mb-1">🍽️ Đặt Bàn Nhanh</h3>
            <p class="text-muted text-center small mb-4">Hoàn tất biểu mẫu dưới đây để giữ chỗ tức thì</p>

            <c:if test="${not empty error}">
                <div class="alert alert-danger p-2 small">${error}</div>
            </c:if>

            <form id="checkForm" method="get" action="${pageContext.request.contextPath}/reservation" onsubmit="return validateStep1()">
                <input type="hidden" name="action" value="choosetable">
                <input type="hidden" name="areaType" id="areaType" value="${areaType}">

                <div class="row g-3 mb-3">
                    <div class="col-12">
                        <label class="form-label fw-semibold text-secondary small">1. Chọn ngày & giờ đến</label>
                        <input type="datetime-local" class="form-control rounded-3" name="orderTime" id="orderTime" value="${orderTime}" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label fw-semibold text-secondary small">2. Chọn khu vực</label>
                        <div class="row g-2">
                            <c:forEach var="area" items="${areaTypes}">
                                <div class="col-6">
                                    <div class="area-btn ${areaType == area ? 'selected' : ''}" onclick="selectArea(this, '${area}')">
                                        <span class="fw-bold">${area == 'public' ? '🌿 Ngoài sảnh' : '🚪 Trong phòng'}</span>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                        <div id="areaError" class="text-danger small mt-1" style="display:none">Vui lòng bấm chọn một khu vực.</div>
                    </div>
                </div>
                
                <button type="submit" class="btn-check-tables mb-4">🔍 Kiểm tra bàn trống</button>
            </form>

            <form id="mainBookingForm" method="post" action="${pageContext.request.contextPath}/reservation" onsubmit="return validateFinalForm()">
                <input type="hidden" name="orderTime" value="${orderTime}">
                <input type="hidden" name="areaType" value="${areaType}">
                <input type="hidden" name="tableID" id="finalTableID" value="${tableID}">

                <%-- HIỂN THỊ SƠ ĐỒ THEO QUY MÔ SỐ LƯỢNG --%>
                <c:if test="${step == 'choose-table'}">
                    <div class="mb-4">
                        <div class="section-title">3. Chọn quy mô sức chứa bàn ăn phù hợp</div>
                        <div class="row g-3">
                            <c:forEach var="g" items="${tableGroups}">
                                <div class="col-6 col-md-4">
                                    <c:choose>
                                        <%-- HẾT BÀN CÙNG LOẠI (Số lượng <= 0) --%>
                                        <c:when test="${g.isActive <= 0}">
                                            <div class="table-card" style="background: #e2e8f0; color: #94a3b8; border-color: #cbd5e1; cursor: not-allowed; opacity: 0.65;" onclick="alert('⚠️ Rất tiếc, loại bàn quy mô ${g.capacity} chỗ tại khu vực này đã hết sạch phòng trống! Vui lòng chọn bàn lớn hơn hoặc đổi khu vực.')">
                                                <div class="fw-bold small text-secondary">❌ Bàn ${g.capacity} chỗ</div>
                                                <span class="badge bg-danger rounded-pill fw-normal" style="font-size: .7rem;">Hết bàn</span>
                                            </div>
                                        </c:when>

                                        <%-- CÒN HÀNG TỒN KHO KHẢ DỤNG (> 0 bàn) --%>
                                        <c:otherwise>
                                            <div class="table-card ${tableID == g.tableID ? 'selected' : ''}" onclick="selectFinalTable(this, '${g.tableID}')">
                                                <div class="fw-bold small">🪑 Bàn ${g.capacity} chỗ</div>
                                                <span class="badge bg-success rounded-pill fw-normal" style="font-size: .7rem;">Còn ${g.isActive} bàn trống</span>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:forEach>
                        </div>
                        <div id="tableError" class="text-danger small mt-2" style="display:none">Vui lòng click chọn một quy mô bàn còn trống màu xanh.</div>
                    </div>
                </c:if>

                <%-- HIỂN THỊ CHỌN MÓN VÀ HOÀN TẤT ĐƠN --%>
                <c:if test="${step == 'choose-table'}">
                    <div class="mb-4">
                        <div class="section-title">4. Đặt trước thực đơn (Tùy chọn)</div>
                        <div class="menu-placeholder">
                            <span class="fs-4">🍲</span>
                            <h6 class="fw-bold text-secondary small mt-1">Hệ thống thực đơn chuẩn bị trước</h6>
                            <p class="text-muted mb-0" style="font-size: .8rem;">Module chọn món đang được cập nhật liên kết. Quý khách có thể bấm nút chốt đơn phía dưới để hoàn tất đặt chỗ trống trực tiếp.</p>
                        </div>
                    </div>

                    <button type="submit" class="btn-main py-3">🚀 HOÀN TẤT ĐẶT BÀN NGAY</button>
                </c:if>
            </form>
        </div>
    </c:if>
</div>

<script>
    function selectArea(btn, value) {
        document.querySelectorAll('.area-btn').forEach(b => b.classList.remove('selected'));
        btn.classList.add('selected');
        document.getElementById('areaType').value = value;
        document.getElementById('areaError').style.display = 'none';
    }

    function validateStep1() {
        if (!document.getElementById('areaType').value) {
            document.getElementById('areaError').style.display = 'block';
            return false;
        }
        return true;
    }

    function selectFinalTable(card, tableID) {
        document.querySelectorAll('.table-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');
        document.getElementById('finalTableID').value = tableID;
        document.getElementById('tableError').style.display = 'none';
    }

    function validateFinalForm() {
        var tableID = document.getElementById('finalTableID').value;
        if (!tableID || tableID === "" || tableID === "-1") {
            document.getElementById('tableError').style.display = 'block';
            return false;
        }
        return true;
    }
</script>
</body>
</html>