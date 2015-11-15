package net.zno_ua.app.text;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;

import net.zno_ua.app.R;

import java.io.File;

/**
 * @author Vojko Vladimir
 */
public class ImageGetter implements Html.ImageGetter {

    private static final int ERROR_DRAWABLE_RES_ID = R.drawable.emo_im_crying;

    private final View mContainer;
    private final Context mContext;

    public ImageGetter(View container) {
        mContainer = container;
        mContext = container.getContext();
    }

    @Override
    public Drawable getDrawable(String source) {
        Resources res = mContext.getResources();
        DisplayMetrics displayMetrics = res.getDisplayMetrics();
        File image = new File(mContext.getFilesDir(), source);

        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());

        if (bitmap == null)
            return ResourcesCompat.getDrawable(res, ERROR_DRAWABLE_RES_ID, mContext.getTheme());

        Drawable drawable = new BitmapDrawable(res, bitmap);

        int width = (int) (drawable.getIntrinsicWidth() * displayMetrics.scaledDensity);
        int height = (int) (drawable.getIntrinsicHeight() * displayMetrics.scaledDensity);

        int reqWidth = mContainer.getWidth() == 0 ? (int) (displayMetrics.widthPixels * 0.7f)
                : mContainer.getWidth();

        if (width > reqWidth) {
            float scale = (float) reqWidth / (float) width;
            width = reqWidth;
            height = (int) (height * scale);
        }

        drawable.setBounds(0, 0, width, height);

        return drawable;
    }

}
