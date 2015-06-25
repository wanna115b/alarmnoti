package com.wanna.app.alarmnoti.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import com.wanna.app.alarmnoti.R;
import com.wanna.app.alarmnoti.activity.AlarmListActivity;
import com.wanna.app.alarmnoti.util.Alarm;
import com.wanna.app.alarmnoti.util.AlarmDB;
import com.wanna.app.alarmnoti.util.Constant;
import com.wanna.app.alarmnoti.util.LineUpAlarm;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String TAG = "onReceive";

    private int YOURAPP_NOTIFICATION_ID = 1234567890;
    //private LineUpAlarm mLineUpAlarm;
    private AlarmDB mAlarmDB;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, R.string.app_name, Toast.LENGTH_SHORT).show();
        boolean alarmOff = intent.getBooleanExtra(Constant.INTENT_ALARM_OFF, false);
        Alarm alarm = intent.getParcelableExtra(Constant.INTENT_ALARM);
        String tempAction = intent.getAction();
        Log.d(TAG, String.format("in onReceive() intent:%s, alarmOff: %s, action:%s", intent, alarmOff, tempAction));
        if (alarmOff == true) {
            alarm.sendAlarm(context, false);
        } else {
            mAlarmDB = (mAlarmDB == null) ? new AlarmDB(context).open() : mAlarmDB;
            alarm = new LineUpAlarm(context, mAlarmDB).findNextEvent();
            alarmOff = alarm.sendAlarm(context);
        }

        //showNotification(context, R.drawable.ic_launcher, R.string.alarm_message, alarmOff ? R.string.alarm_off_on : R.string.alarm_off_off);
        showNotification(context, R.drawable.ic_launcher, (alarm.calendarTitle == null) ? alarm.title : alarm.calendarTitle, context.getString(alarmOff ? R.string.alarm_off_on : R.string.alarm_off_off));
    }

    private void showNotification(Context context, int statusBarIconID,
                                  int statusBarTextID, int detailedTextID) {
        showNotification(context, statusBarIconID, context.getString(statusBarTextID), context.getString(detailedTextID));
    }

    private void showNotification(Context context, int statusBarIconID,
                                  String statusBarTextID, String detailedTextID) {
        Intent contentIntent = new Intent(context, AlarmListActivity.class);
        PendingIntent theappIntent = PendingIntent.getBroadcast(context, 0, contentIntent, 0);
        Notification.Builder mBuilder = new Notification.Builder(context).setSmallIcon(statusBarIconID).setContentTitle(statusBarTextID).setContentText(detailedTextID);
        Notification notify = mBuilder.build();
        mBuilder.setContentIntent(theappIntent);

        //notify.setLatestEventInfo(context, from, message, theappIntent);

        //notify.setLatestEventInfo(context, context.getApplicationContext()., "hackjang", theappIntent);
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        //notify.vibrate = new long[]{200, 200, 500, 300};
        notify.ledARGB = Color.GREEN;
        //notify.sound = Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, "1");//ringURI;
        //notify.sound=Uri.parse("file:/system/media/audio/alarms/Alarm_Beep_01.ogg");
        //Uri ringtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context ,RingtoneManager.TYPE_NOTIFICATION);
        //Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
        //ringtone.play();
        //notify.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        notify.number = 1;

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(this.YOURAPP_NOTIFICATION_ID);
        nm.notify(this.YOURAPP_NOTIFICATION_ID, notify);
    }
}
