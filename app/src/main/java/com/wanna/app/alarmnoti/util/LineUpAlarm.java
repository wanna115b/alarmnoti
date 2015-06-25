package com.wanna.app.alarmnoti.util;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by m on 2015-06-17.
 */
public class LineUpAlarm {
    private static final String TAG = "LineUpAlarm";

    private Context mContext;
    private AlarmDB mAlarmDb;

    public LineUpAlarm(Context context, AlarmDB alarmDb) {
        mContext = context;
        mAlarmDb = alarmDb;
    }

    public Alarm findNextEvent() {
        long today = new Date().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(today));
        AlarmTime targetAt = AlarmTime.getTime(today, Alarm.getWeekDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
        Alarm nextAlarm = null;
        int findPosition = -1;

        Cursor allCursor = mAlarmDb.fetchAllAlarm();
        findPosition = getFindPosition(targetAt, allCursor);
        if (findPosition != -1) {
            allCursor.moveToFirst();
            allCursor.move(findPosition);
            Log.d(TAG, String.format("found All Cursor:%d", findPosition));
            nextAlarm = setAlarm(allCursor);
        }

        return nextAlarm;
    }

    public Alarm setAlarm(Cursor cursor) {
        Alarm alarm = new Alarm();

        alarm.id = cursor.getLong(AlarmDB.COLUMN_INDEX_ALARM_ID);
        alarm.calendarId = cursor.getString(AlarmDB.COLUMN_INDEX_ALARM_CALENDAR_ID);
        alarm.calendarTitle = cursor.getString(AlarmDB.COLUMN_INDEX_ALARM_CALENDAR_TITLE);
        alarm.calendarEventId = cursor.getString(AlarmDB.COLUMN_INDEX_ALARM_CALENDAR_EVENT_ID);
        alarm.alarmOff = cursor.getInt(AlarmDB.COLUMN_INDEX_ALARM_OFF) > 0;
        alarm.title = cursor.getString(AlarmDB.COLUMN_INDEX_ALARM_TITLE);
        alarm.startTime = cursor.getLong(AlarmDB.COLUMN_INDEX_ALARM_START_TIME);
        alarm.endTime = cursor.getLong(AlarmDB.COLUMN_INDEX_ALARM_END_TIME);
        alarm.recurrence = cursor.getInt(AlarmDB.COLUMN_INDEX_ALARM_RECURRENCE);

        // set next time because of recurrence
        long today = new Date().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(today));
        AlarmTime targetAt = AlarmTime.getTime(today, Alarm.getWeekDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
        alarm.startTime = getSystemTimeWithTargetTime(targetAt, alarm.startTime, alarm.recurrence);
        alarm.endTime = getSystemTimeWithTargetTime(targetAt, alarm.endTime, alarm.recurrence);

//        StringBuffer sb = new StringBuffer();
//        for (int j = 0; j < cursor.getColumnCount(); j++) {
//            sb.append(String.format("in setAlarm() Cursor:%s : %s\n", cursor.getColumnName(j), cursor.getString(j)));
//        }
//        Log.d(TAG, sb.toString());
        Log.d(TAG, String.format("setAlarm() start:%s, end:%s", new Date(alarm.startTime), new Date(alarm.endTime)));
        //Toast.makeText(mContext, sb, Toast.LENGTH_SHORT).show();

        return alarm;
    }

    private long getSystemTimeWithTargetTime(AlarmTime targetAt, long systemTime, int recurrence) {
        int compareTime = targetAt.getAbsoluteTime();
        AlarmTime startAt = AlarmTime.getTime(systemTime);
        modifyWithTargetAlarmTime(recurrence, targetAt, startAt);
        int start = getAbsoluteTimeWithCompare(recurrence, startAt, compareTime);
        return AlarmTime.getSystemTime(start);
    }

