package net.zno_ua.app.text;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;

import net.zno_ua.app.FileManager;
import net.zno_ua.app.R;

import java.io.FileNotFoundException;

/**
 * @author Vojko Vladimir
 */
public class ImageGetter implements Html.ImageGetter {

    public static final int UNDEFINED_WIDTH = -1;
    private DisplayMetrics mDisplayMetrics;
    private FileManager mFileManager;
    private Drawable noImageDrawable;
    private int maxWidth = UNDEFINED_WIDTH;

    public ImageGetter(Context context) {
        mDisplayMetrics = context.getResources().getDisplayMetrics();
        mFileManager = new FileManager(context);
        /*
        * TODO: replace with correct image.
        * */
        noImageDrawable = ContextCompat.getDrawable(context, R.drawable.emo_im_crying);
    }

    @Override
    public Drawable getDrawable(String source) {
        try {
            Drawable drawable = mFileManager.openDrawable(source);

            int width = (int) (drawable.getIntrinsicWidth() * mDisplayMetrics.scaledDensity);
            int height = (int) (drawable.getIntrinsicHeight() * mDisplayMetrics.scaledDensity);

            if (maxWidth != UNDEFINED_WIDTH && width > maxWidth) {
                float scale = (float) maxWidth / (float) width;
                width = maxWidth;
                height = (int) (height * scale);
            }

            drawable.setBounds(0, 0, width, height);

            return drawable;
        } catch (FileNotFoundException e) {
            Log.e("ImageGetter", "", e);
        }

        return noImageDrawable;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public boolean maxWidthIsNotSet() {
        return maxWidth == ImageGetter.UNDEFINED_WIDTH;
    }
}
