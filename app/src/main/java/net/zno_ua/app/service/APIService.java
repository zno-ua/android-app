package net.zno_ua.app.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.provider.ZNOContract;
import net.zno_ua.app.service.sync.TestSyncManager;

public class APIService extends IntentService {

    private static final String ACTION_RESTART_PENDING_REQUESTS = BuildConfig.APPLICATION_ID + ".action.RESTART_PENDING_REQUESTS";
    private static final String ACTION_GET_TEST = BuildConfig.APPLICATION_ID + ".action.GET_TEST";
    private static final String ACTION_UPDATE_TEST = BuildConfig.APPLICATION_ID + ".action.UPDATE_TEST";
    private static final String ACTION_DELETE_TEST = BuildConfig.APPLICATION_ID + ".action.DELETE_TEST";

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
        switch (intent.getAction()) {
            case ACTION_GET_TEST:
                mSyncManager.getTest(intent.getLongExtra(KEY_TEST_ID, -1));
                break;
            case ACTION_DELETE_TEST:
                mSyncManager.deleteTest(intent.getLongExtra(KEY_TEST_ID, -1));
                break;
            case ACTION_UPDATE_TEST:
                mSyncManager.updateTest(intent.getLongExtra(KEY_TEST_ID, -1));
                break;
            case ACTION_RESTART_PENDING_REQUESTS:
                restartPendingRequests();
                break;
        }
    }

    private void restartPendingRequests() {
        final Cursor cursor = getContentResolver().query(ZNOContract.Test.CONTENT_URI,
                new String[]{ZNOContract.Test._ID, ZNOContract.Test.STATUS},
                ZNOContract.Test.STATUS + " != " + ZNOContract.Test.STATUS_IDLE,
                null,
                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    int status = cursor.getInt(1);
                    if (status == ZNOContract.Test.STATUS_DOWNLOADING) {
                        mSyncManager.getTest(id);
                    } else if (status == ZNOContract.Test.STATUS_DELETING) {
                        mSyncManager.deleteTest(id);
                    } else if (status == ZNOContract.Test.STATUS_UPDATING) {
                        mSyncManager.updateTest(id);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }


    public static void restartPendingRequests(Context context) {
        context.startService(getIntent(context).setAction(ACTION_RESTART_PENDING_REQUESTS));
    }

    public static void getTest(Context context, long testId) {
        context.startService(getIntent(context).setAction(ACTION_GET_TEST).putExtra(KEY_TEST_ID, testId));
    }

    public static void updateTest(Context context, long testId) {
        context.startService(getIntent(context).setAction(ACTION_UPDATE_TEST).putExtra(KEY_TEST_ID, testId));
    }

    public static void deleteTest(Context context, long testId) {
        context.startService(getIntent(context).setAction(ACTION_DELETE_TEST).putExtra(KEY_TEST_ID, testId));
    }

    private static Intent getIntent(Context context) {
        return new Intent(context, APIService.class);
    }
}
