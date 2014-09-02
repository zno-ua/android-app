package com.vojkovladimir.zno;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.fragments.QuestionFragment;
import com.vojkovladimir.zno.models.Test;

public class TestActivity extends FragmentActivity {

	public static String LOG_TAG = "MyLogs";

	ZNOApplication app;
	ZNODataBaseHelper db;

	Test test;

	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;

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

		test = db.getTest(tableName);

		mPager = (ViewPager) findViewById(R.id.test_question_pager);
		mPagerAdapter = new QuestionsAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
			// Open questions list
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

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
		if (mPager.getCurrentItem() != 0) {
			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
		} else {
			super.onBackPressed();
		}
	}

	private class QuestionsAdapter extends FragmentStatePagerAdapter implements
			QuestionFragment.QuestionActions {

		public QuestionsAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return QuestionFragment.newInstance(getApplicationContext(), test.questions.get(position), test.taskAll, test.lessonId,	this);
		}

		@Override
		public int getCount() {
			return test.taskAll;
		}

		@Override
		public void onAnswerSelected() {
			if (mPager.getCurrentItem() + 1 < test.questions.size()) {
				mPager.setCurrentItem(mPager.getCurrentItem() + 1);
			}
		}

	}

}
