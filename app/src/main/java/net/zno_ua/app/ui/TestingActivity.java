package net.zno_ua.app.ui;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.zno_ua.app.R;
import net.zno_ua.app.provider.ZNOContract;

import java.util.Locale;

import static android.content.ContentUris.parseId;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.Answer;
import static net.zno_ua.app.provider.ZNOContract.Point;
import static net.zno_ua.app.provider.ZNOContract.Question;
import static net.zno_ua.app.provider.ZNOContract.QuestionAndAnswer;
import static net.zno_ua.app.provider.ZNOContract.Subject;
import static net.zno_ua.app.provider.ZNOContract.Subject.buildSubjectUri;
import static net.zno_ua.app.provider.ZNOContract.Test;
import static net.zno_ua.app.provider.ZNOContract.Test.buildTestItemUri;
import static net.zno_ua.app.provider.ZNOContract.Testing;
import static net.zno_ua.app.provider.ZNOContract.Testing.NO_TIME;
import static net.zno_ua.app.provider.ZNOContract.Testing.buildTestingItemUri;


public class TestingActivity extends AppCompatActivity implements View.OnClickListener {
    private static final long MINUTE = 60000;

    public interface Action {
        String VIEW_TEST = "net.zno_ua.app.VIEW_TEST";
        String PASS_TEST = "net.zno_ua.app.PASS_TEST";
        String CONTINUE_PASSAGE_TEST = "net.zno_ua.app.CONTINUE_PASSAGE_TEST";
    }

    public interface Extra {
        String TEST_ID = "net.zno_ua.app.ui.TEST_ID";
        String SUBJECT_ID = "net.zno_ua.app.ui.SUBJECT_ID";
        String TESTING_ID = "net.zno_ua.app.ui.TESTING_ID";
        String TIMER_MODE = "net.zno_ua.app.ui.TIMER_MODE";
        String VIEW_MODE = "net.zno_ua.app.ui.VIEW_MODE";
    }

