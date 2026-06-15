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
        MonthlyShiftPlanDAO planDao = new MonthlyShiftPlanDAO();
        NotificationDAO notifDao    = new NotificationDAO();
        EmployeeShiftDAO shiftDao   = new EmployeeShiftDAO();

        List<MonthlyShiftPlan> pending = planDao.listPending();
        for (MonthlyShiftPlan p : pending) {
            LocalDate firstOfMonth = LocalDate.of(p.getEffectiveYear(), p.getEffectiveMonth(), 1);
            LocalDate notifyDate   = firstOfMonth.minusDays(3);

            if (MonthlyShiftPlan.DRAFT.equals(p.getStatus()) && !today.isBefore(notifyDate)) {
                if (insertNotification(notifDao, p)) {
                    planDao.updateStatus(p.getPlanID(), MonthlyShiftPlan.NOTIFIED);
                    p.setStatus(MonthlyShiftPlan.NOTIFIED); // cho phép apply ngay trong cùng vòng
                }
            }

            if (MonthlyShiftPlan.NOTIFIED.equals(p.getStatus()) && !today.isBefore(firstOfMonth)) {
                if (shiftDao.hasAnyShiftInMonth(p.getEmployeeID(), p.getEffectiveYear(), p.getEffectiveMonth())) {
                    // Đã có người gán thủ công vào tháng này → bỏ qua, không apply, đánh CANCELLED
                    System.out.println("[ShiftPlanTask] Skip apply — conflict planID=" + p.getPlanID());
                    planDao.updateStatus(p.getPlanID(), MonthlyShiftPlan.CANCELLED);
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
        Notifications n = new Notifications();
        n.setRecipientID(p.getEmployeeID());
        n.setRecipientType("staff");
        n.setType("shift_plan");
        n.setMessage(buildMessage(p));
        n.setIsRead(0);
        return dao.insert(n) > 0;
    }

    private String buildMessage(MonthlyShiftPlan p) {
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
