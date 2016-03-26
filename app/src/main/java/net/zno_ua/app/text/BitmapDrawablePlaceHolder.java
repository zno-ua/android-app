package net.zno_ua.app.text;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

/**
 * @author vojkovladimir.
 */
public class BitmapDrawablePlaceHolder extends BitmapDrawable implements Target {

    private Drawable mDrawable;
    private TextView mTextView;

    public BitmapDrawablePlaceHolder(@NonNull TextView textView) {
        this(textView, null);
    }

    private BitmapDrawablePlaceHolder(@NonNull TextView textView, Bitmap bitmap) {
        super(textView.getContext().getResources(), bitmap);
        mTextView = textView;
    }

    @Override
    public void draw(final Canvas canvas) {
        if (mDrawable != null) {
            mDrawable.draw(canvas);
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
        final Resources res = mTextView.getContext().getResources();
        final DisplayMetrics metrics = res.getDisplayMetrics();
        final BitmapDrawable drawable = new BitmapDrawable(res, bitmap);
        int width = (int) (drawable.getIntrinsicWidth() * metrics.scaledDensity);
        int height = (int) (drawable.getIntrinsicHeight() * metrics.scaledDensity);
        int reqWidth = mTextView.getWidth() == 0 ? (int) (metrics.widthPixels * 0.7f)
                : mTextView.getWidth();
        if (width > reqWidth) {
            float scale = (float) reqWidth / (float) width;
            width = reqWidth;
            height = (int) (height * scale);
        }
        drawable.setBounds(0, 0, width, height);
        setDrawable(drawable);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        setDrawable(errorDrawable);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        setDrawable(placeHolderDrawable);
    }

    private void setDrawable(@Nullable Drawable drawable) {
        if (drawable != null) {
            mDrawable = drawable;
            setBounds(drawable.copyBounds());
            mTextView.setText(mTextView.getText());
        }
    }
}
