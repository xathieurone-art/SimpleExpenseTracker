package com.example.simpleexpensetracker;

import android.content.Context;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    private static final String WORK_TAG = "daily_notification_work";

    public static void scheduleDailyNotification(Context context) {
        Calendar currentTime = Calendar.getInstance();
        Calendar dueTime = Calendar.getInstance();
        dueTime.set(Calendar.HOUR_OF_DAY, 20);
        dueTime.set(Calendar.MINUTE, 0);
        dueTime.set(Calendar.SECOND, 0);

        if (dueTime.before(currentTime)) {
            dueTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        long initialDelay = dueTime.getTimeInMillis() - currentTime.getTimeInMillis();

        PeriodicWorkRequest dailyWorkRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 1, TimeUnit.DAYS)
                        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                        .addTag(WORK_TAG)
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
        );
    }

    public static void cancelDailyNotification(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG);
    }
}
