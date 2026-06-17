package model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Lớp ShiftSwapRequestDetail chứa thông tin chi tiết của một yêu cầu đổi ca hoặc xin nghỉ.
 * Lớp này đóng vai trò là một DTO (Data Transfer Object) chứa thông tin của yêu cầu đổi ca
 * kèm theo thông tin chi tiết của ca yêu cầu (Requester Shift) và ca mục tiêu (Target Shift).
 */
public class ShiftSwapRequestDetail {
    private int swapID;
    private int requesterShiftID;
    private Integer targetShiftID;
    private String status;
    private String reason;
    private Timestamp createdAt;
    private String requestType;

    // Requester Shift Info
    private int reqEmployeeID;
    private String reqEmployeeName;
    private String reqShiftName;
    private Date reqWorkDate;
    private Time reqStartTime;
    private Time reqEndTime;

    // Target Shift Info
    private Integer targetEmployeeID;
    private String targetEmployeeName;
    private String targetShiftName;
    private Date targetWorkDate;
    private Time targetStartTime;
    private Time targetEndTime;

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

    public int getReqEmployeeID() {
        return reqEmployeeID;
    }

    public void setReqEmployeeID(int reqEmployeeID) {
        this.reqEmployeeID = reqEmployeeID;
    }

    public String getReqEmployeeName() {
        return reqEmployeeName;
    }

    public void setReqEmployeeName(String reqEmployeeName) {
        this.reqEmployeeName = reqEmployeeName;
    }

    public String getReqShiftName() {
        return reqShiftName;
    }

    public void setReqShiftName(String reqShiftName) {
        this.reqShiftName = reqShiftName;
    }

    public Date getReqWorkDate() {
        return reqWorkDate;
    }

    public void setReqWorkDate(Date reqWorkDate) {
        this.reqWorkDate = reqWorkDate;
    }

    public Time getReqStartTime() {
        return reqStartTime;
    }

    public void setReqStartTime(Time reqStartTime) {
        this.reqStartTime = reqStartTime;
    }

    public Time getReqEndTime() {
        return reqEndTime;
    }

    public void setReqEndTime(Time reqEndTime) {
        this.reqEndTime = reqEndTime;
    }

    public Integer getTargetEmployeeID() {
        return targetEmployeeID;
    }

    public void setTargetEmployeeID(Integer targetEmployeeID) {
        this.targetEmployeeID = targetEmployeeID;
    }

    public String getTargetEmployeeName() {
        return targetEmployeeName;
    }

    public void setTargetEmployeeName(String targetEmployeeName) {
        this.targetEmployeeName = targetEmployeeName;
    }

    public String getTargetShiftName() {
        return targetShiftName;
    }

    public void setTargetShiftName(String targetShiftName) {
        this.targetShiftName = targetShiftName;
    }

    public Date getTargetWorkDate() {
        return targetWorkDate;
    }

    public void setTargetWorkDate(Date targetWorkDate) {
        this.targetWorkDate = targetWorkDate;
    }

    public Time getTargetStartTime() {
        return targetStartTime;
    }

    public void setTargetStartTime(Time targetStartTime) {
        this.targetStartTime = targetStartTime;
    }

    public Time getTargetEndTime() {
        return targetEndTime;
    }

    public void setTargetEndTime(Time targetEndTime) {
        this.targetEndTime = targetEndTime;
    }
}
