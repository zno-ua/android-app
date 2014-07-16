package com.vojkovladimir.zno.api;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.vojkovladimir.zno.ZNOApplication;

public class Api {
	public static String LOG_TAG = "MyLogs";

	public static final String API_URL = "http://zno-ua.net/api/";
	public static final String GET_LESSONS_LIST = "lessons.get/";
	public static final String GET_TESTS_LIST = "tests.get/";
	public static final String GET_TEST = "test.get/";

	public static final String RESPONSE = "response";
	public static final String INFO = "info";
	
	public interface Keys{
		String ID = "id";
		String ID_QUEST = "id-quest";
		String ID_LESSON = "id-lesson";
		String LINK = "link";
		String NAME = "name";
		String NAME_ROD = "name_rod";
		String DB_NAME = "db-name";
		String NAME_LESSON = "name-lesson";
		String LINK_LESSON = "link-lesson";
		String NAME_TEST = "name-test";
		String YEAR = "year";
		String TIME = "time";
		String TASK_BLOCKS = "task-blocks";
		String TASKS_NUM = "tasks-num";
		String TASK_TEST = "task-test";
		String TASK_TEXTS = "task-texts";
		String TASK_VIDPOV = "task-vidpov";
		String TASK_VARS = "task-vars";
		String TASK_ANS = "task-ans";
		String TYPE = "type";
		String TEXT = "text";
		String ANSWERS = "answers";
		String CORRECT = "correct";
		String BALL = "ball";
		String LOADED = "loaded";
	}

	
	public static JsonObjectRequest getTestsListRequest() {
		JsonObjectRequest request = new JsonObjectRequest(API_URL
				+ GET_TESTS_LIST, null, new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject json) {
				Log.i(LOG_TAG, "Good Response. Tests successfully loaded.");
				ZNOApplication.getInstance().onTestsListRespose(json);
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(LOG_TAG, "Bad response. " + error.getMessage());
				Toast.makeText(
						ZNOApplication.getInstance().getApplicationContext(),
						"Bad response.", Toast.LENGTH_SHORT).show();
			}
		});
		return request;
	}
	
	
	public static JsonObjectRequest getTestRequest(String testName) {
		JsonObjectRequest request = new JsonObjectRequest(API_URL
				+ GET_TEST + testName, null, new Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject json) {
				String testTableName;
				try {
					testTableName = json.getJSONObject(Api.INFO).getString(Api.Keys.DB_NAME);
					Log.i(LOG_TAG, "Good Response. Test "+testTableName+" successfully loaded.");
					ZNOApplication.getInstance().onTestRespose(testTableName,json);
				} catch (JSONException e) {
					Log.e(LOG_TAG, "Error in json: " + e.getMessage());
				}
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(LOG_TAG, "Bad response. " + error.getMessage());
				Toast.makeText(
						ZNOApplication.getInstance().getApplicationContext(),
						"Bad response.", Toast.LENGTH_SHORT).show();
			}
		});
		return request;
	}
}
