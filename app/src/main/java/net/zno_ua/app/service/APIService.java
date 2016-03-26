package net.zno_ua.app.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.helper.PreferencesHelper;
import net.zno_ua.app.provider.ZNOContract;
import net.zno_ua.app.service.sync.TestSyncManager;

public class APIService extends IntentService {

    private static final String ACTION_RESTART_PENDING_REQUESTS = BuildConfig.APPLICATION_ID + ".action.RESTART_PENDING_REQUESTS";
    private static final String ACTION_GET_TEST = BuildConfig.APPLICATION_ID + ".action.GET_TEST";
    private static final String ACTION_UPDATE_TEST = BuildConfig.APPLICATION_ID + ".action.UPDATE_TEST";
    private static final String ACTION_DELETE_TEST = BuildConfig.APPLICATION_ID + ".action.DELETE_TEST";
    private static final String ACTION_CHECK_FOR_UPDATES = BuildConfig.APPLICATION_ID + ".action.CHECK_FOR_UPDATES";

    private static final String KEY_TEST_ID = BuildConfig.APPLICATION_ID + ".KEY_TEST_ID";

    private TestSyncManager mSyncManager;

    public APIService() {
        super(APIService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSyncManager = TestSyncManager.getInstance(getBaseContext());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_GET_TEST:
                    mSyncManager.getTest(intent.getLongExtra(KEY_TEST_ID, -1));
                    break;
                case ACTION_DELETE_TEST:
                    mSyncManager.deleteTest(intent.getLongExtra(KEY_TEST_ID, -1));
                    break;
                case ACTION_UPDATE_TEST:
                    final long[] testsId = intent.getLongArrayExtra(KEY_TEST_ID);
                    for (long testId : testsId) {
                        mSyncManager.updateTest(testId);
                    }
                    break;
                case ACTION_CHECK_FOR_UPDATES:
                    mSyncManager.updateTests();
                    break;
                case ACTION_RESTART_PENDING_REQUESTS:
                    restartPendingRequests();
                    break;
            }
        }
    }

    private void restartPendingRequests() {
        final Cursor c = getContentResolver().query(ZNOContract.Test.CONTENT_URI,
                new String[]{ZNOContract.Test._ID, ZNOContract.Test.STATUS},
                ZNOContract.Test.STATUS + " != " + ZNOContract.Test.STATUS_IDLE,
                null,
                null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    int status = c.getInt(1);
                    if (status == ZNOContract.Test.STATUS_DOWNLOADING) {
                        mSyncManager.getTest(id);
                    } else if (status == ZNOContract.Test.STATUS_DELETING) {
                        mSyncManager.deleteTest(id);
                    } else if (status == ZNOContract.Test.STATUS_UPDATING) {
                        mSyncManager.updateTest(id);
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
        restartPendingUpdates();
        if (PreferencesHelper.getInstance(getBaseContext()).needUpdate()) {
            mSyncManager.updateTests();
        }
    }

    private void restartPendingUpdates() {
        final Cursor c = getContentResolver().query(ZNOContract.TestUpdate.CONTENT_URI,
                new String[]{ZNOContract.TestUpdate.TEST_ID},
                null,
                null,
                null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    mSyncManager.updateTest(c.getLong(0));
                } while (c.moveToNext());
            }
            c.close();
        }
    }

    public static void checkTestsUpdates(Context context) {
        context.startService(getIntent(context).setAction(ACTION_CHECK_FOR_UPDATES));
    }

    public static void restartPendingRequests(Context context) {
        context.startService(getIntent(context).setAction(ACTION_RESTART_PENDING_REQUESTS));
    }

    public static void getTest(Context context, long testId) {
        context.startService(getIntent(context).setAction(ACTION_GET_TEST).putExtra(KEY_TEST_ID, testId));
    }

    public static void updateTests(Context context, long[] testsId) {
        context.startService(getIntent(context).setAction(ACTION_UPDATE_TEST).putExtra(KEY_TEST_ID, testsId));
    }

    public static void deleteTest(Context context, long testId) {
        context.startService(getIntent(context).setAction(ACTION_DELETE_TEST).putExtra(KEY_TEST_ID, testId));
    }

    private static Intent getIntent(Context context) {
        return new Intent(context, APIService.class);
    }
}
