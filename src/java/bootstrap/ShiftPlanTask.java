package bootstrap;

import dal.EmployeeShiftDAO;
import dal.MonthlyShiftPlanDAO;
import dal.NotificationDAO;
import model.MonthlyShiftPlan;
import model.Notifications;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

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

        try (MonthlyShiftPlanDAO planDao = new MonthlyShiftPlanDAO(); //lấy kế hoạch ca tháng.
                NotificationDAO notifDao = new NotificationDAO(); //Tạo thông báo
                EmployeeShiftDAO shiftDao = new EmployeeShiftDAO()) { //kiểm tra và tạo ca làm thật.

            List<MonthlyShiftPlan> pending = planDao.listPending(); //lấy danh sách kế hoạch ca tháng đang chờ xử lý.
            for (MonthlyShiftPlan p : pending) {
                LocalDate firstOfMonth = LocalDate.of(p.getEffectiveYear(), p.getEffectiveMonth(), 1);
                LocalDate notifyDate = firstOfMonth.minusDays(3);

                if (MonthlyShiftPlan.DRAFT.equals(p.getStatus()) && !today.isBefore(notifyDate)) {
                    if (insertNotification(notifDao, p)) {
                        planDao.updateStatus(p.getPlanID(), MonthlyShiftPlan.NOTIFIED);
                        p.setStatus(MonthlyShiftPlan.NOTIFIED);
                    }
                }

                if (MonthlyShiftPlan.NOTIFIED.equals(p.getStatus()) && !today.isBefore(firstOfMonth)) {
                    //nhân viên đã có bất kỳ ca nào trong cả tháng chưa.
                    if (shiftDao.hasAnyShiftInMonth(p.getEmployeeID(), p.getEffectiveYear(), p.getEffectiveMonth())) {
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