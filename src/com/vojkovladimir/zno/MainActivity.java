package com.vojkovladimir.zno;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	public static String LOG_TAG = "MyLogs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

	protected void onStart() {
		super.onStart();
	}

	protected void onStop() {
		super.onStop();
	}

	protected void onRestart() {
		super.onRestart();
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onResume() {
		super.onResume();
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	public void beginTesting(View v) {
		Intent testingActivity = new Intent(this, TestingActivity.class);
		startActivity(testingActivity);
	}

	public void lastPassedTests(View v) {

	}

	public void records(View v) {

	}

	public void settings(View v) {
		
	}
}