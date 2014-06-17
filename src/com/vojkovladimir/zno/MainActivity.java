package com.vojkovladimir.zno;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.vojkovladimir.zno.api.Api;

public class MainActivity extends Activity {

<<<<<<< HEAD
	public static String LOG_TAG = "MyLogs";
	private ZNOApplication app = ZNOApplication.getInstance();
=======
	Api api;
>>>>>>> b8bfea08fedccbb1edb739cd5ea9369c7b11127e

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
<<<<<<< HEAD

=======
		api = new Api(this);
>>>>>>> b8bfea08fedccbb1edb739cd5ea9369c7b11127e
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

	}

	public void lastPassedTests(View v) {

	}

	public void records(View v) {

	}

	public void settings(View v) {
		
	}
<<<<<<< HEAD
=======

	public void loadTests() {

	}
>>>>>>> b8bfea08fedccbb1edb739cd5ea9369c7b11127e
}
