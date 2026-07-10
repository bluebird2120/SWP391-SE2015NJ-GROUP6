package bootstrap;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * NGHIỆP VỤ: Bộ lập lịch tự động cho MonthlyShiftPlan.
 *
 * Daily scheduler chạy 00:05 mỗi ngày để xử lý MonthlyShiftPlan:
 * - DRAFT + today >= ngày 1 tháng hiệu lực - 3 ngày:
 *   gửi thông báo lịch tháng cho nhân viên và đổi trạng thái thành NOTIFIED.
 * - NOTIFIED + today >= ngày 1 tháng hiệu lực:
 *   tạo EmployeeShifts theo tháng và đổi trạng thái thành APPLIED.
 *
 * Thread daemon để không block JVM shutdown. Có 1 lần chạy ngay khi context
 * init để bắt kịp các plan đã quá hạn (server bị tắt qua mốc).
 */
@WebListener
public class ShiftPlanScheduler implements ServletContextListener {

    private ScheduledExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Tạo một background thread riêng cho việc quét kế hoạch ca tháng.
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "shift-plan-scheduler");
            t.setDaemon(true);
            return t;
        });

        // Chạy ngay 1 lần khi server vừa khởi động (catch-up).
        executor.execute(new ShiftPlanTask());

        // Sau đó chạy daily lúc 00:05 (ShiftPlanTask).
        long initialDelaySec = computeInitialDelayToNextRun();
        executor.scheduleAtFixedRate(
                new ShiftPlanTask(),
                initialDelaySec,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );
        System.out.println("[ShiftPlanScheduler] Started. Next run in " + initialDelaySec + "s");

        // [THÔNG BÁO ĐẶT BÀN] Chạy DailyReservationNotifyTask lúc 06:00 mỗi ngày.
        // Gom tất cả đơn đặt bàn online (orderTime = hôm nay) chưa được thông báo
        // và gửi một thông báo tổng hợp cho toàn bộ lễ tân đang hoạt động.
        long notifyDelaySec = computeDelayTo(6, 0);
        executor.scheduleAtFixedRate(
                new DailyReservationNotifyTask(),
                notifyDelaySec,
                TimeUnit.DAYS.toSeconds(1),    //Đổi 1 ngày ra giây
                TimeUnit.SECONDS                        //đơn vị tính bằng giây
        );
        System.out.println("[ShiftPlanScheduler] DailyReservationNotifyTask scheduled. Next run in " + notifyDelaySec + "s");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Khi web app stop/redeploy thì shutdown scheduler để tránh thread treo.
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("[ShiftPlanScheduler] Stopped.");
    }

    /**
     * Số giây từ now đến 00:05 sáng mai (theo timezone JVM).
     */
    private long computeInitialDelayToNextRun() {
        return computeDelayTo(0, 5);
    }

    /**
     * Số giây từ now đến giờ:phút tiếp theo (hôm nay nếu chưa qua, ngày mai nếu đã qua).
     * Tối thiểu 60s để tránh edge case.
     */
    private long computeDelayTo(int hour, int minute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.toLocalDate().atTime(hour, minute, 0);
        if (!now.isBefore(target)) {
            target = target.plusDays(1); // đã qua mốc hôm nay → hẹn ngày mai
        }
        long delay = Duration.between(now, target).getSeconds();
        return Math.max(60L, delay);
    }
}