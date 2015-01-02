package net.zno_ua.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import net.zno_ua.app.db.ZNODataBaseHelper;
import net.zno_ua.app.fragments.TestTimerFragment;
import net.zno_ua.app.models.Record;
import net.zno_ua.app.models.Test;
import net.zno_ua.app.service.ApiService;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import java.util.Locale;

@ReportsCrashes(
        formUri = "https://vojkovladimir.cloudant.com/" +
                "acra-zno-ua-app/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = "pectlystinnelyetantibles",
        formUriBasicAuthPassword = "LSiLqvM1IIE6oWaBhSsCIcwM",
        formKey = "",
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.PHONE_MODEL,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE,
                ReportField.USER_CRASH_DATE
        },
        mode = ReportingInteractionMode.SILENT
)
public class ZNOApplication extends Application {

    public static final String TAG = ZNOApplication.class.getSimpleName();
    public static final String APP_SETTINGS = "settings";
    public static final String LAST_UPDATE = "last_update";
    public static final String DEVICE_IOCEAN_X_7 = "3C3DC09C87BFF69EE07221D903FCAFA9";

    /// Interval of time after which will checking for updates. Now it is 24 hours.
    private static final long CHECK_FOR_UPDATES_INTERVAL = 86400000;

    private static ZNOApplication mInstance;
    private RequestQueue mRequestQueue;
    private ZNODataBaseHelper znoDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        if (!BuildConfig.DEBUG) ACRA.init(this);
    }

    public static synchronized ZNOApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ZNODataBaseHelper getZnoDataBaseHelper() {
        if (znoDBHelper == null) {
            znoDBHelper = new ZNODataBaseHelper(getApplicationContext());
        }

        return znoDBHelper;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public long getLastUpdate() {
        SharedPreferences preferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        return preferences.getLong(LAST_UPDATE, 0);
    }

    public void setLastUpdate(long lastUpdate) {
        SharedPreferences preferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(LAST_UPDATE, lastUpdate);
        editor.apply();
    }

    public void onWiFiConnected() {
        long currentDate = System.currentTimeMillis();
        long lastUpdate = getLastUpdate();

        if (currentDate - lastUpdate >= CHECK_FOR_UPDATES_INTERVAL) {
            startService(new Intent(ApiService.ACTION_CHECK_FOR_UPDATES));
        }
    }

    public void saveTestSession(int id, int userAnswersId, int questionNumber, long millisLeft) {
        SharedPreferences preferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Test.TEST_ID, id)
                .putInt(TestActivity.Extra.USER_ANSWERS_ID, userAnswersId)
                .putInt(TestActivity.Extra.QUESTION_NUMBER, questionNumber);
        if (millisLeft != 0) {
            editor.putLong(TestTimerFragment.MILLIS_LEFT, millisLeft);
        }
        editor.apply();
    }

    public void removeSavedSession() {
        SharedPreferences preferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        preferences.edit().remove(Test.TEST_ID)
                .remove(TestActivity.Extra.USER_ANSWERS_ID)
                .remove(TestActivity.Extra.QUESTION_NUMBER)
                .remove(TestTimerFragment.MILLIS_LEFT).apply();
    }

    public boolean hasSavedSession() {
        SharedPreferences preferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        return preferences.contains(TestActivity.Extra.USER_ANSWERS_ID);
    }

    public int getSavedSessionUserAnswersId() {
        SharedPreferences preferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        return preferences.getInt(TestActivity.Extra.USER_ANSWERS_ID, -1);
    }

    public void startSavedSession(Activity parent) {
        SharedPreferences preferences = getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
        int userAnswersId = preferences.getInt(TestActivity.Extra.USER_ANSWERS_ID, -1);
        int questionNumber = preferences.getInt(TestActivity.Extra.QUESTION_NUMBER, -1);
        int testId = preferences.getInt(Test.TEST_ID, -1);

        Intent startedActivity = new Intent(TestActivity.Action.CONTINUE_PASSAGE_TEST);
        startedActivity.putExtra(Test.TEST_ID, testId);
        startedActivity.putExtra(TestActivity.Extra.USER_ANSWERS_ID, userAnswersId);
        startedActivity.putExtra(TestActivity.Extra.QUESTION_NUMBER, questionNumber);
        if (preferences.contains(TestTimerFragment.MILLIS_LEFT)) {
            startedActivity.putExtra(TestTimerFragment.MILLIS_LEFT, preferences.getLong(TestTimerFragment.MILLIS_LEFT, -1));
        }
        parent.startActivity(startedActivity);
    }

    public static SpannableStringBuilder buildBall(float ball, boolean withEmoji, int type) {
        Resources resources = ZNOApplication.getInstance().getResources();
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (ball % 1 == 0) {
            builder.insert(0, String.valueOf((int) ball));
            if (!withEmoji) {
                builder.append("  ");
                builder.setSpan(new RelativeSizeSpan(0.95f), builder.length() - 2, builder.length(), 0);
            }
        } else {
            builder.insert(0, String.format(Locale.US, "%.1f", ball));
            builder.setSpan(new RelativeSizeSpan(0.5f), builder.length() - 2, builder.length(), 0);
        }

        if (type == Record.GOOD_BALL) {
            if (withEmoji) {
                builder.append(" ").append(resources.getString(R.string.happy));
            }
            int highScoreColor = resources.getColor(R.color.dark_green);
            builder.setSpan(new ForegroundColorSpan(highScoreColor), 0, builder.length(), 0);
        } else if (type == Record.BAD_BALL) {
            if (withEmoji) {
                builder.append(" ").append(resources.getString(R.string.sad));
            }
            int lowScoreColor = resources.getColor(R.color.red);
            builder.setSpan(new ForegroundColorSpan(lowScoreColor), 0, builder.length(), 0);
        }
        return builder;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(
                    focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
