package model;

import java.sql.Time;
import java.sql.Timestamp;

/**
 * Kế hoạch phân ca theo tháng (chưa publish).
 * status flow: DRAFT -> NOTIFIED -> APPLIED (hoặc CANCELLED).
 */
public class MonthlyShiftPlan {
    public static final String DRAFT     = "DRAFT";
    public static final String NOTIFIED  = "NOTIFIED";
    public static final String APPLIED   = "APPLIED";
    public static final String CANCELLED = "CANCELLED";

    private int planID;
    private int employeeID;
    private int templateID;
    private int effectiveYear;
    private int effectiveMonth;
    private String status;
    private int createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Transient join fields cho UI (không map trực tiếp DB column)
    private String employeeName;
    private String templateName;
    private Time startTime;
    private Time endTime;

    public MonthlyShiftPlan() {}

    public int getPlanID() { return planID; }
    public void setPlanID(int planID) { this.planID = planID; }

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

    public int getTemplateID() { return templateID; }
    public void setTemplateID(int templateID) { this.templateID = templateID; }

    public int getEffectiveYear() { return effectiveYear; }
    public void setEffectiveYear(int effectiveYear) { this.effectiveYear = effectiveYear; }

    public int getEffectiveMonth() { return effectiveMonth; }
    public void setEffectiveMonth(int effectiveMonth) { this.effectiveMonth = effectiveMonth; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }
}
