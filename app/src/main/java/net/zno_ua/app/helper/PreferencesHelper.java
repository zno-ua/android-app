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

    private PreferencesHelper(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        loadSettings();
    }

    private void loadSettings() {
        mEmail = mPreferences.getString(KEY_EMAIL, null);
        mName = mPreferences.getString(KEY_NAME, null);
        mMessage = mPreferences.getString(KEY_MESSAGE, null);
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

}
