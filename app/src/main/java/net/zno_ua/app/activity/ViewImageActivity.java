package net.zno_ua.app.activity;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.zno_ua.app.FileManager;
import net.zno_ua.app.R;

import java.io.FileNotFoundException;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class ViewImageActivity extends BaseActivity {
    public static final String DATA_SCHEMA = "image";

    private static final String SRC = "src";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        final FileManager fileManager = new FileManager(this);
        final ImageViewTouch imageViewTouch = (ImageViewTouch) findViewById(R.id.image);
        final String path = getIntent().getData().getQueryParameter(SRC);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap image = fileManager.openBitmap(path);
                    imageViewTouch.setImageBitmap(image, new Matrix(), 0.5f, 3.0f);
                    imageViewTouch.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
                } catch (FileNotFoundException e) {
                    finish();
                }
            }
        }).start();
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
