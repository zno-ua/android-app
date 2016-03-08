package net.zno_ua.app.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.GoogleAnalytics;

import net.zno_ua.app.helper.PreferencesHelper;

/**
 * @author vojkovladimir.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private PreferencesHelper mPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferencesHelper = PreferencesHelper.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    protected PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }
}
