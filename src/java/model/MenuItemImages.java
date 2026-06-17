package model;

import java.util.Date;

public class MenuItemImages {

    private int imageID;
    private int itemID;
    private String imagePath;
    private Date createdAt;

    public MenuItemImages() {
    }

    public MenuItemImages(int imageID, int itemID, String imagePath, Date createdAt) {
        this.imageID = imageID;
        this.itemID = itemID;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
