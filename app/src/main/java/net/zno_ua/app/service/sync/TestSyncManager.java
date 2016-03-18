package net.zno_ua.app.service.sync;

import android.content.Context;
import android.support.annotation.NonNull;

import net.zno_ua.app.processor.TestProcessor;
import net.zno_ua.app.provider.ZNOContract;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author vojkovladimir.
 */
public class TestSyncManager implements TestSyncRunnable.Methods {
    private static TestSyncManager sInstance = null;

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final ThreadPoolExecutor mExecutor;
    private final Set<Long> mRequests;
    private final TestProcessor mTestProcessor;
    private final Context mContext;

    private TestSyncManager(@NonNull Context context) {
        mContext = context;
        mExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        mRequests = new HashSet<>();
        mTestProcessor = new TestProcessor(context);
    }

    public static synchronized TestSyncManager getInstance(@NonNull Context context) {
        synchronized (TestSyncManager.class) {
            if (sInstance == null) {
                sInstance = new TestSyncManager(context);
            }
        }

        return sInstance;
    }

    public void updateTests() {
        mExecutor.execute(TestSyncRunnable.checkUpdates(this));
    }

    public void getTest(long testId) {
        synchronized (mRequests) {
            if (!isPending(testId)) {
                mTestProcessor.updateTestStatus(testId, ZNOContract.Test.STATUS_DOWNLOADING);
                mRequests.add(testId);
                mExecutor.execute(TestSyncRunnable.get(testId, this));
            }
        }
    }

    public void updateTest(long testId) {
        synchronized (mRequests) {
            if (isPending(testId) || !getTestProcessor().canBeUpdated(testId)) {
                if (getTestProcessor().getStatus(testId) != ZNOContract.Test.STATUS_UPDATING) {
                    mTestProcessor.requestUpdate(testId);
                }
            } else {
                mTestProcessor.updateTestStatus(testId, ZNOContract.Test.STATUS_UPDATING);
                mRequests.add(testId);
                mExecutor.execute(TestSyncRunnable.update(testId, this));
            }
        }
    }

    public void deleteTest(long testId) {
        synchronized (mRequests) {
            if (!isPending(testId)) {
                mTestProcessor.updateTestStatus(testId, ZNOContract.Test.STATUS_DELETING);
                mRequests.add(testId);
                mExecutor.execute(TestSyncRunnable.delete(testId, this));
            }
        }
    }

    private boolean isPending(long testId) {
        return mRequests.contains(testId);
    }

    @Override
    public TestProcessor getTestProcessor() {
        return mTestProcessor;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void finishTask(long testId) {
        synchronized (mRequests) {
            mRequests.remove(testId);
        }
        mTestProcessor.updateTestStatus(testId, ZNOContract.Test.STATUS_IDLE);
    }
}
