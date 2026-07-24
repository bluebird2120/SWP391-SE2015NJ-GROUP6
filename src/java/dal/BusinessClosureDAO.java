package dal;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Đồng bộ giờ hoạt động với lịch làm việc của nhân viên.
 *
 * Luồng đóng cửa:
 * scheduled -> restaurant_closed -> gửi thông báo nghỉ.
 *
 * Luồng mở lại:
 * restaurant_closed -> scheduled -> gửi thông báo đi làm lại.
 */
public class BusinessClosureDAO extends DBContext {

    /**
     * [RESTAURANT CLOSED] Đóng một ngày cụ thể.
     */
    public boolean closeSpecialDate(Date workDate, String reason) {
        String messageEnd = ". Ca làm việc của bạn trong ngày này đã được hủy.";
        if (reason != null && !reason.isBlank()) {
            messageEnd += " Lý do: " + reason.trim();
        }

        return changeShiftsOnDate(
                workDate,
                "scheduled",
                "restaurant_closed",
                "restaurant_closed",
                "Nhà hàng nghỉ hoạt động ngày ",
                messageEnd
        );
    }

    /**
     * [RESTAURANT REOPENED] Mở lại một ngày cụ thể.
     */
    public boolean reopenSpecialDate(Date workDate) {
        return changeShiftsOnDate(
                workDate,
                "restaurant_closed",
                "scheduled",
                "restaurant_reopened",
                "Nhà hàng hoạt động bình thường ngày ",
                ". Ca làm việc của bạn đã được khôi phục."
        );
    }

    /**
     * [RESTAURANT CLOSED] Đóng một thứ trong lịch tuần.
     * Chỉ tác động các ca từ ngày hiện tại trở đi.
     */
    public boolean closeWeeklyDay(String dayOfWeek, String reason) {
        String messageEnd = ". Ca làm việc của bạn trong ngày này đã được hủy.";
        if (reason != null && !reason.isBlank()) {
            messageEnd += " Lý do: " + reason.trim();
        }

        return changeShiftsOnWeekday(
                dayOfWeek,
                "scheduled",
                "restaurant_closed",
                "restaurant_closed",
                "Nhà hàng nghỉ hoạt động ngày ",
                messageEnd,
                0
        );
    }

    /**
     * [RESTAURANT REOPENED] Mở lại một thứ trong lịch tuần.
     * Ngày đặc biệt vẫn đóng cửa sẽ không được mở lại.
     */
    public boolean reopenWeeklyDay(String dayOfWeek) {
        return changeShiftsOnWeekday(
                dayOfWeek,
                "restaurant_closed",
                "scheduled",
                "restaurant_reopened",
                "Nhà hàng hoạt động bình thường ngày ",
                ". Ca làm việc của bạn đã được khôi phục.",
                1
        );
    }

    /**
     * Đổi trạng thái ca của một ngày cụ thể và gửi thông báo.
     *
     * Có 2 câu SQL vì MySQL không thể vừa INSERT thông báo vừa UPDATE ca
     * trong cùng một câu lệnh. Hai câu được đặt trong một transaction:
     * nếu một câu lỗi thì toàn bộ thay đổi sẽ được hoàn tác.
     */
    private boolean changeShiftsOnDate(
            Date workDate,
            String oldStatus,
            String newStatus,
            String notificationType,
            String messageStart,
            String messageEnd) {

        if (connection == null) {
            return false;
        }

        /**
         * Bước 1:
         * Tạo thông báo cho đúng nhân viên có ca bị ảnh hưởng.
         */
        String insertNotificationSql
                = "INSERT INTO Notifications "
                + "(recipientID, recipientType, type, message, isRead) "
                + "SELECT DISTINCT es.employeeID, 'staff', ?, "
                + "CONCAT(?, DATE_FORMAT(es.workDate, '%d/%m/%Y'), ?), 0 "
                + "FROM EmployeeShifts es "
                + "JOIN Employee e ON e.employeeID = es.employeeID "
                + "WHERE es.workDate = ? "
                + "AND es.status = ? "
                + "AND e.isActive = 1";

        /**
         * Bước 2:
         * Đổi trạng thái các ca của ngày được chọn.
         */
        String updateShiftSql
                = "UPDATE EmployeeShifts "
                + "SET status = ? "
                + "WHERE workDate = ? "
                + "AND status = ?";

        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            /**
             * Phải tạo thông báo trước khi đổi trạng thái.
             * Nếu UPDATE trước, hệ thống sẽ không còn tìm thấy oldStatus.
             */
            try (PreparedStatement ps = connection.prepareStatement(insertNotificationSql)) {
                ps.setString(1, notificationType);
                ps.setString(2, messageStart);
                ps.setString(3, messageEnd);
                ps.setDate(4, workDate);
                ps.setString(5, oldStatus);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(updateShiftSql)) {
                ps.setString(1, newStatus);
                ps.setDate(2, workDate);
                ps.setString(3, oldStatus);
                ps.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException ex) {
            rollbackTransaction();
            ex.printStackTrace();
            return false;
        } finally {
            restoreAutoCommit(oldAutoCommit);
        }
    }

