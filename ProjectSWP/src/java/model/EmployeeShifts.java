package model;

import java.util.Date;

public class EmployeeShifts {

    private int shiftID;
    private int templateID;
    private int employeeID;
    private Date workDate;
    private Date checkInTime;
    private Date checkOutTime;
    private String status; // scheduled / completed / absent / late

    public EmployeeShifts() {
    }

    public EmployeeShifts(int shiftID, int templateID, int employeeID, Date workDate,
            Date checkInTime, Date checkOutTime, String status) {
        this.shiftID = shiftID;
        this.templateID = templateID;
        this.employeeID = employeeID;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
    }

    public int getShiftID() {
        return shiftID;
    }

    public void setShiftID(int shiftID) {
        this.shiftID = shiftID;
    }

    public int getTemplateID() {
        return templateID;
    }

    public void setTemplateID(int templateID) {
        this.templateID = templateID;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public Date getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Date checkInTime) {
        this.checkInTime = checkInTime;
    }

    public Date getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Date checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
