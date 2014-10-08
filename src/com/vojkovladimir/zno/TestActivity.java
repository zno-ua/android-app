package com.vojkovladimir.zno;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import com.vojkovladimir.zno.adapters.QuestionsAdapter;
import com.vojkovladimir.zno.adapters.QuestionsGridAdapter;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.fragments.QuestionFragment;
import com.vojkovladimir.zno.fragments.TestTimerFragment;
import com.vojkovladimir.zno.models.Question;
import com.vojkovladimir.zno.models.Test;

public class TestActivity extends FragmentActivity implements QuestionFragment.OnAnswerSelectedListener, TestTimerFragment.OnTimerStates {

    public interface Action {
        String VIEW_TEST = "com.vojkovladimir.zno.VIEW_TEST";
        String PASS_TEST = "com.vojkovladimir.zno.PASS_TEST";
        String CONTINUE_PASSAGE_TEST = "com.vojkovladimir.zno.CONTINUE_PASSAGE_TEST";
    }

    public interface Extra {
        String USER_ANSWERS_ID = "user_answers_id";
        String QUESTIONS_GRID_VISIBILITY = "q_grid_visibility";
        String VIEW_MODE = "view_mode";
        String TIMER_MODE = "timer_mode";
        String RESUMED = "resumed";
        String QUESTION_NUMBER = "q_num";
    }

    ZNOApplication app;
    ZNODataBaseHelper db;

    boolean viewMode;
    boolean resumed;

    boolean timerMode;
    long millisLeft;
    MenuItem timerAction;

    Test test;
    int userAnswersId;

    boolean questionsGridVisible;
    ViewPager mPager;
    PagerAdapter mPagerAdapter;
    GridView questionsGrid;

