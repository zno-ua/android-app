package net.zno_ua.app.activity;

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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.zno_ua.app.R;
import net.zno_ua.app.fragment.QuestionPagesFragment;
import net.zno_ua.app.model.TestingInfo;
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
import static net.zno_ua.app.provider.ZNOContract.Testing.buildTestingItemUri;


public class TestingActivity extends AppCompatActivity
        implements QuestionPagesFragment.OnViewPagerChangeListener {
    private static final long MINUTE = 60000;

    public interface Action {
        String VIEW_TEST = "net.zno_ua.app.VIEW_TEST";
        String PASS_TEST = "net.zno_ua.app.PASS_TEST";
        String CONTINUE_PASSAGE_TEST = "net.zno_ua.app.CONTINUE_PASSAGE_TEST";
    }

    public interface Key {
        String TEST_ID = "net.zno_ua.app.ui.TEST_ID";
        String SUBJECT_ID = "net.zno_ua.app.ui.SUBJECT_ID";
        String TESTING_ID = "net.zno_ua.app.ui.TESTING_ID";
        String TIMER_MODE = "net.zno_ua.app.ui.TIMER_MODE";
        String VIEW_MODE = "net.zno_ua.app.ui.VIEW_MODE";
    }

    private Timer mTimer = null;
    private final View.OnClickListener mOnFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mTestingInfo.isPassed()) {
                shareResults();
            } else {
                showFinishAlert();
            }
        }
    };
    private final TestingInfo mTestingInfo = new TestingInfo();

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpTestingInfo(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        setUpActionBar();
        setUpFloatingActionButton();
        setUpQuestions();
    }

    private void setUpTestingInfo(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mTestingInfo.setTestId(getIntent().getLongExtra(Key.TEST_ID, -1));
            Cursor cursor = getContentResolver().query(buildTestItemUri(mTestingInfo.getTestId()),
                    new String[]{Test.SUBJECT_ID, Test.TIME}, null, null, null);

            boolean hasTimer = getIntent().getBooleanExtra(Key.TIMER_MODE, false);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    mTestingInfo.setSubjectId(cursor.getLong(0));
                    if (hasTimer) {
                        mTestingInfo.setTime(cursor.getLong(1) * MINUTE);
                    }
                }
                cursor.close();
            }
            switch (getIntent().getAction()) {
                case Action.PASS_TEST:
                    long elapsedTime = hasTimer ? 0L : Testing.NO_TIME;
                    mTestingInfo.setElapsedTime(elapsedTime);
                    mTestingInfo.setTestingId(createTesting(mTestingInfo.getTestId(), elapsedTime));
                    break;
                case Action.CONTINUE_PASSAGE_TEST:
                    mTestingInfo.setTestingId(getIntent().getLongExtra(Key.TESTING_ID, -1));
                    mTestingInfo.setElapsedTime(getElapsedTime(mTestingInfo.getTestingId()));
                    break;
                case Action.VIEW_TEST:
                    mTestingInfo.setTestingId(getIntent().getLongExtra(Key.TESTING_ID, -1));
                    mTestingInfo.setIsPassed(true);
                    break;
            }
        } else {
            mTestingInfo.setTestId(savedInstanceState.getLong(Key.TEST_ID));
            mTestingInfo.setSubjectId(savedInstanceState.getLong(Key.SUBJECT_ID));
            mTestingInfo.setTestingId(savedInstanceState.getLong(Key.TESTING_ID));
            mTestingInfo.setElapsedTime(getElapsedTime(mTestingInfo.getTestingId()));

            Cursor cursor = getContentResolver().query(buildTestItemUri(mTestingInfo.getTestId()),
                    new String[]{Test.TIME}, null, null, null);
            boolean hasTimer = savedInstanceState.getBoolean(Key.TIMER_MODE);
            if (hasTimer && cursor != null) {
                if (cursor.moveToFirst()) {
                    mTestingInfo.setTime(cursor.getLong(0) * MINUTE);
                }
                cursor.close();
            }
            mTestingInfo.setIsPassed(savedInstanceState.getBoolean(Key.VIEW_MODE));
        }
        Cursor cursor = getContentResolver().query(buildSubjectUri(mTestingInfo.getSubjectId()),
                new String[]{Subject.NAME, Subject.NAME_GENITIVE, Subject.LINK}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mTestingInfo.setSubjectName(cursor.getString(0));
                mTestingInfo.setSubjectNameGenitive(cursor.getString(1));
                mTestingInfo.setLink(cursor.getString(2));
            }
            cursor.close();
        }
    }

    public TestingInfo getTestingInfo() {
        return mTestingInfo;
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpActionBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (mTestingInfo.isPassed()) {
            getSupportActionBar().setTitle(R.string.testing_result);
        } else {
            getSupportActionBar().setTitle(R.string.testing);
        }
        getSupportActionBar().setSubtitle(getString(R.string.of) + " "
                + mTestingInfo.getSubjectNameGenitive());
    }

    private void setUpFloatingActionButton() {
        if (mTestingInfo.isPassed()) {
            mFab.setImageResource(R.drawable.ic_share_white_24dp);
        } else {
            mFab.setImageResource(R.drawable.ic_done_black_24dp);
        }
        mFab.setOnClickListener(mOnFabClickListener);
    }

    private void setUpQuestions() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(QuestionPagesFragment.TAG);
        if (fragment == null) {
            fragment = new QuestionPagesFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_content, fragment, QuestionPagesFragment.TAG)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(Key.TEST_ID, mTestingInfo.getTestId());
        outState.putLong(Key.SUBJECT_ID, mTestingInfo.getSubjectId());
        outState.putLong(Key.TESTING_ID, mTestingInfo.getTestingId());
        outState.putBoolean(Key.TIMER_MODE, mTestingInfo.withTimer());
        outState.putBoolean(Key.VIEW_MODE, mTestingInfo.isPassed());
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

    @Override
    protected void onStart() {
        super.onStart();
        if (mTestingInfo.withTimer() && !mTestingInfo.isPassed()) {
            startTimer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTestingInfo.withTimer() && !mTestingInfo.isPassed()) {
            stopTimer();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_testing, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mTestingInfo.withTimer()) {
            menu.findItem(R.id.action_time).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exit();
                return true;
            case R.id.action_time:
                if (mTimer != null) {
                    mTimer.showRemainingTime();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewPagerAttached(ViewPager viewPager) {
        mTabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onViewPagerChanged(PagerAdapter adapter) {
        mTabLayout.setTabsFromPagerAdapter(adapter);
    }

    @Override
    public void onViewPagerVisibilityChanged(boolean isVisible) {
        if (isVisible && mTabLayout.getTabCount() == 0) {
            return;
        }
        mTabLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewPagerDetached() {
        mTabLayout.setVisibility(View.GONE);
    }

    @Override
    public void onCurrentItemChanged(int item) {
        final TabLayout.Tab tab = mTabLayout.getTabAt(item);
        if (tab != null) {
            tab.select();
        }
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private void exit() {
        if (mTestingInfo.isPassed()) {
            finish();
        } else {
            showExitTestAlert();
        }
    }

    private void shareResults() {
        final int testPoint = calculateUserPoints();
        final double ratingPoint = getRatingPoint(testPoint);
        final String shareTemplate = getString(R.string.share_template);
        final String shareText = String.format(Locale.US, shareTemplate,
                mTestingInfo.getSubjectNameGenitive(), ratingPoint, mTestingInfo.getLink(),
                mTestingInfo.getTestId());
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void showFinishAlert() {
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
    }

    private void showExitTestAlert() {
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
        getContentResolver().delete(buildTestingItemUri(mTestingInfo.getTestingId()), null, null);
        getContentResolver().delete(Answer.CONTENT_URI, Answer.TESTING_ID + " = ?",
                new String[]{valueOf(mTestingInfo.getTestingId())});
        finish();
    }

    private void finishTest() {
        if (mTestingInfo.withTimer() && mTimer != null) {
            mTimer.cancel();
        }
        int testPoint = calculateUserPoints();
        double ratingPoint = getRatingPoint(testPoint);

        ContentValues values = new ContentValues();
        values.put(Testing.ELAPSED_TIME, mTestingInfo.getElapsedTime());
        values.put(Testing.DATE, System.currentTimeMillis());
        values.put(Testing.TEST_POINT, testPoint);
        values.put(Testing.RATING_POINT, ratingPoint);
        values.put(Testing.STATUS, Testing.FINISHED);

        getContentResolver().update(buildTestingItemUri(mTestingInfo.getTestingId()), values, null,
                null);

        new MaterialDialog.Builder(this)
                .title(R.string.test_completed)
                .content(format(Locale.US, getString(R.string.your_rating_point_format), ratingPoint))
                .positiveText(R.string.view)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        if (dialogAction == DialogAction.POSITIVE) {
                            Intent intent = new Intent(TestingActivity.this, TestingActivity.class);
                            intent.setAction(Action.VIEW_TEST);
                            intent.putExtra(Key.TEST_ID, mTestingInfo.getTestId());
                            intent.putExtra(Key.TESTING_ID, mTestingInfo.getTestingId());
                            startActivity(intent);
                        }
                        finish();
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

    private double getRatingPoint(int testPoint) {
        double point = 0;
        Cursor cursor = getContentResolver()
                .query(Point.CONTENT_URI,
                        new String[]{Point.RATING_POINT},
                        Point.TEST_ID + " =? AND " + Point.TEST_POINT + " =?",
                        new String[]{valueOf(mTestingInfo.getTestId()), valueOf(testPoint)},
                        Point.SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                if (testPoint >= cursor.getCount()) {
                    cursor.moveToLast();
                } else {
                    cursor.moveToPosition(testPoint);
                }
                point = cursor.getFloat(0);
            }
            cursor.close();
        }

        return point;
    }

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer(mTestingInfo.getTime() - mTestingInfo.getElapsedTime());
        }
        mTimer.start();
        mTimer.showRemainingTime();
    }

    private void stopTimer() {
        if (mTimer != null)
            mTimer.cancel();
        mTimer = null;
        ContentValues values = new ContentValues();
        values.put(ZNOContract.Testing.ELAPSED_TIME, mTestingInfo.getElapsedTime());
        getContentResolver().update(buildTestingItemUri(mTestingInfo.getTestingId()), values, null,
                null);
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
                        new String[]{valueOf(mTestingInfo.getTestingId()),
                                valueOf(valueOf(mTestingInfo.getTestId()))},
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

    @Deprecated
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
            mTestingInfo.setElapsedTime(mTestingInfo.getTime() - millisInFuture);
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
            mTestingInfo.setElapsedTime(mTestingInfo.getTime());
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

        @Deprecated
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
