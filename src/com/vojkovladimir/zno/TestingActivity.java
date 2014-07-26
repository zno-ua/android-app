package com.vojkovladimir.zno;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.vojkovladimir.zno.adapters.LessonsListAdapter;
import com.vojkovladimir.zno.models.Lesson;

public class TestingActivity extends Activity {

	public static String LOG_TAG = "MyLogs";

	ListView lessonsListView;

	LessonsListAdapter lessonsListAdapter;
	ArrayList<Lesson> lessonsList;

	int[] lessonsIds;

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			try {
				Intent testsList = new Intent(getApplicationContext(),
						LessonTestsActivity.class);
				testsList.putExtra(ZNOApplication.ExtrasKeys.ID_LESSON,
						lessonsList.get(position).id);
				testsList.putExtra(ZNOApplication.ExtrasKeys.LESSON_NAME,
						lessonsList.get(position).name);

				startActivity(testsList);
			} catch (ArrayIndexOutOfBoundsException e) {
				Log.e(LOG_TAG, "You try to select lesson #" + position
						+ " from list.");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_testing);

		lessonsList = ZNOApplication.getInstance().getZnoDataBaseHelper().getLessonsList();
		lessonsListAdapter = new LessonsListAdapter(this, lessonsList);
		lessonsListView = (ListView) findViewById(R.id.lessons_list_view);
		lessonsListView.setAdapter(lessonsListAdapter);
		lessonsListView.setOnItemClickListener(itemListener);
	}
}
