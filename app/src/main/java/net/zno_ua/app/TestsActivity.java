package net.zno_ua.app;

import android.app.ActionBar;
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
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;

import net.zno_ua.app.adapters.TestsAdapter;
import net.zno_ua.app.db.ZNODataBaseHelper;
import net.zno_ua.app.models.Lesson;
import net.zno_ua.app.models.Test;
import net.zno_ua.app.models.TestInfo;
import net.zno_ua.app.service.ApiService;
import net.zno_ua.app.service.ApiService.ApiBinder;
import net.zno_ua.app.service.ApiService.TestDLCallBack;

import java.io.IOException;

public class TestsActivity extends Activity implements TestDLCallBack, OnItemClickListener {

    final Context context = this;

    ZNOApplication app;
    ZNODataBaseHelper db;

    ListView testsListView;
    TestsAdapter testsAdapter;

    ProgressDialog dlProgress;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tests);
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        app = ZNOApplication.getInstance();
        db = app.getZnoDataBaseHelper();

        Intent intent = getIntent();

        setTitle(intent.getStringExtra(Lesson.LESSON_NAME));
        idLesson = intent.getIntExtra(Lesson.LESSON_ID, -1);
        testsAdapter = new TestsAdapter(this, db.getLessonTests(idLesson));
        testsListView = (ListView) findViewById(R.id.tests_list_view);
        testsListView.setAdapter(testsAdapter);
        testsListView.setOnItemClickListener(this);
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
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        final TestInfo test = (TestInfo) testsAdapter.getItem(position);

        if (test.loaded) {
            startTest(test.id);
        } else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setMessage(R.string.dialog_load_test_text);
            dialogBuilder.setPositiveButton(R.string.dialog_positive_text, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            dlProgress = new ProgressDialog(context);
                            dlProgress.setMessage(getString(R.string.progress_test_load));
                            dlProgress.setCancelable(false);
                            dlProgress.show();
                            apiService.downLoadTest(test.id, TestsActivity.this);
                            break;
                    }
                }
            });
            dialogBuilder.setNegativeButton(R.string.dialog_negative_text, null);
            dialogBuilder.create().show();
        }
    }

    public void invalidate(int id) {
        testsAdapter.setTestsList(db.getLessonTests(idLesson));
        testsListView.invalidateViews();
        testsListView.setSelection(testsAdapter.getTestPosition(id));
    }

    public void startTest(final int testId) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage(getString(R.string.dialog_start_test_text));
        dialogBuilder.setPositiveButton(R.string.dialog_positive_text, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Intent passTest = new Intent(TestsActivity.this, TestActivity.class);
                passTest.setAction(TestActivity.Action.PASS_TEST);
                passTest.putExtra(Test.TEST_ID, testId);
                passTest.putExtra(TestActivity.Extra.TIMER_MODE, true);
                startActivity(passTest);
            }
        });
        dialogBuilder.setNegativeButton(R.string.dialog_negative_text, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Intent passTest = new Intent(TestsActivity.this, TestActivity.class);
                passTest.setAction(TestActivity.Action.PASS_TEST);
                passTest.putExtra(Test.TEST_ID, testId);
                passTest.putExtra(TestActivity.Extra.TIMER_MODE, false);
                startActivity(passTest);
            }
        });
        dialogBuilder.create().show();
    }

    @Override
    public void onDownloadImagesStart(final int max) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dlProgress.dismiss();
                dlProgress = new ProgressDialog(context);
                dlProgress.setCancelable(false);
                dlProgress.setMessage(getResources().getString(R.string.progress_test_load_images));
                dlProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dlProgress.setMax(max);
                dlProgress.setProgress(0);
                dlProgress.show();
            }
        });
    }

    @Override
    public void onImageDownloaded() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dlProgress.incrementProgressBy(1);
            }
        });
    }

    @Override
    public void onSavingTest() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dlProgress.dismiss();
                dlProgress.setCancelable(false);
                dlProgress = new ProgressDialog(context);
                dlProgress.setMessage(getString(R.string.progress_test_save));
                dlProgress.show();
            }
        });
    }

    @Override
    public void onTestDownloaded(final int id) {
        runOnUiThread(new Runnable() {
            public void run() {
                dlProgress.dismiss();
                invalidate(id);
                Toast.makeText(getApplicationContext(), R.string.test_load_complete, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dlProgress.dismiss();
                int msgId;
                if (e instanceof NoConnectionError) {
                    msgId = R.string.error_no_connection;
                } else if (e instanceof TimeoutError) {
                    msgId = R.string.error_timeout;
                } else if (e instanceof ServerError) {
                    msgId = R.string.error_server_bad;
                } else if (e instanceof IOException) {
                    msgId = R.string.error_cant_save_bitmap;
                } else {
                    msgId = R.string.error_unknown;
                }
                Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
