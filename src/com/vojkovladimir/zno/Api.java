package com.vojkovladimir.zno;

import org.json.JSONArray;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class Api {
	private static String LOG_TAG = "Api";

	private static String API_URL = "http://zno-ua.net/api/";
	private static String LESSONS_LIST = "lessons.get/";
	private static String TESTS_LIST = "tests.get/";
	private static String GET_TEST = "test.get/";

	private RequestQueue queue;
	private JSONArray jsonRequestObject;

	public Api(Context context) {
		queue = Volley.newRequestQueue(context);
	}


}
