package com.example.simpleexpensetracker.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.sendNotification(
                context,
                "💰 Daily Reminder",
                "Don’t forget to log your expenses today!"
        );
    }
}
