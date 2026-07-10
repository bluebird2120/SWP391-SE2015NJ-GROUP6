package model;

public class MenuCategory {

    private int categoryID;
    private int isAvailable;
    private String categoryName;
    
    private int activeMenuItem;
    private int inactiveMenuItem;
    private int totalDish;

    public MenuCategory() {
    }

    public MenuCategory(int categoryID, String categoryName, int activeMenuItem, int inactiveMenuItem, int totalDish) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
        this.activeMenuItem = activeMenuItem;
        this.inactiveMenuItem = inactiveMenuItem;
        this.totalDish = totalDish;
    }
    
    public MenuCategory(int categoryID, String categoryName, int activeMenuItem, int inactiveMenuItem) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
        this.activeMenuItem = activeMenuItem;
        this.inactiveMenuItem = inactiveMenuItem;
    }

    public MenuCategory(int categoryID, int isAvailable, String categoryName) {
        this.categoryID = categoryID;
        this.isAvailable = isAvailable;
        this.categoryName = categoryName;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
