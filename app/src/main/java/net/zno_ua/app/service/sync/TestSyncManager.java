package net.zno_ua.app.service.sync;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import net.zno_ua.app.processor.TestProcessor;
import net.zno_ua.app.provider.ZNOContract;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static net.zno_ua.app.provider.ZNOContract.Test.buildTestItemUri;

/**
 * @author vojkovladimir.
 */
public class TestSyncManager implements TestSyncRunnable.Methods {
    private static TestSyncManager sInstance = null;

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final ThreadPoolExecutor mExecutor;
    private final Context mContext;
    private final Set<Long> mRequests;
    private final TestProcessor mTestProcessor;

    private TestSyncManager(@NonNull Context context) {
        mExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        mContext = context;
        mRequests = new HashSet<>();
        mTestProcessor = new TestProcessor(context);
        prepare();
    }

    private void prepare() {
        /*TODO restart unfinished tasks*/
    }

    @WorkerThread
    public static synchronized TestSyncManager getInstance(@NonNull Context context) {
        synchronized (TestSyncManager.class) {
            if (sInstance == null) {
                sInstance = new TestSyncManager(context);
            }
        }

        return sInstance;
    }

    public void getTest(long testId) {
        synchronized (mRequests) {
            if (!isPending(testId)) {
                updateTestStatus(testId, ZNOContract.Test.STATUS_DOWNLOADING);
                mRequests.add(testId);
                mExecutor.execute(TestSyncRunnable.get(testId, this));
            }
        }
    }

    public void deleteTest(long testId) {
        synchronized (mRequests) {
            if (!isPending(testId)) {
                updateTestStatus(testId, ZNOContract.Test.STATUS_DELETING);
                mRequests.add(testId);
                mExecutor.execute(TestSyncRunnable.delete(testId, this));
            }
        }
    }

    private boolean isPending(long testId) {
        return mRequests.contains(testId);
    }

    void updateTestStatus(long testId, int status) {
        final ContentValues values = new ContentValues();
        values.put(ZNOContract.Test.STATUS, status);
        mContext.getContentResolver().update(buildTestItemUri(testId), values, null, null);
    }

    @Override
    public TestProcessor getTestProcessor() {
        return mTestProcessor;
    }

    @Override
    public void finishTask(long testId) {
        synchronized (mRequests) {
            mRequests.remove(testId);
        }
        updateTestStatus(testId, ZNOContract.Test.STATUS_IDLE);
    }
}
