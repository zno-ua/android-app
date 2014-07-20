package com.vojkovladimir.zno;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
	final int DIALOG_TEST_LOAD = 1;

	ListView testsListView;
	TestsListAdapter testsListAdapter;
	ArrayList<TestInfo> testsList;

	OnClickListener dialogListener = new OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case Dialog.BUTTON_POSITIVE:
				break;
			case Dialog.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			if(!testsList.get(position).loaded){
				onDialogCreate(DIALOG_TEST_LOAD).show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tests_list);

		testsListView = (ListView) findViewById(R.id.tests_list_view);

		Intent intent = getIntent();

		String testTableName = intent
				.getStringExtra(ZNOApplication.ExtrasKeys.TABLE_NAME);
		int idLesson = intent.getIntExtra(ZNOApplication.ExtrasKeys.ID_LESSON,
				-1);

		setTitle(testTableName);

		SQLiteDatabase db = ZNOApplication.getInstance().getZnoDataBaseHelper()
				.getReadableDatabase();

		testsList = new ArrayList<TestInfo>();
		testsListAdapter = new TestsListAdapter(this, testsList);
		testsListView = (ListView) findViewById(R.id.tests_list_view);

		testsListView.setAdapter(testsListAdapter);
		testsListView.setOnItemClickListener(itemListener);

		Cursor c = db.query(ZNODataBaseHelper.TABLE_TESTS_LIST, new String[] {
				ZNODataBaseHelper.KEY_DB_NAME, ZNODataBaseHelper.KEY_NAME_TEST,
				ZNODataBaseHelper.KEY_YEAR, ZNODataBaseHelper.KEY_TASKS_NUM,
				ZNODataBaseHelper.KEY_LOADED }, ZNODataBaseHelper.KEY_ID_LESSON
				+ "=" + idLesson, null, null, null, ZNODataBaseHelper.KEY_YEAR
				+ " DESC");

		TestInfo testInfo;
		if (c.moveToFirst()) {
			int dbNameIndex = c.getColumnIndex(ZNODataBaseHelper.KEY_DB_NAME);
			int nameTestIndex = c
					.getColumnIndex(ZNODataBaseHelper.KEY_NAME_TEST);
			int yearIndex = c.getColumnIndex(ZNODataBaseHelper.KEY_YEAR);
			int tastsNumIndex = c
					.getColumnIndex(ZNODataBaseHelper.KEY_TASKS_NUM);
			int loadedIndex = c.getColumnIndex(ZNODataBaseHelper.KEY_LOADED);
			do {
				testInfo = new TestInfo(c.getString(dbNameIndex),
						c.getString(nameTestIndex), c.getInt(yearIndex),
						c.getInt(tastsNumIndex),
						(c.getInt(loadedIndex) == 0) ? false : true);
				testsList.add(testInfo);
			} while (c.moveToNext());
		}

		Log.i(LOG_TAG, getTitle() + " has " + testsList.size() + " tests");
	}

	private Dialog onDialogCreate(int id) {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		if (id == DIALOG_TEST_LOAD) {
			dialogBuilder.setMessage(R.string.dialog_load_test_text);
			dialogBuilder.setPositiveButton(R.string.dialog_positive_text,
					dialogListener);
			dialogBuilder.setNegativeButton(R.string.dialog_negative_text,
					dialogListener);
		}
		return dialogBuilder.create();
	}

}
