package model;

import java.sql.Time;

public class ShiftTemplates {

    private int templateID;
    private String shiftName;
    private Time startTime;
    private Time endTime;

    public ShiftTemplates() {
    }

    public ShiftTemplates(int templateID, String shiftName, Time startTime, Time endTime) {
        this.templateID = templateID;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getTemplateID() {
        return templateID;
    }

    public void setTemplateID(int templateID) {
        this.templateID = templateID;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }
}
