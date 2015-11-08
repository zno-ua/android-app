package net.zno_ua.app.ui;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import net.zno_ua.app.FileManager;
import net.zno_ua.app.R;

import java.io.FileNotFoundException;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class ViewImageActivity extends AppCompatActivity {
    public static final String DATA_SCHEMA = "image";

    private static final String SRC = "src";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        setSupportActionBar((Toolbar) findViewById(R.id.app_bar));

        FileManager fileManager = new FileManager(this);
        ImageViewTouch imageViewTouch = (ImageViewTouch) findViewById(R.id.image);
        try {
            Bitmap image = fileManager.openBitmap(getIntent().getData().getQueryParameter(SRC));
            imageViewTouch.setImageBitmap(image, new Matrix(), 0.5f, 3.0f);
            imageViewTouch.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        } catch (FileNotFoundException e) {
            Log.e(this.toString(), e.toString());
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_image, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_close) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
