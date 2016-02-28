package net.zno_ua.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.SparseArray;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.processor.TestProcessor;
import net.zno_ua.app.rest.APIClient;
import net.zno_ua.app.rest.ServiceGenerator;

import static net.zno_ua.app.provider.ZNOContract.Test;

public class APIService extends Service {

    public interface Action {
        String RESTART_PENDING_REQUESTS = BuildConfig.APPLICATION_ID + ".RESTART_PENDING_REQUESTS";
    }

    public interface Extra {
        String METHOD = "EXTRA_METHOD";
        String RESOURCE_TYPE = "EXTRA_RESOURCE_TYPE";
        String ID = "EXTRA_ID";
    }

    private final SparseArray<Command> mPendingCommands;
    private APIClient mApiClient;
    private TestProcessor mTestProcessor;

    public APIService() {
        mPendingCommands = new SparseArray<>();
    }

    public static void restartPendingRequests(Context context) {
        context.startService(getIntent(context).setAction(APIService.Action.RESTART_PENDING_REQUESTS));
    }

    public static void getTest(Context context, long testId) {
        context.startService(getIntent(context)
                .putExtra(APIService.Extra.METHOD, APIService.Method.GET)
                .putExtra(APIService.Extra.RESOURCE_TYPE, APIService.ResourceType.TEST)
                .putExtra(APIService.Extra.ID, testId));
    }

    public static void deleteTest(Context context, long testId) {
        context.startService(getIntent(context)
                .putExtra(APIService.Extra.METHOD, APIService.Method.DELETE)
                .putExtra(APIService.Extra.RESOURCE_TYPE, APIService.ResourceType.TEST)
                .putExtra(APIService.Extra.ID, testId));
    }

    private static Intent getIntent(Context context) {
        return new Intent(context, APIService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApiClient = ServiceGenerator.createService(APIClient.class, BuildConfig.API_KEY);
        mTestProcessor = new TestProcessor(getApplicationContext(), mApiClient);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Action.RESTART_PENDING_REQUESTS.equals(intent.getAction())) {
            restartPendingRequests(startId);
            return START_NOT_STICKY;
        }

        int method = intent.getIntExtra(Extra.METHOD, -1);
        int resourceType = intent.getIntExtra(Extra.RESOURCE_TYPE, -1);
        long id = intent.getLongExtra(Extra.ID, -1);

        Command command = new Command(method, resourceType, id);
        if (isCommandPending(command)) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        mPendingCommands.put(startId, command);

        switch (method) {
            case Method.GET:
                switch (resourceType) {
                    case ResourceType.TEST:
                        getTest(id, startId);
                        return START_REDELIVER_INTENT;
                    default:
                        stopSelf(startId);
                        throw new IllegalArgumentException("Illegal resource type " + resourceType
                                + " for GET method.");
                }
            case Method.DELETE:
                switch (resourceType) {
                    case ResourceType.TEST:
                        deleteTest(id, startId);
                        return START_REDELIVER_INTENT;
                    default:
                        stopSelf(startId);
                        throw new IllegalArgumentException("Illegal resource type " + resourceType
                                + " for DELETE method.");
                }
            default:
                throw new IllegalArgumentException("Illegal method " + method);
        }
    }

    private void restartPendingRequests(final int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Cursor cursor = getContentResolver().query(Test.CONTENT_URI,
                        new String[]{Test._ID, Test.STATUS},
                        Test.STATUS + " != " + Test.STATUS_IDLE,
                        null,
                        null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            long id = cursor.getLong(0);
                            int method = cursor.getInt(1) == Test.STATUS_DOWNLOADING ?
                                    Method.GET : Method.DELETE;

                            if (!isCommandPending(method, ResourceType.TEST, id)) {
                                switch (method) {
                                    case Method.GET:
                                        APIService.getTest(getBaseContext(), id);
                                        break;
                                    case Method.DELETE:
                                        APIService.deleteTest(getBaseContext(), id);
                                        break;
                                }
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
                stopSelf(startId);
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void getTest(final long testId, final int startId) {
        if (testId == -1)
            throw new IllegalArgumentException("Invalid test id.");

        new Thread(new Runnable() {
            @Override
            public void run() {
                mTestProcessor.get(testId);
                onStopCommand(startId);
            }
        }).start();

    }

    private void deleteTest(final long testId, final int startId) {
        if (testId == -1)
            throw new IllegalArgumentException("Invalid test id.");

        new Thread(new Runnable() {
            @Override
            public void run() {
                mTestProcessor.delete(testId);
                onStopCommand(startId);
            }
        }).start();
    }

    private void onStopCommand(int startId) {
        mPendingCommands.remove(startId);
        stopSelf(startId);
    }

    private boolean isCommandPending(Command command) {
        for (int i = 0; i < mPendingCommands.size(); i++) {
            if (mPendingCommands.valueAt(i).equals(command)) {
                return true;
            }
        }

        return false;
    }

    private boolean isCommandPending(int method, int resource, long id) {
        Command cmd;
        for (int i = 0; i < mPendingCommands.size(); i++) {
            cmd = mPendingCommands.valueAt(i);
            if (cmd.getMethod() == method && cmd.getResource() == resource && cmd.getId() == id) {
                return true;
            }
        }

        return false;
    }

    public interface Method {
        int GET = 0;
        int DELETE = 1;
    }

    public interface ResourceType {
        int TEST = 0x1;
    }
}
