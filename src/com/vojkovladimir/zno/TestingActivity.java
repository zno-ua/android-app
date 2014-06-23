package com.vojkovladimir.zno;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.vojkovladimir.zno.db.ZNODataBaseHelper;
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
				Log.d(LOG_TAG, "You select lesson with id: "
						+ lessonsIds[position]);

				// Intent testsList = new Intent(getApplicationContext(),
				// LessonTestsActivity.class);
				// testsList.putExtra(ZNOApplication.ExtrasKeys.ID_LESSON,
				// lessonsIds[position]);
				// testsList.putExtra(ZNOApplication.ExtrasKeys.TABLE_NAME,
				// lessonsListAdapter.getItem(position));
				//
				// startActivity(testsList);
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

		lessonsList = new ArrayList<Lesson>();

		SQLiteDatabase db = ZNOApplication.getInstance().getZnoDataBaseHelper()
				.getReadableDatabase();

		Cursor c = db.query(ZNODataBaseHelper.TABLE_LESSONS_LIST, new String[] {
				ZNODataBaseHelper.KEY_ID, ZNODataBaseHelper.KEY_NAME }, null,
				null, null, null, null);

		String lessonName;
		int testsCounter;
		
		lessonsIds = new int[c.getCount()];

		if (c.moveToFirst()) {
			int lessonIdIndex = c.getColumnIndex(ZNODataBaseHelper.KEY_ID);
			int lessonNameIndex = c.getColumnIndex(ZNODataBaseHelper.KEY_NAME);

			for (int i = 0; i < lessonsIds.length; i++, c.moveToNext()) {
				lessonName = c.getString(lessonNameIndex);
				testsCounter = (db
						.query("lessons_list  inner join tests_list on lessons_list.id = tests_list.id_lesson",
								new String[] { "lessons_list.id" }, null, null,
								null, null, null)).getCount();

				lessonsIds[i] = c.getInt(lessonIdIndex);
				lessonsList.add(new Lesson(lessonName, testsCounter));
				Log.i(LOG_TAG, "Lesson: "+lessonName+", counts = "+testsCounter);
			}
		}

		lessonsListAdapter = new LessonsListAdapter(this, lessonsList);
		lessonsListView = (ListView) findViewById(R.id.lessons_list_view);
		lessonsListView.setAdapter(lessonsListAdapter);
		lessonsListView.setOnItemClickListener(itemListener);
	}
}
