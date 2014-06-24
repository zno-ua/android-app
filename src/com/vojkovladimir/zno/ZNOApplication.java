package com.vojkovladimir.zno;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.vojkovladimir.zno.api.Api;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;

public class ZNOApplication extends Application {

	public static String LOG_TAG = "MyLogs";
	public static final String TAG = ZNOApplication.class.getSimpleName();

	private static ZNOApplication mInstance;
	private RequestQueue mRequestQueue;
	private ZNODataBaseHelper znoDBHelper;
	
	public interface ExtrasKeys{
		String TABLE_NAME = "table_name";
		String ID_LESSON = "id-lesson";
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		Log.d(LOG_TAG, "App Instance created.");
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

	public void onTestsListRespose(JSONObject responseObject) {
		try {
			Log.i(LOG_TAG, "ZNOApplication received TestListRespose");
			getZnoDataBaseHelper().fillTableTestsList(
					responseObject.getJSONArray(Api.RESPONSE));
		} catch (JSONException e) {
			Log.e(LOG_TAG,
					"Can't get TestListResposne from JSON:\n"
							+ e.getMessage());
		}
	}

	public void onTestRespose(String testTableName, JSONObject responseObject) {
		try {
			Log.i(LOG_TAG,
					"ZNOApplication received Test "
							+ testTableName);
			getZnoDataBaseHelper().fillTableTest(testTableName, responseObject.getJSONArray(Api.RESPONSE));
		} catch (JSONException e) {
			Log.e(LOG_TAG,
					"Can't get TestResposne from JSON:\n"
							+ e.getMessage());
		}
	}

}
