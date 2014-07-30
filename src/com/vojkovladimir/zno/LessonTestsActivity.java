package com.vojkovladimir.zno;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.vojkovladimir.zno.adapters.TestsListAdapter;
import com.vojkovladimir.zno.api.Api;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.models.TestInfo;

public class LessonTestsActivity extends Activity {

	public static String LOG_TAG = "MyLogs";

	ZNOApplication app;
	ZNODataBaseHelper db;

	ListView testsListView;
	TestsListAdapter testsListAdapter;
	ArrayList<TestInfo> testsList;
	ProgressDialog downloadProgress;

	int idLesson;

	OnTestLoadListener testLoad = new OnTestLoadListener() {

		@Override
		public void onTestLoad(JSONObject json) {
			try {
				JSONArray info = json.getJSONArray(Api.INFO);
				final JSONArray response = json.getJSONArray(Api.RESPONSE);
				final String dbName = info.getJSONObject(0).getString(
						Api.Keys.DB_NAME);
				final Runnable invalidateList = new Runnable() {

					@Override
					public void run() {
						invalidate();
					}
				};

				Runnable fillTableTest = new Runnable() {

					@Override
					public void run() {
						db.fillTableTest(dbName, response);
						runOnUiThread(invalidateList);
						downloadProgress.cancel();
					}
				};

				new Thread(fillTableTest).start();
			} catch (JSONException e) {
				Log.e(LOG_TAG, "Error on load test: " + e.getMessage());
			}
		}
	};

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			TestInfo currTest = testsList.get(position);
			if (currTest.loaded) {
				Intent testActivity = new Intent(getApplicationContext(),
						TestActivity.class);

				testActivity.putExtra(ZNOApplication.ExtrasKeys.LESSON_NAME,
						currTest.lessonName);
				testActivity.putExtra(ZNOApplication.ExtrasKeys.DB_NAME,
						currTest.dbName);
				testActivity.putExtra(ZNOApplication.ExtrasKeys.YEAR,
						currTest.year);
				startActivity(testActivity);
			} else {
				TestLoadDialogFragment f = TestLoadDialogFragment.newInstance(
						currTest.dbName, testLoad, downloadProgress);
				f.show(getFragmentManager(), null);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tests_list);

		app = ZNOApplication.getInstance();
		db = app.getZnoDataBaseHelper();

		Intent intent = getIntent();

		setTitle(intent.getStringExtra(ZNOApplication.ExtrasKeys.LESSON_NAME));
		idLesson = intent.getIntExtra(ZNOApplication.ExtrasKeys.ID_LESSON, -1);
		testsList = db.getLessonTestsList(idLesson);
		testsListAdapter = new TestsListAdapter(this, testsList);
		testsListView = (ListView) findViewById(R.id.tests_list_view);
		testsListView.setAdapter(testsListAdapter);
		testsListView.setOnItemClickListener(itemListener);
		downloadProgress = new ProgressDialog(this);
		downloadProgress.setMessage(getResources().getString(
				R.string.progress_test_load));
	}

	public void invalidate() {
		testsList = db.getLessonTestsList(idLesson);
		testsListAdapter.setTestsList(testsList);
		testsListView.invalidateViews();
	}

}
