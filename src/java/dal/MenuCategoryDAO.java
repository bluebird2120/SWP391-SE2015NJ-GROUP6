/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.MenuCategory;

/**
 *
 * @author Admin
 */
public class MenuCategoryDAO extends DBContext {

    public List<MenuCategory> getAllMenuCategory() {
        List<MenuCategory> list = new ArrayList<>();
        String sql = "select * from MenuCategory where isAvailable = 1";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MenuCategory mc = new MenuCategory(
                        rs.getInt("categoryID"),
                        rs.getInt("isAvailable"),
                        rs.getString("categoryName"));
                list.add(mc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countDishByStatus(int categoryID, int isAvailable) {
        String sql = "select count(*) from MenuItem "
                + "where categoryID = ? and isAvailable = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, categoryID);
            ps.setInt(2, isAvailable);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public int countDishByCategory(int categoryID) {
        String sql = "select count(*) from MenuItem "
                + "where categoryID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, categoryID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public boolean updateCategory(String categoryName, int categoryID) {
        String sql = "update MenuCategory set categoryName = ? "
                + "where categoryID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, categoryName);
            ps.setInt(2, categoryID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
        }
        return false;
    }

    public boolean insertCategory(String categoryName) {
        String sql = "insert into MenuCategory (categoryName) "
                + "values(?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, categoryName);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
        }
        return false;
    }

    public boolean changeStatusCategory(int categoryID, int status) {
        String sql = "update MenuCategory set isAvailable = ? where categoryID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, status);
            ps.setInt(2, categoryID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changeStatusItemsByCategory(int categoryID, int status) {
        String sql = "update MenuItem set isAvailable = ? where categoryID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, status);
            ps.setInt(2, categoryID);
            return ps.executeUpdate() >= 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int countSearchCategory(String search, int isAvailable) {
        int total = 0;
        String sql = "select count(*) from MenuCategory where categoryName like ?";
        if (isAvailable >= 0) {
            sql += " and isAvailable = ?";
        }
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, "%" + search + "%");
            if (isAvailable >= 0) {
                ps.setInt(2, isAvailable);
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    public List<MenuCategory> searchCategoryPaging(String search, int isAvailable, int offset, int pageSize) {
        List<MenuCategory> list = new ArrayList<>();
        String sql = "select * from MenuCategory where categoryName like ?";
        if (isAvailable >= 0) {
            sql += " and isAvailable = ?";
        }
        sql += " LIMIT ?, ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            int index = 1;
            ps.setString(index++, "%" + search + "%");
            if (isAvailable >= 0) {
                ps.setInt(index++, isAvailable);
            }
            ps.setInt(index++, offset);
            ps.setInt(index++, pageSize);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MenuCategory mc = new MenuCategory();
                mc.setCategoryID(rs.getInt("categoryID"));
                mc.setCategoryName(rs.getString("categoryName"));
                mc.setIsAvailable(rs.getInt("isAvailable")); // Cần setter này trong Model
                list.add(mc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean checkDuplicateCategory(String categoryName, int categoryID) {
        String sql = "select count(*) from MenuCategory "
                + "where categoryName = ? and categoryID != ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, categoryName);
            ps.setInt(2, categoryID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total = rs.getInt(1);
                return total > 0;
            }
        } catch (Exception e) {
        }
        return false;
    }
}
