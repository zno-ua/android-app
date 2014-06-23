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

	private final Context context;
	private final ArrayList<Lesson> list;
	private LayoutInflater lInflater;
	

	public LessonsListAdapter(Context context,  ArrayList<Lesson> list) {
		this.list = list;
		this.context = context;
		lInflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		
		View lessonItem = lInflater.inflate(R.layout.lessons_list_item, parent, false);

		TextView name = (TextView) lessonItem
				.findViewById(R.id.lessons_list_lesson_name);
		TextView testsCounter = (TextView) lessonItem
				.findViewById(R.id.lessons_list_lesson_tests_counter);

		Lesson curLesson = list.get(position);

		name.setText(curLesson.name);
		String counter = String.valueOf(curLesson.testsCount)+" ";

		switch (curLesson.testsCount) {
		case 1:
			counter += context.getResources().getString(R.string.tests_one);
			break;
		case 2:
		case 3:
		case 4:
			counter += context.getResources()
					.getString(R.string.tests_two_four);
			break;
		default:
			counter += context.getResources().getString(
					R.string.tests_over_five);
		}
		
		testsCounter.setText(counter);

		return lessonItem;
	}

}
