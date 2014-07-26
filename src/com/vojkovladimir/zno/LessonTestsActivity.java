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

import com.vojkovladimir.zno.api.Api;
import com.vojkovladimir.zno.models.TestInfo;

public class LessonTestsActivity extends Activity {

	public static String LOG_TAG = "MyLogs";

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
				JSONArray response = json.getJSONArray(Api.RESPONSE);
				String dbName = info.getJSONObject(0).getString(Api.Keys.DB_NAME);
				ZNOApplication.getInstance().getZnoDataBaseHelper()
						.fillTableTest(dbName, response);
				testsList = ZNOApplication.getInstance().getZnoDataBaseHelper()
						.getLessonTestsList(idLesson);
				testsListAdapter = new TestsListAdapter(
						getApplicationContext(), testsList);
				testsListView.setAdapter(testsListAdapter);
				testsListView.invalidateViews();
				downloadProgress.cancel();
			} catch (JSONException e) {
				Log.e(LOG_TAG, "Error on load test: " + e.getMessage());
			}
		}
	};

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			if (testsList.get(position).loaded) {

			} else {
				TestLoadDialogFragment f = TestLoadDialogFragment.newInstance(
						testsList.get(position).dbName, testLoad,
						downloadProgress);
				f.show(getFragmentManager(), null);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tests_list);


		Intent intent = getIntent();

		setTitle(intent.getStringExtra(ZNOApplication.ExtrasKeys.TABLE_NAME));
		idLesson = intent.getIntExtra(ZNOApplication.ExtrasKeys.ID_LESSON, -1);
		testsList = ZNOApplication.getInstance().getZnoDataBaseHelper()
				.getLessonTestsList(idLesson);
		testsListAdapter = new TestsListAdapter(this, testsList);
		testsListView = (ListView) findViewById(R.id.tests_list_view);
		testsListView.setAdapter(testsListAdapter);
		testsListView.setOnItemClickListener(itemListener);
		downloadProgress = new ProgressDialog(this);
		downloadProgress.setMessage(getResources().getString(
				R.string.progress_test_load));
	}

}
