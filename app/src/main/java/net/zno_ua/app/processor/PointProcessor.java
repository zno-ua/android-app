package net.zno_ua.app.processor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.zno_ua.app.provider.Query;
import net.zno_ua.app.rest.model.Point;

import java.util.List;

import static net.zno_ua.app.provider.ZNOContract.Point.CONTENT_URI;
import static net.zno_ua.app.provider.ZNOContract.Point.RATING_POINT;
import static net.zno_ua.app.provider.ZNOContract.Point.TEST_ID;
import static net.zno_ua.app.provider.ZNOContract.Point.TEST_POINT;

/**
 * @author vojkovladimir.
 */
public class PointProcessor extends Processor<Point> {

    public PointProcessor(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void insert(@NonNull Point point) {
        getContentResolver().insert(CONTENT_URI, createContentValuesForInsert(point));
    }

    @Override
    protected void update(@NonNull Point point, @NonNull Cursor cursor) {
        final String selection = TEST_ID + "=? AND " + TEST_POINT + "=?";
        final String[] selectionArgs = Query.selectionArgs(point.getTestId(), point.getTestPoint());
        final ContentValues values = createContentValuesForUpdate(point);
        getContentResolver().update(CONTENT_URI, values, selection, selectionArgs);
    }

    @Override
    protected Cursor query(@NonNull Point point) {
        final String[] projection = new String[]{RATING_POINT};
        final String selection = TEST_ID + "=? AND " + TEST_POINT + "=?";
        final String[] selectionArgs = Query.selectionArgs(point.getTestId(), point.getTestPoint());
        return getContentResolver().query(CONTENT_URI, projection, selection, selectionArgs, null);
    }

    @Override
    protected void cleanUp(@Nullable List<Point> data) {
    }

    @Override
    protected boolean shouldUpdate(@NonNull Point point, @NonNull Cursor cursor) {
        return point.getRatingPoint() != cursor.getFloat(0);
    }

    @Override
    public ContentValues createContentValuesForInsert(Point point) {
        return createContentValuesForUpdate(point);
    }

    @Override
    public ContentValues createContentValuesForUpdate(Point point) {
        final ContentValues values = new ContentValues(3);
        values.put(TEST_ID, point.getTestId());
        values.put(TEST_POINT, point.getTestPoint());
        values.put(RATING_POINT, point.getRatingPoint());
        return values;
    }

    @Override
    protected String createSelectionArg(@NonNull Point point) {
        return null;
    }

    public void delete(long testId) {
        getContentResolver().delete(CONTENT_URI, TEST_ID + "=" + testId, null);
    }
}
