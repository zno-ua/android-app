package com.vojkovladimir.zno.adapters;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.models.Record;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RecordsAdapter extends BaseAdapter {

    final String FOR;
    final String YEAR;
    final String SESSION;
    final String MIN;
    final String PASSED;
    final String[] MONTHS;
    final int HIGH_BALL_COLOR;

    private LayoutInflater inflater;

    static class ViewHolder {
        public TextView lessonName;
        public TextView testProperties;
        public TextView additionalInfo;
        public TextView recordBall;
    }

    ArrayList<?> records;

    public RecordsAdapter(Context context, ArrayList<?> records) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FOR = context.getString(R.string.for_);
        YEAR = context.getString(R.string.year);
        MIN = context.getString(R.string.minutes_short);
        SESSION = context.getString(R.string.session_text);
        MONTHS = context.getResources().getStringArray(R.array.months);
        HIGH_BALL_COLOR = context.getResources().getColor(R.color.dark_green);
        PASSED = context.getString(R.string.passed);

        this.records = records;
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public Object getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.record, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.lessonName = (TextView) view.findViewById(R.id.lesson_name);
            holder.testProperties = (TextView) view.findViewById(R.id.year_and_session);
            holder.recordBall = (TextView) view.findViewById(R.id.record_ball);
            holder.additionalInfo = (TextView) view.findViewById(R.id.date_and_time);
            view.setTag(holder);
        }

        Record record = (Record) records.get(position);
        String testProperties = "";
        switch (record.session) {
            case 1:
                testProperties += "I " + SESSION + ", ";
                break;
            case 2:
                testProperties += "II " + SESSION + ", ";
                break;
        }
        testProperties += String.format("%d " + YEAR, record.year);

        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(record.date);
        int day = date.get(Calendar.DAY_OF_MONTH);
        int month = date.get(Calendar.MONTH);

        String additionalInfo = PASSED + " " + day + " " + MONTHS[month];
        int minutes = (int) (record.elapsedTime / 60000);
        if (minutes != 0) {
            additionalInfo += String.format(", " + FOR + " %d " + MIN, minutes);
        }

        SpannableString recordBall;
        if (record.ball % 1 == 0) {
            recordBall = new SpannableString(String.valueOf((int) record.ball));
        } else {
            recordBall = new SpannableString(String.format(Locale.US, "%.1f", record.ball));
            recordBall.setSpan(new RelativeSizeSpan(0.5f), recordBall.length() - 2, recordBall.length(), 0);
        }


        if (record.ball >= 190.0f) {
            recordBall.setSpan(new ForegroundColorSpan(HIGH_BALL_COLOR), 0, recordBall.length(), 0);
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.lessonName.setText(record.lessonName);
        holder.testProperties.setText(testProperties);
        holder.additionalInfo.setText(additionalInfo);
        holder.recordBall.setText(recordBall);

        return view;
    }

}
