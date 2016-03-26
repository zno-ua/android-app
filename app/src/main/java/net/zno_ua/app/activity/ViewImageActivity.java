package net.zno_ua.app.activity;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import net.zno_ua.app.R;

import java.io.File;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class ViewImageActivity extends BaseActivity {
    public static final String DATA_SCHEMA = "image";

    private static final String SRC = "src";
    private ImageViewTouch mImageViewTouch;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        mImageViewTouch = (ImageViewTouch) findViewById(R.id.image);
        findViewById(R.id.fab).setOnClickListener(mFABClickListener);

        final String path = getIntent().getData().getQueryParameter(SRC);
        final File image = new File(getFilesDir(), path);

        Picasso.with(this).load(image).into(mImageTarget);
    }

    private final Target mImageTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
            mImageViewTouch.setImageBitmap(bitmap, new Matrix(), 0.5f, 3.0f);
            mImageViewTouch.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            finish();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    private final OnClickListener mFABClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}
