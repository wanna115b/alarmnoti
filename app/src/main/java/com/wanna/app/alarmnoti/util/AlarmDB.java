package com.wanna.app.alarmnoti.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmDB {

    private static final String DATABASE_NAME = "alarmoff.db";
    private static final String DATABASE_TABLE = "contents";
    private static final int DATABASE_VERSION = 1;

    public static final String KEY_ALARM_ID = "_id";
    public static final String KEY_ALARM_CALENDAR_EVENT_ID = "calendar_event_id";
    public static final String KEY_ALARM_OFF = "off";
    public static final String KEY_ALARM_TITLE = "title";
    public static final String KEY_ALARM_START_TIME = "start_time";
    public static final String KEY_ALARM_END_TIME = "end_time";
    public static final String KEY_ALARM_RECURRENCE = "recurrence";

    public static final int COLUMN_INDEX_ALARM_ID = 0;
    public static final int COLUMN_INDEX_ALARM_CALENDAR_EVENT_ID = 1;
    public static final int COLUMN_INDEX_ALARM_OFF = 2;
    public static final int COLUMN_INDEX_ALARM_TITLE = 3;
    public static final int COLUMN_INDEX_ALARM_START_TIME = 4;
    public static final int COLUMN_INDEX_ALARM_END_TIME = 5;
    public static final int COLUMN_INDEX_ALARM_RECURRENCE = 6;

    private static final String TAG = "AlarmDB";
    private static final String STACK_TRACE = "\nThe stack trace is:";

    private Context mContext;
    private AlarmDBHelper mAlarmDBHelper;
    private SQLiteDatabase mDb;

    private final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_TABLE + " ("
            + KEY_ALARM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_ALARM_CALENDAR_EVENT_ID + " TINYTEXT, "
            + KEY_ALARM_OFF + " BOOLEAN, "
            + KEY_ALARM_TITLE + " TINYTEXT,"
            + KEY_ALARM_START_TIME + " INTEGER, "
            + KEY_ALARM_END_TIME + " INTEGER, "
            + KEY_ALARM_RECURRENCE + " TINYTEXT);";

    private static final String DATABASE_SHOW_TABLE = "SHOW TABLES LIKE " + DATABASE_TABLE;

    public AlarmDB(Context context) {
        mContext = context;
    }

    public AlarmDB open() throws SQLException {
        mAlarmDBHelper = new AlarmDBHelper(mContext);
        mDb = mAlarmDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void createAlarm(long startTime, long endTime, String recurrence, String calendarEventId) {
        createAlarm(calendarEventId, startTime, endTime, recurrence);
    }

    public void createAlarm(String title, long startTime, long endTime, String recurrence, String calendarEventId) {
        if (mDb == null) {
            Log.e(TAG, "in createAlarm(), mDb is null~!!");
            return;
        }

        Cursor cursor = fetchAlarmByCalendarEventId(calendarEventId);
        if (cursor.getCount() > 0) {
            return;
        }

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ALARM_CALENDAR_EVENT_ID, calendarEventId);
        initialValues.put(KEY_ALARM_OFF, true);
        initialValues.put(KEY_ALARM_TITLE, title);
        initialValues.put(KEY_ALARM_START_TIME, startTime);
        initialValues.put(KEY_ALARM_END_TIME, endTime);
        initialValues.put(KEY_ALARM_RECURRENCE, recurrence);
        mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public long createAlarm(long startTime, long endTime, String recurrence) {
        return createAlarm("", startTime, endTime, recurrence);
    }

    public long createAlarm(String title, long startTime, long endTime, String recurrence) {
        if (mDb == null) {
            Log.e(TAG, "in createAlarm(), mDb is null~!!");
            return 0;
        }

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ALARM_CALENDAR_EVENT_ID, -1);
        initialValues.put(KEY_ALARM_OFF, true);
        initialValues.put(KEY_ALARM_TITLE, title);
        initialValues.put(KEY_ALARM_START_TIME, startTime);
        initialValues.put(KEY_ALARM_END_TIME, endTime);
        initialValues.put(KEY_ALARM_RECURRENCE, recurrence);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteAlarm(long rowId) {
        if (mDb == null) {
            Log.e(TAG, "in deleteAlarm(), mDb is null~!!");
            return false;
        }

        return mDb.delete(DATABASE_TABLE, KEY_ALARM_ID + "=" + rowId, null) > 0;
    }

    public boolean deleteAllAlarm() {
        if (mDb == null) {
            Log.e(TAG, "in deleteAllAlarm(), mDb is null~!!");
            return false;
        }

        return mDb.delete(DATABASE_TABLE, null, null) == 0;
    }

    public int getAlarmCount() {
        if (mDb == null) {
            Log.e(TAG, "in getAlarmCount(), mDb is null~!!");
            return 0;
        }

        Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_ALARM_ID}, null, null, null, null, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }

        return count;
    }

    public Cursor fetchAllAlarm() {
        if (mDb == null) {
            Log.e(TAG, "in fetchAllAlarm(), mDb is null~!!");
            return null;
        }

        return mDb.query(DATABASE_TABLE, new String[]{KEY_ALARM_ID, KEY_ALARM_CALENDAR_EVENT_ID, KEY_ALARM_OFF, KEY_ALARM_TITLE, KEY_ALARM_START_TIME, KEY_ALARM_END_TIME, KEY_ALARM_RECURRENCE}, //, KEY_ALARM_VOLUME, KEY_ALARM_RINGER_MODE},
                null, null, null, null, null);
    }

    public Cursor fetchAlarmByCalendarEventId(String calendarEventId) throws SQLException {
        if (mDb == null) {
            Log.e(TAG, "in fetchAlarm(), mDb is null~!!");
            return null;
        }

        Cursor cursor = mDb.query(true, DATABASE_TABLE, new String[]{KEY_ALARM_ID, KEY_ALARM_CALENDAR_EVENT_ID, KEY_ALARM_OFF, KEY_ALARM_TITLE, KEY_ALARM_START_TIME, KEY_ALARM_END_TIME, KEY_ALARM_RECURRENCE}, //, KEY_ALARM_VOLUME, KEY_ALARM_RINGER_MODE},
                KEY_ALARM_CALENDAR_EVENT_ID + "= ?", new String[]{calendarEventId}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchAlarmById(long rowId) throws SQLException {
        if (mDb == null) {
            Log.e(TAG, "in fetchAlarm(), mDb is null~!!");
            return null;
        }

        Cursor cursor = mDb.query(true, DATABASE_TABLE, new String[]{KEY_ALARM_ID, KEY_ALARM_CALENDAR_EVENT_ID, KEY_ALARM_OFF, KEY_ALARM_TITLE, KEY_ALARM_START_TIME, KEY_ALARM_END_TIME, KEY_ALARM_RECURRENCE}, //, KEY_ALARM_VOLUME, KEY_ALARM_RINGER_MODE},
                KEY_ALARM_ID + "=" + rowId, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public boolean updateAlarm(long rowId, boolean off, String title, int day, long startTime, long endTime, String recurrence) {
        if (mDb == null) {
            Log.e(TAG, "in updateAlarm(), mDb is null~!!");
            return false;
        }
        ContentValues args = new ContentValues();
        args.put(KEY_ALARM_CALENDAR_EVENT_ID, -1);
        args.put(KEY_ALARM_OFF, off);
        args.put(KEY_ALARM_TITLE, title);
        args.put(KEY_ALARM_START_TIME, startTime);
        args.put(KEY_ALARM_END_TIME, endTime);

        return mDb.update(DATABASE_TABLE, args, KEY_ALARM_ID + "=" + rowId, null) > 0;
    }

    private class AlarmDBHelper extends SQLiteOpenHelper {
        AlarmDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DATABASE_CREATE);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() + STACK_TRACE);
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
                onCreate(db);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage() + STACK_TRACE);
                e.printStackTrace();
            }
        }
    }
}