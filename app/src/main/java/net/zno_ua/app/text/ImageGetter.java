package net.zno_ua.app.text;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Html;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.zno_ua.app.ZNOApplication;

import java.io.File;

/**
 * @author Vojko Vladimir
 */
public class ImageGetter implements Html.ImageGetter {

    private final TextView mTextView;

    public ImageGetter(@NonNull TextView textView) {
        mTextView = textView;
    }

    @Override
    public Drawable getDrawable(String source) {
        final Context context = ZNOApplication.getInstance();
        final BitmapDrawablePlaceHolder drawable = new BitmapDrawablePlaceHolder(mTextView);
        Picasso.with(context).load(new File(context.getFilesDir(), source)).into(drawable);
        return drawable;
    }

}
