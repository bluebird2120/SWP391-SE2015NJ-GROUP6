package model;

import java.sql.Timestamp;

public class ShiftSwapRequests {

    private int swapID;
    private int requesterShiftID;
    private Integer targetShiftID; // Can be null for leave requests
    private Integer approvedByID;  // Can be null initially
    private String status;         // pending / approved / rejected
    private String reason;
    private Timestamp createdAt;   // DATETIME
    private String requestType;    // swap / leave

    public ShiftSwapRequests() {
        this.requestType = "swap"; // default
    }

    public ShiftSwapRequests(int swapID, int requesterShiftID, Integer targetShiftID,
            Integer approvedByID, String status, String reason, Timestamp createdAt, String requestType) {
        this.swapID = swapID;
        this.requesterShiftID = requesterShiftID;
        this.targetShiftID = targetShiftID;
        this.approvedByID = approvedByID;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.requestType = requestType;
    }

    public int getSwapID() {
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

    public Integer getTargetShiftID() {
        return targetShiftID;
    }

    public void setTargetShiftID(Integer targetShiftID) {
        this.targetShiftID = targetShiftID;
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

