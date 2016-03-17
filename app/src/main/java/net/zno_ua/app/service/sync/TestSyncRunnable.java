package net.zno_ua.app.service.sync;

import android.support.annotation.NonNull;

import net.zno_ua.app.processor.TestProcessor;

/**
 * @author vojkovladimir.
 */
public class TestSyncRunnable implements Runnable {
    private static final long MIN_DELAY = 500L;

    public static final int GET = 0x1;
    public static final int DELETE = 0x2;
    public static final int UPDATE = 0x3;

    private final Methods mMethods;
    private final int mOperation;
    private final long mTestId;

    private TestSyncRunnable(long testId, int operation, @NonNull Methods methods) {
        mTestId = testId;
        mMethods = methods;
        mOperation = operation;
    }

    public static TestSyncRunnable get(long testId, @NonNull Methods methods) {
        return new TestSyncRunnable(testId, GET, methods);
    }

    public static TestSyncRunnable update(long testId, @NonNull Methods methods) {
        return new TestSyncRunnable(testId, UPDATE, methods);
    }

    public static TestSyncRunnable delete(long testId, @NonNull Methods methods) {
        return new TestSyncRunnable(testId, DELETE, methods);
    }

    @Override
    public void run() {
        switch (mOperation) {
            case GET:
                mMethods.getTestProcessor().get(mTestId);
                break;
            case UPDATE:
                mMethods.getTestProcessor().update(mTestId);
                break;
            case DELETE:
                final long startTime = System.currentTimeMillis();
                mMethods.getTestProcessor().delete(mTestId);
                final long delay = System.currentTimeMillis() - startTime;
                if (delay < MIN_DELAY) {
                    try {
                        Thread.sleep(MIN_DELAY - delay);
                    } catch (InterruptedException ignored) {
                    }
                }
                break;
        }
        mMethods.finishTask(mTestId);
    }

    public interface Methods {
        TestProcessor getTestProcessor();

        void finishTask(long testId);
    }
}
