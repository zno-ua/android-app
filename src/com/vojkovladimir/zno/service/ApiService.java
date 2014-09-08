package com.vojkovladimir.zno.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.vojkovladimir.zno.FileManager;
import com.vojkovladimir.zno.ZNOApplication;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;

public class ApiService extends Service {

	public static String LOG_TAG = "MyLogs";
	public static final String DOWNLOAD_TEST_ACTION = "api.download.test";

	private static final String SITE_URL = "http://new.zno-ua.net";
	private static final String API_URL = SITE_URL + "/api/v1/";
//	private static final String GET_TESTS = "test/?format=json";
	private static final String GET_TEST = "question/?format=json&test=";
	
	public static interface Keys {
		String OBJECTS = "objects";
		String IMAGES = "images";
		String META = "meta";
		String ID = "id";
		String ID_ON_TEST = "id_on_test";
		String LESSON_ID = "lesson_id";
		String LINK = "link";
		String NAME = "name";
		String TASK_ALL = "task_all";
		String TASK_MATCHES = "task_matches";
		String TASK_OPEN_ANSWER = "task_open_answer";
		String TASK_TEST = "task_test";
		String TASK_VARS = "task_vars";
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
		String STATUS = "status";
	}
	
	public interface OnTestLoadedListener {
		void onTestLoad();
		void onError();
	}
	
	private final IBinder mBinder = new ApiBinder();
	
	private ZNOApplication app;
	private ZNODataBaseHelper db;
	private FileManager fm;

	@Override
	public void onCreate() {
		app = ZNOApplication.getInstance();
		db = app.getZnoDataBaseHelper();
		fm = new FileManager(getApplicationContext());
		Log.i(LOG_TAG, "ApiService: started");
		super.onCreate();
	}
	
	public void downLoadTest(final OnTestLoadedListener onTestLoadedListener,final String link, final int year, final int id) {
		ErrorListener errorListener = new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				onTestLoadedListener.onError();
			}
		};
		
		Listener<JSONObject> responceListener = new Listener<JSONObject>() {

			@Override
			public void onResponse(final JSONObject responce) {
				try {
					final JSONArray questions = responce.getJSONArray(ApiService.Keys.OBJECTS);
					
					for (int i = 0; i < questions.length(); i++) {
						final JSONArray images = questions.getJSONObject(i).optJSONArray(ApiService.Keys.IMAGES);
						if (images != null) {
							final String path = questions.getJSONObject(i).getString(ApiService.Keys.IMAGES_RELATIVE_URL);
							
							for (int j = 0; j < images.length(); j++) {
								final String name = images.getJSONObject(j).getString(ApiService.Keys.NAME);
								
								final Listener<Bitmap> imageListener = new Listener<Bitmap>() {

									@Override
									public void onResponse(Bitmap image) {
										fm.saveBitmap(path, name, image);
									}
								};
								
								ImageRequest imageRequest = new ImageRequest(ApiService.SITE_URL + path +"/"+ name,
										imageListener, 0, 0, null, null);
								app.addToRequestQueue(imageRequest);
							}
						}
					}
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							db.updateTableTest(link, year, id, questions);
							onTestLoadedListener.onTestLoad();
						}
					}).start();
				} catch (JSONException e) {
					onTestLoadedListener.onError();
				}
			}
		};
		
		JsonObjectRequest request = new JsonObjectRequest(API_URL + GET_TEST + id, null, responceListener, errorListener);
		app.addToRequestQueue(request);
	}

	@Override
	public void onDestroy() {
		Log.i(LOG_TAG, "ApiService: stoped");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(LOG_TAG, "ApiService: bind");
		return mBinder;
	}
	
	@Override
	public void onRebind(Intent intent) {
		Log.i(LOG_TAG, "ApiService: rebind");
		super.onRebind(intent);
	}
	
	public class ApiBinder extends Binder {
        public ApiService getService() {
            return ApiService.this;
        }
    }
}
