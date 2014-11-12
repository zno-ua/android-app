package com.vojkovladimir.zno.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.vojkovladimir.zno.FileManager;
import com.vojkovladimir.zno.MainActivity;
import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.ZNOApplication;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ApiService extends Service {

    private static final String SITE_URL = "http://zno-ua.net";
    private static final String API_URL = SITE_URL + "/api/v1/";
    private static final String GET_TESTS = API_URL + "test/?format=json";
    private static final String GET_TEST = API_URL + "question/?format=json&test=";
    private static final String GET_TEST_BALLS = API_URL + "result/?format=json&test_id=%d&limit=0";

    private static final int ZNO_UPDATE_NOTIFY = 0x1;

    public static final String REQUEST_TAG = "api_request";
    public static final String ACTION_CHECK_FOR_UPDATES = "api.CHECK_FOR_UPDATES";

    public static interface Keys {
        String OBJECTS = "objects";
        String IMAGES = "images";
        String ID = "id";
        String ID_ON_TEST = "id_on_test";
        String LESSON_ID = "lesson_id";
        String LINK = "link";
        String NAME = "name";
        String TASK_ALL = "task_all";
        String YEAR = "year";
        String TIME = "time";
        String ANSWERS = "answers";
        String BALLS = "balls";
        String CORRECT_ANSWER = "correct_answer";
        String ID_TEST_QUESTION = "id_test_question";
        String QUESTION = "question";
        String TYPE_QUESTION = "type_question";
        String IMAGES_RELATIVE_URL = "images_relative_url";
        String LAST_UPDATE = "last_update";
        String PARENT_QUESTION = "parent_question";
        String ZNO_BALL = "zno_ball";
    }

    public interface TestDLCallBack {
        void onDownloadImagesStart(int imagesCount);

        void onImageDownloaded();

        void onSavingTest();

        void onTestDownloaded();

        void onError(Exception e);
    }

    class CheckForUpdatesThread extends Thread implements TestDLCallBack, ErrorListener {

        @Override
        public void run() {
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ApiService.this);
            Notification notification;
            boolean successful = false;
            boolean notify = false;
            try {
                DLTestThread dlTestThread;
                JSONArray tests = getTests(this);
                ArrayList<Integer> ids = db.getTestsForUpdate(tests);

                if (ids.size() > 0) {
                    mBuilder.setSmallIcon(android.R.drawable.stat_notify_sync)
                            .setContentTitle(getString(R.string.zno))
                            .setProgress(0, 0, true)
                            .setContentText(getString(R.string.updating_db));
                    notification = mBuilder.build();
                    notification.flags = Notification.FLAG_NO_CLEAR;
                    mNotifyMgr.notify(ZNO_UPDATE_NOTIFY, notification);
                    notify = true;
                    for (int i = 0; i < ids.size(); i++) {
                        dlTestThread = new DLTestThread(ids.get(i), this);
                        dlTestThread.start();
                        dlTestThread.join();
                        mBuilder.setProgress(ids.size(), i + 1, false)
                                .setContentText(getString(R.string.downloading_tests))
                                .setSmallIcon(android.R.drawable.stat_sys_download);
                        notification = mBuilder.build();
                        notification.flags = Notification.FLAG_NO_CLEAR;
                        mNotifyMgr.notify(ZNO_UPDATE_NOTIFY, notification);
                    }
                }
                db.updateTests(tests);
                successful = true;
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (successful && notify) {
                    app.setLastUpdate(System.currentTimeMillis());
                    ComponentName mainActivity =
                            new ComponentName(ApiService.this, MainActivity.class);
                    Intent main = Intent.makeMainActivity(mainActivity);
                    main.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent startApp =
                            PendingIntent.getActivity(
                                    ApiService.this,
                                    0,
                                    main,
                                    PendingIntent.FLAG_ONE_SHOT
                            );
                    mBuilder.setContentIntent(startApp);
                    mBuilder.setContentText(getString(R.string.db_updated))
                            .setSmallIcon(android.R.drawable.stat_sys_download_done);
                    mBuilder.setProgress(0, 0, false);
                    notification = mBuilder.build();
                    notification.flags = Notification.FLAG_ONLY_ALERT_ONCE |
                            Notification.FLAG_AUTO_CANCEL;
                    mNotifyMgr.notify(ZNO_UPDATE_NOTIFY, notification);
                } else {
                    mNotifyMgr.cancel(ZNO_UPDATE_NOTIFY);
                }
                ApiService.this.stopSelf();
            }
        }

        @Override
        public void onDownloadImagesStart(int imagesCount) {

        }

        @Override
        public void onImageDownloaded() {

        }

        @Override
        public void onSavingTest() {

        }

        @Override
        public void onTestDownloaded() {

        }

        @Override
        public void onError(Exception e) {
            this.interrupt();
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            this.interrupt();
        }
    }

    class DLTestThread extends Thread {

        int id;
        TestDLCallBack callBack;

        public DLTestThread(int id, TestDLCallBack callBack) {
            this.id = id;
            this.callBack = callBack;
        }

        @Override
        public void run() {

            ErrorListener errorListener = new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    callBack.onError(volleyError);
                }
            };

            try {
                JSONArray questions = getQuestions(id, errorListener);
                JSONArray balls = getBalls(id, errorListener);
                ArrayList<String> imagesUrls = getImagesUrls(questions);
                callBack.onDownloadImagesStart(imagesUrls.size());

                for (String url : imagesUrls) {
                    String path = url.substring(0, url.lastIndexOf('/'));
                    String name = url.substring(url.lastIndexOf('/') + 1);
                    Bitmap image = getImage(url, errorListener);
                    fm.saveBitmap(path, name, image);
                    callBack.onImageDownloaded();
                }

                callBack.onSavingTest();
                db.updateTest(id, questions, balls);
                callBack.onTestDownloaded();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                callBack.onError(e);
                e.printStackTrace();
            } catch (JSONException e) {
                callBack.onError(e);
                e.printStackTrace();
            }
        }

    }

    final IBinder mBinder = new ApiBinder();
    ZNOApplication app;
    ZNODataBaseHelper db;
    FileManager fm;

    @Override
    public void onCreate() {
        app = ZNOApplication.getInstance();
        db = app.getZnoDataBaseHelper();
        fm = new FileManager(getApplicationContext());
        super.onCreate();
    }

    public void downLoadTest(int id, TestDLCallBack callBack) {
        new DLTestThread(id, callBack).start();
    }

    private JSONArray getTests(ErrorListener errorListener) throws ExecutionException,
            InterruptedException, JSONException {
        RequestFuture<JSONObject> testFuture = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(GET_TESTS, null, testFuture, errorListener);
        app.addToRequestQueue(request, REQUEST_TAG);
        return testFuture.get().getJSONArray(Keys.OBJECTS);
    }

    private JSONArray getQuestions(int id, ErrorListener errorListener) throws ExecutionException,
            InterruptedException, JSONException {
        RequestFuture<JSONObject> testFuture = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(GET_TEST + id, null, testFuture, errorListener);
        app.addToRequestQueue(request, REQUEST_TAG);
        return testFuture.get().getJSONArray(Keys.OBJECTS);
    }

    private JSONArray getBalls(int id, ErrorListener errorListener) throws ExecutionException,
            InterruptedException, JSONException {
        RequestFuture<JSONObject> ballsFuture = RequestFuture.newFuture();
        app.addToRequestQueue(new JsonObjectRequest(String.format(GET_TEST_BALLS, id), null, ballsFuture, errorListener), REQUEST_TAG);
        return ballsFuture.get().getJSONArray(Keys.OBJECTS);
    }

    private ArrayList<String> getImagesUrls(JSONArray questions) throws JSONException {
        ArrayList<String> imageUrls = new ArrayList<String>();
        JSONObject question;
        JSONArray images;
        String imgRelPath;
        String name;
        String imageUrl;

        for (int i = 0; i < questions.length(); i++) {
            question = questions.getJSONObject(i);
            images = question.optJSONArray(Keys.IMAGES);

            if (images != null) {
                imgRelPath = question.getString(Keys.IMAGES_RELATIVE_URL);

                for (int j = 0; j < images.length(); j++) {
                    name = images.getJSONObject(j).getString(ApiService.Keys.NAME);
                    imageUrl = imgRelPath + "/" + name;

                    if (!fm.isFileExists(imageUrl)) {
                        imageUrls.add(imageUrl);
                    }
                }

            }
        }

        return imageUrls;
    }

    private void checkForTestsUpdates() {
        new CheckForUpdatesThread().start();
    }

    private Bitmap getImage(String imageUrl, ErrorListener errorListener) throws JSONException,
            ExecutionException, InterruptedException {
        RequestFuture<Bitmap> imageFuture = RequestFuture.newFuture();
        ImageRequest imageRequest = new ImageRequest(SITE_URL + imageUrl, imageFuture, 0, 0, null, errorListener);
        app.addToRequestQueue(imageRequest, REQUEST_TAG);
        return imageFuture.get();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null && action.equals(ACTION_CHECK_FOR_UPDATES)) {
            checkForTestsUpdates();
            return START_REDELIVER_INTENT;
        }
        stopSelf();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ApiBinder extends Binder {
        public ApiService getService() {
            return ApiService.this;
        }
    }

}