    private int getFindPosition(AlarmTime targetAt, Cursor calendarCursor) {
        int compareTime = targetAt.getAbsoluteTime();
        int beforeCompare = AlarmTime.ALARMTIME_MAX_YEARS;
        int findPosition = -1;
        int start = 0;
        int end = 0;
        int recurrence = 0;
        int calendarCount = calendarCursor.getCount();

        for (int i = 0; i < calendarCount; i++, calendarCursor.moveToNext()) {
//            Log.d(TAG, String.format("%s : %d", calendarCursor.getColumnName(AlarmDB.COLUMN_INDEX_ALARM_ID), calendarCursor.getLong(AlarmDB.COLUMN_INDEX_ALARM_ID)));
//            Log.d(TAG, String.format("%s : %s", calendarCursor.getColumnName(AlarmDB.COLUMN_INDEX_ALARM_CALENDAR_TITLE), calendarCursor.getString(AlarmDB.COLUMN_INDEX_ALARM_CALENDAR_TITLE)));
//            Log.d(TAG, String.format("%s : %s", calendarCursor.getColumnName(AlarmDB.COLUMN_INDEX_ALARM_CALENDAR_EVENT_ID), calendarCursor.getString(AlarmDB.COLUMN_INDEX_ALARM_CALENDAR_EVENT_ID)));
//            Log.d(TAG, String.format("%s : %s", calendarCursor.getColumnName(AlarmDB.COLUMN_INDEX_ALARM_OFF), calendarCursor.getInt(AlarmDB.COLUMN_INDEX_ALARM_OFF) > 0));
//            Log.d(TAG, String.format("%s : %s", calendarCursor.getColumnName(AlarmDB.COLUMN_INDEX_ALARM_TITLE), calendarCursor.getString(AlarmDB.COLUMN_INDEX_ALARM_TITLE)));
//            Log.d(TAG, String.format("%s : %s", calendarCursor.getColumnName(AlarmDB.COLUMN_INDEX_ALARM_START_TIME), new Date(calendarCursor.getLong(AlarmDB.COLUMN_INDEX_ALARM_START_TIME))));
//            Log.d(TAG, String.format("%s : %s", calendarCursor.getColumnName(AlarmDB.COLUMN_INDEX_ALARM_END_TIME), new Date(calendarCursor.getLong(AlarmDB.COLUMN_INDEX_ALARM_END_TIME))));
//            Log.d(TAG, String.format("%s : %s", calendarCursor.getColumnName(AlarmDB.COLUMN_INDEX_ALARM_RECURRENCE), Alarm.convertRecurrence(calendarCursor.getInt(AlarmDB.COLUMN_INDEX_ALARM_RECURRENCE))));
//            Log.d(TAG, " ");

            recurrence = calendarCursor.getInt(AlarmDB.COLUMN_INDEX_ALARM_RECURRENCE);
            AlarmTime startAt = AlarmTime.getTime(calendarCursor.getLong(AlarmDB.COLUMN_INDEX_ALARM_START_TIME));
            AlarmTime endAt = AlarmTime.getTime(calendarCursor.getLong(AlarmDB.COLUMN_INDEX_ALARM_END_TIME));
            // There is no relationship with end time in google calendar (maybe they'll delete if event is ruined)
//            if ((time > startAt.systemTime && ((startAt.weekday & Alarm.ALARM_DAY_INDEX_EVERY_YEAR & Alarm.ALARM_DAY_INDEX_EVERY_MONTH & Alarm.ALARM_DAY_INDEX_EVERY_DAY) == 0)) ||
//                    (time > endAt.systemTime)) { // Delete past event with no recurrence and past event
//                Log.d(TAG, String.format("delete %s : %s", calendarCursor.getColumnName(AlarmDB.COLUMN_INDEX_ALARM_TITLE), calendarCursor.getString(AlarmDB.COLUMN_INDEX_ALARM_TITLE)));
//                continue;
//            }

            if (targetAt.systemTime < startAt.systemTime) {
                modifyWithTargetAlarmTime(recurrence, targetAt, startAt);
                start = getAbsoluteTimeWithCompare(recurrence, startAt, compareTime);
            } else { //Don't modify when event does not occur
                start = startAt.getAbsoluteTime();
            }

            if (start > compareTime && start < beforeCompare) {
                Log.d(TAG, String.format("in getFindPosition() : start %d", i));
                beforeCompare = start;
                findPosition = i;
                continue;
            }

            modifyWithTargetAlarmTime(recurrence, targetAt, endAt);
            end = getAbsoluteTimeWithCompare(recurrence, endAt, compareTime);
            if (start > end) {
                Log.d(TAG, String.format("%s : %s", calendarCursor.getColumnName(AlarmDB.COLUMN_INDEX_ALARM_TITLE), calendarCursor.getString(AlarmDB.COLUMN_INDEX_ALARM_TITLE)));
                continue;
            }
            if (end > compareTime && end < beforeCompare) {
                Log.d(TAG, String.format("in getFindPosition() : end %d", i));
                beforeCompare = end;
                findPosition = i;
            }
        }
        return findPosition;
    }

