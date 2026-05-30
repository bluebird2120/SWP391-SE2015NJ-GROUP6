package model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class BusinessSchedule {

    private int scheduleID;
    private String dayOfWeek;
    private Date specificDate;   // DATE
    private Time openTime;       // TIME
    private Time closeTime;      // TIME
    private int isClosed;
    private String reason;
    private Timestamp updatedAt; // DATETIME

    public BusinessSchedule() {
    }

    public BusinessSchedule(int scheduleID, String dayOfWeek, Date specificDate, Time openTime,
            Time closeTime, int isClosed, String reason, Timestamp updatedAt) {
        this.scheduleID = scheduleID;
        this.dayOfWeek = dayOfWeek;
        this.specificDate = specificDate;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed;
        this.reason = reason;
        this.updatedAt = updatedAt;
    }

    public int getScheduleID() {
        return scheduleID;
    }

    public void setScheduleID(int scheduleID) {
        this.scheduleID = scheduleID;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Date getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(Date specificDate) {
        this.specificDate = specificDate;
    }

    public Time getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Time openTime) {
        this.openTime = openTime;
    }

    public Time getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Time closeTime) {
        this.closeTime = closeTime;
    }

    public int getIsClosed() {
        return isClosed;
    }

    public void setIsClosed(int isClosed) {
        this.isClosed = isClosed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
