package com.vojkovladimir.zno;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.fragments.AnswersFragment;
import com.vojkovladimir.zno.fragments.QuestionFragment;
import com.vojkovladimir.zno.models.Question;
import com.vojkovladimir.zno.models.Test;

public class TestActivity extends Activity {

	public static String LOG_TAG = "MyLogs";

	ZNOApplication app;
	ZNODataBaseHelper db;

	Test test;

	FragmentManager manager;
	FragmentTransaction transaction;
	Fragment currentQuestion;

	Button next;

	int current;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		Log.i(LOG_TAG, "TestActivity: onCreate()");

		Intent intent = getIntent();
		setTitle(intent.getStringExtra(ZNOApplication.ExtrasKeys.LESSON_NAME)
				+ " " + intent.getIntExtra(ZNOApplication.ExtrasKeys.YEAR, 0));

		app = ZNOApplication.getInstance();
		db = app.getZnoDataBaseHelper();
		manager = getFragmentManager();

		String dbName = intent
				.getStringExtra(ZNOApplication.ExtrasKeys.DB_NAME);

		test = db.getTest(dbName);
		current = 0;

		next = (Button) findViewById(R.id.test_skip_btn);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
//
//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		outState.putInt(KEY_CURRENT, current);
//		Log.i(LOG_TAG, "TestActivity: onSaveInstanceState()");
//		super.onSaveInstanceState(outState);
//	}
//
//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		Log.i(LOG_TAG, "TestActivity: onRestoreInstanceState()");
//		current = savedInstanceState.getInt(KEY_CURRENT);
//		super.onRestoreInstanceState(savedInstanceState);
//	}

	@Override
	protected void onStart() {
		Log.i(LOG_TAG, "TestActivity: onStart()");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.i(LOG_TAG, "TestActivity: onResume()");
		loadQuestion();
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

	public void skip(View v) {

		if (current < test.tasksNum) {
			current++;
		} else {
			//end of testing
			//print result
			current = 0;
		}
		loadQuestion();
	}

	@Override
	public void onBackPressed() {
		Log.i(LOG_TAG, "TestActivity: onBackPressed()");
		if (current > 0) {
			current--;
			loadQuestion();
		} else {
			super.onBackPressed();
		}
	}

	private void loadQuestion() {
		transaction = manager.beginTransaction();

		Question currQuest = test.questions.get(current);

		currentQuestion = QuestionFragment.newIntstance(currQuest.id,
				test.tasksNum, currQuest.text);
		
		AnswersFragment currentQuestionAnswers = AnswersFragment.newIntstance(currQuest.answers.split("\n")); 

		transaction.replace(R.id.test_question_container, currentQuestion);
		transaction.replace(R.id.test_answers_container, currentQuestionAnswers);
		transaction.commit();
	}

}
