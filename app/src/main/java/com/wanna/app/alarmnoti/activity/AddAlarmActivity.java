package com.wanna.app.alarmnoti.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wanna.app.alarmnoti.R;
import com.wanna.app.alarmnoti.fragment.AlarmListFragment;


public class AddAlarmActivity extends Activity {
    private final String TAG = "AddAlarmActivity";

    private Button mStartBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        mStartBt = (Button) findViewById(R.id.start);
        mStartBt.setOnClickListener(v -> AddAlarmActivity.this.setResult(AlarmListFragment.REQUEST_ALARM));
    }
}
