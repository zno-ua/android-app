package net.zno_ua.app.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.zno_ua.app.rest.model.Review;

/**
 * @author vojkovladimir.
 */
public class PreferencesHelper {

    private static final String KEY_EMAIL = "KEY_EMAIL";
    private static final String KEY_NAME = "KEY_NAME";
    private static final String KEY_MESSAGE = "KEY_MESSAGE";
    private static final String KEY_LAST_UPDATE = "KEY_LAST_UPDATE";
    private static final String KEY_NOTIFICATION_SOUND = "KEY_NOTIFICATION_SOUND";

    private static volatile PreferencesHelper sInstance = null;

    @NonNull
    public static PreferencesHelper getInstance(Context context) {
        PreferencesHelper localInstance = sInstance;
        if (localInstance == null) {
            synchronized (PreferencesHelper.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = new PreferencesHelper(context);
                    localInstance = sInstance;
                }
            }
        }

        return localInstance;
    }

    private final SharedPreferences mPreferences;

    private String mEmail;
    private String mName;
    private String mMessage;
    private long mLastUpdateTime;
    private boolean mIsNotificationSoundEnabled;

    private PreferencesHelper(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        loadSettings();
    }

    private void loadSettings() {
        mEmail = mPreferences.getString(KEY_EMAIL, null);
        mName = mPreferences.getString(KEY_NAME, null);
        mMessage = mPreferences.getString(KEY_MESSAGE, null);
        mLastUpdateTime = mPreferences.getLong(KEY_LAST_UPDATE, 0L);
        mIsNotificationSoundEnabled = mPreferences.getBoolean(KEY_NOTIFICATION_SOUND, true);
    }

    @Nullable
    public String getEmail() {
        return mEmail;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    @Nullable
    public String getMessage() {
        return mMessage;
    }

    public long getLastUpdateTime() {
        return mLastUpdateTime;
    }

    public boolean isNotificationSoundEnabled() {
        return mIsNotificationSoundEnabled;
    }

    public void saveEmail(@Nullable String email) {
        mEmail = email;
        mPreferences.edit().putString(KEY_EMAIL, email).apply();
    }

    public void saveName(@Nullable String name) {
        mName = name;
        mPreferences.edit().putString(KEY_NAME, name).apply();
    }

    public void saveMessage(@Nullable String message) {
        mMessage = message;
        mPreferences.edit().putString(KEY_MESSAGE, message).apply();
    }

    public void saveReview(@NonNull Review review) {
        saveName(review.getName());
        saveEmail(review.getMail());
        saveMessage(review.getMessage());
    }

    public void saveLastUpdateTime(long time) {
        mLastUpdateTime = time;
        mPreferences.edit().putLong(KEY_LAST_UPDATE, time).apply();
    }

    public void saveNotificationSoundEnabled(boolean isEnabled) {
        mIsNotificationSoundEnabled = isEnabled;
        mPreferences.edit().putBoolean(KEY_NOTIFICATION_SOUND, isEnabled).apply();
    }

    public boolean needUpdate() {
        return System.currentTimeMillis() - getLastUpdateTime() > 3 * 24 * 60 * 60 * 1000;
    }
}
