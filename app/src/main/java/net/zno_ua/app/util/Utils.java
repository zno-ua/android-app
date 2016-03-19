package net.zno_ua.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.R;
import net.zno_ua.app.helper.CustomTabActivityHelper;

import java.util.regex.Pattern;

import static android.media.RingtoneManager.getDefaultUri;

/**
 * @author Vojko Vladimir
 */
public class Utils {

    public static final int[] SUBJECT_IMAGE_RES_ID = {
            0,
            R.drawable.ic_ukrainian,
            R.drawable.ic_history_ukr,
            0,
            R.drawable.ic_math,
            R.drawable.ic_biology,
            R.drawable.ic_geography,
            R.drawable.ic_english,
            R.drawable.ic_physics,
            R.drawable.ic_chemistry
    };

    public static final int[] SUBJECT_COLOR_RES_ID = {
            0,
            R.color.ukrainian,
            R.color.history_ukr,
            0,
            R.color.math,
            R.color.biology,
            R.color.geography,
            R.color.english,
            R.color.physics,
            R.color.chemistry
    };

    public static final long MIN_TASK_DELAY = 1_500;

    public static final Uri SITE_URI = Uri.parse(BuildConfig.SERVER_URL);
    public static final Uri CALCULATOR_URI = Uri.parse(BuildConfig.SERVER_URL + "/calculator");

    public static final Uri DEFAULT_SOUND_URI = getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    public static TypedValue getThemeAttribute(Context context, int attr) {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        return value;
    }

    public static void hideSoftKeyboard(@Nullable View view) {
        if (view != null) {
            final InputMethodManager imm = (InputMethodManager) view.getContext()
                    .getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (view.getWindowToken() != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static void disableSupportsChangeAnimations(RecyclerView recyclerView) {
        final RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    public static String getFilesDirPath(Context context) {
        return "file://" + context.getFilesDir().getPath();
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]" +
            "+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)" +
            "+[a-zA-Z]{2,}))$");

    public static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static void openUriInCustomTabs(@NonNull Activity activity,
                                           @NonNull CustomTabActivityHelper helper,
                                           @NonNull Uri uri) {
        if (helper.mayLaunchUrl(uri, null, null)) {
            final CustomTabsSession session = helper.getSession();
            final CustomTabsIntent intent = new CustomTabsIntent.Builder(session)
                    .setToolbarColor(ContextCompat.getColor(activity, R.color.indigo_500))
                    .setSecondaryToolbarColor(ContextCompat.getColor(activity, R.color.indigo_700))
                    .setStartAnimations(activity, R.anim.activity_open_translate_right,
                            R.anim.activity_close_alpha)
                    .setExitAnimations(activity, R.anim.activity_open_alpha,
                            R.anim.activity_close_translate_right)
                    .build();
            CustomTabActivityHelper.openCustomTab(activity, intent, uri, null);
        } else {
            activity.startActivity(new Intent(Intent.ACTION_VIEW).setData(uri));
        }
    }

    public static void writeMeEmail(@NonNull Activity activity) {
        ShareCompat.IntentBuilder.from(activity)
                .setType("message/rfc822")
                .addEmailTo(activity.getResources().getStringArray(R.array.emails))
                .setSubject(activity.getString(R.string.zno_email_subject))
                .setChooserTitle(activity.getString(R.string.write_me)).startChooser();
    }
}
