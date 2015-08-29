package net.zno_ua.app.util;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import net.zno_ua.app.R;

/**
 * @author Vojko Vladimir
 */
public class UiUtils {

    public static final int[] SUBJECT_IMAGE_RES_ID = {
            0,
            R.drawable.ic_ukrainian,
            R.drawable.ic_history_ukr,
            R.drawable.ic_history_world,
            R.drawable.ic_math,
            R.drawable.ic_biology,
            R.drawable.ic_geography,
            R.drawable.ic_english,
            R.drawable.ic_physics,
                /*
                * TODO: find new picture that will correspond to this resource type
                * */
            R.drawable.ic_chemistry
    };

    public static final int[] SUBJECT_COLOR_RES_ID = {
            0,
            R.color.ukrainian,
            R.color.history_ukr,
            R.color.black,
            R.color.math,
            R.color.biology,
            R.color.geography,
            R.color.english,
            R.color.physics,
            R.color.chemistry
    };

    public static TypedValue getThemeAttribute(Context context, int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        return value;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null)
            if (focusedView.getWindowToken() != null)
                inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
