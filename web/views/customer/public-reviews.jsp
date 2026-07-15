<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đánh giá khách hàng</title>
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
        <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
        <style>
            * {
                box-sizing: border-box;
            }
            body {
                margin: 0;
                font-family: 'Inter', sans-serif;
                background: #faf6f2;
                color: #4a3528;
            }
            .review-page {
                max-width: 1120px;
                margin: 0 auto;
                padding: 38px 20px 64px;
            }
            .page-title {
                font-family: 'Playfair Display', serif;
                color: #76493b;
                font-size: 2.15rem;
                margin: 0 0 8px;
            }
            .page-sub {
                color: #9a745c;
                margin: 0 0 24px;
                line-height: 1.55;
            }
            .summary {
                display: flex;
                align-items: center;
                gap: 18px;
                flex-wrap: wrap;
                background: #fff;
                color: #4a3528;
                border: 1px solid #ede0d8;
                border-radius: 10px;
                padding: 14px 16px;
                margin-bottom: 18px;
                box-shadow: 0 8px 24px rgba(118, 73, 59, 0.06);
            }
            .summary-content {
                display: flex;
                align-items: center;
                gap: 14px;
                flex-wrap: wrap;
                font-size: 0.98rem;
                font-weight: 600;
                min-width: 0;
            }
            .summary-item {
                display: inline-flex;
                align-items: center;
                gap: 6px;
                white-space: nowrap;
            }
            .summary-item i {
                color: #76493b;
            }
            .summary-score {
                color: #76493b;
                font-weight: 800;
            }
            .summary-stars {
                color: #ffc107;
                letter-spacing: 0;
            }
            .review-list {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(290px, 1fr));
                gap: 16px;
            }
            .review-actions {
                display: flex;
                justify-content: center;
                margin-top: 22px;
            }
            .review-action-button {
                display: inline-flex;
                align-items: center;
                gap: 8px;
                color: #fff;
                background: #76493b;
                border: 1px solid #76493b;
                border-radius: 8px;
                padding: 11px 18px;
                font-weight: 700;
                text-decoration: none;
                box-shadow: 0 8px 18px rgba(118, 73, 59, 0.18);
            }
            .review-action-button:hover {
                background: #5d3a2e;
                border-color: #5d3a2e;
            }
            .review-card {
                background: #fff;
                border: 1px solid #ede0d8;
                border-radius: 10px;
                padding: 18px;
                box-shadow: 0 10px 28px rgba(118, 73, 59, 0.07);
                min-height: 220px;
                display: flex;
                flex-direction: column;
            }
            .review-head {
                display: flex;
                justify-content: space-between;
                gap: 12px;
                align-items: flex-start;
                margin-bottom: 12px;
            }
            .customer-name {
                font-weight: 700;
                color: #5d3a2e;
                overflow-wrap: anywhere;
            }
            .meta {
                color: #9a745c;
                font-size: 0.82rem;
                margin-top: 4px;
            }
            .rating {
                color: #d49b2f;
                white-space: nowrap;
                font-size: 0.96rem;
            }
            .comment {
                line-height: 1.6;
                margin: 6px 0 16px;
                white-space: pre-wrap;
                overflow-wrap: anywhere;
                flex: 1;
            }
            .owner-reply {
                margin-top: auto;
                padding: 11px 12px;
                background: #fff7e6;
                border-left: 3px solid #d49b2f;
                border-radius: 6px;
            }
            .owner-reply .label {
                font-weight: 700;
                color: #76493b;
                font-size: 0.82rem;
                margin-bottom: 5px;
            }
            .owner-reply p {
                margin: 0;
                line-height: 1.55;
                overflow-wrap: anywhere;
            }
            .owner-reply .reply-meta {
                color: #9a745c;
                font-size: 0.75rem;
                margin-top: 5px;
            }
            .empty {
                text-align: center;
                padding: 46px 22px;
                color: #8a6e5a;
                border: 1px dashed #decabd;
                border-radius: 10px;
                background: #fff;
            }
            .empty i {
                color: #d7bfa4;
                margin-bottom: 12px;
            }
            @media (max-width: 640px) {
                .review-page {
                    padding: 28px 14px 48px;
                }
                .page-title {
                    font-size: 1.75rem;
                }
                .summary-content {
                    font-size: 0.92rem;
                    gap: 8px;
                }
                .summary-item {
                    white-space: normal;
                }
                .review-list {
                    grid-template-columns: 1fr;
                }
            }
        </style>
    </head>
    <body>
        <%@ include file="/views/includes/header.jsp" %>

        <main class="review-page">
            <h1 class="page-title">Đánh giá khách hàng</h1>
            <p class="page-sub">Những chia sẻ công khai từ khách hàng sau khi trải nghiệm dịch vụ tại nhà hàng.</p>

            <div class="summary">
                <div class="summary-content">
                    <span class="summary-item">
                        <i class="fa-solid fa-comments"></i>
                        <strong><c:out value="${totalPublicReviewsText}" /> đánh giá </strong>
                    </span>
                    <span class="summary-item">
                        <i class="fa-solid fa-star-half-stroke"></i>
                        Điểm trung bình:
                        <span class="summary-score"><c:out value="${averageRatingText}" /></span>
                    </span>
                    <span class="summary-item">
                        <span class="summary-stars">
                            <c:forEach var="star" begin="1" end="5">
                                <c:choose>
                                    <c:when test="${star <= fullAverageStars}">
                                        <i class="fa-solid fa-star"></i>
                                    </c:when>
                                    <c:when test="${star == fullAverageStars + 1 && hasHalfAverageStar}">
                                        <i class="fa-solid fa-star-half-stroke"></i>
                                    </c:when>
                                    <c:otherwise>
                                        <i class="fa-regular fa-star"></i>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </span>
                    </span>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty reviews}">
                    <div class="empty">
                        <i class="fa-regular fa-comment fa-2x"></i>
                        <p>Chưa có đánh giá công khai nào.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <section class="review-list">
                        <c:forEach var="review" items="${reviews}">
                            <article class="review-card">
                                <div class="review-head">
                                    <div>
                                        <div class="customer-name">
                                            <i class="fa-solid fa-user" style="color:#a0714f;"></i>
                                            <c:out value="${review.userName}" />
                                        </div>
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
                            </article>
                        </c:forEach>
                    </section>

                    <c:if test="${hasMoreReviews}">
                        <c:url var="showAllReviewsUrl" value="/reviews">
                            <c:param name="show" value="all" />
                        </c:url>
                        <div class="review-actions">
                            <a class="review-action-button" href="${showAllReviewsUrl}">
                                <i class="fa-solid fa-clock-rotate-left"></i>
                                Xem review trước đó
                            </a>
                        </div>
                    </c:if>

                    <c:if test="${showAll}">
                        <c:url var="latestReviewsUrl" value="/reviews" />
                        <div class="review-actions">
                            <a class="review-action-button" href="${latestReviewsUrl}">
                                <i class="fa-solid fa-arrow-up"></i>
                                Thu gọn đánh giá
                            </a>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </main>

        <%@ include file="/views/includes/footer.jsp" %>
    </body>
</html>
