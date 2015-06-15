package net.zno_ua.app.ui.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;

import net.zno_ua.app.R;

/**
 * @author Vojko Vladimir
 */
public class UiUtils {

    public static TypedValue getThemeAttribute(Context context, int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        return value;
    }

    public static void setDrawerLayoutWidth(Activity activity, View drawerView) {
        final Display display = activity.getWindowManager().getDefaultDisplay();
        final int actionBarSize = activity.getResources().getDimensionPixelSize(
                R.dimen.abc_action_bar_default_height_material
        );
        final Point size = new Point();
        display.getSize(size);

        final ViewGroup.LayoutParams params = drawerView.getLayoutParams();
        params.width =  Math.min(size.x - actionBarSize, 6 * actionBarSize);
    }
}
