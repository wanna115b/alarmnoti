package com.wanna.app.alarmnoti.activity;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.wanna.app.alarmnoti.R;
import com.wanna.app.alarmnoti.util.Alarm;
import com.wanna.app.alarmnoti.util.AlarmTime;
import com.wanna.app.alarmnoti.util.Constant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class AddAlarmActivity extends Activity {
    private final String TAG = "AddAlarmActivity";

    private Button mAddBt;
    private EditText mTitleEt;
    private TextView mStartTimeTv;
    private Button mStartTimeBt;
    private TextView mDuringTimeTv;
    private Button mDuringTimeBt;
    private ToggleButton mMonTb;
    private ToggleButton mTueTb;
    private ToggleButton mWedTb;
    private ToggleButton mThuTb;
    private ToggleButton mFriTb;
    private ToggleButton mSatTb;
    private ToggleButton mSunTb;

    private long mAlarmId;
    private TimePickerDialog mStartTimePickerDialog;
    private TimePickerDialog mEndTimePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        setReference();

        mStartTimeBt.setOnClickListener(v -> new TimePickerDialog(AddAlarmActivity.this, (view, hourOfDay, minute) -> {
            String msg = String.format("%02d:%02d", hourOfDay, minute);
            mStartTimeTv.setText(msg);
        }, 0, 0, false).show());
        mDuringTimeBt.setOnClickListener(v -> new TimePickerDialog(AddAlarmActivity.this, (view, hourOfDay, minute) -> {
            String msg = String.format("%02d:%02d", hourOfDay, minute);
            mDuringTimeTv.setText(msg);
        }, 0, 0, false).show());

        mAddBt.setOnClickListener(v -> {
            long startTime = getTime(mStartTimeTv.getText().toString());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(startTime));
            //transform from during time to end time
            calendar.add(Calendar.HOUR, Integer.parseInt(mDuringTimeTv.getText().toString().substring(0, 2)));
            calendar.add(Calendar.MINUTE, Integer.parseInt(mDuringTimeTv.getText().toString().substring(3, 5)));
            long endTime = calendar.getTimeInMillis();

            int recurrence = (mMonTb.isChecked() ? Alarm.ALARM_DAY_INDEX_MON : 0) | (mTueTb.isChecked() ? Alarm.ALARM_DAY_INDEX_TUE : 0) |
                    (mWedTb.isChecked() ? Alarm.ALARM_DAY_INDEX_WED : 0) | (mThuTb.isChecked() ? Alarm.ALARM_DAY_INDEX_THU : 0) |
                    (mFriTb.isChecked() ? Alarm.ALARM_DAY_INDEX_FRI : 0) | (mSatTb.isChecked() ? Alarm.ALARM_DAY_INDEX_SAT : 0) |
                    (mSunTb.isChecked() ? Alarm.ALARM_DAY_INDEX_SUN : 0);
            Alarm alarm = new Alarm(mAlarmId, null, null, null, mTitleEt.getText().toString(), startTime, endTime, -1, recurrence);
            Intent intent = new Intent();
            intent.putExtra(Constant.INTENT_ALARM, alarm);
            AddAlarmActivity.this.setResult(Activity.RESULT_OK, intent);
            finish();
        });

        Alarm alarm = this.getIntent().getParcelableExtra(Constant.INTENT_ALARM);
        int weekday = 0;
        if (alarm != null) {
            mAlarmId = alarm.id;
            mTitleEt.setText(alarm.title);
            AlarmTime startAt = AlarmTime.getTime(alarm.startTime);
            mStartTimeTv.setText(String.format("%02d:%02d", startAt.hour, startAt.minute));
            AlarmTime endAt = AlarmTime.getTime(alarm.endTime);
            mDuringTimeTv.setText(endAt.substractTime(startAt));
            weekday = alarm.recurrence;
        } else {
            AlarmTime alarmAt = AlarmTime.getTime(System.currentTimeMillis());
            mStartTimeTv.setText(String.format("%02d:%02d", alarmAt.hour, alarmAt.minute + 1));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            weekday = Alarm.getWeekDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK));
        }
        mMonTb.setChecked((weekday & Alarm.ALARM_DAY_INDEX_MON) > 0);
        mTueTb.setChecked((weekday & Alarm.ALARM_DAY_INDEX_TUE) > 0);
        mWedTb.setChecked((weekday & Alarm.ALARM_DAY_INDEX_WED) > 0);
        mThuTb.setChecked((weekday & Alarm.ALARM_DAY_INDEX_THU) > 0);
        mFriTb.setChecked((weekday & Alarm.ALARM_DAY_INDEX_FRI) > 0);
        mSatTb.setChecked((weekday & Alarm.ALARM_DAY_INDEX_SAT) > 0);
        mSunTb.setChecked((weekday & Alarm.ALARM_DAY_INDEX_SUN) > 0);
    }

    private void setReference() {
        mTitleEt = (EditText) findViewById(R.id.addalarm_title);
        mStartTimeTv = (TextView) findViewById(R.id.addalarm_starttime);
        mStartTimeBt = (Button) findViewById(R.id.addalarm_starttime_modify);
        mDuringTimeTv = (TextView) findViewById(R.id.addalarm_duringtime);
        mDuringTimeBt = (Button) findViewById(R.id.addalarm_duringtime_modify);
        mMonTb = (ToggleButton) findViewById(R.id.addalarm_mon);
        mTueTb = (ToggleButton) findViewById(R.id.addalarm_tue);
        mWedTb = (ToggleButton) findViewById(R.id.addalarm_wed);
        mThuTb = (ToggleButton) findViewById(R.id.addalarm_thu);
        mFriTb = (ToggleButton) findViewById(R.id.addalarm_fri);
        mSatTb = (ToggleButton) findViewById(R.id.addalarm_sat);
        mSunTb = (ToggleButton) findViewById(R.id.addalarm_sun);
        mAddBt = (Button) findViewById(R.id.add);
    }

    private long getTime(String HourMinute) {
        Date date = new Date();
        try {
            String ymdhmDate = new SimpleDateFormat("yyyy:MM:dd").format(date) + "_" + HourMinute;
            date = new SimpleDateFormat("yyyy:MM:dd_HH:mm").parse(ymdhmDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
}
