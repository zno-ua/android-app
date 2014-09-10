package com.vojkovladimir.zno;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.vojkovladimir.zno.ZNOApplication.ExtrasKeys;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.fragments.QuestionFragment;
import com.vojkovladimir.zno.models.Question;
import com.vojkovladimir.zno.models.Test;

public class TestActivity extends FragmentActivity {

	public static String LOG_TAG = "MyLogs";

	ZNOApplication app;
	ZNODataBaseHelper db;

	Test test;

	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private GridView questionsGride;
	
	boolean questionsGrideVisible = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Log.i(LOG_TAG, "TestActivity: onCreate()");

		Intent intent = getIntent();
		int testId = intent.getIntExtra(ExtrasKeys.ID_TEST, -1);

		app = ZNOApplication.getInstance();
		db = app.getZnoDataBaseHelper();

		test = db.getTest(testId);

		mPager = (ViewPager) findViewById(R.id.test_question_pager);
		mPagerAdapter = new QuestionsAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		
		questionsGride = (GridView) findViewById(R.id.test_questions);
		questionsGride.setAdapter(new QuestionsGrideAdapter());
		questionsGride.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mPager.setCurrentItem(position);
				hideQuestionsGride();
			}
		});
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
			if (questionsGrideVisible) {
				hideQuestionsGride();
			} else {
				showQuestionsGride();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}
	
	private void showQuestionsGride() {
		questionsGride.invalidateViews();
		questionsGride.bringToFront();
		questionsGride.setVisibility(View.VISIBLE);
		mPager.setVisibility(View.INVISIBLE);
		questionsGrideVisible = true;
	}
	
	private void hideQuestionsGride() {
		questionsGride.setVisibility(View.INVISIBLE);
		mPager.setVisibility(View.VISIBLE);
		questionsGrideVisible = false;
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
			return QuestionFragment.newInstance(getApplicationContext(), test.questions.get(position), test.taskAll, test.lessonId, this);
		}

		@Override
		public int getCount() {
			return test.questions.size();
		}

		@Override
		public void onAnswerSelected() {
			if (mPager.getCurrentItem() + 1 < test.questions.size()) {
				mPager.setCurrentItem(mPager.getCurrentItem() + 1);
			}
		}

	}

	static class ViewHolder {
		TextView questionNum;
	}
	
	class QuestionsGrideAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public QuestionsGrideAdapter() {
			inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return test.questions.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.test_questions_gride_item, parent, false);
				holder = new ViewHolder();
				holder.questionNum = (TextView) convertView.findViewById(R.id.test_questions_gride_item_num);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			Question question = test.questions.get(position);
			
			if (question.answer.isEmpty() || (question.typeQuestion == Question.TYPE_3 && question.answer.contains("0"))) {
				convertView.setBackgroundResource(R.drawable.item_background_unselected);
				holder.questionNum.setTextColor(getResources().getColorStateList(R.color.item_text_color));
			} else {
				convertView.setBackgroundResource(R.drawable.item_background_selected);
				holder.questionNum.setTextColor(getResources().getColorStateList(R.color.item_text_color_selected));
			}
			
			holder.questionNum.setText(String.valueOf(position + 1));
			
			return convertView;
		}

	}

}
