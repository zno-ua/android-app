package com.vojkovladimir.zno.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.models.Lesson;

public class LessonsListAdapter extends BaseAdapter {

	private final String TEST_ONE;
	private final String TESTS_TWO_FOUR;
	private final String TESTS_OVER_FIVE;
	private final String PACKAGE_NAME;
	private final String LOG_TAG = "MyLogs";

	private ArrayList<Lesson> list;
	private LayoutInflater lInflater;
	private Resources resources;

	static class ViewHolder {
		public ImageView icon;
		public TextView name;
		public TextView testsCounter;
	}

	public LessonsListAdapter(Context context, ArrayList<Lesson> list) {
		this.list = list;
		PACKAGE_NAME = context.getPackageName();
		resources = context.getResources();
		lInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TEST_ONE = context.getResources().getString(R.string.tests_one);
		TESTS_TWO_FOUR = context.getResources()
				.getString(R.string.tests_two_four);
		TESTS_OVER_FIVE = context.getResources().getString(
				R.string.tests_over_five);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View lessonItem = convertView;

		if (lessonItem == null) {
			lessonItem = lInflater.inflate(R.layout.lessons_list_item, parent,
					false);
			ViewHolder viewHolder = new ViewHolder();

			viewHolder.icon = (ImageView) lessonItem
					.findViewById(R.id.lessons_list_lesson_icon);
			viewHolder.name = (TextView) lessonItem
					.findViewById(R.id.lessons_list_lesson_name);
			viewHolder.testsCounter = (TextView) lessonItem
					.findViewById(R.id.lessons_list_lesson_tests_counter);
			lessonItem.setTag(viewHolder);
		}

		ViewHolder viewHolder = (ViewHolder) lessonItem.getTag();

		Lesson currentLesson = list.get(position);

		Log.i(LOG_TAG,currentLesson.link);
		viewHolder.icon.setImageDrawable(resources.getDrawable(resources
				.getIdentifier("ic_"+currentLesson.link, "drawable", PACKAGE_NAME)));

		viewHolder.name.setText(currentLesson.name);

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

		viewHolder.testsCounter.setText(counter);

		return lessonItem;
	}

}
