package com.wanna.app.alarmnoti.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.wanna.app.alarmnoti.R;
import com.wanna.app.alarmnoti.util.AlarmDB;

import java.util.Date;

public class AlarmListAdapter extends CursorAdapter {
    LayoutInflater mInflater;

    class ViewHolder {
        CheckBox setAlarmCb;
        TextView titleTv;
        TextView startTimeTv;
        TextView endTimeTv;
        TextView recurrenceTv;
    }

    public AlarmListAdapter(Context context, Cursor c) {
        super(context, c);

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // when the view will be created for first time,
        // we need to tell the adapters, how each item will look
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View retView = inflater.inflate(R.layout.listitem_alarm, parent, false);

        return retView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // here we are setting our data
        // that means, take the data from the cursor and put it in views

        ViewHolder holder;
        if (view == null || view.getTag() == null) {
            holder = new ViewHolder();
            holder.setAlarmCb = (CheckBox) view.findViewById(R.id.set_alarm);
            holder.titleTv = (TextView) view.findViewById(R.id.alarm_title);
            holder.startTimeTv = (TextView) view.findViewById(R.id.alarm_start_time);
            holder.endTimeTv = (TextView) view.findViewById(R.id.alarm_end_time);
            holder.recurrenceTv = (TextView) view.findViewById(R.id.alarm_recurrence);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.setAlarmCb.setChecked(cursor.getInt(AlarmDB.COLUMN_INDEX_ALARM_OFF) > 0);
        holder.titleTv.setText(cursor.getString(AlarmDB.COLUMN_INDEX_ALARM_TITLE));

        Date date = new Date(cursor.getLong(AlarmDB.COLUMN_INDEX_ALARM_START_TIME));
        holder.startTimeTv.setText(date.toString());

        date = new Date(cursor.getLong(AlarmDB.COLUMN_INDEX_ALARM_END_TIME));
        holder.endTimeTv.setText(date.toString());

        holder.recurrenceTv.setText(cursor.getString(AlarmDB.COLUMN_INDEX_ALARM_RECURRENCE));
    }
}