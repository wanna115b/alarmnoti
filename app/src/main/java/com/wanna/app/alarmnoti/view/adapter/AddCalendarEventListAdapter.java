package com.wanna.app.alarmnoti.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.wanna.app.alarmnoti.R;
import com.wanna.app.alarmnoti.util.CalendarEvent;

import java.util.HashMap;

public class AddCalendarEventListAdapter extends ArrayAdapter {
    private Context mContext;
    private int mResource;
    private LayoutInflater mInflater;
    private HashMap<Integer, CalendarEvent> mCalendarEventMap;

    static class ViewHolder {
        View listItemLayout;
        CheckBox checkBox;
        TextView titleView;
        TextView subtitleView;
    }

    public AddCalendarEventListAdapter(Context context, int textViewResourceId, HashMap<Integer, CalendarEvent> calendarEventMap) {
        super(context, textViewResourceId);
        mContext = context;
        mResource = textViewResourceId;
        if (context != null) {
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        mCalendarEventMap = calendarEventMap;
    }

    @Override
    public int getCount() {
        return mCalendarEventMap.size();
    }

    @Override
    public Object getItem(int position) {
        return mCalendarEventMap.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (v == null) {
            holder = new ViewHolder();
            v = mInflater.inflate(R.layout.listitem_add_calendar_event, null);
            holder.listItemLayout = (View) v.findViewById(R.id.list_item_layout);
            holder.checkBox = (CheckBox) v.findViewById(R.id.set_alarm);
            holder.titleView = (TextView) v.findViewById(R.id.alarm_title);
            holder.subtitleView = (TextView) v.findViewById(R.id.alarm_day);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        if (mCalendarEventMap == null || mCalendarEventMap.get(position) == null) {
            holder.titleView.setText("");
            holder.subtitleView.setText("");
            return v;
        }

        if (holder.checkBox != null) {
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mCalendarEventMap.get(position).checked = isChecked;
                ((ListView) parent).setItemChecked(position, isChecked);
            });
        }

        if (holder.titleView != null) {
            String titleText = mCalendarEventMap.get(position).summary;
            holder.titleView.setText(titleText);
        }

        if (holder.subtitleView != null) {
            String subtitleText = mCalendarEventMap.get(position).accessRole;
            holder.subtitleView.setText(subtitleText);
        }

        return v;
    }
}
