package com.example.simpleexpensetracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.Random;

public class NotificationWorker extends Worker {

    private static final String CHANNEL_ID = "DailyReminderChannel";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        String[] messages = {
                "Don't forget to track your expenses today to stay on budget!",
                "Every entry counts! Have you logged your recent spending?",
                "Keeping tabs on your money is the key to financial success. Add your expenses now!",
                "Check-in time! How's your budget looking today? Log your expenses to see."
        };

        Random random = new Random();
        String randomMessage = messages[random.nextInt(messages.length)];

        DatabaseHelper db = new DatabaseHelper(context);
        db.addNotification(randomMessage, "reminder");

        sendSystemNotification(context, "Expense Tracker Reminder", randomMessage);

        return Result.success();
    }

    private void sendSystemNotification(Context context, String title, String content) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Periodic Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
}
