package com.wanna.app.alarmnoti.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.wanna.app.alarmnoti.R;
import com.wanna.app.alarmnoti.activity.AlarmListActivity;
import com.wanna.app.alarmnoti.receiver.AlarmReceiver;

import java.util.Calendar;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link AlarmListActivity}
 * in two-pane mode (on tablets) or a {@link //ItemDetailActivity}
 * on handsets.
 */
public class AlarmDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Button mSetAlarm;
    private Button mAlarmOn;
    private Button mAlarmOff;

    private Fragment mFragment;
    private int mDefaultVolume = -1;
    private int mRingerMode = -1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AlarmDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alarm_detail, container, false);

        mSetAlarm = (Button) rootView.findViewById(R.id.set_alarm);
        mSetAlarm.setOnClickListener(v -> {
            Intent intent = new Intent(mFragment.getActivity(), AlarmReceiver.class);
            PendingIntent appIntent =
                    PendingIntent.getBroadcast(mFragment.getActivity(), 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, 1); // 1초 뒤에 발생..

            AlarmManager am = (AlarmManager) mFragment.getActivity().getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), appIntent);
        });

        mAlarmOn = (Button) rootView.findViewById(R.id.alarm_on);
        mAlarmOn.setOnClickListener(v -> Toast.makeText(mFragment.getActivity(), "Alarm On", Toast.LENGTH_SHORT).show());

        mAlarmOff = (Button) rootView.findViewById(R.id.alarm_off);
        mAlarmOff.setOnClickListener(v -> Toast.makeText(mFragment.getActivity(), "Alarm Off", Toast.LENGTH_SHORT).show());

        return rootView;
    }


    private void setVolumeDefault() {
        if (mDefaultVolume == -1 || mRingerMode == -1) {
            Toast.makeText(getActivity(), "not changed volume", Toast.LENGTH_SHORT).show();
            return;
        }

        AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mDefaultVolume, 0);
        mDefaultVolume = -1;
        am.setRingerMode(mRingerMode);
        mRingerMode = -1;
    }

    private void setVolumeMin() {
        if (mDefaultVolume != -1 || mRingerMode != -1) {
            Toast.makeText(getActivity(), "Already set volume", Toast.LENGTH_SHORT).show();
            return;
        }

        AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mDefaultVolume = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
        mRingerMode = am.getRingerMode();
        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    private void setVolumeMax() {
        if (mDefaultVolume != -1 || mRingerMode != -1) {
            Toast.makeText(getActivity(), "Already set volume", Toast.LENGTH_SHORT).show();
            return;
        }

        AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mDefaultVolume = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), 0);
        mRingerMode = am.getRingerMode();
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    public void make(Context context, long time) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent appIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        //Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(System.currentTimeMillis());
        //calendar.add(Calendar.SECOND, 1); // 1초 뒤에 발생..

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), appIntent);
        am.set(AlarmManager.RTC_WAKEUP, time, appIntent);
    }
}
