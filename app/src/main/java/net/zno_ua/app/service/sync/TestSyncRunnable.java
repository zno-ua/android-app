package net.zno_ua.app.service.sync;

import android.content.Context;
import android.support.annotation.NonNull;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.ZNOApplication;
import net.zno_ua.app.helper.PreferencesHelper;
import net.zno_ua.app.processor.TestProcessor;
import net.zno_ua.app.rest.APIServiceGenerator;
import net.zno_ua.app.rest.model.Objects;
import net.zno_ua.app.rest.model.TestInfo;
import net.zno_ua.app.service.APIService;

import java.io.IOException;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @author vojkovladimir.
 */
public class TestSyncRunnable implements Runnable {
    private static final long MIN_DELAY = 1000L;

    public static final int GET = 0x1;
    public static final int DELETE = 0x2;
    public static final int UPDATE = 0x3;
    public static final int CHECK_UPDATES = 0x4;

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

    public static TestSyncRunnable checkUpdates(@NonNull Methods methods) {
        return new TestSyncRunnable(0, CHECK_UPDATES, methods);
    }

    @Override
    public void run() {
        if (BuildConfig.DEBUG) {
            ZNOApplication.log("----TestSync: #" + mTestId + " " + getOperationDescription(mOperation) + " - started----");
        }
        final long startTime = System.currentTimeMillis();
        switch (mOperation) {
            case GET:
                mMethods.getTestProcessor().get(mTestId);
                break;
            case UPDATE:
                mMethods.getTestProcessor().update(mTestId);
                break;
            case DELETE:
                mMethods.getTestProcessor().delete(mTestId);
                break;
            case CHECK_UPDATES:
                final Call<Objects<TestInfo>> testsCall = APIServiceGenerator.getAPIClient()
                        .getTestsInfo();
                try {
                    final Response<Objects<TestInfo>> testsResponse = testsCall.execute();
                    if (testsResponse.isSuccess()) {
                        mMethods.getTestProcessor().process(testsResponse.body().get());
                        PreferencesHelper.getInstance(mMethods.getContext())
                                .saveLastUpdateTime(System.currentTimeMillis());
                    }
                } catch (IOException ignored) {
                }
                break;
        }
        final long delay = System.currentTimeMillis() - startTime;
        if (delay < MIN_DELAY) {
            try {
                Thread.sleep(MIN_DELAY - delay);
            } catch (InterruptedException ignored) {
            }
        }
        if (BuildConfig.DEBUG) {
            ZNOApplication.log("----TestSync: #" + mTestId + " " + getOperationDescription(mOperation) + " - finished----");
        }
        mMethods.finishTask(mTestId);
    }

    private static String getOperationDescription(int mOperation) {
        return mOperation == GET ? "GET" :
                mOperation == DELETE ? "DELETE" :
                        mOperation == UPDATE ? "UPDATE" : "CHECK_UPDATES";
    }

    public interface Methods {
        TestProcessor getTestProcessor();

        Context getContext();

        void finishTask(long testId);
    }
}
