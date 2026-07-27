package model;

import java.sql.Date;
import java.sql.Timestamp;

public class Employee {

    private int employeeID;
    private int roleID;
    private String password;
    private String fullName;
    private Date dob;
    private String phoneNumber;
    private String email;
    // Đã xóa: private int salary;
    private int isActive;
    private String address;
    private String image;
    private Timestamp createdAt;
    private Timestamp lastPasswordChangedAt;
    private int mustChangePassword;
    private String resetToken;
    private Timestamp resetTokenExpiry;

    public Employee() {
    }

    public Employee(int employeeID, int roleID, String password, String fullName, Date dob, 
                    String phoneNumber, String email, int isActive, String address, 
                    String image, Timestamp createdAt, Timestamp lastPasswordChangedAt, int mustChangePassword) {
        this.employeeID = employeeID;
        this.roleID = roleID;
        this.password = password;
        this.fullName = fullName;
        this.dob = dob;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.isActive = isActive;
        this.address = address;
        this.image = image;
        this.createdAt = createdAt;
        this.lastPasswordChangedAt = lastPasswordChangedAt;
        this.mustChangePassword = mustChangePassword;
    }

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

    public int getRoleID() { return roleID; }
    public void setRoleID(int roleID) { this.roleID = roleID; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getIsActive() { return isActive; }
    public void setIsActive(int isActive) { this.isActive = isActive; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getLastPasswordChangedAt() { return lastPasswordChangedAt; }
    public void setLastPasswordChangedAt(Timestamp lastPasswordChangedAt) { this.lastPasswordChangedAt = lastPasswordChangedAt; }

    public int getMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(int mustChangePassword) { this.mustChangePassword = mustChangePassword; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public Timestamp getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(Timestamp resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
}