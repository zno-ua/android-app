package net.zno_ua.app.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * @author Vojko Vladimir
 */
public class UiUtils {

    public static TypedValue getThemeAttribute(Context context, int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        return value;
    }
}
