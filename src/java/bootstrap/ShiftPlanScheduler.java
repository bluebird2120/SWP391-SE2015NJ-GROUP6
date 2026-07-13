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

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "shift-plan-scheduler");
            t.setDaemon(true);
            return t;
        });

        executor.execute(new ShiftPlanTask());

        long initialDelaySec = computeInitialDelayToNextRun();
        executor.scheduleAtFixedRate(
                new ShiftPlanTask(),
                initialDelaySec,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );
        System.out.println("[ShiftPlanScheduler] Started. Next run in " + initialDelaySec + "s");

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
