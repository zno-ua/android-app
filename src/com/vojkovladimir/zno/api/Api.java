package com.vojkovladimir.zno.api;

import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

public class Api {
	public static String LOG_TAG = "MyLogs";

	public static final String SITE_URL = "http://new.zno-ua.net";
	public static final String API_URL = SITE_URL + "/api/v1/";
	public static final String GET_LESSONS = "lesson/?format=json";
	public static final String GET_TESTS = "test/?format=json";
	public static final String GET_TEST = "question/?format=json&test=";

	public interface Keys {
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
		
	}

	public static JsonObjectRequest getTestsListRequest(
			Listener<JSONObject> listener, ErrorListener errorListener) {
		JsonObjectRequest request = new JsonObjectRequest(API_URL
				+ GET_TESTS, null, listener, errorListener);
		return request;
	}

	public static JsonObjectRequest getTestRequest(int testId,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		JsonObjectRequest request = new JsonObjectRequest(API_URL + GET_TEST
				+ testId, null, listener, errorListener);
		return request;
	}
}
