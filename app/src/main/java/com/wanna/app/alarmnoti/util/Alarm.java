package com.wanna.app.alarmnoti.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.wanna.app.alarmnoti.receiver.AlarmReceiver;

public class Alarm {
    public static final int ALARM_DAY_NONE = 0x00;
    public static final int ALARM_DAY_MON = 0x001;
    public static final int ALARM_DAY_TUE = 0x004;
    public static final int ALARM_DAY_WED = 0x008;
    public static final int ALARM_DAY_THU = 0x010;
    public static final int ALARM_DAY_FRI = 0x020;
    public static final int ALARM_DAY_SAT = 0x040;
    public static final int ALARM_DAY_SUN = 0x080;
    public static final int ALARM_DAY_HOL = 0x100;
    public static final int ALARM_DAY_EVERY_YEAR = 0x200;
    public static final int ALARM_DAY_EVERY_MONTH = 0x400;
    public static final int ALARM_DAY_EVERY_WEEK = 0x800;

    public long id;
    public String calendarEventId;
    public long startTime;
    public long endTime;
    public long day;
    public String recurrence;
    public String title;

    public static void makeAlarm(Context context, long time) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent appIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        //Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(System.currentTimeMillis());
        //calendar.add(Calendar.SECOND, 1); // 1초 뒤에 발생..

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), appIntent);
        am.set(AlarmManager.RTC_WAKEUP, time, appIntent);
    }

    public int convertRecurrence(String recurrence) {
        if (recurrence == null) {
            return 0;
        }

        if( recurrence.equals("[\"RRULE:FREQ=YEARLY\"]") == true) {
            return ALARM_DAY_EVERY_YEAR;
        } else {
            return ALARM_DAY_EVERY_MONTH;
        }
    }
}