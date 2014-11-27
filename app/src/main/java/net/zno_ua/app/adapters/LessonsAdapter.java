package net.zno_ua.app.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.models.Lesson;

import java.util.ArrayList;

public class LessonsAdapter extends BaseAdapter {

    private final String TEST_ONE;
    private final String TESTS_TWO_FOUR;
    private final String TESTS_OVER_FIVE;
    private final String PACKAGE_NAME;

    private ArrayList<Lesson> lessonsList;
    private ArrayList<Drawable> lessonsIconsList;
    private LayoutInflater lInflater;
    private Resources resources;

    static class ViewHolder {
        public ImageView icon;
        public TextView name;
        public TextView testsCounter;
    }

    public LessonsAdapter(Context context, ArrayList<Lesson> lessonsList) {
        this.lessonsList = lessonsList;
        PACKAGE_NAME = context.getPackageName();
        resources = context.getResources();
        lInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TEST_ONE = context.getResources().getString(R.string.tests_one);
        TESTS_TWO_FOUR = context.getResources().getString(
                R.string.tests_two_four);
        TESTS_OVER_FIVE = context.getResources().getString(
                R.string.tests_over_five);
        lessonsIconsList = new ArrayList<Drawable>();
        for (Lesson lesson : lessonsList) {
            lessonsIconsList.add(resources.getDrawable(resources.getIdentifier(
                    "ic_" + lesson.link, "drawable", PACKAGE_NAME)));
        }
    }

    @Override
    public int getCount() {
        return lessonsList.size();
    }

    @Override
    public Object getItem(int position) {
        return lessonsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View lessonItem = convertView;

        if (lessonItem == null) {
            lessonItem = lInflater.inflate(R.layout.lesson, parent,
                    false);
            ViewHolder viewHolder = new ViewHolder();

            viewHolder.icon = (ImageView) lessonItem
                    .findViewById(R.id.lesson_image);
            viewHolder.name = (TextView) lessonItem
                    .findViewById(R.id.lesson_name);
            viewHolder.testsCounter = (TextView) lessonItem
                    .findViewById(R.id.lesson_tests_counter);
            lessonItem.setTag(viewHolder);
        }

        Lesson currentLesson = lessonsList.get(position);
        String counter = String.valueOf(currentLesson.testsCount) + " ";

        switch (currentLesson.testsCount) {
            case 1:
                counter += TEST_ONE;
                break;
            case 2:
            case 3:
            case 4:
                counter += TESTS_TWO_FOUR;
                break;
            default:
                counter += TESTS_OVER_FIVE;
        }

        ViewHolder viewHolder = (ViewHolder) lessonItem.getTag();
        viewHolder.icon.setImageDrawable(lessonsIconsList.get(position));
        viewHolder.name.setText(currentLesson.name);
        viewHolder.testsCounter.setText(counter);

        return lessonItem;
    }

}
