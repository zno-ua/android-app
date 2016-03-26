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

    private final BitmapDrawablePlaceHolder mDrawable;

    public ImageGetter(@NonNull TextView textView) {
        mDrawable = new BitmapDrawablePlaceHolder(textView);
    }

    @Override
    public Drawable getDrawable(String source) {
        final Context context = ZNOApplication.getInstance();
        Picasso.with(context).load(new File(context.getFilesDir(), source)).into(mDrawable);
        return mDrawable;
    }

}
