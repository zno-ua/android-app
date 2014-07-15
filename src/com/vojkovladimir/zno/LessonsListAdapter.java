package com.vojkovladimir.zno;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vojkovladimir.zno.models.Lesson;

public class LessonsListAdapter extends BaseAdapter {

	private final ArrayList<Lesson> list;
	private LayoutInflater lInflater;
	private String testsOne;
	private String testsTwoFour;
	private String testsOverFive;

	static class ViewHolder {
		public TextView name;
		public TextView testsCounter;
	}

	public LessonsListAdapter(Context context, ArrayList<Lesson> list) {
		this.list = list;
		lInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		testsOne = context.getResources().getString(R.string.tests_one);
		testsTwoFour = context.getResources()
				.getString(R.string.tests_two_four);
		testsOverFive = context.getResources().getString(
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

			viewHolder.name = (TextView) lessonItem
					.findViewById(R.id.lessons_list_lesson_name);
			viewHolder.testsCounter = (TextView) lessonItem
					.findViewById(R.id.lessons_list_lesson_tests_counter);
			lessonItem.setTag(viewHolder);
		}

		ViewHolder viewHolder = (ViewHolder) lessonItem.getTag();

		Lesson curLesson = list.get(position);

		viewHolder.name.setText(curLesson.name);

		String counter = String.valueOf(curLesson.testsCount)+" ";

		switch (curLesson.testsCount) {
		case 1:
			counter += testsOne;
			break;
		case 2:
		case 3:
		case 4:
			counter += testsTwoFour;
			break;
		default:
			counter += testsOverFive;
		}
		
		viewHolder.testsCounter.setText(counter);

		return lessonItem;
	}

}