    private long testId;
    private long subjectId;
    private long testingId;
    private long time;
    private volatile long elapsedTime;
    private boolean isPassed;
    private Timer mTimer = null;
    private CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        setUpTesting(savedInstanceState);
        setUpActionBar();
        setUpFloatingActionButton();
        setUpMainContent();
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
    }

    private void setUpTesting(Bundle savedInstanceState) {
        time = NO_TIME;
        isPassed = false;
        if (savedInstanceState == null) {
            testId = getIntent().getLongExtra(Extra.TEST_ID, -1);
            Cursor cursor = getContentResolver().query(buildTestItemUri(testId),
                    new String[]{Test.SUBJECT_ID, Test.TIME}, null, null, null);

            boolean hasTimer = getIntent().getBooleanExtra(Extra.TIMER_MODE, false);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    subjectId = cursor.getLong(0);
                    if (hasTimer)
                        time = cursor.getLong(1) * MINUTE;
                }
                cursor.close();
            }

            switch (getIntent().getAction()) {
                case Action.PASS_TEST:
                    elapsedTime = hasTimer ? 0L : Testing.NO_TIME;
                    testingId = createTesting(testId, elapsedTime);
                    break;
                case Action.CONTINUE_PASSAGE_TEST:
                    testingId = getIntent().getLongExtra(Extra.TESTING_ID, -1);
                    elapsedTime = getElapsedTime(testingId);
                    break;
                case Action.VIEW_TEST:
                    testingId = getIntent().getLongExtra(Extra.TESTING_ID, -1);
                    isPassed = true;
                    break;
                default:
                    throw new IllegalArgumentException("Illegal action " + getIntent().getAction()
                            + " for " + this.toString());
            }
        } else {
            testId = savedInstanceState.getLong(Extra.TEST_ID);
            subjectId = savedInstanceState.getLong(Extra.SUBJECT_ID);
            testingId = savedInstanceState.getLong(Extra.TESTING_ID);
            elapsedTime = getElapsedTime(testingId);

            Cursor cursor = getContentResolver().query(buildTestItemUri(testId),
                    new String[]{Test.TIME}, null, null, null);
            boolean hasTimer = savedInstanceState.getBoolean(Extra.TIMER_MODE);
            if (hasTimer && cursor != null) {
                if (cursor.moveToFirst()) {
                    time = cursor.getLong(0) * MINUTE;
                }
                cursor.close();
            }
            isPassed = savedInstanceState.getBoolean(Extra.VIEW_MODE);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpActionBar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(isPassed ? R.string.testing_result : R.string.testing);

        Cursor cursor = getContentResolver().query(buildSubjectUri(subjectId),
                new String[]{Subject.NAME_GENITIVE}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                getSupportActionBar()
                        .setSubtitle(getString(R.string.of) + " " + cursor.getString(0));
            }
            cursor.close();
        }
    }

    private void setUpFloatingActionButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(isPassed ? View.GONE : View.VISIBLE);
        fab.setOnClickListener(this);
    }

    private void setUpMainContent() {
        TestingFragment testingFragment = TestingFragment
                .newInstance(testingId, testId, subjectId, isPassed);
        getFragmentManager()
                .beginTransaction()
                .add(R.id.main_content, testingFragment)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(Extra.TEST_ID, testId);
        outState.putLong(Extra.SUBJECT_ID, subjectId);
        outState.putLong(Extra.TESTING_ID, testingId);
        outState.putBoolean(Extra.TIMER_MODE, withTimer());
        outState.putBoolean(Extra.VIEW_MODE, isPassed);
        super.onSaveInstanceState(outState);
    }

    private long createTesting(long testId, long elapsedTime) {
        ContentValues values = new ContentValues();
        values.put(Testing.TEST_ID, testId);
        values.put(Testing.STATUS, ZNOContract.Testing.IN_PROGRESS);
        values.put(Testing.ELAPSED_TIME, elapsedTime);
        return parseId(getContentResolver().insert(Testing.CONTENT_URI, values));
    }

    private long getElapsedTime(long testingId) {
        Cursor cursor = getContentResolver().query(buildTestingItemUri(testingId),
                new String[]{Testing.ELAPSED_TIME}, null, null, null);
        long elapsedTime = 0L;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                elapsedTime = cursor.getLong(0);
            }
            cursor.close();
        }

        return elapsedTime;
    }

    private boolean withTimer() {
        return time != NO_TIME;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (withTimer() && !isPassed)
            startTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (withTimer() && !isPassed)
            stopTimer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_testing, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (withTimer()) {
            menu.findItem(R.id.action_time).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showExitTestAlert();
                return true;
            case R.id.action_time:
                if (mTimer != null)
                    mTimer.showRemainingTime();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        showExitTestAlert();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (!isPassed)
                    new MaterialDialog.Builder(this)
                            .title(R.string.finish_test_question)
                            .content(R.string.finish_test_description)
                            .positiveText(R.string.finish)
                            .negativeText(R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog,
                                                    @NonNull DialogAction dialogAction) {
                                    finishTest();
                                }
                            }).show();
                break;
        }
    }

    private void showExitTestAlert() {
        if (isPassed)
            finish();
        else
            new MaterialDialog.Builder(this)
                    .title(R.string.exit_test_question)
                    .content(R.string.exit_test_description)
                    .positiveText(R.string.exit)
                    .negativeText(R.string.cancel)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog,
                                            @NonNull DialogAction dialogAction) {
                            exitTest();
                        }
                    }).show();
    }

    private void exitTest() {
        getContentResolver().delete(buildTestingItemUri(testingId), null, null);
        getContentResolver().delete(Answer.CONTENT_URI, Answer.TESTING_ID + " = ?",
                new String[]{valueOf(testingId)});
        finish();
    }

    private void finishTest() {
        isPassed = true;
        if (withTimer() && mTimer != null)
            mTimer.cancel();
        int testPoint = calculateUserPoints();
        double ratingPoint = getRatingPoint(testPoint);

        ContentValues values = new ContentValues();
        values.put(Testing.ELAPSED_TIME, elapsedTime);
        values.put(Testing.DATE, System.currentTimeMillis());
        values.put(Testing.TEST_POINT, testPoint);
        values.put(Testing.RATING_POINT, ratingPoint);
        values.put(Testing.STATUS, Testing.FINISHED);

        getContentResolver().update(buildTestingItemUri(testingId), values, null, null);

        new MaterialDialog.Builder(this)
                .title(R.string.test_completed)
                .content(format(Locale.US, getString(R.string.your_rating_point_format), ratingPoint))
                .positiveText(R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        showResultsDialog();
                    }
                })
                .negativeText(R.string.exit)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        finish();
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).show();
    }

    private void showResultsDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.view_results_question)
                .content(R.string.view_results_description)
                .positiveText(R.string.view)
                .negativeText(R.string.do_not_view)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            Intent intent = new Intent(TestingActivity.this, TestingActivity.class);
                            intent.setAction(Action.VIEW_TEST);
                            intent.putExtra(Extra.TEST_ID, testId);
                            intent.putExtra(Extra.TESTING_ID, testingId);
                            startActivity(intent);
                        }
                        finish();
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).show();
    }

    private double getRatingPoint(int testPoint) {
        double point = 0;

        Cursor cursor = getContentResolver()
                .query(Point.CONTENT_URI,
                        new String[]{Point.RATING_POINT},
                        Point.TEST_ID + " =? AND " + Point.TEST_POINT + " =?",
                        new String[]{valueOf(testId), valueOf(testPoint)},
                        Point.SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (testPoint >= cursor.getCount())
                    cursor.moveToLast();
                else
                    cursor.moveToPosition(testPoint);

                point = cursor.getFloat(0);
            }
            cursor.close();
        }

        return point;
    }

    private void startTimer() {
        if (mTimer == null)
            mTimer = new Timer(time - elapsedTime);
        mTimer.start();
        mTimer.showRemainingTime();
    }

    private void stopTimer() {
        if (mTimer != null)
            mTimer.cancel();
        mTimer = null;
        ContentValues values = new ContentValues();
        values.put(ZNOContract.Testing.ELAPSED_TIME, elapsedTime);
        getContentResolver()
                .update(buildTestingItemUri(testingId), values, null, null);
    }

    private int calculateUserPoints() {
        Cursor cursor = getContentResolver()
                .query(QuestionAndAnswer.CONTENT_URI,
                        new String[]{QuestionAndAnswer.TYPE,
                                QuestionAndAnswer.CORRECT_ANSWER,
                                QuestionAndAnswer.ANSWER,
                                QuestionAndAnswer.POINT
                        },
                        QuestionAndAnswer.TEST_ID + " = ?",
                        new String[]{valueOf(testingId), valueOf(valueOf(testId))},
                        null);
        int points = 0;

        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    points += calculateQuestionPoint(cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getInt(3));
                } while (cursor.moveToNext());
            cursor.close();
        }

        return points;
    }

    private int calculateQuestionPoint(int type, String correctAnswer, String userAnswer,
                                       int maxPoint) {
        if (userAnswer == null && type != Question.TYPE_2) {
            return 0;
        }
        switch (type) {
            case Question.TYPE_1:
            case Question.TYPE_5:
                if (correctAnswer.equals(userAnswer))
                    return maxPoint;
                break;
            case Question.TYPE_2:
                return userAnswer == null ? maxPoint / 2 : parseInt(userAnswer);
            case Question.TYPE_3: {
                int point = 0;

                for (int i = 0; i < correctAnswer.length() && i < userAnswer.length(); i++) {
                    if (correctAnswer.charAt(i) == userAnswer.charAt(i))
                        point += maxPoint;

                }

                return point;
            }
            case Question.TYPE_4: {
                maxPoint = 1;
                int point = 0;
                char[] chars = correctAnswer.toCharArray();

                char c;
                int index;
                for (int i = 0; i < userAnswer.length(); i++) {
                    c = userAnswer.charAt(i);
                    index = correctAnswer.indexOf(c);

                    if (index != -1 && chars[index] == c) {
                        chars[index] = ' ';
                        point += maxPoint;
                    }
                }

                return point;
            }
        }

        return 0;
    }

    private class Timer extends CountDownTimer {

        private static final long COUNT_DOWN_INTERVAL = 1000;

        private long millisLeft;
        private Snackbar mSnackbar;

        public Timer(long millisInFuture) {
            super(millisInFuture, COUNT_DOWN_INTERVAL);
            millisLeft = millisInFuture;
        }

        @Override
        public void onTick(long millisInFuture) {
            millisLeft = millisInFuture;
            elapsedTime = time - millisInFuture;
            int minutes = (int) (millisInFuture / 60000);
            int seconds = (int) (millisInFuture % 60000 / 1000);

            if (mSnackbar != null)
                mSnackbar.setText(getTimerText(minutes, seconds));

            if (minutes != 0 && seconds == 0) {
                if (minutes % 30 == 0 || (minutes < 30 && minutes % 10 == 0)
                        || minutes == 5 || minutes <= 3) {
                    showRemainingTime(false);
                }
            } else if (minutes == 0 && seconds == 15)
                showRemainingTime(false);
        }

        @Override
        public void onFinish() {
            elapsedTime = time;
            finishTest();
        }

        public void showRemainingTime() {
            showRemainingTime(true);
        }

        private void showRemainingTime(boolean hideIfShown) {
            if (mSnackbar == null || !mSnackbar.isShown()) {
                mSnackbar = Snackbar.make(mCoordinatorLayout,
                        getTimerText((int) millisLeft / 60000, (int) millisLeft % 60000 / 1000),
                        Snackbar.LENGTH_LONG).setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        mSnackbar = null;
                    }
                });
                mSnackbar.show();
            } else {
                if (hideIfShown)
                    mSnackbar.dismiss();
            }
        }

        private String getTimerText(int minutes, int seconds) {
            if (minutes > 0) {
                if ((minutes < 10) || (minutes > 20 && minutes < 110) || minutes > 120) {
                    switch (minutes % 10) {
                        case 1:
                            return format(getString(R.string.time_one_left),
                                    minutes,
                                    getString(R.string.one_minute)
                            );
                        case 2:
                        case 3:
                        case 4:
                            return format(getString(R.string.time_two_four_left),
                                    minutes,
                                    getString(R.string.two_four_minutes)
                            );
                    }
                }
                return format(getString(R.string.time_left),
                        minutes,
                        getString(R.string.minutes)
                );
            } else {
                if (seconds == 0) {
                    return getString(R.string.time_is_up);
                } else if (seconds >= 20 || (seconds > 0 && seconds < 10)) {
                    switch (seconds % 10) {
                        case 1:
                            return format(getString(R.string.time_one_left),
                                    seconds,
                                    getString(R.string.one_second)
                            );
                        case 2:
                        case 3:
                        case 4:
                            return format(getString(R.string.time_one_left),
                                    seconds,
                                    getString(R.string.two_four_seconds)
                            );
                    }
                }
                return format(getString(R.string.time_one_left),
                        seconds,
                        getString(R.string.seconds)
                );
            }
        }
    }

}
