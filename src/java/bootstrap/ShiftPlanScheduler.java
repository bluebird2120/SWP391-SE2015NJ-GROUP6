package bootstrap;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class ShiftPlanScheduler implements ServletContextListener {

    private ScheduledExecutorService executor;
    //Mỗi khi app khởi động là hàm này sẽ chạy cùng
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "shift-plan-scheduler");
            t.setDaemon(true);
            return t;
        });
        //Vừa khởi động web sẽ kiểm tra kế hoạch của tháng
        executor.execute(new ShiftPlanTask());
        //Hẹn lịch chạy mỗi ngày (lịch làm việc)
        long initialDelaySec = computeInitialDelayToNextRun();
        executor.scheduleAtFixedRate(
                new ShiftPlanTask(),
                initialDelaySec,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );
        System.out.println("[ShiftPlanScheduler] Started. Next run in " + initialDelaySec + "s");

        //Chạy lịch order
        long notifyDelaySec = computeDelayTo(6, 0);
        executor.scheduleAtFixedRate(
                new DailyReservationNotifyTask(),
                notifyDelaySec,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );
        System.out.println("[ShiftPlanScheduler] DailyReservationNotifyTask scheduled. Next run in " + notifyDelaySec + "s");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
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
    private long computeInitialDelayToNextRun() {
        return computeDelayTo(0, 5);
    }

    //Tính số giầy còn lại từ thời điểm hiện tại tới thời điểm task tự động chạy
    private long computeDelayTo(int hour, int minute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.toLocalDate().atTime(hour, minute, 0);
        if (!now.isBefore(target)) {
            target = target.plusDays(1);
        }
        long delay = Duration.between(now, target).getSeconds();
        return Math.max(60L, delay);
    }
}
