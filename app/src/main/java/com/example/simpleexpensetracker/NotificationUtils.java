package com.example.simpleexpensetracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {

    public static final String CHANNEL_ID = "budget_warnings";
    private static final String CHANNEL_NAME = "Budget Warnings";
    private static final String CHANNEL_DESC = "Notifications for when budget limits are exceeded";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showBudgetWarningNotification(Context context, String message) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning_red)
                .setContentTitle("Budget Alert")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        int notificationId = (int) System.currentTimeMillis();

        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e("NotificationUtils", "Missing notification permission. Please request POST_NOTIFICATIONS.", e);
        }
    }

    public static void showWelcomeNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Use a different request code (e.g., 1) to ensure the PendingIntent is unique
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round) // Use your app's icon
                .setContentTitle("Welcome to Expense Tracker!")
                .setContentText("Tap to get started and manage your finances.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Use a fixed ID for the welcome notification so you don't accidentally create multiple
        int notificationId = 101;

        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e("NotificationUtils", "Missing notification permission for welcome message.", e);
        }
    }
}
