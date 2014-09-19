package com.vojkovladimir.zno;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.vojkovladimir.zno.adapters.QuestionsAdapter;
import com.vojkovladimir.zno.adapters.QuestionsGridAdapter;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.fragments.QuestionFragment;
import com.vojkovladimir.zno.models.Test;

public class TestActivity extends FragmentActivity implements QuestionFragment.OnAnswerSelectedListener {

    public static String LOG_TAG = "MyLogs";

    public interface Action {
        String VIEW_TEST = "com.vojkovladimir.zno.VIEW_TEST";
        String PASS_TEST = "com.vojkovladimir.zno.PASS_TEST";
        String CONTINUE_PASSAGE_TEST = "com.vojkovladimir.zno.CONTINUE_PASSAGE_TEST";
    }

    public interface Extra {
        String TEST_ID = "test_id";
        String USER_ANSWERS_ID = "user_answers_id";
        String QUESTIONS_GRID_VISIBILITY = "q_grid_visibility";
        String VIEW_MODE = "view_mode";
    }

    private ZNOApplication app;
    private ZNODataBaseHelper db;

    private boolean viewMode;
    private Test test;
    private int userAnswersId = -1;
    private boolean questionsGridVisible;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private GridView questionsGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        app = ZNOApplication.getInstance();
        db = app.getZnoDataBaseHelper();

        if (savedInstanceState == null) {
            String action = getIntent().getAction();
            if (action.equals(Action.PASS_TEST)) {
                int testId = getIntent().getIntExtra(Extra.TEST_ID, -1);
                test = db.getTest(testId);
            } else if (action.equals(Action.CONTINUE_PASSAGE_TEST)) {
                userAnswersId = getIntent().getIntExtra(Extra.USER_ANSWERS_ID, -1);
            } else if (action.equals(Action.VIEW_TEST)) {
                int testId = getIntent().getIntExtra(Extra.TEST_ID, -1);
                userAnswersId = getIntent().getIntExtra(Extra.USER_ANSWERS_ID, -1);
                test = db.getTest(testId);
                test.putAnswers(db.getSavedAnswers(userAnswersId));
                viewMode = true;
            } else {
                finish();
            }
        } else {
            int testId = savedInstanceState.getInt(Extra.TEST_ID);
            userAnswersId = savedInstanceState.getInt(Extra.USER_ANSWERS_ID);
            test = db.getTest(testId);
            test.putAnswers(db.getSavedAnswers(userAnswersId));
            questionsGridVisible = savedInstanceState.getBoolean(Extra.QUESTIONS_GRID_VISIBILITY);
            viewMode = savedInstanceState.getBoolean(Extra.VIEW_MODE);
        }

        mPager = (ViewPager) findViewById(R.id.test_question_pager);
        mPagerAdapter = new QuestionsAdapter(getApplicationContext(), getSupportFragmentManager(), test, viewMode);
        mPager.setAdapter(mPagerAdapter);

