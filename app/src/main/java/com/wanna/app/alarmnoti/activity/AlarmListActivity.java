package com.wanna.app.alarmnoti.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.wanna.app.alarmnoti.R;
import com.wanna.app.alarmnoti.fragment.AlarmDetailFragment;
import com.wanna.app.alarmnoti.fragment.AlarmListFragment;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items.
 * On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link AlarmListFragment} and the item details
 * (if present) is a {@link AlarmDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link AlarmListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class AlarmListActivity extends FragmentActivity
        implements AlarmListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        ((AlarmListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.alarm_list))
                .setActivateOnItemClick(true);

        if (findViewById(R.id.alarm_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((AlarmListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.alarm_list))
                    .setActivateOnItemClick(true);
        }
    }

    /**
     * Callback method from {@link AlarmListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(AlarmDetailFragment.ARG_ITEM_ID, id);
            AlarmDetailFragment fragment = new AlarmDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.alarm_detail_container, fragment)
                    .commit();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alarm_list, menu);
        return true;
    }
}


