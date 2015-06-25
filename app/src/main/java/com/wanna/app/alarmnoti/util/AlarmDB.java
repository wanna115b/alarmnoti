package com.wanna.app.alarmnoti.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AlarmDB {

    private static final String DATABASE_NAME = "alarmnoti.db";
    private static final String DATABASE_TABLE = "contents";
    private static final int DATABASE_VERSION = 1;

    public static final String KEY_ALARM_ID = "_id";
    public static final String KEY_ALARM_CALENDAR_ID = "calendar_id";
    public static final String KEY_ALARM_CALENDAR_TITLE = "calendar_title";
    public static final String KEY_ALARM_CALENDAR_EVENT_ID = "calendar_event_id";
    public static final String KEY_ALARM_OFF = "off";
    public static final String KEY_ALARM_TITLE = "title";
    public static final String KEY_ALARM_START_TIME = "start_time";
    public static final String KEY_ALARM_END_TIME = "end_time";
    public static final String KEY_ALARM_RECURRENCE = "recurrence";

    public static final int COLUMN_INDEX_ALARM_ID = 0;
    public static final int COLUMN_INDEX_ALARM_CALENDAR_ID = 1;
    public static final int COLUMN_INDEX_ALARM_CALENDAR_TITLE = 2;
    public static final int COLUMN_INDEX_ALARM_CALENDAR_EVENT_ID = 3;
    public static final int COLUMN_INDEX_ALARM_OFF = 4;
    public static final int COLUMN_INDEX_ALARM_TITLE = 5;
    public static final int COLUMN_INDEX_ALARM_START_TIME = 6;
    public static final int COLUMN_INDEX_ALARM_END_TIME = 7;
    public static final int COLUMN_INDEX_ALARM_RECURRENCE = 8;

    private static final String TAG = "AlarmDB";
    private static final String STACK_TRACE = "\nThe stack trace is:";

    private Context mContext;
    private AlarmDBHelper mAlarmDBHelper;
    private SQLiteDatabase mDb;

    private final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_TABLE + " ("
            + KEY_ALARM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KEY_ALARM_CALENDAR_ID + " TINYTEXT,"
            + KEY_ALARM_CALENDAR_TITLE + " TINYTEXT, "
            + KEY_ALARM_CALENDAR_EVENT_ID + " TINYTEXT, "
            + KEY_ALARM_OFF + " BOOLEAN, "
            + KEY_ALARM_TITLE + " TINYTEXT,"
            + KEY_ALARM_START_TIME + " INTEGER, "
            + KEY_ALARM_END_TIME + " INTEGER, "
            + KEY_ALARM_RECURRENCE + " INTEGER);";

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

    public void createAlarm(String calendarId, String calendarTitle, String calendarEventId, String title, long startTime, long endTime, int recurrence) {
        if (mDb == null) {
            Log.e(TAG, "In createAlarm(), mDb is null~!!");
            return;
        }

        Cursor cursor = fetchAlarmByCalendarEventId(calendarEventId);
        if (cursor.getCount() > 0) {
            return;
        }

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ALARM_CALENDAR_ID, calendarId);
        initialValues.put(KEY_ALARM_CALENDAR_TITLE, calendarTitle);
        initialValues.put(KEY_ALARM_CALENDAR_EVENT_ID, calendarEventId);
        initialValues.put(KEY_ALARM_OFF, true);
        initialValues.put(KEY_ALARM_TITLE, title);
        initialValues.put(KEY_ALARM_START_TIME, startTime);
        initialValues.put(KEY_ALARM_END_TIME, endTime);
        initialValues.put(KEY_ALARM_RECURRENCE, recurrence);
        mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public long createAlarm(String title, long startTime, long endTime, int recurrence) {
        if (mDb == null) {
            Log.e(TAG, "In createAlarm(), mDb is null~!!");
            return 0;
        }

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ALARM_CALENDAR_EVENT_ID, -1);
        initialValues.put(KEY_ALARM_OFF, true);
        initialValues.put(KEY_ALARM_TITLE, title);
        initialValues.put(KEY_ALARM_START_TIME, startTime);
        initialValues.put(KEY_ALARM_END_TIME, endTime);
        initialValues.put(KEY_ALARM_RECURRENCE, recurrence);
        long id = mDb.insert(DATABASE_TABLE, null, initialValues);
        if (copy2AlarmCalendarId(id) == false) {
            Log.e(TAG, "In createAlarm(), 'Copy to Alarm Calendar Id' is fail ~!!");
        }
        return id;
    }

    public boolean deleteAlarm(long rowId) {
        if (mDb == null) {
            Log.e(TAG, "In deleteAlarm(), mDb is null~!!");
            return false;
        }

        return mDb.delete(DATABASE_TABLE, KEY_ALARM_ID + "=" + rowId, null) > 0;
    }

