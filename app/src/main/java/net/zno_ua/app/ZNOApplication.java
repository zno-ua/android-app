package net.zno_ua.app;

import android.app.Application;

import net.zno_ua.app.service.APIServiceHelper;

public class ZNOApplication extends Application {

    private static ZNOApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        APIServiceHelper.restartPendingRequests(this);
    }

    public static synchronized ZNOApplication getInstance() {
        return mInstance;
    }

}
