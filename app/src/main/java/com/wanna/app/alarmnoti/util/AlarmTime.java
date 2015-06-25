package com.wanna.app.alarmnoti.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by m on 2015-06-11.
 */
public class AlarmTime {
    public static final String TAG = "AlarmTime";
    public static final int ALARMTIME_MAX_YEARS = 3000 * 365 * 24 * 60;
    public static final int ALARMTIME_MAX_YEAR = 365 * 24 * 60;
    public static final int ALARMTIME_MAX_MONTH = 31 * 24 * 60;
    public static final int ALARMTIME_MAX_WEEK = 7 * 24 * 60;
    public static final int ALARMTIME_MAX_DAY = 24 * 60;
    public static final int ALARMTIME_DAY = 24;
    public static final int ALARMTIME_MAX_HOUR = 60;

    public int year;
    public int month;
    public int day;
    public int weekday;
    public int hour;
    public int minute;
    public long systemTime;

    public interface WeekDay {
        int getWeekDay(int day);
    }

    public AlarmTime(int year, int month, int day, int weekday, int hour, int minute, long systemTime) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.weekday = weekday;
        this.hour = hour;
        this.minute = minute;
        this.systemTime = systemTime;
    }

    public static AlarmTime getTime(long time) {
        return getTime(time, -1);
    }

    public static AlarmTime getTime(long systemTime, int weekDay) {
        Date date = new Date(systemTime);
        String hmDate = new SimpleDateFormat("yyyy:MM:dd:HH:mm").format(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return new AlarmTime(Integer.parseInt(hmDate.substring(0, 4)), Integer.parseInt(hmDate.substring(5, 7)), Integer.parseInt(hmDate.substring(8, 10)), weekDay,
                Integer.parseInt(hmDate.substring(11, 13)), Integer.parseInt(hmDate.substring(14, 16)), systemTime);
    }

    // 'Absolute Time' is (year + month + day + hour + minute) for comparing because of recurrence.
    public int getAbsoluteTime() {
        return this.year * ALARMTIME_MAX_YEAR + this.month * ALARMTIME_MAX_MONTH + this.day * ALARMTIME_MAX_DAY + this.hour * ALARMTIME_MAX_HOUR + this.minute;
    }

    public static long getSystemTime(int absoluteTime) {
        long systemTime = 0;
        int year = absoluteTime / ALARMTIME_MAX_YEAR;
        int restYear = absoluteTime % ALARMTIME_MAX_YEAR;
        int month = (restYear) / ALARMTIME_MAX_MONTH;
        int restMonth = (restYear) % ALARMTIME_MAX_MONTH;
        int day = (restMonth) / ALARMTIME_MAX_DAY;
        int restDay = (restMonth) % ALARMTIME_MAX_DAY;
        int hour = (restDay) / ALARMTIME_MAX_HOUR;
        int restHour = (restDay) % ALARMTIME_MAX_HOUR;
        String timeStr = String.format("%04d:%02d:%02d:%02d:%02d", year, month, day, hour, restHour);
        try {
            Date date = new SimpleDateFormat("yyyy:MM:dd:HH:mm").parse(timeStr);
            Log.d(TAG, timeStr);
            systemTime = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            return systemTime;
        }
    }

    public String substractTime(AlarmTime tagetTime) {
        int substractHour = this.hour;
        int substractMin = this.minute - tagetTime.minute;
        if (substractMin < 0) {
            substractMin += ALARMTIME_MAX_HOUR;
            substractHour -= 1;
        }
        substractHour = substractHour - tagetTime.hour;
        substractHour += substractHour < 0 ? ALARMTIME_DAY : 0;

        return String.format("%02d:%02d", substractHour, substractMin);
    }
}
