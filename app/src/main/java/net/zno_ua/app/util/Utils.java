package net.zno_ua.app.util;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import net.zno_ua.app.R;

import java.util.regex.Pattern;

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
}
