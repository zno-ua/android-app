package net.zno_ua.app;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import net.zno_ua.app.service.APIService;

public class ZNOApplication extends Application {

    public static final String TAG = "ZNOLogs";
    private static ZNOApplication sInstance;

    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        final GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
//        analytics.setDryRun(BuildConfig.DEBUG);
        mTracker = analytics.newTracker(R.xml.global_tracker);
        mTracker.enableAdvertisingIdCollection(true);
        mTracker.enableAutoActivityTracking(true);
        mTracker.enableExceptionReporting(true);
        APIService.restartPendingRequests(this);
    }

    public static synchronized ZNOApplication getInstance() {
        return sInstance;
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    public Tracker getTracker() {
        return mTracker;
    }

    public static void log(String message) {
        Log.d(TAG, message);
    }
}
