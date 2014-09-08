package com.vojkovladimir.zno;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.vojkovladimir.zno.adapters.TestsListAdapter;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.models.TestInfo;
import com.vojkovladimir.zno.service.ApiService;
import com.vojkovladimir.zno.service.ApiService.ApiBinder;
import com.vojkovladimir.zno.service.ApiService.OnTestLoadedListener;

public class LessonTestsActivity extends Activity {

	final Context context = this;
	
	ZNOApplication app;
	ZNODataBaseHelper db;

	ListView testsListView;
	TestsListAdapter testsListAdapter;
	ArrayList<TestInfo> tests;
	
	ProgressDialog downloadProgress;
	
	ApiService apiService;
    boolean apiBound = false;
	private ServiceConnection apiConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ApiBinder binder = (ApiBinder) service;
			apiService = binder.getService();
			apiBound = true;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			apiBound = false;
		}
		
	};
	
	String link;
	int idLesson;
	
	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			final TestInfo test = tests.get(position);

			if (test.loaded) {
				Intent testActivity = new Intent(getApplicationContext(), TestActivity.class);

				testActivity.putExtra(ZNOApplication.ExtrasKeys.TABLE_NAME, link + "_" + test.year + "_" + test.id);
				testActivity.putExtra(ZNOApplication.ExtrasKeys.ID_TEST, "" + test.id);
				startActivity(testActivity);
			} else {
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

				dialogBuilder.setMessage(R.string.dialog_load_test_text);
				dialogBuilder.setPositiveButton(R.string.dialog_positive_text,new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							downloadProgress.show();
							downloadProgress.setCancelable(false);
							apiService.downLoadTest(new OnTestLoadedListener() {
								
								@Override
								public void onTestLoad() {
									runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											downloadProgress.cancel();
											invalidate();											
										}
									});
								}
								
								@Override
								public void onError() {
									runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											downloadProgress.cancel();
										}
									});
								}
							}, link, test.year, test.id);
							break;
						}
					}
				});
				dialogBuilder.setNegativeButton(R.string.dialog_negative_text, null);
				dialogBuilder.create().show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tests_list);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		app = ZNOApplication.getInstance();
		db = app.getZnoDataBaseHelper();

		Intent intent = getIntent();

		setTitle(intent.getStringExtra(ZNOApplication.ExtrasKeys.LESSON_NAME));
		idLesson = intent.getIntExtra(ZNOApplication.ExtrasKeys.ID_LESSON, -1);
		link = intent.getStringExtra(ZNOApplication.ExtrasKeys.LINK);
		tests = new ArrayList<TestInfo>();
		tests = db.getLessonTests(idLesson);
		testsListAdapter = new TestsListAdapter(this, tests);
		testsListView = (ListView) findViewById(R.id.tests_list_view);
		testsListView.setAdapter(testsListAdapter);
		testsListView.setOnItemClickListener(itemListener);
		downloadProgress = new ProgressDialog(context);
		downloadProgress.setMessage(getResources().getString(R.string.progress_test_load));
	}

	@Override
	protected void onStart() {
		Intent intent = new Intent(this, ApiService.class);
        bindService(intent, apiConnection, Context.BIND_AUTO_CREATE);
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (apiBound) {
			unbindService(apiConnection);
			apiBound = false;
		}
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

	public void invalidate() {
		tests = db.getLessonTests(idLesson);
		testsListAdapter.setTestsList(tests);
		testsListView.invalidateViews();
	}
	
}
