package com.wanna.app.alarmnoti.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.wanna.app.alarmnoti.receiver.AlarmReceiver;

import java.util.Date;

public class Alarm implements Parcelable {
    private static final String TAG = "Alarm";

    public static final int ALARM_DAY_INDEX_NONE = 0x00;
    public static final int ALARM_DAY_INDEX_MON = 0x001;
    public static final int ALARM_DAY_INDEX_TUE = 0x002;
    public static final int ALARM_DAY_INDEX_WED = 0x004;
    public static final int ALARM_DAY_INDEX_THU = 0x008;
    public static final int ALARM_DAY_INDEX_FRI = 0x010;
    public static final int ALARM_DAY_INDEX_SAT = 0x020;
    public static final int ALARM_DAY_INDEX_SUN = 0x040;
    public static final int ALARM_DAY_INDEX_EVERY_YEAR = 0x080;
    public static final int ALARM_DAY_INDEX_EVERY_MONTH = 0x100;
    public static final int ALARM_DAY_INDEX_EVERY_DAY = ALARM_DAY_INDEX_MON | ALARM_DAY_INDEX_TUE | ALARM_DAY_INDEX_WED
            | ALARM_DAY_INDEX_THU | ALARM_DAY_INDEX_FRI | ALARM_DAY_INDEX_SAT | ALARM_DAY_INDEX_SUN;

    public static final String ALARM_MON = "MON";
    public static final String ALARM_TUE = "TUE";
    public static final String ALARM_WED = "WED";
    public static final String ALARM_THU = "THU";
    public static final String ALARM_FRI = "FRI";
    public static final String ALARM_SAT = "SAT";
    public static final String ALARM_SUN = "SUN";
    public static final String ALARM_EVERY_YEAR = "Every Year";
    public static final String ALARM_EVERY_MONTH = "Every Month";

    public static final String ALARM_CALENDAR_MON = "MO";
    public static final String ALARM_CALENDAR_TUE = "TU";
    public static final String ALARM_CALENDAR_WED = "WE";
    public static final String ALARM_CALENDAR_THU = "TH";
    public static final String ALARM_CALENDAR_FRI = "FR";
    public static final String ALARM_CALENDAR_SAT = "SA";
    public static final String ALARM_CALENDAR_SUN = "SU";
    public static final String ALARM_CALENDAR_EVERY_YEAR = "[\"RRULE:FREQ=YEARLY\"]";
    public static final String ALARM_CALENDAR_EVERY_MONTH = "[\"RRULE:FREQ=MONTHLY\"]";
    public static final String ALARM_CALENDAR_EVERY_WEEK = "[\"RRULE:FREQ=WEEKLY";
    public static final String ALARM_CALENDAR_EVERY_DAY = "[\"RRULE:FREQ=DAILY\"]";

    private static final String INTENT_ACTION = "com.wanna.app.alarmnoti.alarm";

    public long id;
    public String calendarId;
    public String calendarTitle;
    public String calendarEventId;
    public boolean alarmOff;
    public String title;
    public long startTime;
    public long endTime;
    public long day;
    public int recurrence;

    public Alarm() {
    }

