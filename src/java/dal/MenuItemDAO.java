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

    public int countSearchMenuItem(String search, int categoryId, int methodID, int status,
            int minPrice, int maxPrice, String priceType) {
        int total = 0;
        String sql = "select count(*) from MenuItem mi "
                + "join MenuCategory mc on mi.categoryID = mc.categoryID "
                + "join CookingMethod cm on mi.methodID = cm.methodID "
                + "where mi.itemName like ? ";

        if (status != -1) {
            sql += "and mi.isAvailable = ? ";
        }
        if (categoryId > 0) {
            sql += "and mc.categoryID = ? ";
        }
        if (methodID > 0) {
            sql += "and cm.methodID = ? ";
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
            if (methodID > 0) {
                ps.setInt(index++, methodID);
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

    public List<MenuItem> searchMenuItemPaging(String search, int categoryId, int methodID, int status,
            int minPrice, int maxPrice, String sort, String priceType, int offSet, int pageSize) {
        List<MenuItem> list = new ArrayList<>();
        String sql = "select * from MenuItem mi "
                + "join MenuCategory mc on mi.categoryID = mc.categoryID "
                + "join CookingMethod cm on mi.methodID = cm.methodID "
                + "where mi.itemName like ? ";

        if (status != -1) {
            sql += "and mi.isAvailable = ? ";
        }
        if (categoryId > 0) {
            sql += "and mc.categoryID = ? ";
        }
        if (methodID > 0) {
            sql += "and cm.methodID = ? ";
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
            if (methodID > 0) {
                ps.setInt(index++, methodID);
            }
            ps.setInt(index++, minPrice);
            ps.setInt(index++, maxPrice);
            ps.setInt(index++, offSet);
            ps.setInt(index++, pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MenuItem item = new MenuItem(rs.getInt("itemID"),
                        rs.getInt("categoryID"),
                        rs.getInt("methodID"),
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
                + "join CookingMethod cm on mi.methodID = cm.methodID "
                + "where mi.itemID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                mi = new MenuItem(rs.getInt("itemID"),
                        rs.getInt("categoryID"),
                        rs.getInt("methodID"),
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
            int discountPercent, String image, int isAvailable, String allergyNotes, int methodID) {
        String sql = "update MenuItem "
                + "set categoryID = ? , "
                + "itemName = ? , "
                + "description = ? , "
                + "price = ? , "
                + "discountPercent = ? , "
                + "discountedPrice = ? , "
                + "image = ? , "
                + "isAvailable = ? , "
                + "allergyNotes = ? , "
                + "methodID = ? "
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
            ps.setInt(10, methodID);
            ps.setInt(11, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int insertMenuItem(int categoryId, String itemName, String description, int price,
            int discountPercent, String image, int isAvailable, String allergyNotes, int methodID) {
        String sql = "insert into MenuItem (categoryID, itemName, description, price, discountPercent, discountedPrice, image, isAvailable, allergyNotes, methodID) "
                + "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            ps.setInt(10, methodID);
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

    public List<MenuItemImages> getImagesByMenuItemId(int itemID) {
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

    public int countSearchDish(String itemName, java.sql.Date date, int categoryID, int methodID) {
        int total = 0;
        int index = 1;
        String sql = "select count(*) from MenuItem mi "
                + "join MenuCategory mc on mi.categoryID = mc.categoryID "
                + "join CookingMethod cm on mi.methodID = cm.methodID "
                + "left join DailyInventory di on mi.itemID = di.itemID and di.workingDate = ? "
                + "where mi.itemName like ? ";

        String todayStr = new java.sql.Date(System.currentTimeMillis()).toString();
        if (date != null && date.toString().equals(todayStr)) {
            sql += "and (mi.isAvailable = 1 or di.workingDate is not null) ";
        } else {
            sql += "and di.workingDate is not null ";
        }
        if (categoryID > 0) {
            sql += "and mi.categoryID = ?";
        }
        if (methodID > 0) {
            sql += "and cm.methodID = ? ";
        }
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDate(index++, date);
            ps.setString(index++, "%" + itemName + "%");
            if (categoryID > 0) {
                ps.setInt(index++, categoryID);
            }
            if (methodID > 0) {
                ps.setInt(index++, methodID);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
        }
        return total;
    }

    public List<MenuItem> searchDishPaging(String itemName, java.sql.Date date, int categoryID, int methodID, int offset, int pageSize) {
        List<MenuItem> list = new ArrayList<>();
        int index = 1;
        String sql = "select mi.*, mc.categoryName, di.workingDate, di.initialQuantity, di.quantityInStock from MenuItem mi "
                + "join MenuCategory mc on mi.categoryID = mc.categoryID "
                + "join CookingMethod cm on mi.methodID = cm.methodID "
                + "left join DailyInventory di on mi.itemID = di.itemID and di.workingDate = ? "
                + "where mi.itemName like ? ";

        String todayStr = new java.sql.Date(System.currentTimeMillis()).toString();
        if (date != null && date.toString().equals(todayStr)) {
            sql += "and (mi.isAvailable = 1 or di.workingDate is not null) ";
        } else {
            sql += "and di.workingDate is not null ";
        }

        if (categoryID > 0) {
            sql += "and mi.categoryID = ? ";
        }
        if (methodID > 0) {
            sql += "and cm.methodID = ? ";
        }
        sql += "LIMIT ?, ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDate(index++, date);
            ps.setString(index++, "%" + itemName + "%");
            if (categoryID > 0) {
                ps.setInt(index++, categoryID);
            }
            if (methodID > 0) {
                ps.setInt(index++, methodID);
            }
            ps.setInt(index++, offset);
            ps.setInt(index++, pageSize);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MenuItem mi = new MenuItem(rs.getInt("itemID"),
                        rs.getString("itemName"),
                        rs.getString("categoryName"),
                        rs.getDate("workingDate"),
                        rs.getInt("initialQuantity"),
                        rs.getInt("quantityInStock"));
                list.add(mi);
            }
        } catch (Exception e) {
        }
        return list;
    }

    public List<MenuItem> getPerformanceDish(String search, int categoryId, int methodId,
            String startDate, String endDate, int offset, int pageSize) {

        List<MenuItem> dishList = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT mi.itemID, mi.itemName, "
                + "cm.methodName, "
                + "mc.categoryName, "
                + "IFNULL(SUM(CASE "
                + "WHEN o.orderID IS NOT NULL THEN oi.quantity "
                + "ELSE 0 END), 0) AS totalQuantity "
                + "FROM MenuItem mi "
                + "JOIN MenuCategory mc ON mi.categoryID = mc.categoryID "
                + "JOIN CookingMethod cm ON mi.methodID = cm.methodID "
                + "LEFT JOIN OrderItem oi ON oi.itemID = mi.itemID "
                + "LEFT JOIN `Order` o ON o.orderID = oi.orderID "
                + "AND o.orderStatus = 'completed' "
                + "AND o.createdAt BETWEEN ? AND ? "
                + "WHERE 1=1 ");

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND mi.itemName LIKE ? ");
        }

        if (categoryId > 0) {
            sql.append("AND mi.categoryID = ? ");
        }

        if (methodId > 0) {
            sql.append("AND mi.methodID = ? ");
        }

        sql.append("GROUP BY mi.itemID, mi.itemID, mi.itemName, cm.methodName, mc.categoryName ");
        sql.append("ORDER BY totalQuantity DESC, ");
        sql.append("SUM(CASE WHEN o.orderID IS NOT NULL THEN oi.quantity * oi.price ELSE 0 END) DESC ");
        sql.append("LIMIT ? OFFSET ?");

        try {
            PreparedStatement ps = connection.prepareStatement(sql.toString());

            int index = 1;

            ps.setString(index++, startDate + " 00:00:00");
            ps.setString(index++, endDate + " 23:59:59");

            if (search != null && !search.trim().isEmpty()) {
                ps.setString(index++, "%" + search.trim() + "%");
            }

            if (categoryId > 0) {
                ps.setInt(index++, categoryId);
            }

            if (methodId > 0) {
                ps.setInt(index++, methodId);
            }

            ps.setInt(index++, pageSize);
            ps.setInt(index++, offset);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MenuItem mi = new MenuItem();
                
                mi.setItemID(rs.getInt("itemID"));
                mi.setItemName(rs.getString("itemName"));
                mi.setCategoryName(rs.getString("categoryName"));
                mi.setMethodName(rs.getString("methodName"));
                mi.setTotalQuantity(rs.getInt("totalQuantity"));

                dishList.add(mi);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dishList;
    }

    public int getTotalPerformanceDishCount(String search, int categoryId, int methodId, String startDate, String endDate) {
        int total = 0;
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT mi.itemID) AS total "
                + "FROM MenuItem mi "
                + "JOIN MenuCategory mc ON mi.categoryID = mc.categoryID "
                + "JOIN CookingMethod cm ON mi.methodID = cm.methodID "
                + "LEFT JOIN OrderItem oi ON oi.itemID = mi.itemID "
                + "LEFT JOIN `Order` o ON o.orderID = oi.orderID "
                + "AND o.orderStatus = 'completed' "
                + "AND o.createdAt BETWEEN ? AND ? "
                + "WHERE 1=1 "
        );
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND mi.itemName LIKE ? ");
        }
        if (categoryId > 0) {
            sql.append("AND mi.categoryID = ? ");
        }
        if (methodId > 0) {
            sql.append("AND mi.methodID = ? ");
        }
        try {
            PreparedStatement ps = connection.prepareStatement(sql.toString());
            int index = 1;
            ps.setString(index++, startDate + " 00:00:00");
            ps.setString(index++, endDate + " 23:59:59");
            if (search != null && !search.trim().isEmpty()) {
                ps.setString(index++, "%" + search.trim() + "%");
            }
            if (categoryId > 0) {
                ps.setInt(index++, categoryId);
            }
            if (methodId > 0) {
                ps.setInt(index++, methodId);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    public int totalMenuItem() {
        int total = 0;
        String sql = "select count(*) from MenuItem";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
        }
        return total;
    }
    
    public List<MenuItem> getSaleHistoryByItem(int itemID, String startDate, String endDate){
        List<MenuItem> historyList = new ArrayList<>();
        String sql = "select mi.itemName, date(o.createdAt) as saleDate, sum(oi.quantity) as dailyQuantity from MenuItem mi "
                + "join OrderItem oi on mi.itemID = oi.itemID "
                + "join `Order` o on oi.orderID = o.orderID "
                + "where o.orderStatus = 'completed' and oi.itemID = ? "
                + "and o.createdAt between ? and ? "
                + "group by mi.itemName, date(o.createdAt) "
                + "order by saleDate desc";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, itemID);
            ps.setString(2, startDate + " 00:00:00");
            ps.setString(3, endDate + " 23:59:59");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {                
                MenuItem mi = new MenuItem();
                mi.setItemName(rs.getString("itemName"));
                mi.setWorkingDate(rs.getDate("saleDate"));
                mi.setTotalQuantity(rs.getInt("dailyQuantity"));
                historyList.add(mi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return historyList;
    }
}
