package com.wanna.app.alarmnoti.fragment;

import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.api.client.util.DateTime;
import com.wanna.app.alarmnoti.R;
import com.wanna.app.alarmnoti.util.Alarm;
import com.wanna.app.alarmnoti.util.AlarmDB;
import com.wanna.app.alarmnoti.util.AuthPreferences;
import com.wanna.app.alarmnoti.util.CalendarEvent;
import com.wanna.app.alarmnoti.util.GoogleAccount;
import com.wanna.app.alarmnoti.view.adapter.AddCalendarEventListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A list fragment representing a list of Items. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link AlarmDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class AddCalendarEventListFragment extends ListFragment implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "AddCalendarListFragment";
    private final String SERVER_API_KEY = "AIzaSyDHLiOGJqWfEV2S5Q9Msi4URZW7c8vrkSU";
    private final String GET_METHOD_CALENDAR_LIST = "https://www.googleapis.com/calendar/v3/users/me/calendarList?access_token=%s";
    public final String GET_METHOD_CALENDAR_EVENT = "https://www.googleapis.com/calendar/v3/calendars/%s/events?%s";

    private TextView mEmptyTextView;
    private ListView mListView;
    private AddCalendarEventListAdapter mCalendarAlarmListAdapter;
    private Button mAddButton;

    private HashMap<Integer, CalendarEvent> mCalendarEventMap = new HashMap<>();
    private HashMap<Integer, Alarm> mAlarmMap = new HashMap<>();
    private AuthPreferences mAuthPreferences;
    private AlarmDB mAlarmDb;

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected()");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "onNothingSelected()");
    }

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
        return inflater.inflate(R.layout.fragment_add_calendar_event_list, container, false);
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
        mCalendarAlarmListAdapter = new AddCalendarEventListAdapter(getActivity(), R.layout.listitem_add_calendar_event, mCalendarEventMap);
        mListView.setAdapter(mCalendarAlarmListAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener((parent, view, position, id) -> Toast.makeText(getActivity(), "click", Toast.LENGTH_SHORT).show());
        mListView.setOnItemSelectedListener(this);

        mAddButton = (Button) getActivity().findViewById(R.id.add);
        mAddButton.setOnClickListener(v -> {
            SparseBooleanArray sba = mListView.getCheckedItemPositions();
            mAuthPreferences = new AuthPreferences(getActivity());
            String accessToken = mAuthPreferences.getToken();
            getCalendarEvent(accessToken);
        });

        mAuthPreferences = new AuthPreferences(getActivity());
        String accessToken = mAuthPreferences.getToken();
        makeCalendarEventList(accessToken);

        mAlarmDb = new AlarmDB(getActivity());
        mAlarmDb.open();
    }

    private void makeCalendarEventList(String accessToken) {
        Observable.just(accessToken)
                .subscribeOn(Schedulers.newThread())
                .map(s -> String.format(GET_METHOD_CALENDAR_LIST, s))
                .map(this::getGetMethod)
                .map(this::getCalendarSubejcts)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                            mCalendarAlarmListAdapter.notifyDataSetChanged();
                        }
                );
    }

    private void getCalendarEvent(String accessToken) {
        Observer<HashMap<Integer, Alarm>> myObserver = new Observer<HashMap<Integer, Alarm>>() {
            @Override
            public void onCompleted() {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(HashMap<Integer, Alarm> hm) {
                for (int i = 0; i < hm.size(); i++) {
                    Alarm alarm = hm.get(i);
                    mAlarmDb.createAlarm(alarm.calendarId, alarm.calendarTitle, alarm.calendarEventId, alarm.title, alarm.startTime, alarm.endTime, alarm.recurrence);
                    Log.e(TAG, String.format("createAlarm DB:\n  title:%s, start%s, end:%s, recurrence:%s, id:%s", alarm.title, alarm.startTime, alarm.endTime, alarm.recurrence, alarm.calendarEventId));
                }
            }
        };

        Observable.just(accessToken)
                .map(this::getCheckedEvent)
                .flatMap(Observable::from)
                .subscribeOn(Schedulers.newThread())
                .map(o -> getGetMethod((CalendarEvent) o))
                .map(this::getCalendarAlarm)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(myObserver);
    }

    private Object[] getCheckedEvent(String accessToken) {
        SparseBooleanArray sba = mListView.getCheckedItemPositions();
        int eventSize = mListView.getCount();
        ArrayList<CalendarEvent> al = new ArrayList<>();
        for (int i = 0; i < eventSize; i++) {
            if (sba != null && sba.get(i) == true) {
                CalendarEvent ce = mCalendarEventMap.get(i);
                ce.uri = String.format(GET_METHOD_CALENDAR_EVENT, ce.id, makeAccessToken(ce.accessRole));
                al.add(ce);
            }
        }
        return al.toArray();
    }

    private String getGetMethod(String urlStr) {
        HttpRequest hr = HttpRequest.get(urlStr);
        String response = hr.body();

        if (hr.ok() == true) {
            if (response != null && TextUtils.isEmpty(response) == false) {
                getNextSyncToken(response);
            }
        } else {
            Log.d(TAG, "Response Code:" + hr.code() + "\nResponse Message:" + response);
            mAuthPreferences.setToken(null);
            AccountManager accountManager = AccountManager.get(getActivity());
            accountManager.invalidateAuthToken("com.google", null);
            Log.d(TAG, getActivity().getString(R.string.restart_app));
            getActivity().finish();
        }

        return response;
    }

    private CalendarEvent getGetMethod(CalendarEvent calendarEvent) {
        HttpRequest hr = HttpRequest.get(calendarEvent.uri);
        String response = hr.body();

        if (hr.ok() == true) {
            if (response != null && TextUtils.isEmpty(response) == false) {
                getNextSyncToken(response);
            }
        } else {
            Log.d(TAG, "Response Code:" + hr.code() + "\nResponse Message:" + response);
//            mAuthPreferences.setToken(null);
//            AccountManager accountManager = AccountManager.get(getActivity());
//            accountManager.invalidateAuthToken("com.google", mAuthPreferences.getToken());
            GoogleAccount ga = new GoogleAccount(this.getActivity(), new GoogleAccount.AuthenticatedStuff() {
                @Override
                public void doCoolAuthenticatedStuff() {
                }
            });
            ga.requestToken();
            Log.d(TAG, getActivity().getString(R.string.restart_app));
            getActivity().finish();
        }

        calendarEvent.uri = response;
        return calendarEvent;
    }

    private HashMap<Integer, CalendarEvent> getCalendarSubejcts(String htmlResponse) {
        try {
            JSONObject jo = new JSONObject(htmlResponse);
            JSONArray ja = new JSONArray(jo.getString("items"));

            int length = ja.length();
            JSONObject jItem = null;

            for (int i = 0; i < length; i++) {
                jItem = ja.getJSONObject(i);
                CalendarEvent ce = new CalendarEvent();
                ce.id = jItem.getString("id");
                ce.summary = jItem.getString("summary");
                ce.accessRole = jItem.getString("accessRole");
                mCalendarEventMap.put(i, ce);
                Log.e(TAG, "JASON:\n" + i + "-id:" + jItem.getString("id") + ", summary:" + jItem.getString("summary")
                        + ", accessRole:" + jItem.getString("accessRole"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getMessage());
        } finally {
            return mCalendarEventMap;
        }
    }

    private HashMap<Integer, Alarm> getCalendarAlarm(CalendarEvent calendarEvent) {
        String htmlResponse = calendarEvent.uri;
        try {
            JSONObject jo = new JSONObject(htmlResponse);
            String calendarTitle = jo.getString("summary");

            JSONArray ja = new JSONArray(jo.getString("items"));
            int length = ja.length();
            JSONObject jItem = null;
            Log.e(TAG, htmlResponse);
            for (int i = 0; i < length; i++) {
                jItem = ja.getJSONObject(i);
                //Log.d(TAG, String.format("JSON Public:\n  id:%s, summary:%s, start%s, end:%s", jItem.getString("id"), jItem.getString("summary"), jItem.getString("start"), jItem.getString("end")));
                Alarm alarm = new Alarm();
                alarm.calendarId = calendarEvent.id;
                alarm.calendarTitle = calendarEvent.summary;
                alarm.calendarEventId = jItem.getString("id");
                if (jItem.has("summary") == true) {
                    alarm.title = jItem.getString("summary");
                    Log.e(TAG, "summary : " + alarm.title);
                }

                if (jItem.has("recurrence") == true) {
                    String rec = jItem.getString("recurrence");
                    alarm.recurrence = alarm.convertRecurrence(rec);
                    Log.e(TAG, String.format("recurrence : %s - %d", rec, alarm.recurrence));
                }
                if (jItem.has("recurringEventId") == true) {
                    String recId = jItem.getString("recurringEventId");
                    Log.e(TAG, "recurringEventId : " + recId);
                }

                JSONObject joStartTime = new JSONObject(jItem.getString("start"));
                String startTime = null;
                if (joStartTime.has("date") == true) {
                    startTime = joStartTime.getString("date");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = dateFormat.parse(startTime);
                    alarm.startTime = date.getTime();
                } else if (joStartTime.has("dateTime") == true) {
                    startTime = joStartTime.getString("dateTime");
                    DateTime dt = DateTime.parseRfc3339(startTime);
                    alarm.startTime = dt.getValue();
                }

                JSONObject joEndTime = new JSONObject(jItem.getString("end"));
                String endTime = null;
                if (joEndTime.has("date") == true) {
                    endTime = joEndTime.getString("date");
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = dateFormat.parse(endTime);
                    alarm.endTime = date.getTime();
                } else if (joEndTime.has("dateTime") == true) {
                    endTime = joEndTime.getString("dateTime");
                    DateTime dt = DateTime.parseRfc3339(endTime);
                    alarm.endTime = dt.getValue();
                }
                mAlarmMap.put(i, alarm);
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            return mAlarmMap;
        }
    }

    private void getNextSyncToken(String htmlRes) {
        try {
            JSONObject jo = new JSONObject(htmlRes);
            if (jo.has("nextSyncToken") == true) {
                mAuthPreferences.setNextToken(jo.getString("nextSyncToken"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String makeAccessToken(String accessRole) {
        if (accessRole != null && accessRole.equals("owner") == true) {
            return "access_token=" + mAuthPreferences.getToken();
        } else {
            return "key=" + SERVER_API_KEY;
        }
    }
}