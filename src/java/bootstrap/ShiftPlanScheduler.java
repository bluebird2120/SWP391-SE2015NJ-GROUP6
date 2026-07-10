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

        // Sau đó chạy daily lúc 00:05.
        long initialDelaySec = computeInitialDelayToNextRun();
        executor.scheduleAtFixedRate(
                new ShiftPlanTask(),
                initialDelaySec,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );
        System.out.println("[ShiftPlanScheduler] Started. Next run in " + initialDelaySec + "s");
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.toLocalDate().plusDays(1).atTime(0, 5, 0);
        long delay = Duration.between(now, next).getSeconds();
        return Math.max(60L, delay); // tối thiểu 60s tránh edge case ngay sát mốc
    }
}
