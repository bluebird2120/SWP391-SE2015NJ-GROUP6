package model;

import java.sql.Timestamp;

public class Reviews {

    private int reviewID;
    private int customerID;
    private Integer orderID;
    private int rating;
    private String comment;
    private Timestamp createdAt;  // DATETIME
    private int isHidden;
    private String ownerReply;
    private Timestamp ownerReplyAt;

    // Transient: chỉ điền bởi getAllReviewsForOwner để hiển thị tên khách trong
    // trang quản trị; các query khác để null.
    private String customerUserName;

    public Reviews() {
    }

    public Reviews(int reviewID, int customerID, Integer orderID, int rating, String comment, Timestamp createdAt) {
        this.reviewID = reviewID;
        this.customerID = customerID;
        this.orderID = orderID;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Reviews(int reviewID, int customerID, int rating, String comment, Timestamp createdAt) {
        this(reviewID, customerID, null, rating, comment, createdAt);
    }

    public int getReviewID() {
        return reviewID;
    }

    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public Integer getOrderID() {
        return orderID;
    }

    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
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

    public int getIsHidden() {
        return isHidden;
    }

    public void setIsHidden(int isHidden) {
        this.isHidden = isHidden;
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

    public String getCustomerUserName() {
        return customerUserName;
    }

    public void setCustomerUserName(String customerUserName) {
        this.customerUserName = customerUserName;
    }
}
