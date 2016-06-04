package com.example.notesapplication.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Extends Broadcast Receiver to set the alarm after the device has been rebooted
 */
public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            /* Setting the alarm here */
            Intent alarmIntent = new Intent(context, OnAlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, alarmIntent, 0);

            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        }
    }
}
