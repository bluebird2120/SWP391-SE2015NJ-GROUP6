package bootstrap;

import dal.NotificationDAO;
import model.Notifications;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DailyReservationNotifyTask implements Runnable {

    @Override
    public void run() {
        long t0 = System.currentTimeMillis();
        try {
            process();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("[DailyReservationNotifyTask] done in "
                    + (System.currentTimeMillis() - t0) + "ms");
        }
    }

    private void process() {

        // 1. Lấy tất cả lễ tân đang hoạt động
        List<Integer> receptionistIDs = getReceptionistIDs();
        if (receptionistIDs.isEmpty()) {
            System.out.println("[DailyReservationNotifyTask] Không có lễ tân nào đang hoạt động.");
            return;
        }

        // 2. Gom đơn cần báo hôm nay
        List<int[]> orders = getTodayPendingOrders();
        if (orders.isEmpty()) {
            System.out.println("[DailyReservationNotifyTask] Không có đơn nào cần báo hôm nay.");
            return;
        }

        // 3. Tạo nội dung thông báo tổng hợp duy nhất lúc 06:00h
        StringBuilder sb = new StringBuilder();
        sb.append("Hôm nay có ").append(orders.size())
          .append(" đơn đặt bàn online cần gán bàn: ");
        for (int i = 0; i < orders.size(); i++) {
            sb.append("#").append(orders.get(i)[0]);
            if (i < orders.size() - 1) sb.append(", ");
        }
        sb.append(".");

        String message = sb.toString();

        // 4. Gửi thông báo cho từng lễ tân
        try (NotificationDAO notifDAO = new NotificationDAO()) {
            for (int recID : receptionistIDs) {
                Notifications n = new Notifications();
                n.setRecipientID(recID);
                n.setRecipientType("staff");
                n.setType("reservation_needs_table");
                n.setMessage(message);
                n.setIsRead(0);
                notifDAO.insert(n);
            }
        }

        System.out.println("[DailyReservationNotifyTask] Đã gửi thông báo cho "
                + receptionistIDs.size() + " lễ tân. Nội dung: " + message);
    }

    //Lấy ra những đơn đã thanh toán cọc xong trong quá khứ của ngày hôm nay
    private List<int[]> getTodayPendingOrders() {
        List<int[]> list = new ArrayList<>();

        String sql = "SELECT o.orderID "
                   + "FROM `Order` o "
                   + "WHERE o.orderType = 1 "
                   + "  AND o.orderStatus = 'reserved' "
                   + "  AND DATE(o.orderTime) = CURDATE() "
                   + "  AND NOT EXISTS ( "
                   + "      SELECT 1 FROM Notifications n "
                   + "      WHERE n.type = 'reservation_needs_table' "
                   + "        AND n.message LIKE CONCAT('%#', o.orderID, '%') "
                   + "        AND DATE(n.createdAt) = CURDATE() "
                   + "  ) "
                   + "ORDER BY o.orderTime ASC";

        try (dal.DBContext db = new dal.DBContext();
             PreparedStatement ps = db.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new int[]{rs.getInt("orderID")});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<Integer> getReceptionistIDs() {
        // CHỈ BÁO CHO LỄ TÂN CÓ CA HÔM NAY] Trước đây lấy TẤT CẢ
        // Employee roleID=3 isActive=1, kể cả người không có lịch làm hôm
        // nay. Giờ lọc đúng theo EmployeeShifts của hôm nay.
        try (dal.EmployeeShiftDAO esDAO = new dal.EmployeeShiftDAO()) {
            return esDAO.getReceptionistsScheduledToday();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}