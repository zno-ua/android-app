package com.vojkovladimir.zno;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;

import java.util.Calendar;

public class ZNOApplication extends Application {

    public static String LOG_TAG = "MyLogs";
    public static final String TAG = ZNOApplication.class.getSimpleName();
    public static final String APP_SETTINGS = "settings";

    private static ZNOApplication mInstance;
    private RequestQueue mRequestQueue;
    private ZNODataBaseHelper znoDBHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        Log.v(LOG_TAG, "App Instance created.");
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

    ;

}
