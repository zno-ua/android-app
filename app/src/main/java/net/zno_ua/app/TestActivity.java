package net.zno_ua.app;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import net.zno_ua.app.adapters.QuestionsAdapter;
import net.zno_ua.app.adapters.QuestionsGridAdapter;
import net.zno_ua.app.db.ZNODataBaseHelper;
import net.zno_ua.app.fragments.QuestionFragment;
import net.zno_ua.app.fragments.TestTimerFragment;
import net.zno_ua.app.models.Lesson;
import net.zno_ua.app.models.Record;
import net.zno_ua.app.models.Test;

import java.util.Locale;

public class TestActivity extends FragmentActivity
        implements QuestionFragment.OnAnswerSelectedListener,
        TestTimerFragment.OnTimeChangedListener {

    final int FINISH_ALERT = 0;
    final int CANCEL_ALERT = 1;

    public interface Action {
        String VIEW_TEST = "net.zno_ua.app.VIEW_TEST";
        String PASS_TEST = "net.zno_ua.app.PASS_TEST";
        String CONTINUE_PASSAGE_TEST = "net.zno_ua.app.CONTINUE_PASSAGE_TEST";
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
    boolean isDestroying = false;

    boolean timerMode;
    long millisLeft;
    long timerLastShowTime;
    MenuItem timerAction;

    Test test;
    Record results;
    int userAnswersId;
    int currentPage;

    boolean questionsGridVisible;
    ViewPager mPager;
    PagerAdapter mPagerAdapter;
    GridView questionsGrid;

    android.app.FragmentManager manager;
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        app = ZNOApplication.getInstance();
        db = app.getZnoDataBaseHelper();
        manager = getFragmentManager();

        currentPage = 0;

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            String action = intent.getAction();
            switch (action) {
                case Action.PASS_TEST: {
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
                    break;
                }
                case Action.CONTINUE_PASSAGE_TEST: {
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
                    currentPage = intent.getIntExtra(Extra.QUESTION_NUMBER, 0);
                    break;
                }
                case Action.VIEW_TEST: {
                    int testId = intent.getIntExtra(Test.TEST_ID, -1);
                    userAnswersId = intent.getIntExtra(Extra.USER_ANSWERS_ID, -1);
                    test = db.getTest(testId);
                    test.putAnswers(db.getUserAnswers(userAnswersId));
                    viewMode = true;
                    resumed = intent.getBooleanExtra(Extra.RESUMED, false);
                    timerMode = false;
                    break;
                }
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
                timerFragment = (TestTimerFragment) manager
                        .getFragment(savedInstanceState, TestTimerFragment.TAG);
            }
        }

        if (timerMode && timerFragment == null) {
            timerFragment = TestTimerFragment.newInstance(millisLeft);
        }

        mPager = (ViewPager) findViewById(R.id.test_question_pager);

        if (viewMode) {
            results = db.getResult(userAnswersId);
            mPagerAdapter = new QuestionsAdapter(this, getSupportFragmentManager(), test, results);
        } else {
            mPagerAdapter = new QuestionsAdapter(this, getSupportFragmentManager(), test);
        }
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(currentPage);
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
            if (timerMode) {
                millisLeft = timerFragment.getMillisLeft();
                outState.putLong(TestTimerFragment.MILLIS_LEFT, timerFragment.getMillisLeft());
            }
            app.saveTestSession(test.id, userAnswersId, mPager.getCurrentItem(), millisLeft);
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
    protected void onDestroy() {
        super.onDestroy();
        isDestroying = true;
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
                String shareTemplate = getString(R.string.share_template);
                Lesson lesson = db.getLesson(test.lessonId);
                String shareText =
                        String.format(Locale.US, shareTemplate,
                                lesson.nameRod, results.znoBall, lesson.link, test.id);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        if (viewMode) {
            finish();
        } else {
            showAlert(CANCEL_ALERT);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View view = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (view != null && view instanceof EditText) {
            view = getCurrentFocus();
            if (view != null) {
                int scrCoordinates[] = new int[2];
                view.getLocationOnScreen(scrCoordinates);

                float x = event.getRawX() + view.getLeft() - scrCoordinates[0];
                float y = event.getRawY() + view.getTop() - scrCoordinates[1];

                if (event.getAction() == MotionEvent.ACTION_UP
                        && (x < view.getLeft() || x >= view.getRight()
                        || y < view.getTop() || y > view.getBottom())) {
                    ZNOApplication.hideKeyboard(this);
                }
            }
        }
        return ret;
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
        test.questions.get(id).setUserAnswer(answer);
        if (switchToNext) {
            if (test.hasUnAnsweredQuestions()) {
                switchToNext();
            } else {
                if (askToFinish) {
                    showAlert(FINISH_ALERT);
                }
            }
        }
    }

    public void switchToNext() {
        int next = mPager.getCurrentItem() + 1;
        if (next < mPagerAdapter.getCount()) {
            mPager.setCurrentItem(next);
        }
    }

    @Override
    public void onTimeIsUp() {
        Toast.makeText(this, R.string.time_is_up, Toast.LENGTH_SHORT).show();
        finishTest();
    }

    @Override
    public void onMinutePassed(long millisLeft) {
        int minutesLeft = (int) (millisLeft / 60000);

        if (timerLastShowTime - millisLeft > 60000) {
            if (minutesLeft % 30 == 0
                    || (minutesLeft < 30 && minutesLeft % 10 == 0)
                    || minutesLeft == 5
                    || (minutesLeft <= 3)) {
                showHideTimer();
            }
        }

        if (minutesLeft == 10) {
            timerAction.setIcon(getResources().getDrawable(R.drawable.ic_action_time_low));
        }

        this.millisLeft = millisLeft;
    }

    public void showHideTimer() {
        timerLastShowTime = millisLeft;
        final Runnable hide = new Runnable() {
            @Override
            public void run() {
                if (!isDestroying) {
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
                    String message = getString(R.string.dialog_unanswered_questions_text);
                    message += "\n" + getString(R.string.dialog_finish_text);
                    dialogBuilder.setMessage(message);
                    dialogBuilder.setNegativeButton(R.string.dialog_negative_text, resumeWithAsk);
                } else {
                    dialogBuilder.setMessage(R.string.dialog_finish_text);
                    dialogBuilder.setNegativeButton(R.string.dialog_negative_text, resume);
                }
                dialogBuilder.setPositiveButton(R.string.dialog_positive_text, finish);
                break;
            case CANCEL_ALERT:
                dialogBuilder.setMessage(R.string.dialog_cancel_test_text);
                dialogBuilder.setPositiveButton(R.string.dialog_continue_text, cancel);
                dialogBuilder.setNegativeButton(R.string.dialog_cancel_text, null);
                break;
        }
        dialogBuilder.create().show();
    }

    public void finishTest() {
        String[] testBalls = db.getTestBalls(test.id);
        final int testBall = test.getTestBall();
        final float znoBall = Float.parseFloat(
                (testBall < testBalls.length) ?
                        testBalls[testBall] :
                        testBalls[testBalls.length - 1]
        );
        long elapsedTime = 0;
        if (timerMode) {
            elapsedTime = test.time * 60000 - timerFragment.getMillisLeft();
        }
        long date = System.currentTimeMillis();

        if (userAnswersId == -1) {
            userAnswersId = db.saveUserAnswers(test.lessonId, test.id, test.getAnswers());
            db.completeUserAnswers(userAnswersId, testBall, znoBall, elapsedTime, date);
        } else {
            db.updateUserAnswers(userAnswersId, test.getAnswers());
            db.completeUserAnswers(userAnswersId, testBall, znoBall, elapsedTime, date);
        }

        if (userAnswersId != -1 || resumed) {
            app.removeSavedSession();
        }

        Intent viewResults = new Intent(TestActivity.Action.VIEW_TEST);
        viewResults.putExtra(Test.TEST_ID, test.id);
        viewResults.putExtra(Extra.USER_ANSWERS_ID, userAnswersId);
        viewResults.putExtra(Extra.RESUMED, resumed);
        startActivity(viewResults);
        finish();
    }

    public void cancelTest() {
        if (userAnswersId != -1) {
            db.deleteUserAnswers(userAnswersId);
            app.removeSavedSession();
        }
        finish();
    }

}
