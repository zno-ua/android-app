package com.vojkovladimir.zno.service;

import java.util.ArrayList;

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

	private static final String SITE_URL = "http://new.zno-ua.net";
	private static final String API_URL = SITE_URL + "/api/v1/";
//	private static final String GET_TESTS = "test/?format=json";
	private static final String GET_TEST = "question/?format=json&test=";
	
	public static final String DOWNLOAD_TEST_ACTION = "api.download.test";
	public static final String LOG_TAG = "MyLogs";
	public static final String REQUEST_TAG = "api_request";

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
		String PARENT_QUESTION = "parent_question";
	}
	
	public interface TestDownloadingFeedBack {
		void onTestLoaded();
		void onError(Exception e);
		void onExtraDownloadingStart(int max);
		void onExtraDownloadingProgressInc();
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
	
	public void downLoadTest(final TestDownloadingFeedBack feedBack,final String link, final int year, final int id) {
		final ErrorListener errorListener = new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.i(LOG_TAG, error.toString());
				app.getRequestQueue().cancelAll(REQUEST_TAG);
				feedBack.onError(error);
			}
		};
		
		Listener<JSONObject> responceListener = new Listener<JSONObject>() {

			@Override
			public void onResponse(final JSONObject responce) {
				try {
					Log.i(LOG_TAG, link+"_"+year+"_"+id+" downloading start");
					final JSONArray questions = responce.getJSONArray(Keys.OBJECTS);
					JSONObject question = null;
					JSONArray images = null;
					ArrayList<String> imageUrls = new ArrayList<String>();
					for (int i = 0; i < questions.length(); i++) {
						question = questions.getJSONObject(i);
						images = question.optJSONArray(Keys.IMAGES);
						if (images != null) {
							final String path = question.getString(Keys.IMAGES_RELATIVE_URL);
							for (int j = 0; j < images.length(); j++) {
								final String name = images.getJSONObject(j).getString(ApiService.Keys.NAME);
								imageUrls.add(path +"/"+ name);
							}
						}
					}
					Log.i(LOG_TAG, imageUrls.size()+" images count");
					
					final Thread onTestLoaded = new Thread(new Runnable() {
							
							@Override
							public void run() {
								db.updateTableTest(link, year, id, questions);
								feedBack.onTestLoaded();
							}
						});
					
					if (imageUrls.size() != 0) {
						final RequestsCounter counter = new RequestsCounter(imageUrls.size(),onTestLoaded, feedBack);
						
						for (final String url:imageUrls) {
							ErrorListener errorListener = new ErrorListener() {

								@Override
								public void onErrorResponse(VolleyError error) {
									Log.i(LOG_TAG, error.toString());
									app.getRequestQueue().cancelAll(REQUEST_TAG);
									feedBack.onError(error);
								}
							};
							Listener<Bitmap> imageListener = new Listener<Bitmap>() {
								
								@Override
								public void onResponse(Bitmap image) {
									String path = url.substring(0, url.lastIndexOf('/'));
									String name = url.substring(url.lastIndexOf('/')+1);
									boolean bitmapSaveStatus = fm.saveBitmap(path, name, image);
									Log.i(LOG_TAG, path+"/"+name+" saved status: "+bitmapSaveStatus);
									counter.requestFinished();
								}
							};
							ImageRequest imageRequest = new ImageRequest(ApiService.SITE_URL + url, imageListener, 0, 0, null, errorListener);
							app.addToRequestQueue(imageRequest,REQUEST_TAG);
						}
					} else {
						onTestLoaded.start();
					}
					
				} catch (JSONException e) {
					feedBack.onError(e);
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
	
	public class ApiBinder extends Binder {
        public ApiService getService() {
            return ApiService.this;
        }
    }
	
	private class RequestsCounter {
		
		int pendingRequests = 0;
		TestDownloadingFeedBack feedBack;
		Thread onTestLoaded;
		
		public RequestsCounter(int pendingRequests,Thread onTestLoaded,TestDownloadingFeedBack feedBack) {
			this.feedBack = feedBack;
			this.onTestLoaded = onTestLoaded;
			this.pendingRequests = pendingRequests;
			feedBack.onExtraDownloadingStart(pendingRequests);
		}
		
		public void requestFinished() {
			pendingRequests--;
			feedBack.onExtraDownloadingProgressInc();
			if (pendingRequests == 0) {
				onTestLoaded.start();
			}
		}
		
	}
}
