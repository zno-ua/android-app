package com.vojkovladimir.zno;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
	ArrayList<Lesson> lessons;

	int[] lessonsIds;

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			Intent testsList = new Intent(getApplicationContext(),
					LessonTestsActivity.class);
			testsList.putExtra(ZNOApplication.ExtrasKeys.ID_LESSON,
					lessons.get(position).id);
			testsList.putExtra(ZNOApplication.ExtrasKeys.LESSON_NAME,
					lessons.get(position).name);
			testsList.putExtra(ZNOApplication.ExtrasKeys.LINK,
					lessons.get(position).link);
			startActivity(testsList);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_testing);

		lessons = new ArrayList<Lesson>();
		lessons = ZNOApplication.getInstance().getZnoDataBaseHelper()
				.getLessons();
		lessonsListAdapter = new LessonsListAdapter(this, lessons);
		lessonsListView = (ListView) findViewById(R.id.lessons_list_view);
		lessonsListView.setAdapter(lessonsListAdapter);
		lessonsListView.setOnItemClickListener(itemListener);
	}
}
