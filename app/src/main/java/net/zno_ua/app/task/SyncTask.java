package net.zno_ua.app.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import net.zno_ua.app.processor.TestProcessor;

import java.lang.ref.WeakReference;

/**
 * @author vojkovladimir.
 */
public class SyncTask extends AsyncTask<Integer, Void, Void> {
    private static final long MIN_DELAY = 1_000L;
    public static final int SYNC = 0x1;
    public static final int CLEAN_UP = 0x2;

    private final TestProcessor mTestProcessor;
    private final WeakReference<Callback> mCallbackWeakReference;

    public SyncTask(@NonNull Context context, @NonNull Callback callback) {
        mTestProcessor = new TestProcessor(context);
        mCallbackWeakReference = new WeakReference<>(callback);
    }

    @Override
    protected Void doInBackground(Integer... params) {
        final long startTime = System.currentTimeMillis();
        switch (params[0]) {
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
        if (mCallbackWeakReference.get() != null) {
            mCallbackWeakReference.get().onFinished();
        }
    }

    public interface Callback {
        void onFinished();
    }
}
