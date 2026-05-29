package model;

public class Table {

    private int tableID;
    private int employeeID;
    private int reservationID;
    private int currentStaffID;
    private String tableName;
    private int capacity;
    private String QRCodeToken;
    private String areaType; // public / private
    private int isActive;

    public Table() {
    }

    public Table(int tableID, int employeeID, int reservationID, int currentStaffID,
            String tableName, int capacity, String QRCodeToken, String areaType, int isActive) {
        this.tableID = tableID;
        this.employeeID = employeeID;
        this.reservationID = reservationID;
        this.currentStaffID = currentStaffID;
        this.tableName = tableName;
        this.capacity = capacity;
        this.QRCodeToken = QRCodeToken;
        this.areaType = areaType;
        this.isActive = isActive;
    }

    public int getTableID() {
        return tableID;
    }

    public void setTableID(int tableID) {
        this.tableID = tableID;
    }

    public int getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(int employeeID) {
        this.employeeID = employeeID;
    }

    public int getReservationID() {
        return reservationID;
    }

    public void setReservationID(int reservationID) {
        this.reservationID = reservationID;
    }

    public int getCurrentStaffID() {
        return currentStaffID;
    }

    public void setCurrentStaffID(int currentStaffID) {
        this.currentStaffID = currentStaffID;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getQRCodeToken() {
        return QRCodeToken;
    }

    public void setQRCodeToken(String QRCodeToken) {
        this.QRCodeToken = QRCodeToken;
    }

    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }
}
