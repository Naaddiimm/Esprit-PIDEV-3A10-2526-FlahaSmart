package scheduler;

import services.NotificationService;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationScheduler {
    public static void startDailyNotification() {
        Timer timer = new Timer("NotificationScheduler", true);
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, 9);
        target.set(Calendar.MINUTE, 30);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (target.getTime().before(new Date())) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        long initialDelay = target.getTimeInMillis() - System.currentTimeMillis();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new NotificationService().checkAndSendNotifications();
            }
        }, initialDelay, 24 * 60 * 60 * 1000);
    }
}