    /**
     * Đổi trạng thái các ca thuộc một thứ trong tuần và gửi thông báo.
     */
    private boolean changeShiftsOnWeekday(
            String dayOfWeek,
            String oldStatus,
            String newStatus,
            String notificationType,
            String messageStart,
            String messageEnd,
            int specialDateStatusToSkip) {

        if (connection == null) {
            return false;
        }

        /**
         * Lịch ngày đặc biệt luôn được ưu tiên hơn lịch tuần:
         * - Đóng lịch tuần: bỏ qua ngày đặc biệt đang Mở (isClosed = 0).
         * - Mở lịch tuần: bỏ qua ngày đặc biệt đang Nghỉ (isClosed = 1).
         */
        String specialDateCondition
                = "AND NOT EXISTS ("
                + "SELECT 1 FROM BusinessSchedule bs "
                + "WHERE bs.specificDate = es.workDate "
                + "AND bs.isClosed = " + specialDateStatusToSkip
                + ") ";

        /**
         * Bước 1:
         * Tạo thông báo cho nhân viên có ca bị ảnh hưởng.
         */
        String insertNotificationSql
                = "INSERT INTO Notifications "
                + "(recipientID, recipientType, type, message, isRead) "
                + "SELECT DISTINCT es.employeeID, 'staff', ?, "
                + "CONCAT(?, DATE_FORMAT(es.workDate, '%d/%m/%Y'), ?), 0 "
                + "FROM EmployeeShifts es "
                + "JOIN Employee e ON e.employeeID = es.employeeID "
                + "WHERE es.workDate >= CURRENT_DATE "
                + "AND UPPER(DAYNAME(es.workDate)) = ? "
                + "AND es.status = ? "
                + "AND e.isActive = 1 "
                + specialDateCondition;

        /**
         * Bước 2:
         * Đổi trạng thái ca.
         */
        String updateShiftSql
                = "UPDATE EmployeeShifts es "
                + "SET es.status = ? "
                + "WHERE es.workDate >= CURRENT_DATE "
                + "AND UPPER(DAYNAME(es.workDate)) = ? "
                + "AND es.status = ? "
                + specialDateCondition;

        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(insertNotificationSql)) {
                ps.setString(1, notificationType);
                ps.setString(2, messageStart);
                ps.setString(3, messageEnd);
                ps.setString(4, dayOfWeek);
                ps.setString(5, oldStatus);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(updateShiftSql)) {
                ps.setString(1, newStatus);
                ps.setString(2, dayOfWeek);
                ps.setString(3, oldStatus);
                ps.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException ex) {
            rollbackTransaction();
            ex.printStackTrace();
            return false;
        } finally {
            restoreAutoCommit(oldAutoCommit);
        }
    }

    /**
     * Hoàn tác transaction khi có lỗi.
     */
    private void rollbackTransaction() {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            rollbackError.printStackTrace();
        }
    }

    /**
     * Trả kết nối về chế độ auto-commit ban đầu.
     */
    private void restoreAutoCommit(boolean oldAutoCommit) {
        try {
            connection.setAutoCommit(oldAutoCommit);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
