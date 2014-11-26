package net.zno_ua.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.ZNOApplication;
import net.zno_ua.app.models.Record;

import java.util.ArrayList;
import java.util.Calendar;

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
        MIN = context.getString(R.string.min);
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
            additionalInfo += String.format(", " + FOR + " %d " + MIN + ".", minutes);
        }

        int ballType = (record.znoBall >= 190f) ? Record.GOOD_BALL :
                (record.znoBall < 124f) ? Record.BAD_BALL : 0;

        ViewHolder holder = (ViewHolder) view.getTag();
        holder.lessonName.setText(record.lessonName);
        holder.testProperties.setText(testProperties);
        holder.additionalInfo.setText(additionalInfo);
        holder.additionalInfo.setSelected(true);
        holder.recordBall.setText(ZNOApplication.buildBall(record.znoBall, false, ballType));

        return view;
    }

}
