package dal;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * DTO dùng cho list shift (JOIN Employee + ShiftTemplates).
 */
public class ShiftRow {

    private int shiftID;
    private int employeeID;
    private String fullName;
    private int templateID;
    private String shiftName;
    private Time startTime;
    private Time endTime;
    private Date workDate;
    private Timestamp checkInTime;
    private Timestamp checkOutTime;
    private String status;

    public int getShiftID() { return shiftID; }
    public void setShiftID(int v) { this.shiftID = v; }

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int v) { this.employeeID = v; }

    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }

    public int getTemplateID() { return templateID; }
    public void setTemplateID(int v) { this.templateID = v; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String v) { this.shiftName = v; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time v) { this.startTime = v; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time v) { this.endTime = v; }

    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date v) { this.workDate = v; }

    public Timestamp getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Timestamp v) { this.checkInTime = v; }

    public Timestamp getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Timestamp v) { this.checkOutTime = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}
