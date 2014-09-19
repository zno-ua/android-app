package com.vojkovladimir.zno;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.vojkovladimir.zno.adapters.LessonsListAdapter;
import com.vojkovladimir.zno.models.Lesson;

import java.util.ArrayList;

public class TestingActivity extends Activity {

	ListView lessonsListView;

	LessonsListAdapter lessonsListAdapter;
	ArrayList<Lesson> lessons;

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			Intent testsList = new Intent(getApplicationContext(), LessonTestsActivity.class);
			testsList.putExtra(Lesson.LESSON_ID, lessons.get(position).id);
			testsList.putExtra(Lesson.LESSON_NAME, lessons.get(position).name);
			startActivity(testsList);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_testing);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		lessons = new ArrayList<Lesson>();
		lessons = ZNOApplication.getInstance().getZnoDataBaseHelper().getLessons();
		lessonsListAdapter = new LessonsListAdapter(this, lessons);
		lessonsListView = (ListView) findViewById(R.id.lessons_list_view);
		lessonsListView.setAdapter(lessonsListAdapter);
		lessonsListView.setOnItemClickListener(itemListener);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
