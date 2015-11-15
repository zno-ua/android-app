package net.zno_ua.app.ui.picasso.transformation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

public class Rounded implements Transformation {
    private final int radius;
    private final Corners corners;

    public enum Corners {
        TOP(0x8),
        BOTTOM(0x4),
        LEFT(0x2),
        RIGHT(0x1),
        TOP_LEFT(0xA),
        TOP_RIGHT(0x9),
        BOTTOM_LEFT(0x6),
        BOTTOM_RIGHT(0x5),
        ALL(0xF);

        private int code;

        Corners(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public Rounded(int radius, Corners corners) {
        this.radius = radius;
        this.corners = corners;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int w = source.getWidth();
        int h = source.getHeight();
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);
        final float roundPx = radius;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);  // Zero everything out, I guess.
        paint.setColor(color);  // Grayish.
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  // Grayish round rect.

        // Draw selected rounded corners only.
        // This turns unwanted rounded corners into square corners.
        boolean top = (corners.getCode() & Corners.TOP.getCode()) > 0;
        boolean bottom = (corners.getCode() & Corners.BOTTOM.getCode()) > 0;
        boolean left = (corners.getCode() & Corners.LEFT.getCode()) > 0;
        boolean right = (corners.getCode() & Corners.RIGHT.getCode()) > 0;

        canvas.drawRect(left ? roundPx : 0,
                top ? roundPx : 0,
                right ? w - roundPx : w,
                bottom ? h - roundPx : h,
                paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, rect, rect, paint);

        source.recycle();
        return output;
    }

    @Override
    public String key() {
        return "rounded();";
    }
}