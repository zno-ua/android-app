package com.vojkovladimir.zno;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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
import com.vojkovladimir.zno.fragments.TestTimerFragment;
import com.vojkovladimir.zno.models.Test;

public class TestActivity extends FragmentActivity implements QuestionFragment.OnAnswerSelectedListener,
        TestTimerFragment.OnTimeChangedListener {

    final int FINISH_ALERT = 0;
    final int CANCEL_ALERT = 1;

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
        String ASK_TO_FINISH = "ask_to_finish";
    }

    ZNOApplication app;
    ZNODataBaseHelper db;

    boolean viewMode;
    boolean resumed;
    boolean askToFinish;

    boolean timerMode;
    long millisLeft;
    MenuItem timerAction;

    Test test;
    int userAnswersId;

    boolean questionsGridVisible;
    ViewPager mPager;
    PagerAdapter mPagerAdapter;
    GridView questionsGrid;

    FragmentManager manager;
    TestTimerFragment timerFragment;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        app = ZNOApplication.getInstance();
        db = app.getZnoDataBaseHelper();
        manager = getSupportFragmentManager();

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
            askToFinish = true;
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
            askToFinish = savedInstanceState.getBoolean(Extra.ASK_TO_FINISH);
            if (timerMode) {
                timerFragment = (TestTimerFragment) manager.getFragment(savedInstanceState, TestTimerFragment.TAG);
            }
        }

        if (timerMode && timerFragment == null) {
            timerFragment = TestTimerFragment.newInstance(millisLeft);
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

        handler = new Handler();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!viewMode) {
            if (userAnswersId == -1) {
                userAnswersId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers());
            } else {
                db.updateUserAnswers(userAnswersId, test.getAnswers());
            }
            SharedPreferences preferences = getSharedPreferences(ZNOApplication.APP_SETTINGS, Context.MODE_PRIVATE);
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
        outState.putBoolean(Extra.ASK_TO_FINISH, askToFinish);
        if (timerMode) {
            manager.putFragment(outState, TestTimerFragment.TAG, timerFragment);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (timerMode && timerFragment != null) {
            showHideTimer();
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
                    showAlert(CANCEL_ALERT);
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
                showAlert(FINISH_ALERT);
                return true;
            case R.id.action_time:
                if (timerMode && timerFragment != null) {
                    showHideTimer();
                }
                return true;
            case R.id.action_share:
                /*
                    Write share
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);*/
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

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
            showAlert(CANCEL_ALERT);
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
    public void onAnswerSelected(int id, String answer, boolean switchToNext) {
        test.questions.get(id).userAnswer = answer;
        if (switchToNext) {
            if (test.hasUnAnsweredQuestions()) {
                if (mPager.getCurrentItem() + 1 < test.questions.size()) {
                    mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                }
            } else {
                if (askToFinish) {
                    showAlert(FINISH_ALERT);
                }
            }
        }
    }

    @Override
    public void onTimeIsUp() {
        millisLeft = 0;
        finishTest();
    }

    @Override
    public void onMinutePassed(long millisLeft) {
        int minutesLeft = (int) (millisLeft / 60000);
        if (minutesLeft % 30 == 0
                || (minutesLeft < 30 && minutesLeft % 10 == 0)
                || (minutesLeft < 10 && minutesLeft % 5 == 0)) {
            showHideTimer();
        }
        if (minutesLeft == 10) {
            timerAction.setIcon(getResources().getDrawable(R.drawable.ic_action_time_low));
        }
    }

    public void showHideTimer() {
        final Runnable hide = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() || !isDestroyed()) {
                    manager.beginTransaction()
                            .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                            .hide(timerFragment)
                            .commitAllowingStateLoss();
                }
            }
        };
        if (timerFragment.isAdded()) {
            if (timerFragment.isHidden()) {
                manager.beginTransaction()
                        .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                        .show(timerFragment)
                        .commit();
                handler.postDelayed(hide, TestTimerFragment.SHOW_TIME);
            } else if (timerFragment.isVisible()) {
                handler.postDelayed(hide, TestTimerFragment.SHOW_TIME);
            } else {
                manager.beginTransaction()
                        .hide(timerFragment)
                        .commit();
                manager.beginTransaction()
                        .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                        .show(timerFragment)
                        .commit();
                handler.postDelayed(hide, TestTimerFragment.SHOW_TIME);
            }
        } else {
            manager.beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out)
                    .add(R.id.test_content_container, timerFragment, TestTimerFragment.TAG)
                    .commit();
            handler.postDelayed(hide, TestTimerFragment.SHOW_TIME);
        }

    }

    public void showAlert(int type) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        DialogInterface.OnClickListener resume = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        askToFinish = false;
                        break;
                }
            }
        };

        DialogInterface.OnClickListener resumeWithAsk = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        askToFinish = true;
                        break;
                }
            }
        };

        DialogInterface.OnClickListener finish = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        finishTest();
                        break;
                }
            }
        };

        DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        cancelTest();
                        break;
                }
            }
        };

        switch (type) {
            case FINISH_ALERT:
                if (test.hasUnAnsweredQuestions()) {
                    String message = getString(R.string.has_unanswered_questions);
                    message += "\n" + getString(R.string.want_to_finish);
                    dialogBuilder.setMessage(message);
                    dialogBuilder.setNegativeButton(R.string.dialog_negative_text, resumeWithAsk);
                } else {
                    dialogBuilder.setMessage(R.string.want_to_finish);
                    dialogBuilder.setNegativeButton(R.string.dialog_negative_text, resume);
                }
                dialogBuilder.setPositiveButton(R.string.dialog_positive_text, finish);
                break;
            case CANCEL_ALERT:
                dialogBuilder.setMessage(R.string.cancel_test_confirm);
                dialogBuilder.setPositiveButton(R.string.dialog_positive_text, cancel);
                dialogBuilder.setNegativeButton(R.string.dialog_negative_text, null);
                break;
        }
        dialogBuilder.create().show();
    }

    public void finishTest() {
        final int testBall = test.getTestBall();
        final float znoBall = Float.parseFloat(db.getTestBalls(test.id)[testBall]);
        long elapsedTime = 0;
        if (timerMode) {
            elapsedTime = test.time * 60000 - millisLeft;
        }
        long date = System.currentTimeMillis();

        if (userAnswersId == -1) {
            userAnswersId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers());
            db.completeUserAnswers(userAnswersId, znoBall, elapsedTime, date);
        } else {
            db.updateUserAnswers(userAnswersId, test.getAnswers());
            db.completeUserAnswers(userAnswersId, znoBall, elapsedTime, date);
        }

        if (userAnswersId != -1 || resumed) {
            removeSavedPref();
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

    public void cancelTest() {
        if (userAnswersId != -1) {
            db.deleteUserAnswers(userAnswersId);
            removeSavedPref();
        }
        if (resumed) {
            Intent main = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(main);
        }
        finish();
    }

    public void removeSavedPref() {
        SharedPreferences preferences = getSharedPreferences(ZNOApplication.APP_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Test.TEST_ID);
        editor.remove(Extra.USER_ANSWERS_ID);
        editor.remove(Extra.QUESTION_NUMBER);
        editor.remove(TestTimerFragment.MILLIS_LEFT);
        editor.apply();
    }

}
