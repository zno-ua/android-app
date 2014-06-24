package com.vojkovladimir.zno;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.models.TestInfo;

public class LessonTestsActivity extends Activity {

	public static String LOG_TAG = "MyLogs";

	ListView testsListView;
	TestsListAdapter testsListAdapter;
	ArrayList<TestInfo> testsList;

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			Log.d(LOG_TAG, "You select test #: " + position);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tests_list);

		testsListView = (ListView) findViewById(R.id.tests_list_view);

		Intent intent = getIntent();

		String testaTableName = intent
				.getStringExtra(ZNOApplication.ExtrasKeys.TABLE_NAME);
		int idLesson = intent.getIntExtra(ZNOApplication.ExtrasKeys.ID_LESSON,
				-1);

		setTitle(testaTableName);

		SQLiteDatabase db = ZNOApplication.getInstance().getZnoDataBaseHelper()
				.getReadableDatabase();

		testsList = new ArrayList<TestInfo>();
		testsListAdapter = new TestsListAdapter(this, testsList);
		testsListView = (ListView) findViewById(R.id.tests_list_view);

		testsListView.setAdapter(testsListAdapter);

		Cursor c = db.query(ZNODataBaseHelper.TABLE_TESTS_LIST,
				new String[] { ZNODataBaseHelper.KEY_NAME_TEST,ZNODataBaseHelper.KEY_YEAR,ZNODataBaseHelper.KEY_TASKS_NUM },
				ZNODataBaseHelper.KEY_ID_LESSON + "=" + idLesson, null, null,
				null, ZNODataBaseHelper.KEY_YEAR+" DESC");

		TestInfo testInfo;
		if (c.moveToFirst()) {
			int nameTestIndex = c
					.getColumnIndex(ZNODataBaseHelper.KEY_NAME_TEST);
			int yearIndex = c
					.getColumnIndex(ZNODataBaseHelper.KEY_YEAR);
			int tastsNumIndex = c
					.getColumnIndex(ZNODataBaseHelper.KEY_TASKS_NUM);
			do {
				testInfo = new TestInfo(c.getString(nameTestIndex), c.getInt(yearIndex), c.getInt(tastsNumIndex));
				testsList.add(testInfo);
			} while (c.moveToNext());
		}
		
		Log.i(LOG_TAG, getTitle()+" has "+testsList.size()+" tests");
	}
}