    private void modifyWithTargetAlarmTime(int recurrence, AlarmTime targetAt, AlarmTime startAt) {
        if ((recurrence & Alarm.ALARM_DAY_INDEX_EVERY_MONTH) > 0) {
            startAt.year = targetAt.year;
            startAt.month = targetAt.month;
        }

        if ((recurrence & Alarm.ALARM_DAY_INDEX_EVERY_DAY) > 0) { //weekday compare
            startAt.year = targetAt.year;
            startAt.month = targetAt.month;
            startAt.day = targetAt.day;

            if ((startAt.weekday & targetAt.weekday) > 0) { // same day with target
                startAt.day = targetAt.day;
                if (startAt.hour * 60 + startAt.minute < targetAt.hour * 60 + targetAt.minute) {
                    startAt.day += 7;
                }
            } else if (startAt.weekday > targetAt.weekday) { // after weekday from target
                int weekday = (startAt.weekday / targetAt.weekday) * targetAt.weekday; // clean small day than target
                int smallestWeekday = getSmallestWeekday(weekday);
                int dayDifferSquare = targetAt.weekday / smallestWeekday; // subtract day from target
                int dayDiffer = (int) Math.log(dayDifferSquare);
                startAt.day = targetAt.day + dayDiffer;
            } else if (startAt.weekday < targetAt.weekday) { // before weekday from target
                int smallestWeekday = getSmallestWeekday(startAt.weekday);
                int dayDifferSquare = targetAt.weekday / smallestWeekday; // subtract day from target
                int dayDiffer = (int) Math.log(dayDifferSquare);
                startAt.day = targetAt.day - dayDiffer + 7;
            }
        }

        if ((recurrence & Alarm.ALARM_DAY_INDEX_EVERY_DAY) == Alarm.ALARM_DAY_INDEX_EVERY_DAY) {
            startAt.year = targetAt.year;
            startAt.month = targetAt.month;
            startAt.day = targetAt.day;
        }
    }

    private int getSmallestWeekday(int weekday) { /* get smallest weekday */
        int smallestWeekday = 0;
        smallestWeekday = (weekday & Alarm.ALARM_DAY_INDEX_SUN) > 0 ? Alarm.ALARM_DAY_INDEX_SUN : 0;
        smallestWeekday = (weekday & Alarm.ALARM_DAY_INDEX_SAT) > 0 ? Alarm.ALARM_DAY_INDEX_SAT : 0;
        smallestWeekday = (weekday & Alarm.ALARM_DAY_INDEX_FRI) > 0 ? Alarm.ALARM_DAY_INDEX_FRI : 0;
        smallestWeekday = (weekday & Alarm.ALARM_DAY_INDEX_THU) > 0 ? Alarm.ALARM_DAY_INDEX_THU : 0;
        smallestWeekday = (weekday & Alarm.ALARM_DAY_INDEX_WED) > 0 ? Alarm.ALARM_DAY_INDEX_WED : 0;
        smallestWeekday = (weekday & Alarm.ALARM_DAY_INDEX_TUE) > 0 ? Alarm.ALARM_DAY_INDEX_TUE : 0;
        smallestWeekday = (weekday & Alarm.ALARM_DAY_INDEX_MON) > 0 ? Alarm.ALARM_DAY_INDEX_MON : 0;
        return smallestWeekday;
    }

    // 'Absolute Time' is (year + month + day + hour + minute) for comparing because of recurrence.
    private int getAbsoluteTimeWithCompare(int recurrence, AlarmTime startAt, int compare) {
        int start = startAt.getAbsoluteTime();
        if (start < compare) {
            start += (recurrence & Alarm.ALARM_DAY_INDEX_EVERY_YEAR) > 0 ? AlarmTime.ALARMTIME_MAX_YEAR :
                    (recurrence & Alarm.ALARM_DAY_INDEX_EVERY_MONTH) > 0 ? AlarmTime.ALARMTIME_MAX_MONTH :
                            (recurrence & Alarm.ALARM_DAY_INDEX_EVERY_DAY) == Alarm.ALARM_DAY_INDEX_EVERY_DAY ? AlarmTime.ALARMTIME_MAX_DAY : 0;
        }

        return start;
    }
}
