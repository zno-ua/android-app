package net.zno_ua.app.activity;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.R;
import net.zno_ua.app.service.APIService;
import net.zno_ua.app.util.Utils;

/**
 * @author vojkovladimir.
 */
public class SettingsActivity extends BaseActivity
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private SwitchCompat mSwSoundNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initUi();
    }

    @SuppressWarnings("ConstantConditions")
    private void initUi() {
        final SwitchCompat autoSyncSwitch = (SwitchCompat) findViewById(R.id.auto_sync_switch);
        mSwSoundNotification = (SwitchCompat) findViewById(R.id.play_notification_sound_switch);
        final SwitchCompat notificationNews = (SwitchCompat) findViewById(R.id.receive_news_notification_switch);
        final TextView tvAppVersion = (TextView) findViewById(R.id.app_version);

        autoSyncSwitch.setChecked(getPreferencesHelper().isAutoSyncEnabled());
        autoSyncSwitch.setOnCheckedChangeListener(this);
        notificationNews.setChecked(getPreferencesHelper().receiveNewsNotification());
        notificationNews.setOnCheckedChangeListener(this);
        mSwSoundNotification.setEnabled(getPreferencesHelper().receiveNewsNotification());
        mSwSoundNotification.setChecked(getPreferencesHelper().playNotificationSound());
        mSwSoundNotification.setOnCheckedChangeListener(this);
        tvAppVersion.setText(String.format(getString(R.string.app_version_format), BuildConfig.VERSION_NAME));
        findViewById(R.id.remove_cache).setOnClickListener(this);
        findViewById(R.id.sync_now).setOnClickListener(this);
        findViewById(R.id.write_me).setOnClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_open_alpha, R.anim.activity_close_translate_right);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.auto_sync_switch:
                getPreferencesHelper().saveAutoSync(isChecked);
                if (isChecked) {
                    APIService.checkTestsUpdates(this);
                }
                break;
            case R.id.play_notification_sound_switch:
                getPreferencesHelper().saveNotificationSound(isChecked);
                break;
            case R.id.receive_news_notification_switch:
                getPreferencesHelper().saveReceiveNewsNotification(isChecked);
                mSwSoundNotification.setEnabled(isChecked);
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.remove_cache:
                break;
            case R.id.sync_now:
                break;
            case R.id.write_me:
                Utils.writeMeEmail(this);
                break;
        }
    }
}
