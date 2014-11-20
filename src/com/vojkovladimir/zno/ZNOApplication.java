package com.vojkovladimir.zno;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.fragments.TestTimerFragment;
import com.vojkovladimir.zno.models.Record;
import com.vojkovladimir.zno.models.Test;
import com.vojkovladimir.zno.service.ApiService;

import java.util.Calendar;
import java.util.Locale;

public class ZNOApplication extends Application {

    public static final String TAG = ZNOApplication.class.getSimpleName();
    public static final String APP_SETTINGS = "settings";
    public static final String LAST_UPDATE = "last_update";

    /// Interval of time after which will checking for updates. Now it is 24 hours.
    private static final long CHECK_FOR_UPDATES_INTERVAL = 86400000;

    private static ZNOApplication mInstance;
    private RequestQueue mRequestQueue;
    private ZNODataBaseHelper znoDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
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

    public static void buildLogo(TextView logoTextView, Resources resources, AssetManager assets) {
        String appName = resources.getString(R.string.app_name);
        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR) + ((calendar.get(Calendar.MONTH) < 7) ? 0 : 1));
        String logoText = appName + " " + year;

        SpannableString spannableString = new SpannableString(logoText);
        spannableString.setSpan(new ForegroundColorSpan(resources.getColor(R.color.blue_light)), 0, appName.length(), 0);
        spannableString.setSpan(new ForegroundColorSpan(resources.getColor(R.color.quote_color)), appName.length() + 1, logoText.length(), 0);

        Typeface ptSansBold = Typeface.createFromAsset(assets, "fonts/PT_Sans-Web-Bold.ttf");

        logoTextView.setText(spannableString);
        logoTextView.setTypeface(ptSansBold);
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

}
