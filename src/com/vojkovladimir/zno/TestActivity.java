package com.vojkovladimir.zno;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.vojkovladimir.zno.ZNOApplication.ExtrasKeys;
import com.vojkovladimir.zno.adapters.QuestionsAdapter;
import com.vojkovladimir.zno.adapters.QuestionsGridAdapter;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.fragments.QuestionFragment;
import com.vojkovladimir.zno.models.Test;

public class TestActivity extends FragmentActivity implements QuestionFragment.OnAnswerSelectedListener {

    public static String LOG_TAG = "MyLogs";

    private static final String TEST_ID = "test_id";
    private static final String USER_TEST_ID = "user_test_id";
    private static final String QUESTIONS_GRID_VISIBILITY = "q_gride_visibility";

    private ZNOApplication app;
    private ZNODataBaseHelper db;

    private Test test;
    private long userTestId = -1;
    private boolean questionsGridVisible;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private GridView questionsGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.i(LOG_TAG, "TestActivity: onCreate()");

        app = ZNOApplication.getInstance();
        db = app.getZnoDataBaseHelper();

        if (savedInstanceState == null) {
            int testId = getIntent().getIntExtra(ExtrasKeys.ID_TEST, -1);
            test = db.getTest(testId);
            questionsGridVisible = false;
        } else {
            int testId = savedInstanceState.getInt(TEST_ID);
            userTestId = savedInstanceState.getLong(USER_TEST_ID);
            test = db.getTest(testId);
            test.putAnswers(db.getSavedAnswers(userTestId));
            questionsGridVisible = savedInstanceState.getBoolean(QUESTIONS_GRID_VISIBILITY);
            Log.i(LOG_TAG, "TestActivity: restore");
        }

        mPager = (ViewPager) findViewById(R.id.test_question_pager);
        mPagerAdapter = new QuestionsAdapter(getApplicationContext(),getSupportFragmentManager(),test);
        mPager.setAdapter(mPagerAdapter);

        questionsGrid = (GridView) findViewById(R.id.test_questions);
        questionsGrid.setAdapter(new QuestionsGridAdapter(getApplicationContext(),test));
        questionsGrid.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPager.setCurrentItem(position);
                hideQuestionsGride();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(TEST_ID, test.id);
        outState.putBoolean(QUESTIONS_GRID_VISIBILITY, questionsGridVisible);
        if (userTestId == -1) {
            userTestId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers());
        } else {
            db.updateUserAnswers(userTestId, test.lessonId, test.id, test.getAnswers());
        }
        outState.putLong(USER_TEST_ID, userTestId);
        super.onSaveInstanceState(outState);
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
                if (questionsGridVisible) {
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
        questionsGrid.invalidateViews();
        questionsGrid.bringToFront();
        questionsGrid.setVisibility(View.VISIBLE);
        mPager.setVisibility(View.INVISIBLE);
        questionsGridVisible = true;
    }

    private void hideQuestionsGride() {
        questionsGrid.setVisibility(View.INVISIBLE);
        mPager.setVisibility(View.VISIBLE);
        questionsGridVisible = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onAnswerSelected(int id, String answer) {
        if (mPager.getCurrentItem() + 1 < test.questions.size()) {
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        }
        test.questions.get(id).answer = answer;
    }
}