    FragmentManager fm;
    TestTimerFragment timerFragment;
    Handler timerFragmentHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MyLogs", "onCreate");
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
                userAnswersId = -1;
                test = db.getTest(testId);
                timerMode = intent.getBooleanExtra(Extra.TIMER_MODE, false);
                resumed = false;
                viewMode = false;
                timerMode = intent.getBooleanExtra(Extra.TIMER_MODE, false);
                if (timerMode) {
                    millisLeft = test.time * 60000;
                }
            } else if (action.equals(Action.CONTINUE_PASSAGE_TEST)) {
                userAnswersId = intent.getIntExtra(Extra.USER_ANSWERS_ID, -1);
                int testId = intent.getIntExtra(Test.TEST_ID, -1);
                test = db.getTest(testId);
                test.putAnswers(db.getUserAnswers(userAnswersId));
                resumed = true;
                viewMode = false;
                timerMode = intent.hasExtra(TestTimerFragment.MILLIS_LEFT);
                if (timerMode) {
                    millisLeft = intent.getLongExtra(TestTimerFragment.MILLIS_LEFT, test.time * 60000);
                    if (millisLeft == -1) {
                        millisLeft = test.time * 60000;
                    }
                }
                startItemNum = intent.getIntExtra(Extra.QUESTION_NUMBER, 0);
            } else if (action.equals(Action.VIEW_TEST)) {
                int testId = intent.getIntExtra(Test.TEST_ID, -1);
                userAnswersId = intent.getIntExtra(Extra.USER_ANSWERS_ID, -1);
                test = db.getTest(testId);
                test.putAnswers(db.getUserAnswers(userAnswersId));
                viewMode = true;
                resumed = intent.getBooleanExtra(Extra.RESUMED, false);
                timerMode = false;
            }
        } else {
            int testId = savedInstanceState.getInt(Test.TEST_ID);
            userAnswersId = savedInstanceState.getInt(Extra.USER_ANSWERS_ID);
            test = db.getTest(testId);
            test.putAnswers(db.getUserAnswers(userAnswersId));
            questionsGridVisible = savedInstanceState.getBoolean(Extra.QUESTIONS_GRID_VISIBILITY);
            viewMode = savedInstanceState.getBoolean(Extra.VIEW_MODE);
            resumed = savedInstanceState.getBoolean(Extra.RESUMED);
            if (!viewMode) {
                timerMode = savedInstanceState.containsKey(TestTimerFragment.MILLIS_LEFT);
                if (timerMode) {
                    millisLeft = savedInstanceState.getLong(TestTimerFragment.MILLIS_LEFT);
                }
            }
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
        fm = getSupportFragmentManager();
        timerFragmentHandler = new Handler();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i("MyLogs", "onSave");
        if (!viewMode) {
            if (userAnswersId == -1) {
                userAnswersId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers());
            } else {
                userAnswersId = db.updateUserAnswers(userAnswersId, test.getAnswers());
            }
            SharedPreferences preferences = getSharedPreferences(app.APP_SETTINGS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(Test.TEST_ID, test.id);
            editor.putInt(Extra.USER_ANSWERS_ID, userAnswersId);
            editor.putInt(Extra.QUESTION_NUMBER, mPager.getCurrentItem());
            if (timerMode) {
                millisLeft = timerFragment.getMillisLeft();
                editor.putLong(TestTimerFragment.MILLIS_LEFT, timerFragment.getMillisLeft());
                outState.putLong(TestTimerFragment.MILLIS_LEFT, timerFragment.getMillisLeft());
            }
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
    protected void onStart() {
        Log.i("MyLogs", "onStart");
        super.onStart();
        if (timerMode) {
            timerFragment = TestTimerFragment.newInstance(millisLeft);
            showTimerFragment();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timerMode) {
            timerFragment.cancel();
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(timerFragment);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (viewMode) {
            inflater.inflate(R.menu.test_menu_view_mode, menu);
        } else {
            inflater.inflate(R.menu.test_menu, menu);
            timerAction = menu.findItem(R.id.action_time);
            if (millisLeft <= 600000) {
                timerAction.setIcon(getResources().getDrawable(R.drawable.ic_action_time_low));
            }
            if (!timerMode) {
                timerAction.setVisible(false);
            }
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
                return true;
            case R.id.action_time:
                showTimerFragment();
                return true;
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
        dialogBuilder.setMessage(R.string.cancel_test_confirm);
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
                        if (timerMode) {
                            timerFragment.cancel();
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
                            long elapsedTime = 0;
                            if (timerMode) {
                                millisLeft = timerFragment.getMillisLeft();
                                elapsedTime = test.time * 60000 - millisLeft;
                            }
                            long date = System.currentTimeMillis();

                            if (userAnswersId == -1) {
                                userAnswersId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers());
                                userAnswersId = db.completeUserAnswers(userAnswersId, znoBall, elapsedTime, date);
                            } else {
                                userAnswersId = db.updateUserAnswers(userAnswersId, test.getAnswers());
                                userAnswersId = db.completeUserAnswers(userAnswersId, znoBall, elapsedTime, date);
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
                        long elapsedTime = 0;
                        if (timerMode) {
                            millisLeft = timerFragment.getMillisLeft();
                            elapsedTime = test.time * 60000 - millisLeft;
                        }
                        long date = System.currentTimeMillis();

                        if (userAnswersId == -1) {
                            userAnswersId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers());
                            userAnswersId = db.completeUserAnswers(userAnswersId, znoBall, elapsedTime, date);
                        } else {
                            userAnswersId = db.updateUserAnswers(userAnswersId, test.getAnswers());
                            userAnswersId = db.completeUserAnswers(userAnswersId, znoBall, elapsedTime, date);
                        }

                        if (resumed) {
                            SharedPreferences preferences = getSharedPreferences(app.APP_SETTINGS, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.remove(Test.TEST_ID);
                            editor.remove(Extra.USER_ANSWERS_ID);
                            editor.remove(Extra.QUESTION_NUMBER);
                            if (preferences.contains(TestTimerFragment.MILLIS_LEFT)) {
                                editor.remove(TestTimerFragment.MILLIS_LEFT);
                            }
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
        if (timerMode) {
            timerFragment.cancel();
        }
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

    @Override
    public void onTick(long millisInFuture) {
        int minutesLeft = (int) (millisInFuture / 60000);
        if (minutesLeft % 30 == 0
                || (minutesLeft < 30 && minutesLeft % 10 == 0)
                || (minutesLeft < 10 && minutesLeft % 5 == 0)) {
            showTimerFragment();
        }
        if (minutesLeft == 10) {
            timerAction.setIcon(getResources().getDrawable(R.drawable.ic_action_time_low));
        }
    }

    @Override
    public void onFinish() {
        final int testBall = test.getTestBall();
        final float znoBall = Float.parseFloat(db.getTestBalls(test.id)[testBall]);
        long elapsedTime = 0;
        if (timerMode) {
            millisLeft = timerFragment.getMillisLeft();
            elapsedTime = test.time * 60000 - millisLeft;
        }
        long date = System.currentTimeMillis();

        if (userAnswersId == -1) {
            userAnswersId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers());
            userAnswersId = db.completeUserAnswers(userAnswersId, znoBall, elapsedTime, date);
        } else {
            userAnswersId = db.updateUserAnswers(userAnswersId, test.getAnswers());
            userAnswersId = db.completeUserAnswers(userAnswersId, znoBall, elapsedTime, date);
        }

        if (resumed) {
            SharedPreferences preferences = getSharedPreferences(app.APP_SETTINGS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(Test.TEST_ID);
            editor.remove(Extra.USER_ANSWERS_ID);
            editor.remove(Extra.QUESTION_NUMBER);
            if (preferences.contains(TestTimerFragment.MILLIS_LEFT)) {
                editor.remove(TestTimerFragment.MILLIS_LEFT);
            }
            editor.apply();
        }
        showTestResults(testBall, znoBall);
    }

    public void showTimerFragment() {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
        if (!timerFragment.isAdded()) {
            ft.add(R.id.test_content_container, timerFragment);
        } else if (timerFragment.isHidden()) {
            ft.show(timerFragment);
        } else {
            return;
        }
        ft.commitAllowingStateLoss();
        timerFragmentHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (timerFragment.isAdded()) {
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);
                    ft.hide(timerFragment);
                    ft.commitAllowingStateLoss();
                }
            }
        }, TestTimerFragment.SHOW_TIME);
    }

}