//    public boolean deleteAlarms(String[] rowId) {
//        if (mDb == null) {
//            Log.e(TAG, "In deleteAlarms(), mDb is null~!!");
//            return false;
//        }
//
//        return mDb.delete(DATABASE_TABLE, KEY_ALARM_ID + "= ?", rowId) > 0;
//    }

    public boolean deleteAlarmByCalendarId(String calendarId) {
        if (mDb == null) {
            Log.e(TAG, "In deleteAlarm(), mDb is null~!!");
            return false;
        }

        Log.e(TAG, "In deleteAlarm() calendarId:" + calendarId);
        return mDb.delete(DATABASE_TABLE, KEY_ALARM_CALENDAR_ID + "= ?", new String[]{calendarId}) > 0;
    }

//    public boolean deleteAlarmByCalendarIds(String[] calendarIds) {
//        if (mDb == null) {
//            Log.e(TAG, "In deleteAlarm(), mDb is null~!!");
//            return false;
//        }
//
//        return mDb.delete(DATABASE_TABLE, KEY_ALARM_CALENDAR_ID + "= ?", calendarIds) > 0;
//    }

    public boolean deleteAllAlarm() {
        if (mDb == null) {
            Log.e(TAG, "In deleteAllAlarm(), mDb is null~!!");
            return false;
        }

        return mDb.delete(DATABASE_TABLE, null, null) == 0;
    }

    public int getAlarmCount() {
        if (mDb == null) {
            Log.e(TAG, "In getAlarmCount(), mDb is null~!!");
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
            Log.e(TAG, "In fetchAllAlarm(), mDb is null~!!");
            return null;
        }

        Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_ALARM_ID, KEY_ALARM_CALENDAR_ID, KEY_ALARM_CALENDAR_TITLE, KEY_ALARM_CALENDAR_EVENT_ID, KEY_ALARM_OFF, KEY_ALARM_TITLE, KEY_ALARM_START_TIME, KEY_ALARM_END_TIME, KEY_ALARM_RECURRENCE}, //, KEY_ALARM_VOLUME, KEY_ALARM_RINGER_MODE},
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchUserAlarm() {
        if (mDb == null) {
            Log.e(TAG, "In fetchUserAlarm(), mDb is null~!!");
            return null;
        }

        Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_ALARM_ID, KEY_ALARM_CALENDAR_ID, KEY_ALARM_CALENDAR_TITLE, KEY_ALARM_CALENDAR_EVENT_ID, KEY_ALARM_OFF, KEY_ALARM_TITLE, KEY_ALARM_START_TIME, KEY_ALARM_END_TIME, KEY_ALARM_RECURRENCE}, //, KEY_ALARM_VOLUME, KEY_ALARM_RINGER_MODE},
                KEY_ALARM_CALENDAR_TITLE + " IS NULL", null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchCalendarAlarm() {
        if (mDb == null) {
            Log.e(TAG, "In fetchCalendrAlarm(), mDb is null~!!");
            return null;
        }

        Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_ALARM_ID, KEY_ALARM_CALENDAR_ID, KEY_ALARM_CALENDAR_TITLE, KEY_ALARM_CALENDAR_EVENT_ID, KEY_ALARM_OFF, KEY_ALARM_TITLE, KEY_ALARM_START_TIME, KEY_ALARM_END_TIME, KEY_ALARM_RECURRENCE}, //, KEY_ALARM_VOLUME, KEY_ALARM_RINGER_MODE},
                KEY_ALARM_CALENDAR_TITLE + " IS NOT NULL", null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchAllAlarmWithCalendar() {
        if (mDb == null) {
            Log.e(TAG, "In fetchAllAlarmWithCalendar(), mDb is null~!!");
            return null;
        }

        Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_ALARM_ID, KEY_ALARM_CALENDAR_ID, KEY_ALARM_CALENDAR_TITLE, KEY_ALARM_CALENDAR_EVENT_ID, KEY_ALARM_OFF, KEY_ALARM_TITLE, KEY_ALARM_START_TIME, KEY_ALARM_END_TIME, KEY_ALARM_RECURRENCE}, //, KEY_ALARM_VOLUME, KEY_ALARM_RINGER_MODE},
                null, null, KEY_ALARM_CALENDAR_ID, null, KEY_ALARM_ID + " DESC");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchAlarmByCalendarEventId(String calendarEventId) throws SQLException {
        if (mDb == null) {
            Log.e(TAG, "In fetchAlarmByCalendarEventId(), mDb is null~!!");
            return null;
        }

        Cursor cursor = mDb.query(true, DATABASE_TABLE, new String[]{KEY_ALARM_ID, KEY_ALARM_CALENDAR_ID, KEY_ALARM_CALENDAR_TITLE, KEY_ALARM_CALENDAR_EVENT_ID, KEY_ALARM_OFF, KEY_ALARM_TITLE, KEY_ALARM_START_TIME, KEY_ALARM_END_TIME, KEY_ALARM_RECURRENCE}, //, KEY_ALARM_VOLUME, KEY_ALARM_RINGER_MODE},
                KEY_ALARM_CALENDAR_EVENT_ID + "= ?", new String[]{calendarEventId}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchAlarmById(long rowId) throws SQLException {
        if (mDb == null) {
            Log.e(TAG, "In fetchAlarmById(), mDb is null~!!");
            return null;
        }

        Cursor cursor = mDb.query(true, DATABASE_TABLE, new String[]{KEY_ALARM_ID, KEY_ALARM_CALENDAR_ID, KEY_ALARM_CALENDAR_TITLE, KEY_ALARM_CALENDAR_EVENT_ID, KEY_ALARM_OFF, KEY_ALARM_TITLE, KEY_ALARM_START_TIME, KEY_ALARM_END_TIME, KEY_ALARM_RECURRENCE}, //, KEY_ALARM_VOLUME, KEY_ALARM_RINGER_MODE},
                KEY_ALARM_ID + "=" + rowId, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public boolean updateAlarm(long rowId, boolean off, String title, long startTime, long endTime, int recurrence) {
        if (mDb == null) {
            Log.e(TAG, "In updateAlarm(), mDb is null~!!");
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

    public boolean updateAlarmSet(long rowId, boolean off) {
        if (mDb == null) {
            Log.e(TAG, "In updateAlarmSet(), mDb is null~!!");
            return false;
        }
        ContentValues args = new ContentValues();
        args.put(KEY_ALARM_OFF, off);
        return mDb.update(DATABASE_TABLE, args, KEY_ALARM_ID + "=" + rowId, null) > 0;
    }

    private boolean copy2AlarmCalendarId(long rowId) {
        if (mDb == null) {
            Log.e(TAG, "In copy2AlarmCalendarId(), mDb is null~!!");
            return false;
        }
        ContentValues args = new ContentValues();
        args.put(KEY_ALARM_CALENDAR_ID, rowId);

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