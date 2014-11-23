package net.zno_ua.app;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class ViewImageActivity extends Activity implements OnClickListener,
		OnTouchListener {

    public static final String DATA_SCHEMA = "open.image";

    private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;

	FileManager fm;
	ImageView img;

	String source;

	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	int mode = NONE;
	boolean isMoved = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_image);
		Intent intent = getIntent();
		Uri data = intent.getData();
		
		if (data == null ) {
			finish();
		} else {
			source = data.getQueryParameter("src");
		}

		fm = new FileManager(this);
		img = (ImageView) findViewById(R.id.view_image_img);
		img.setOnClickListener(this);
		img.setOnTouchListener(this);

		try {
			Drawable imgDrawable = fm.openDrawable(source);
			DisplayMetrics dMetrics = getResources().getDisplayMetrics();

			int drawableWidth = imgDrawable.getIntrinsicWidth();
			int drawableHeight = imgDrawable.getIntrinsicHeight();

			int screenWidth = dMetrics.widthPixels;
			int screenHeight = dMetrics.heightPixels;

			imgDrawable.setBounds(0, 0, drawableWidth, drawableHeight);
			img.setImageDrawable(imgDrawable);

			Matrix m = img.getImageMatrix();

			RectF drawableRect = new RectF(0, 0, drawableWidth, drawableHeight);
			RectF viewRect = new RectF(0, 0, screenWidth, screenHeight);
			m.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
			img.setImageMatrix(m);

			matrix.set(img.getImageMatrix());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View v) {
		finish();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			isMoved = false;
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (!isMoved) {
				v.performClick();
			}
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {

				float length = (float) Math.sqrt(Math.pow(event.getX()
						- start.x, 2)
						+ Math.pow(event.getY() - start.y, 2));
				if (length < 10f) {
					isMoved = false;
				} else {
					isMoved = true;
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - start.x, event.getY()
							- start.y);
				}
			} else if (mode == ZOOM) {
				isMoved = true;
				float newDist = spacing(event);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}
		view.setImageMatrix(matrix);
		
		return true;
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

}
