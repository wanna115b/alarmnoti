package com.wanna.app.alarmnoti.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wanna.app.alarmnoti.R;
import com.wanna.app.alarmnoti.activity.AddAlarmActivity;
import com.wanna.app.alarmnoti.util.Alarm;
import com.wanna.app.alarmnoti.util.AlarmDB;
import com.wanna.app.alarmnoti.util.AuthPreferences;
import com.wanna.app.alarmnoti.view.adapter.AlarmListAdapter;
import com.wanna.app.alarmnoti.activity.AddCalendarEventListActivity;
import com.wanna.app.alarmnoti.util.CalendarEvent;

import java.util.Calendar;
import java.util.HashMap;

/**
 * A list fragment representing a list of Items. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link AlarmDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class AlarmListFragment extends ListFragment implements AbsListView.MultiChoiceModeListener {
    private static final String TAG = "AlarmListFragment";
    public static final int REQUEST_CALENDAR_EVENT = 100;
    public static final int REQUEST_ALARM = 101;

    private TextView mEmptyTextView;
    private ListView mListView;
    private AlarmListAdapter mAlarmListAdapter;
    private AlarmDB mAlarmDb;
    private Button mAddCalendarEventBtn;
    private Button mAddAlarmBtn;
    private Button mDeleteAllBtn;

    HashMap<Integer, CalendarEvent> mCalendarEventMap = new HashMap<>();
    HashMap<Integer, Alarm> mAlarmMap = new HashMap<>();
    private AuthPreferences mAuthPreferences;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
            Log.d(TAG, "onItemSelected");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarm_list, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView = (ListView) getActivity().findViewById(android.R.id.list);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);
        mListView.setOnItemClickListener((parent, view, position, id) -> Toast.makeText(getActivity(), "click", Toast.LENGTH_SHORT).show());
        mAlarmDb = new AlarmDB(getActivity());
        mAlarmDb.open();
        //createTestAlarm(getActivity());
        setAdapter();

        mAddCalendarEventBtn = (Button) getActivity().findViewById(R.id.add_calendar_event);
        mAddCalendarEventBtn.setOnClickListener(v -> addCalendarEvent());
        mAddAlarmBtn = (Button) getActivity().findViewById(R.id.add_alarm);
        mAddAlarmBtn.setOnClickListener(v -> addAlarm());
        mDeleteAllBtn = (Button) getActivity().findViewById(R.id.delete_all);
        mDeleteAllBtn.setOnClickListener(v -> deleteAllAlarm());
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        Log.d(TAG, "onCreateActionMode()");
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Log.d(TAG, "onPrepareActionMode()");
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Log.d(TAG, "onActionItemClicked()");
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Log.d(TAG, "onDestroyActionMode()");
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        Log.d(TAG, "onItemCheckedStateChanged()");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.add_calendar_event: {
                addCalendarEvent();
                return true;
            }
            case R.id.add_alarm: {
                addAlarm();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setAdapter() {
        new Handler().post(() -> {
            mAlarmListAdapter = new AlarmListAdapter(getActivity(), mAlarmDb.fetchAllAlarm());
            mListView.setAdapter(mAlarmListAdapter);
        });
    }

    private void addCalendarEvent() {
        Intent intent = new Intent(getActivity(), AddCalendarEventListActivity.class);
        startActivityForResult(intent, REQUEST_CALENDAR_EVENT);
    }

    private void addAlarm() {
        Intent intent = new Intent(getActivity(), AddAlarmActivity.class);
        startActivityForResult(intent, REQUEST_ALARM);
    }

    private void deleteAllAlarm() {
        mAlarmDb.deleteAllAlarm();
        mAlarmListAdapter.changeCursor(mAlarmDb.fetchAllAlarm());
    }

    private void createTestAlarm(Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, 1, 25);
        //calendar.setTime(new Date("2015, 24, 1 23:00"));
        //calendar.add(Calendar.SECOND, 1); // 1초 뒤에 발생..
        mAlarmDb.createAlarm("test 1", calendar.getTimeInMillis(), calendar.getTimeInMillis() + 3600000, "");
        mAlarmDb.createAlarm("test 2", calendar.getTimeInMillis() + 60000, calendar.getTimeInMillis() + 3600000 + 6000, "");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CALENDAR_EVENT) {
            if (resultCode == Activity.RESULT_OK) {
                mAlarmListAdapter.changeCursor(mAlarmDb.fetchAllAlarm());
            }
        }
    }
}