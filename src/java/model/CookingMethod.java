/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Admin
 */
public class CookingMethod {
    
    private int methodID;
    private int isAvailable;
    private String methodName;
    
    private int activeMenuItem;
    private int inactiveMenuItem;
    private int totalDish;

    public CookingMethod() {
    }

    public CookingMethod(int methodID, String methodName, int activeMenuItem, int inactiveMenuItem, int totalDish) {
        this.methodID = methodID;
        this.methodName = methodName;
        this.activeMenuItem = activeMenuItem;
        this.inactiveMenuItem = inactiveMenuItem;
        this.totalDish = totalDish;
    }

    public CookingMethod(int methodID, int isAvailable, String methodName) {
        this.methodID = methodID;
        this.isAvailable = isAvailable;
        this.methodName = methodName;
    }

    public int getMethodID() {
        return methodID;
    }

    public void setMethodID(int methodID) {
        this.methodID = methodID;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getActiveMenuItem() {
        return activeMenuItem;
    }

    public void setActiveMenuItem(int activeMenuItem) {
        this.activeMenuItem = activeMenuItem;
    }

    public int getInactiveMenuItem() {
        return inactiveMenuItem;
    }

    public void setInactiveMenuItem(int inactiveMenuItem) {
        this.inactiveMenuItem = inactiveMenuItem;
    }

    public int getTotalDish() {
        return totalDish;
    }

    public void setTotalDish(int totalDish) {
        this.totalDish = totalDish;
    }

    public int getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(int isAvailable) {
        this.isAvailable = isAvailable;
    }
    
}