    public Alarm(long id, String calendarId, String calendarTitle, String calendarEventId, String title, long startTime, long endTime, long day, int recurrence) {
        this.id = id;
        this.calendarId = calendarId;
        this.calendarTitle = calendarTitle;
        this.calendarEventId = calendarEventId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.recurrence = recurrence;
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Alarm(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(calendarId);
        dest.writeString(calendarTitle);
        dest.writeString(calendarEventId);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeLong(day);
        dest.writeInt(recurrence);
        dest.writeString(title);
    }

    private void readFromParcel(Parcel in) {
        id = in.readLong();
        calendarId = in.readString();
        calendarTitle = in.readString();
        calendarEventId = in.readString();
        startTime = in.readLong();
        endTime = in.readLong();
        day = in.readLong();
        recurrence = in.readInt();
        title = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Alarm createFromParcel(Parcel in) {
            return new Alarm(in);
        }

        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };

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

    public static int convertRecurrence(String recurrence) {
        if (recurrence == null) {
            return 0;
        }

        if (recurrence.equals(ALARM_CALENDAR_EVERY_YEAR) == true) {
            return ALARM_DAY_INDEX_EVERY_YEAR;
        } else if (recurrence.equals(ALARM_CALENDAR_EVERY_MONTH) == true) {
            return ALARM_DAY_INDEX_EVERY_MONTH;
        } else if (recurrence.equals(ALARM_CALENDAR_EVERY_DAY) == true) {
            return ALARM_DAY_INDEX_EVERY_DAY;
        } else if (recurrence.compareTo(ALARM_CALENDAR_EVERY_WEEK) > 0) {
            String every = recurrence.substring(ALARM_CALENDAR_EVERY_WEEK.length(), recurrence.length());
            int day = 0;
            day = day | (every.indexOf(ALARM_CALENDAR_SUN) > 0 ? ALARM_DAY_INDEX_SUN : 0);
            day = day | (every.indexOf(ALARM_CALENDAR_MON) > 0 ? ALARM_DAY_INDEX_MON : 0);
            day = day | (every.indexOf(ALARM_CALENDAR_TUE) > 0 ? ALARM_DAY_INDEX_TUE : 0);
            day = day | (every.indexOf(ALARM_CALENDAR_WED) > 0 ? ALARM_DAY_INDEX_WED : 0);
            day = day | (every.indexOf(ALARM_CALENDAR_THU) > 0 ? ALARM_DAY_INDEX_THU : 0);
            day = day | (every.indexOf(ALARM_CALENDAR_FRI) > 0 ? ALARM_DAY_INDEX_FRI : 0);
            day = day | (every.indexOf(ALARM_CALENDAR_SAT) > 0 ? ALARM_DAY_INDEX_SAT : 0);
            return day;
        } else {
            Log.e(TAG, String.format("in convertRecurrence(), incorrect recurrence : %s ~!!", recurrence));
            return ALARM_DAY_INDEX_NONE;
        }
    }

    public static String convertRecurrence(int recurrence) {
        StringBuffer sb = new StringBuffer();

        if ((recurrence & ALARM_DAY_INDEX_MON) > 0) {
            sb.append(ALARM_MON + ", ");
        }
        if ((recurrence & ALARM_DAY_INDEX_TUE) > 0) {
            sb.append(ALARM_TUE + ", ");
        }
        if ((recurrence & ALARM_DAY_INDEX_WED) > 0) {
            sb.append(ALARM_WED + ", ");
        }
        if ((recurrence & ALARM_DAY_INDEX_THU) > 0) {
            sb.append(ALARM_THU + ", ");
        }
        if ((recurrence & ALARM_DAY_INDEX_FRI) > 0) {
            sb.append(ALARM_FRI + ", ");
        }
        if ((recurrence & ALARM_DAY_INDEX_SAT) > 0) {
            sb.append(ALARM_SAT + ", ");
        }
        if ((recurrence & ALARM_DAY_INDEX_SUN) > 0) {
            sb.append(ALARM_SUN + ", ");
        }
        if ((recurrence & ALARM_DAY_INDEX_EVERY_YEAR) > 0) {
            sb.append(ALARM_EVERY_YEAR + ", ");
        }
        if ((recurrence & ALARM_DAY_INDEX_EVERY_MONTH) > 0) {
            sb.append(ALARM_EVERY_MONTH + ", ");
        }

        if (sb.length() > 0) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    public static int getWeekDayFromCalendarDay(int day) {
        switch (day) {
            case 1:
                return Alarm.ALARM_DAY_INDEX_SUN;
            case 2:
                return Alarm.ALARM_DAY_INDEX_MON;
            case 3:
                return Alarm.ALARM_DAY_INDEX_TUE;
            case 4:
                return Alarm.ALARM_DAY_INDEX_WED;
            case 5:
                return Alarm.ALARM_DAY_INDEX_THU;
            case 6:
                return Alarm.ALARM_DAY_INDEX_FRI;
            case 7:
                return Alarm.ALARM_DAY_INDEX_SAT;
            default:
                Log.e(TAG, String.format("in parseDayFromCalendarDay(), incorrect day %d ~!!", day));
                return Alarm.ALARM_DAY_INDEX_NONE;
        }
    }

    public static int getNextWeekDay(int day) {
        switch (day) {
            case ALARM_DAY_INDEX_SAT:
                return Alarm.ALARM_DAY_INDEX_SUN;
            case ALARM_DAY_INDEX_SUN:
                return Alarm.ALARM_DAY_INDEX_MON;
            case ALARM_DAY_INDEX_MON:
                return Alarm.ALARM_DAY_INDEX_TUE;
            case ALARM_DAY_INDEX_TUE:
                return Alarm.ALARM_DAY_INDEX_WED;
            case ALARM_DAY_INDEX_WED:
                return Alarm.ALARM_DAY_INDEX_THU;
            case ALARM_DAY_INDEX_THU:
                return Alarm.ALARM_DAY_INDEX_FRI;
            case ALARM_DAY_INDEX_FRI:
                return Alarm.ALARM_DAY_INDEX_SAT;
            default:
                Log.e(TAG, String.format("in getNextDay(), incorrect day %d ~!!", day));
                return Alarm.ALARM_DAY_INDEX_NONE;
        }
    }

    public boolean sendAlarm(Context context, boolean isStartTime) {
        Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(Constant.INTENT_ALARM_OFF, isStartTime);
        intent.putExtra(Constant.INTENT_ALARM, this);
        PendingIntent appIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT); // have to write 'PendingIntent.FLAG_UPDATE_CURRENT'
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.d(TAG, String.format("int sendAlarm(Context context, boolean startTime) start? %s  -  start:%s, end:%s", isStartTime, new Date(this.startTime), new Date(this.endTime)));
        //am.cancel(appIntent);
        am.set(AlarmManager.RTC_WAKEUP, isStartTime ? this.startTime : this.endTime, appIntent);

        return isStartTime;
    }

    public boolean sendAlarm(Context context) {
        long currentTime = System.currentTimeMillis();
        return sendAlarm(context, this.startTime < this.endTime) == false;
    }
}