        questionsGrid = (GridView) findViewById(R.id.test_questions);
        questionsGrid.setAdapter(new QuestionsGridAdapter(getApplicationContext(), test, viewMode));
        questionsGrid.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPager.setCurrentItem(position);
                hideQuestionsGrid();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Extra.TEST_ID, test.id);
        outState.putBoolean(Extra.QUESTIONS_GRID_VISIBILITY, questionsGridVisible);
        if (!viewMode) {
            if (userAnswersId == -1) {
                userAnswersId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers());
            } else {
                db.updateUserAnswers(userAnswersId, test.lessonId, test.id, test.getAnswers());
            }
        }
        outState.putInt(Extra.USER_ANSWERS_ID, userAnswersId);
        outState.putBoolean(Extra.VIEW_MODE, viewMode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (viewMode) {
            inflater.inflate(R.menu.test_menu_view_mode, menu);
        } else {
            inflater.inflate(R.menu.test_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (viewMode) {
                    finish();
                } else {
                    showCancelTestAlert();
                }
                return true;
            case R.id.action_questions_list:
                if (questionsGridVisible) {
                    hideQuestionsGrid();
                } else {
                    showQuestionsGrid();
                }
                return true;
            case R.id.action_finish_testing:
                showFinishTestAlert();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showQuestionsGrid() {
        questionsGrid.invalidateViews();
        questionsGrid.bringToFront();
        questionsGrid.setVisibility(View.VISIBLE);
        mPager.setVisibility(View.INVISIBLE);
        questionsGridVisible = true;
    }

    private void hideQuestionsGrid() {
        questionsGrid.setVisibility(View.INVISIBLE);
        mPager.setVisibility(View.VISIBLE);
        questionsGridVisible = false;
    }

    @Override
    public void onBackPressed() {
        if (viewMode) {
            finish();
        } else {
            showCancelTestAlert();
        }
    }

    @Override
    public void onAnswerSelected(int id, String answer, boolean switchToNext) {
        if (switchToNext) {
            if (mPager.getCurrentItem() + 1 < test.questions.size()) {
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
            }
        }
        test.questions.get(id).userAnswer = answer;
        if (!test.hasUnAnswerdQuestions()) {
            showConfirmFinishTestAlert();
        }
    }

    public void showCancelTestAlert() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.cancle_test_confirm);
        dialogBuilder.setPositiveButton(R.string.dialog_positive_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (userAnswersId != -1) {
                            db.deleteUserAnswers(userAnswersId);
                        }
                        finish();
                        break;
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.dialog_negative_text, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.create().show();
    }

    public void showFinishTestAlert() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        if (test.hasUnAnswerdQuestions()) {
            dialogBuilder.setMessage(getString(R.string.has_unanswered_questions) + "\n" + getString(R.string.want_to_finish));
            dialogBuilder.setPositiveButton(R.string.dialog_positive_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            final int testBall = test.getTestBall();
                            final float znoBall = Float.parseFloat(db.getTestBalls(test.id)[testBall]);
                            if (userAnswersId == -1) {
                                userAnswersId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers(), testBall, znoBall);
                            } else {
                                userAnswersId = db.updateUserAnswers(userAnswersId, test.lessonId, test.id, test.getAnswers(), testBall, znoBall);
                            }
                            showTestResults(testBall, znoBall);
                            break;
                    }
                }
            });
            dialogBuilder.setNegativeButton(R.string.dialog_negative_text, null);
            dialogBuilder.setCancelable(false);
            dialogBuilder.create().show();
        } else {
            showConfirmFinishTestAlert();
        }
    }

    public void showConfirmFinishTestAlert() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(getString(R.string.want_to_finish));
        dialogBuilder.setPositiveButton(R.string.dialog_positive_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        final int testBall = test.getTestBall();
                        final float znoBall = Float.parseFloat(db.getTestBalls(test.id)[testBall]);
                        if (userAnswersId == -1) {
                            userAnswersId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers(), testBall, znoBall);
                        } else {
                            userAnswersId = db.updateUserAnswers(userAnswersId, test.lessonId, test.id, test.getAnswers(), testBall, znoBall);
                        }
                        showTestResults(testBall, znoBall);
                        break;
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.dialog_negative_text, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.create().show();
    }

    public void showTestResults(int testBall, float znoBall) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Тест Завершено!\nтестовий бал: " + testBall + "\n" + "рейтинговий бал: " + znoBall + "\nподивитися помилки?");
        dialogBuilder.setPositiveButton("Так", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent viewResults = new Intent(TestActivity.Action.VIEW_TEST);
                        viewResults.putExtra(TestActivity.Extra.TEST_ID, test.id);
                        viewResults.putExtra(TestActivity.Extra.USER_ANSWERS_ID, userAnswersId);
                        startActivity(viewResults);
                        finish();
                        break;
                }
            }
        });
        dialogBuilder.setNegativeButton("Ні", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        finish();
                        break;
                }
            }
        });
        dialogBuilder.setCancelable(false);
        dialogBuilder.create().show();
    }

}
