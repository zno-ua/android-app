package com.vojkovladimir.zno;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static String LOG_TAG = "MyLogs";

	String[] quotes;
	String quoteTitle;
	TextView quote;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		quote = (TextView) findViewById(R.id.quote);
		quotes = getResources().getStringArray(R.array.quotes_2011);
		quoteTitle = getResources().getString(R.string.quotes_2011_title);
	}

	protected void onStart() {
		super.onStart();
		refreshQuote(null);
	}

	protected void onStop() {
		super.onStop();
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

	public void refreshQuote(View v) {
		String text = quoteTitle + "<br>";
		Random rand = new Random();
		int num = rand.nextInt(quotes.length);
		text += quotes[num];
		quote.setText(Html.fromHtml(text));

	}
}