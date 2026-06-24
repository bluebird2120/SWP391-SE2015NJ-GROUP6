<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đánh giá của tôi</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: 'Inter', sans-serif; background: #faf6f2; color: #4a3528; }
        .review-page { max-width: 1080px; margin: 0 auto; padding: 36px 20px 60px; }
        .page-title { font-family: 'Playfair Display', serif; color: #76493b; font-size: 2rem; margin: 0 0 6px; }
        .page-sub { color: #9a745c; margin: 0 0 22px; }
        .alert { border-radius: 12px; padding: 12px 14px; margin-bottom: 16px; font-size: 0.92rem; }
        .alert-danger { background: #fff1f1; border: 1px solid #f3c1c1; color: #9f2727; }
        .alert-success { background: #effaf1; border: 1px solid #bfe7c5; color: #226c34; }
        .card { background: #fff; border: 1px solid #ede0d8; border-radius: 16px; box-shadow: 0 10px 28px rgba(118, 73, 59, 0.08); }
        .form-card { padding: 22px; margin-bottom: 22px; }
        .form-title { color: #5d3a2e; font-weight: 700; font-size: 1.1rem; margin-bottom: 14px; }
        .field { margin-bottom: 16px; }
        .field label { display: block; font-weight: 600; color: #5d3a2e; margin-bottom: 8px; }
        .stars { display: flex; gap: 10px; flex-wrap: wrap; }
        .star-option input { display: none; }
        .star-option span { display: inline-flex; align-items: center; gap: 5px; padding: 9px 12px; border: 1px solid #e4d3c4; border-radius: 999px; color: #8a6e5a; cursor: pointer; transition: all .18s ease; }
        .star-option input:checked + span { background: #fff4d8; border-color: #d49b2f; color: #9b6700; font-weight: 700; }
        textarea { width: 100%; min-height: 130px; border: 1px solid #e4d3c4; border-radius: 12px; padding: 12px 14px; font-family: inherit; resize: vertical; color: #4a3528; }
        textarea:focus { outline: none; border-color: #a0714f; box-shadow: 0 0 0 3px rgba(160, 113, 79, .14); }
        .actions { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
        .btn { border: none; border-radius: 999px; padding: 10px 16px; font-weight: 700; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 7px; font-size: 0.9rem; }
        .btn-primary { background: #76493b; color: #fff; }
        .btn-primary:hover { background: #5d3a2e; }
        .btn-muted { background: #f4ebe4; color: #76493b; }
        .btn-danger { background: #fff1f1; color: #b42323; }
        .list { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 14px; }
        .review-item { padding: 18px; }
        .review-head { display: flex; justify-content: space-between; gap: 12px; align-items: flex-start; margin-bottom: 10px; }
        .rating { color: #d49b2f; white-space: nowrap; }
        .meta { color: #9a745c; font-size: 0.82rem; line-height: 1.6; }
        .comment { line-height: 1.55; margin: 12px 0 16px; white-space: pre-wrap; }
        .empty { text-align: center; padding: 42px 20px; color: #8a6e5a; border: 1px dashed #decabd; border-radius: 16px; background: #fff; }
        .badge-hidden { display: inline-block; background: #f4dada; color: #9f2727; font-size: 0.72rem; font-weight: 600; padding: 2px 9px; border-radius: 999px; margin-left: 6px; }
        .owner-reply { margin: 8px 0 14px; padding: 10px 12px; background: #fff7e6; border-left: 3px solid #d49b2f; border-radius: 6px; }
        .owner-reply .label { font-weight: 700; color: #76493b; font-size: 0.82rem; margin-bottom: 4px; }
        .owner-reply p { margin: 0; line-height: 1.55; }
        .owner-reply .reply-meta { color: #9a745c; font-size: 0.75rem; margin-top: 4px; }
        .small-actions { display: flex; gap: 8px; flex-wrap: wrap; }
        .inline-form { display: inline; }
    </style>
</head>
<body>
    <%@ include file="/views/includes/header.jsp" %>

    <div>
    <main class="review-page">
        <h1 class="page-title">Đánh giá của tôi</h1>
        <p class="page-sub">Chia sẻ trải nghiệm của bạn sau khi đơn đặt bàn đã hoàn tất.</p>

        <c:if test="${not empty error || not empty param.error}">
            <div class="alert alert-danger">
                <c:out value="${not empty error ? error : param.error}" />
            </div>
        </c:if>

        <c:if test="${not empty param.success}">
            <div class="alert alert-success">
                <c:choose>
                    <c:when test="${param.success == 'created'}">Đã gửi đánh giá của bạn.</c:when>
                    <c:when test="${param.success == 'updated'}">Đã cập nhật đánh giá.</c:when>
                    <c:when test="${param.success == 'deleted'}">Đã xóa đánh giá.</c:when>
                    <c:when test="${param.success == 'delete_failed'}">Không thể xóa đánh giá. Vui lòng thử lại.</c:when>
                    <c:otherwise>Thao tác đã được xử lý.</c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <c:if test="${formMode == 'create'}">
            <section class="card form-card">
                <div class="form-title">Đánh giá đơn #${orderID}</div>
                <form method="post" action="${pageContext.request.contextPath}/customer/reviews">
                    <input type="hidden" name="action" value="create">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="orderID" value="${orderID}">
                    <div class="field">
                        <label>Số sao</label>
                        <div class="stars">
                            <c:forEach var="star" begin="1" end="5">
                                <label class="star-option">
                                    <input type="radio" name="rating" value="${star}" ${ratingValue == star ? 'checked' : ''}>
                                    <span>${star} <i class="fa-solid fa-star"></i></span>
                                </label>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="field">
                        <label for="comment">Bình luận</label>
                        <textarea id="comment" name="comment" maxlength="1000" placeholder="Nhập cảm nhận của bạn về trải nghiệm tại nhà hàng..."><c:out value="${commentValue}" /></textarea>
                    </div>
                    <div class="actions">
                        <button type="submit" class="btn btn-primary"><i class="fa-solid fa-paper-plane"></i> Gửi đánh giá</button>
                        <a class="btn btn-muted" href="${pageContext.request.contextPath}/reservation?action=history">Quay lại lịch sử</a>
                    </div>
                </form>
            </section>
        </c:if>

        <c:if test="${formMode == 'edit'}">
            <section class="card form-card">
                <div class="form-title">Sửa đánh giá #${editingReview.reviewID}</div>
                <form method="post" action="${pageContext.request.contextPath}/customer/reviews">
                    <input type="hidden" name="action" value="update">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="reviewID" value="${editingReview.reviewID}">
                    <div class="field">
                        <label>Số sao</label>
                        <div class="stars">
                            <c:forEach var="star" begin="1" end="5">
                                <label class="star-option">
                                    <input type="radio" name="rating" value="${star}" ${editingReview.rating == star ? 'checked' : ''}>
                                    <span>${star} <i class="fa-solid fa-star"></i></span>
                                </label>
                            </c:forEach>
                        </div>
                    </div>
                    <div class="field">
                        <label for="editComment">Bình luận</label>
                        <textarea id="editComment" name="comment" maxlength="1000"><c:out value="${editingReview.comment}" /></textarea>
                    </div>
                    <div class="actions">
                        <button type="submit" class="btn btn-primary"><i class="fa-solid fa-save"></i> Lưu thay đổi</button>
                        <a class="btn btn-muted" href="${pageContext.request.contextPath}/customer/reviews">Hủy</a>
                    </div>
                </form>
            </section>
        </c:if>

        <c:choose>
            <c:when test="${empty reviews}">
                <div class="empty">
                    <i class="fa-regular fa-star fa-2x" style="color:#d7bfa4;"></i>
                    <p>Bạn chưa có đánh giá nào.</p>
                    <a class="btn btn-muted" href="${pageContext.request.contextPath}/reservation?action=history">Xem lịch sử đặt bàn</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="list">
                    <c:forEach var="review" items="${reviews}">
                        <article class="card review-item">
                            <div class="review-head">
                                <div>
                                    <strong>Đơn #<c:out value="${review.orderID}" /></strong>
                                    <c:if test="${review.isHidden == 1}">
                                        <span class="badge-hidden" title="Đánh giá đang bị ẩn khỏi trang công khai">
                                            <i class="fa-solid fa-eye-slash"></i> Đã ẩn
                                        </span>
                                    </c:if>
                                    <div class="meta">
                                        <fmt:formatDate value="${review.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </div>
                                </div>
                                <div class="rating">
                                    <c:forEach var="star" begin="1" end="5">
                                        <i class="${star <= review.rating ? 'fa-solid' : 'fa-regular'} fa-star"></i>
                                    </c:forEach>
                                </div>
                            </div>
                            <div class="comment"><c:out value="${review.comment}" /></div>
                            <c:if test="${not empty review.ownerReply}">
                                <div class="owner-reply">
                                    <div class="label"><i class="fa-solid fa-reply"></i> Phản hồi từ nhà hàng</div>
                                    <p><c:out value="${review.ownerReply}" /></p>
                                    <div class="reply-meta">
                                        <fmt:formatDate value="${review.ownerReplyAt}" pattern="dd/MM/yyyy HH:mm" />
                                    </div>
                                </div>
                            </c:if>
                            <div class="small-actions">
                                <a class="btn btn-muted" href="${pageContext.request.contextPath}/customer/reviews?action=edit&reviewID=${review.reviewID}">
                                    <i class="fa-solid fa-pen"></i> Sửa
                                </a>
                                <form class="inline-form" method="post" action="${pageContext.request.contextPath}/customer/reviews" onsubmit="return confirm('Bạn chắc chắn muốn xóa đánh giá này?')">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="reviewID" value="${review.reviewID}">
                                    <button type="submit" class="btn btn-danger"><i class="fa-solid fa-trash"></i> Xóa</button>
                                </form>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    <%@ include file="/views/includes/footer.jsp" %>
</body>
</html>
