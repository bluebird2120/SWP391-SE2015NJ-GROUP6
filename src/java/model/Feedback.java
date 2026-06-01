package model;

import java.sql.Date;
import java.sql.Timestamp;

public class Feedback {

    private int feedbackID;
    private int employeeID;
    private int customerID;
    private int orderID;
    private String title;
    private Timestamp createdAt;    // DATETIME
    private String content;
    private String replyContent;
    private Date repliedAt;         // DATE (theo SQL)

    public Feedback() {
    }

    public Feedback(int feedbackID, int employeeID, int customerID, int orderID, String title,
            Timestamp createdAt, String content, String replyContent, Date repliedAt) {
        this.feedbackID = feedbackID;
        this.employeeID = employeeID;
        this.customerID = customerID;
        this.orderID = orderID;
        this.title = title;
        this.createdAt = createdAt;
        this.content = content;
        this.replyContent = replyContent;
        this.repliedAt = repliedAt;
    }

    public int getFeedbackID() {
        return feedbackID;
    }

    public void setFeedbackID(int feedbackID) {
        this.feedbackID = feedbackID;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public Date getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(Date repliedAt) {
        this.repliedAt = repliedAt;
    }
}
