/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.util.ArrayList;
import java.util.List;
import model.MenuItem;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.*;
import model.MenuItemImages;

/**
 *
 * @author Admin
 */
public class MenuItemDAO extends DBContext {

    public List<MenuItem> getAllMenuItem() {

        List<MenuItem> list = new ArrayList<>();
        String sql = "select * from MenuItem mi "
                + "join MenuCategory mc on mi.categoryID = mc.categoryID";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MenuItem mi = new MenuItem(rs.getInt("itemID"),
                        rs.getInt("categoryID"),
                        rs.getString("itemName"),
                        rs.getString("description"),
                        rs.getInt("price"),
                        rs.getInt("discountPercent"),
                        rs.getInt("discountedPrice"),
                        rs.getString("image"),
                        rs.getInt("isAvailable"),
                        rs.getString("allergyNotes"),
                        rs.getString("categoryName"));

                list.add(mi);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countSearchMenuItem(String search, int categoryId, int status,
            int minPrice, int maxPrice, String priceType) {
        int total = 0;
        String sql = "select count(*) from MenuItem mi "
                + "join MenuCategory mc on mi.categoryID = mc.categoryID "
                + "where mi.itemName like ? ";

        if (status != -1) {
            sql += "and mi.isAvailable = ? ";
        }
        if (categoryId > 0) {
            sql += "and mc.categoryID = ? ";
        }
        if (priceType.equals("price")) {
            sql += "and mi.price >= ? and mi.price <= ? ";
        } else {
            sql += "and mi.discountedPrice >= ? and mi.discountedPrice <= ? ";
        }

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            int index = 1;

            ps.setString(index++, "%" + search + "%");
            if (status != -1) {
                ps.setInt(index++, status);
            }
            if (categoryId > 0) {
                ps.setInt(index++, categoryId);
            }
            ps.setInt(index++, minPrice);
            ps.setInt(index++, maxPrice);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public List<MenuItem> searchMenuItemPaging(String search, int categoryId, int status,
            int minPrice, int maxPrice, String sort, String priceType, int offSet, int pageSize) {
        List<MenuItem> list = new ArrayList<>();
        String sql = "select * from MenuItem mi "
                + "join MenuCategory mc on mi.categoryID = mc.categoryID "
                + "where mi.itemName like ? ";

        if (status != -1) {
            sql += "and mi.isAvailable = ? ";
        }
        if (categoryId > 0) {
            sql += "and mc.categoryID = ? ";
        }
        if (priceType.equals("price")) {
            sql += "and mi.price >= ? "
                    + "and mi.price <= ? "
                    + "order by price " + sort;
        } else {
            sql += "and mi.discountedPrice >= ? "
                    + "and mi.discountedPrice <= ? "
                    + "order by discountedPrice " + sort;
        }
        sql += " LIMIT ?, ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            int index = 1;

            ps.setString(index++, "%" + search + "%");
            if (status != -1) {
                ps.setInt(index++, status);
            }
            if (categoryId > 0) {
                ps.setInt(index++, categoryId);
            }
            ps.setInt(index++, minPrice);
            ps.setInt(index++, maxPrice);
            ps.setInt(index++, offSet);
            ps.setInt(index++, pageSize);
            ResultSet rs = ps.executeQuery();
            rs = ps.executeQuery();
            while (rs.next()) {
                MenuItem item = new MenuItem(rs.getInt("itemID"),
                        rs.getInt("categoryID"),
                        rs.getString("itemName"),
                        rs.getString("description"),
                        rs.getInt("price"),
                        rs.getInt("discountPercent"),
                        rs.getInt("discountedPrice"),
                        rs.getString("image"),
                        rs.getInt("isAvailable"),
                        rs.getString("allergyNotes"),
                        rs.getString("categoryName"));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public MenuItem getMenuItemById(int id) {
        MenuItem mi;
        String sql = "select * from MenuItem mi "
                + "join MenuCategory mc on mi.categoryID = mc.categoryID "
                + "where mi.itemID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                mi = new MenuItem(rs.getInt("itemID"),
                        rs.getInt("categoryID"),
                        rs.getString("itemName"),
                        rs.getString("description"),
                        rs.getInt("price"),
                        rs.getInt("discountPercent"),
                        rs.getInt("discountedPrice"),
                        rs.getString("image"),
                        rs.getInt("isAvailable"),
                        rs.getString("allergyNotes"),
                        rs.getString("categoryName"));
                return mi;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public boolean updateMenuItem(int id, int categoryId, String itemName, String description, int price,
            int discountPercent, String image, int isAvailable, String allergyNotes) {
        String sql = "update MenuItem "
                + "set categoryID = ? , "
                + "itemName = ? , "
                + "description = ? , "
                + "price = ? , "
                + "discountPercent = ? , "
                + "discountedPrice = ? , "
                + "image = ? , "
                + "isAvailable = ? , "
                + "allergyNotes = ? "
                + "where itemID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, categoryId);
            ps.setString(2, itemName);
            ps.setString(3, description);
            ps.setInt(4, price);
            ps.setInt(5, discountPercent);
            ps.setInt(6, (int) Math.round((price * (1 - (double) discountPercent / 100))));
            ps.setString(7, image);
            ps.setInt(8, isAvailable);
            ps.setString(9, allergyNotes);
            ps.setInt(10, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int insertMenuItem(int categoryId, String itemName, String description, int price,
            int discountPercent, String image, int isAvailable, String allergyNotes) {
        String sql = "insert into MenuItem (categoryID, itemName, description, price, discountPercent, discountedPrice, image, isAvailable, allergyNotes) "
                + "values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, categoryId);
            ps.setString(2, itemName);
            ps.setString(3, description);
            ps.setInt(4, price);
            ps.setInt(5, discountPercent);
            ps.setInt(6, (int) Math.round((price * (1 - (double) discountPercent / 100))));
            ps.setString(7, image);
            ps.setInt(8, isAvailable);
            ps.setString(9, allergyNotes);

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean checkDuplicateMenuItem(String name, int itemID) {
        String sql = "select count(*) from MenuItem "
                + "where itemName = ? and itemID != ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, name);
            ps.setInt(2, itemID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total = rs.getInt(1);
                return total > 0;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean deleteMenuItemImages(int itemID) {

        String sql = "DELETE FROM MenuItemImages WHERE itemID = ?";

        try {

            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setInt(1, itemID);

            return ps.executeUpdate() >= 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean insertMenuItemImage(int itemID, String imagePath) {

        String sql = "INSERT INTO MenuItemImages(itemID, imagePath) "
                + "VALUES (?, ?)";

        try {

            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setInt(1, itemID);
            ps.setString(2, imagePath);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public List<MenuItemImages> getImagesByMenuItemId(int itemID){
        List<MenuItemImages> list = new ArrayList<>();
        String sql = "select * from MenuItemImages "
                + "where itemID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, itemID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {   
                MenuItemImages mim = new MenuItemImages(rs.getInt("imageID"), 
                        rs.getInt("itemID"),
                        rs.getString("imagePath"), 
                        rs.getDate("createdAt"));
                list.add(mim);
            }
            return list;
        } catch (Exception e) {
        }
        return null;
    }
    
}
