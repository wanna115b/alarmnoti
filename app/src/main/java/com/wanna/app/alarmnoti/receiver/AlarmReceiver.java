package com.wanna.app.alarmnoti.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.wanna.app.alarmnoti.R;
import com.wanna.app.alarmnoti.activity.AlarmListActivity;

public class AlarmReceiver extends BroadcastReceiver {
    private int YOURAPP_NOTIFICATION_ID;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, R.string.app_name, Toast.LENGTH_SHORT).show();

        showNotification(context, R.drawable.ic_launcher,
                R.string.alarm_message, R.string.alarm_message);
    }

    private void showNotification(Context context, int statusBarIconID,
                                  int statusBarTextID, int detailedTextID) {
        Intent contentIntent = new Intent(context, AlarmListActivity.class);
        PendingIntent theappIntent =
                PendingIntent.getBroadcast(context, 0, contentIntent, 0);
        CharSequence from = "Alarm Manager";
        CharSequence message = "The Alarm was fired";

        Notification notify =
                new Notification(statusBarIconID, null, System.currentTimeMillis());
        //notify.setLatestEventInfo(context, from, message, theappIntent);

        notify.setLatestEventInfo(context, "alimtitle", "hackjang", theappIntent);
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notify.vibrate = new long[]{200, 200, 500, 300};
        notify.ledARGB = Color.GREEN;
        notify.number++;

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(this.YOURAPP_NOTIFICATION_ID, notify);
    }

}
