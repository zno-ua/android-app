package com.vojkovladimir.zno;

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
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.vojkovladimir.zno.adapters.TestsAdapter;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.models.Lesson;
import com.vojkovladimir.zno.models.Test;
import com.vojkovladimir.zno.models.TestInfo;
import com.vojkovladimir.zno.service.ApiService;
import com.vojkovladimir.zno.service.ApiService.ApiBinder;
import com.vojkovladimir.zno.service.ApiService.TestDownloadingFeedBack;

import org.json.JSONException;

public class TestsActivity extends Activity {

	final Context context = this;

	ZNOApplication app;
	ZNODataBaseHelper db;

	ListView testsListView;
	TestsAdapter testsAdapter;

	ProgressDialog downloadProgress;

	ApiService apiService;
    boolean apiBound = false;
    int idLesson;

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

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            final TestInfo test = (TestInfo) testsAdapter.getItem(position);

			if (test.loaded) {
                startTest(test.id);
			} else {
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

				dialogBuilder.setMessage(R.string.dialog_load_test_text);
				dialogBuilder.setPositiveButton(R.string.dialog_positive_text,new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							downloadProgress = new ProgressDialog(context);
							downloadProgress.setCancelable(false);
							downloadProgress.setMessage(getResources().getString(R.string.progress_test_load));
							downloadProgress.show();
							apiService.downLoadTest(new TestDownloadingFeedBack() {

								@Override
								public void onTestLoaded() {
									runOnUiThread(new Runnable() {
										public void run() {
											invalidate();
										}
									});
									downloadProgress.dismiss();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            startTest(test.id);
                                        }
                                    });
								}

								@Override
								public void onError(Exception e) {
									downloadProgress.dismiss();
									if (e instanceof VolleyError) {
										if ( e instanceof NoConnectionError) {
											Toast.makeText(context, getResources().getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
										}else if (e instanceof TimeoutError) {
											Toast.makeText(context, getResources().getString(R.string.error_timeout), Toast.LENGTH_SHORT).show();
										}
									} else if (e instanceof JSONException) {
									}
								}

								@Override
								public void onExtraDownloadingStart(int max) {
									downloadProgress.dismiss();
									downloadProgress = new ProgressDialog(context);
									downloadProgress.setCancelable(false);
									downloadProgress.setMessage(getResources().getString(R.string.progress_test_load_images));
									downloadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
									downloadProgress.setMax(max);
									downloadProgress.setProgress(0);
									downloadProgress.show();
								}

								@Override
								public void onExtraDownloadingProgressInc() {
									downloadProgress.incrementProgressBy(1);
								}
							}, test.id);
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
		setContentView(R.layout.activity_tests);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		app = ZNOApplication.getInstance();
		db = app.getZnoDataBaseHelper();

		Intent intent = getIntent();

		setTitle(intent.getStringExtra(Lesson.LESSON_NAME));
		idLesson = intent.getIntExtra(Lesson.LESSON_ID, -1);
		testsAdapter = new TestsAdapter(this, db.getLessonTests(idLesson));
		testsListView = (ListView) findViewById(R.id.tests_list_view);
		testsListView.setAdapter(testsAdapter);
		testsListView.setOnItemClickListener(itemListener);
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
		testsAdapter.setTestsList(db.getLessonTests(idLesson));
		testsListView.invalidateViews();
	}

    public void startTest(final int testId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage("Розпочати тест з урахуванням часу?");
        dialogBuilder.setPositiveButton(R.string.dialog_positive_text,new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Intent passTest = new Intent(TestActivity.Action.PASS_TEST);
                passTest.putExtra(Test.TEST_ID, testId);
                passTest.putExtra(TestActivity.Extra.TIMER_MODE, true);
                startActivity(passTest);
            }
        });
        dialogBuilder.setNegativeButton(R.string.dialog_negative_text, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Intent passTest = new Intent(TestActivity.Action.PASS_TEST);
                passTest.putExtra(Test.TEST_ID, testId);
                passTest.putExtra(TestActivity.Extra.TIMER_MODE, false);
                startActivity(passTest);
            }
        });
        dialogBuilder.create().show();
    }

}
