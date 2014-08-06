package com.vojkovladimir.zno;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;

public class ZNOApplication extends Application {

	public static String LOG_TAG = "MyLogs";
	public static final String TAG = ZNOApplication.class.getSimpleName();

	private static ZNOApplication mInstance;
	private RequestQueue mRequestQueue;
	private ZNODataBaseHelper znoDBHelper;

	public interface ExtrasKeys {
		String LESSON_NAME = "lesson_name";
		String LINK = "link";
		String ID_LESSON = "id_lesson";
		String ID_TEST = "id_test";
		String TABLE_NAME= "table_name";
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		Log.v(LOG_TAG, "App Instance created.");
	}

	public static synchronized ZNOApplication getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public ZNODataBaseHelper getZnoDataBaseHelper() {
		if (znoDBHelper == null) {
			znoDBHelper = new ZNODataBaseHelper(getApplicationContext());
		}

		return znoDBHelper;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}

}
