package net.zno_ua.app.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.R;
import net.zno_ua.app.ZNOApplication;
import net.zno_ua.app.processor.TestProcessor;

import java.lang.ref.WeakReference;

/**
 * @author vojkovladimir.
 */
public class SyncTask extends AsyncTask<Void, Void, Void> {
    private static final long MIN_DELAY = 1_000L;
    private static final int SYNC = 0x1;
    private static final int CLEAN_UP = 0x2;

    private final TestProcessor mTestProcessor;
    private MaterialDialog mDialog;
    private final WeakReference<Callback> mCallbackWeakReference;
    private final int mOperation;

    private SyncTask(@NonNull Context context, @NonNull Callback callback, int operation) {
        mTestProcessor = new TestProcessor(context);
        mCallbackWeakReference = new WeakReference<>(callback);
        mDialog = new MaterialDialog.Builder(context).progress(true, 0).cancelable(false).build();
        mOperation = operation;
    }

    @Override
    protected void onPreExecute() {
        switch (mOperation) {
            case SYNC:
                mDialog = mDialog.getBuilder().content(R.string.syncing).show();
                break;
            case CLEAN_UP:
                mDialog = mDialog.getBuilder().content(R.string.cleaning_cache).show();
                break;
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        final long startTime = System.currentTimeMillis();
        switch (mOperation) {
            case SYNC:
                mTestProcessor.checkForUpdates();
                break;
            case CLEAN_UP:
                mTestProcessor.cleanUp();
                break;
        }
        final long delay = System.currentTimeMillis() - startTime;
        if (delay < MIN_DELAY) {
            try {
                Thread.sleep(MIN_DELAY - delay);
            } catch (InterruptedException ignored) {
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        try {
            mDialog.dismiss();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                ZNOApplication.log("SyncTask " + (mOperation == SYNC ? "SYNC" : "CLEAN") + ":" + e);
            }
        }
        if (mCallbackWeakReference.get() != null) {
            mCallbackWeakReference.get().onFinished();
        }
    }

    public static SyncTask sync(@NonNull Context context, @NonNull Callback callback) {
        return new SyncTask(context, callback, SYNC);
    }

    public static SyncTask cleanUp(@NonNull Context context, @NonNull Callback callback) {
        return new SyncTask(context, callback, CLEAN_UP);
    }

    public interface Callback {
        void onFinished();
    }
}
