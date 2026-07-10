package bootstrap;

import dal.EmployeeShiftDAO;
import dal.MonthlyShiftPlanDAO;
import dal.NotificationDAO;
import model.MonthlyShiftPlan;
import model.Notifications;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

/**
 * NGHIỆP VỤ: Một lần chạy của scheduler xử lý kế hoạch phân ca tháng.
 *
 * Quét MonthlyShiftPlan và đẩy trạng thái:
 *   DRAFT     → NOTIFIED (today >= notifyDate = ngày 1 tháng - 3 ngày)
 *   NOTIFIED  → APPLIED  (today >= ngày 1 tháng — batch insert EmployeeShifts)
 *
 * Mỗi run tạo DAO mới (DBContext mở connection riêng) để tránh stale.
 */
public class ShiftPlanTask implements Runnable {

    @Override
    public void run() {
        long t0 = System.currentTimeMillis();
        try {
            process();
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("[ShiftPlanTask] done in " + (System.currentTimeMillis() - t0) + "ms");
        }
    }

    private void process() {
        LocalDate today = LocalDate.now();
        // Mỗi lần chạy tạo DAO mới để connection không bị stale giữa các lần scheduler chạy.
        MonthlyShiftPlanDAO planDao = new MonthlyShiftPlanDAO();
        NotificationDAO notifDao    = new NotificationDAO();
        EmployeeShiftDAO shiftDao   = new EmployeeShiftDAO();

        List<MonthlyShiftPlan> pending = planDao.listPending();
        for (MonthlyShiftPlan p : pending) {
            // firstOfMonth là ngày kế hoạch bắt đầu có hiệu lực.
            LocalDate firstOfMonth = LocalDate.of(p.getEffectiveYear(), p.getEffectiveMonth(), 1);
            // notifyDate là mốc trước 3 ngày để báo nhân viên biết lịch tháng mới.
            LocalDate notifyDate   = firstOfMonth.minusDays(3);

            if (MonthlyShiftPlan.DRAFT.equals(p.getStatus()) && !today.isBefore(notifyDate)) {
                // Gửi thông báo một lần rồi đổi DRAFT -> NOTIFIED để không gửi lặp lại.
                if (insertNotification(notifDao, p)) {
                    planDao.updateStatus(p.getPlanID(), MonthlyShiftPlan.NOTIFIED);
                    p.setStatus(MonthlyShiftPlan.NOTIFIED); // cho phép apply ngay trong cùng vòng
                }
            }

            if (MonthlyShiftPlan.NOTIFIED.equals(p.getStatus()) && !today.isBefore(firstOfMonth)) {
                // Đến tháng hiệu lực thì tạo ca thật trong EmployeeShifts.
                if (shiftDao.hasAnyShiftInMonth(p.getEmployeeID(), p.getEffectiveYear(), p.getEffectiveMonth())) {
                    // Đã có ca gán sẵn hoặc tự động tạo trước đó -> cập nhật thành APPLIED luôn thay vì CANCELLED
                    System.out.println("[ShiftPlanTask] Already has shifts — mark as APPLIED for planID=" + p.getPlanID());
                    planDao.updateStatus(p.getPlanID(), MonthlyShiftPlan.APPLIED);
                    continue;
                }
                int rows = shiftDao.assignMonth(p.getEmployeeID(), p.getTemplateID(),
                        p.getEffectiveYear(), p.getEffectiveMonth());
                if (rows > 0) {
                    planDao.updateStatus(p.getPlanID(), MonthlyShiftPlan.APPLIED);
                }
            }
        }
    }

    private boolean insertNotification(NotificationDAO dao, MonthlyShiftPlan p) {
        // Notification gửi cho đúng nhân viên trong kế hoạch tháng.
        Notifications n = new Notifications();
        n.setRecipientID(p.getEmployeeID());
        n.setRecipientType("staff");
        n.setType("shift_plan");
        n.setMessage(buildMessage(p));
        n.setIsRead(0);
        return dao.insert(n) > 0;
    }

    private String buildMessage(MonthlyShiftPlan p) {
        // Chuẩn hóa message để nhân viên nhìn thấy tháng, tên ca và giờ làm.
        Time st = p.getStartTime();
        Time et = p.getEndTime();
        String shiftName = p.getTemplateName() == null ? "?" : p.getTemplateName();
        String stStr = st == null ? "?" : st.toString();
        String etStr = et == null ? "?" : et.toString();
        return String.format(
                "Lịch ca tháng %02d/%d của bạn: ca %s (%s - %s), bắt đầu ngày 01/%02d/%d.",
                p.getEffectiveMonth(), p.getEffectiveYear(),
                shiftName, stStr, etStr,
                p.getEffectiveMonth(), p.getEffectiveYear()
        );
    }
}
