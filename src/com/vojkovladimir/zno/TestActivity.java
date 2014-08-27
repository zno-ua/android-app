package com.vojkovladimir.zno;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.vojkovladimir.zno.adapters.QuestionFragmentAdapter;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.models.Test;

public class TestActivity extends Activity {

	public static String LOG_TAG = "MyLogs";

	ZNOApplication app;
	ZNODataBaseHelper db;

	Test test;

	QuestionFragmentAdapter questionsAdapter;
	
	Button nextBtn;
	OnClickListener onNext = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			questionsAdapter.next();			
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Log.i(LOG_TAG, "TestActivity: onCreate()");

		Intent intent = getIntent();
		String tableName = intent.getStringExtra(ZNOApplication.ExtrasKeys.TABLE_NAME);

		app = ZNOApplication.getInstance();
		db = app.getZnoDataBaseHelper();
		
		nextBtn = (Button)findViewById(R.id.test_skip_btn);
		nextBtn.setOnClickListener(onNext);
		
		test = db.getTest(tableName);
		questionsAdapter = new QuestionFragmentAdapter(this, test, db);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.test_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_questions_list:
			//Open questions list
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}

	//
	// @Override
	// protected void onSaveInstanceState(Bundle outState) {
	// outState.putInt(KEY_CURRENT, current);
	// Log.i(LOG_TAG, "TestActivity: onSaveInstanceState()");
	// super.onSaveInstanceState(outState);
	// }
	//
	// @Override
	// protected void onRestoreInstanceState(Bundle savedInstanceState) {
	// Log.i(LOG_TAG, "TestActivity: onRestoreInstanceState()");
	// current = savedInstanceState.getInt(KEY_CURRENT);
	// super.onRestoreInstanceState(savedInstanceState);
	// }

	@Override
	protected void onStart() {
		Log.i(LOG_TAG, "TestActivity: onStart()");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.i(LOG_TAG, "TestActivity: onResume()");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.i(LOG_TAG, "TestActivity: onPause()");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.i(LOG_TAG, "TestActivity: onStop()");
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		Log.i(LOG_TAG, "TestActivity: onBackPressed()");
		if (questionsAdapter.getCurrent() > 0) {
			questionsAdapter.previous();
		} else {
			super.onBackPressed();
		}
	}

}
