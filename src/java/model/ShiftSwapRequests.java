package model;

import java.sql.Timestamp;

public class ShiftSwapRequests {

    private int swapID;
    private int requesterShiftID;
    private Integer approvedByID;  // Employee asked to cover, or owner who approved leave
    private String status;         // pending / approved / rejected
    private String reason;
    private Timestamp createdAt;   // DATETIME
    private String requestType;    // cover / leave

    public ShiftSwapRequests() {
        this.requestType = "cover";
    }

    public ShiftSwapRequests(int swapID, int requesterShiftID,
            Integer approvedByID, String status, String reason, Timestamp createdAt, String requestType) {
        this.swapID = swapID;
        this.requesterShiftID = requesterShiftID;
        this.approvedByID = approvedByID;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.requestType = requestType;
    }

    public int getSwapID() {
        return swapID;
    }

    public int getRequestID() {
        return swapID;
    }

    public void setSwapID(int swapID) {
        this.swapID = swapID;
    }

    public int getRequesterShiftID() {
        return requesterShiftID;
    }

    public void setRequesterShiftID(int requesterShiftID) {
        this.requesterShiftID = requesterShiftID;
    }

    public Integer getApprovedByID() {
        return approvedByID;
    }

    public void setApprovedByID(Integer approvedByID) {
        this.approvedByID = approvedByID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
}

