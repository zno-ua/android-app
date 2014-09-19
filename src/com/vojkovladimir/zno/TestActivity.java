package com.vojkovladimir.zno;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.vojkovladimir.zno.models.Question;
import com.vojkovladimir.zno.models.Test;

public class TestActivity extends FragmentActivity implements QuestionFragment.OnAnswerSelectedListener {

    public static String LOG_TAG = "MyLogs";

    public interface Action {
        String VIEW_TEST = "com.vojkovladimir.zno.VIEW_TEST";
        String PASS_TEST = "com.vojkovladimir.zno.PASS_TEST";
        String CONTINUE_PASSAGE_TEST = "com.vojkovladimir.zno.CONTINUE_PASSAGE_TEST";
    }

    public interface Extra {
        String USER_ANSWERS_ID = "user_answers_id";
        String QUESTIONS_GRID_VISIBILITY = "q_grid_visibility";
        String VIEW_MODE = "view_mode";
        String RESUMED = "resumed";
        String QUESTION_NUMBER = "q_num";
    }

    private ZNOApplication app;
    private ZNODataBaseHelper db;

    private boolean viewMode;
    private boolean resumed;
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

        int startItemNum = 0;

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (action.equals(Action.PASS_TEST)) {
                int testId = intent.getIntExtra(Test.TEST_ID, -1);
                test = db.getTest(testId);
                resumed = false;
                viewMode = false;
            } else if (action.equals(Action.CONTINUE_PASSAGE_TEST)) {
                userAnswersId = intent.getIntExtra(Extra.USER_ANSWERS_ID, -1);
                int testId = intent.getIntExtra(Test.TEST_ID, -1);
                test = db.getTest(testId);
                test.putAnswers(db.getSavedAnswers(userAnswersId));
                resumed = true;
                viewMode = false;
                startItemNum = intent.getIntExtra(Extra.QUESTION_NUMBER, 0);
            } else if (action.equals(Action.VIEW_TEST)) {
                int testId = intent.getIntExtra(Test.TEST_ID, -1);
                userAnswersId = intent.getIntExtra(Extra.USER_ANSWERS_ID, -1);
                test = db.getTest(testId);
                test.putAnswers(db.getSavedAnswers(userAnswersId));
                viewMode = true;
                resumed = intent.getBooleanExtra(Extra.RESUMED, false);
            } else {
                if (resumed) {
                    Intent main = new Intent(this, MainActivity.class);
                    startActivity(main);
                }
                finish();
            }
        } else {
            int testId = savedInstanceState.getInt(Test.TEST_ID);
            userAnswersId = savedInstanceState.getInt(Extra.USER_ANSWERS_ID);
            test = db.getTest(testId);
            test.putAnswers(db.getSavedAnswers(userAnswersId));
            questionsGridVisible = savedInstanceState.getBoolean(Extra.QUESTIONS_GRID_VISIBILITY);
            viewMode = savedInstanceState.getBoolean(Extra.VIEW_MODE);
            resumed = savedInstanceState.getBoolean(Extra.RESUMED);
        }

        mPager = (ViewPager) findViewById(R.id.test_question_pager);
        mPagerAdapter = new QuestionsAdapter(getApplicationContext(), getSupportFragmentManager(), test, viewMode);
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(startItemNum);

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
        if (!viewMode) {
            if (userAnswersId == -1) {
                userAnswersId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers());
            } else {
                db.updateUserAnswers(userAnswersId, test.lessonId, test.id, test.getAnswers());
            }
            SharedPreferences preferences = getSharedPreferences(app.APP_SETTINGS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(Test.TEST_ID, test.id);
            editor.putInt(Extra.USER_ANSWERS_ID, userAnswersId);
            editor.putInt(Extra.QUESTION_NUMBER, mPager.getCurrentItem());
            editor.apply();
        }
        outState.putInt(Test.TEST_ID, test.id);
        outState.putBoolean(Extra.QUESTIONS_GRID_VISIBILITY, questionsGridVisible);
        outState.putInt(Extra.USER_ANSWERS_ID, userAnswersId);
        outState.putBoolean(Extra.VIEW_MODE, viewMode);
        outState.putBoolean(Extra.RESUMED, resumed);
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
                    if (resumed) {
                        Intent main = new Intent(this, MainActivity.class);
                        startActivity(main);
                    }
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
            if (resumed) {
                Intent main = new Intent(this, MainActivity.class);
                startActivity(main);
            }
            finish();
        } else {
            showCancelTestAlert();
        }
    }

    @Override
    public void onAnswerSelected(int id, String answer, boolean switchToNext) {
        test.questions.get(id).userAnswer = answer;
        if (switchToNext) {
            if (test.hasUnAnsweredQuestions()) {
                if (mPager.getCurrentItem() + 1 < test.questions.size()) {
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                }
            } else {
                Question question = test.questions.get(test.questions.size() - 1);
                if (question.type == Question.TYPE_2 && question.balls != 0 && !question.userAnswer.isEmpty()) {
                    showConfirmFinishTestAlert();
                }
            }
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
                        if (resumed) {
                            Intent main = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(main);
                            SharedPreferences preferences = getSharedPreferences(app.APP_SETTINGS, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.remove(Test.TEST_ID);
                            editor.remove(Extra.USER_ANSWERS_ID);
                            editor.remove(Extra.QUESTION_NUMBER);
                            editor.apply();
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

        if (test.hasUnAnsweredQuestions()) {
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
                            if (resumed) {
                                SharedPreferences preferences = getSharedPreferences(app.APP_SETTINGS, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.remove(Test.TEST_ID);
                                editor.remove(Extra.USER_ANSWERS_ID);
                                editor.remove(Extra.QUESTION_NUMBER);
                                editor.apply();
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
                        if (resumed) {
                            SharedPreferences preferences = getSharedPreferences(app.APP_SETTINGS, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.remove(Test.TEST_ID);
                            editor.remove(Extra.USER_ANSWERS_ID);
                            editor.remove(Extra.QUESTION_NUMBER);
                            editor.apply();
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
                        viewResults.putExtra(Test.TEST_ID, test.id);
                        viewResults.putExtra(Extra.USER_ANSWERS_ID, userAnswersId);
                        viewResults.putExtra(Extra.RESUMED, resumed);
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
                        if (resumed) {
                            Intent main = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(main);
                        }
                        finish();
                        break;
                }
            }
        });
        dialogBuilder.setCancelable(false);
        dialogBuilder.create().show();
    }

}
