package net.zno_ua.app.processor2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

/**
 * @author vojkovladimir.
 */
public abstract class Processor<T> {
    private final Context mContext;
    private final ContentResolver mContentResolver;

    public Processor(@NonNull Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    @WorkerThread
    public void process(@Nullable List<T> data) {
        if (data != null) {
            for (T t : data) {
                processItem(t);
            }
        }
        cleanUp(data);
    }

    @WorkerThread
    public void process(T t) {
        processItem(t);
        cleanUp(t);
    }

    protected void processItem(T t) {
        final Cursor cursor = query(t);

        if (cursor != null) {
            if (!cursor.moveToFirst()) {
                insert(t);
            } else if (shouldUpdate(t, cursor)) {
                update(t);
            }
            cursor.close();
        }
    }

    protected abstract void insert(@NonNull T t);

    protected abstract void update(@NonNull T t);

    protected abstract Cursor query(@NonNull T t);

    protected abstract void cleanUp(@Nullable List<T> data);

    protected void cleanUp(@NonNull T t) {
    }

    protected abstract boolean shouldUpdate(@NonNull T t, @NonNull Cursor cursor);

    public abstract ContentValues createContentValuesForInsert(T t);

    public abstract ContentValues createContentValuesForUpdate(T t);

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @NonNull
    protected ContentResolver getContentResolver() {
        return mContentResolver;
    }

    @NonNull
    protected String[] createSelectionArgs(@Nullable List<T> data) {
        if (data == null) {
            return new String[]{};
        }
        final String[] args = new String[data.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = createSelectionArg(data.get(i));
        }
        return args;
    }

    @NonNull
    protected abstract String createSelectionArg(@NonNull T t);

    public static String createPlaceHolders(@Nullable List data) {
        final StringBuilder placeholders = new StringBuilder("(");

        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                placeholders.append("?");
                if (i != data.size() - 1) {
                    placeholders.append(",");
                }
            }
        }

        placeholders.append(")");

        return placeholders.toString();
    }

}