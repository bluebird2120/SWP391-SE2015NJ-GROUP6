/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import java.util.ArrayList;
import java.util.List;
import model.CookingMethod;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Admin
 */
public class CookingMethodDAO extends DBContext {

    public List<CookingMethod> getAllCookingMethod() {
        List<CookingMethod> list = new ArrayList<>();
        String sql = "select * from CookingMethod "
                + "where isAvailable = 1";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CookingMethod cm = new CookingMethod(
                        rs.getInt("methodID"),
                        rs.getInt("isAvailable"),
                        rs.getString("methodName"));
                list.add(cm);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countSearchMethod(String search, int isAvailable) {
        int total = 0;
        String sql = "select count(*) from CookingMethod where methodName like ?";
        if(isAvailable >= 0){
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

    public List<CookingMethod> searchMethodPaging(String search, int isAvailable, int offset, int pageSize) {
        List<CookingMethod> list = new ArrayList<>();
        String sql = "select * from CookingMethod where methodName like ?";
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
                CookingMethod cookingMethod = new CookingMethod();
                cookingMethod.setMethodID(rs.getInt("methodID"));
                cookingMethod.setMethodName(rs.getString("methodName"));
                cookingMethod.setIsAvailable(rs.getInt("isAvailable"));
                list.add(cookingMethod);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countMethodByStatus(int methodID, int isAvailable) {
        String sql = "select count(*) from MenuItem "
                + "where methodID = ? and isAvailable = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, methodID);
            ps.setInt(2, isAvailable);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public int countDishByMethod(int methodID) {
        String sql = "select count(*) from MenuItem "
                + "where methodID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, methodID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public boolean changeStatusMethod(int methodID, int isAvailable) {
        String sql = "UPDATE CookingMethod SET isAvailable = ? WHERE methodID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, isAvailable);
            ps.setInt(2, methodID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changeStatusItemsByMethod(int methodID, int isAvailable) {
        String sql = "UPDATE MenuItem SET isAvailable = ? WHERE methodID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, isAvailable);
            ps.setInt(2, methodID);
            return ps.executeUpdate() >= 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateMethod(String methodName, int methodID) {
        String sql = "update CookingMethod "
                + "set methodName = ? "
                + "where methodID = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, methodName);
            ps.setInt(2, methodID);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
        }
        return false;
    }

    public boolean insertMethod(String methodName) {
        String sql = "insert into CookingMethod (methodName) "
                + "values(?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, methodName);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
        }
        return false;
    }

    public boolean checkDuplicateMethod(String methodName, int methodID) {
        String sql = "select count(*) from CookingMethod "
                + "where methodName = ? and methodID != ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, methodName);
            ps.setInt(2, methodID);
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
