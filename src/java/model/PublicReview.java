package model;

import java.sql.Timestamp;

/**
 * DTO hiển thị review công khai cho mọi người xem.
 * Khác với {@link Reviews}: không lộ customerID, có thêm userName để hiển thị
 * và đã được lọc bỏ những review do tài khoản của Owner đăng (trùng email/phone
 * với Employee có roleID = 1).
 */
public class PublicReview {

    private int reviewID;
    private String userName;
    private int rating;
    private String comment;
    private Timestamp createdAt;
    private String ownerReply;
    private Timestamp ownerReplyAt;

    public PublicReview() {
    }

    public PublicReview(int reviewID, String userName, int rating, String comment, Timestamp createdAt) {
        this.reviewID = reviewID;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public int getReviewID() {
        return reviewID;
    }

    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getOwnerReply() {
        return ownerReply;
    }

    public void setOwnerReply(String ownerReply) {
        this.ownerReply = ownerReply;
    }

    public Timestamp getOwnerReplyAt() {
        return ownerReplyAt;
    }

    public void setOwnerReplyAt(Timestamp ownerReplyAt) {
        this.ownerReplyAt = ownerReplyAt;
    }
}
