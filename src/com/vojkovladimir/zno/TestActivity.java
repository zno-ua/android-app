package com.vojkovladimir.zno;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.models.Test;

public class TestActivity extends Activity {

	public static String LOG_TAG = "MyLogs";

	ZNOApplication app;
	ZNODataBaseHelper db;

	Test test;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		setTitle(intent.getStringExtra(ZNOApplication.ExtrasKeys.LESSON_NAME)
				+ " " + intent.getIntExtra(ZNOApplication.ExtrasKeys.YEAR, 0));

		app =ZNOApplication.getInstance();
		db = app.getZnoDataBaseHelper();
		
		String dbName = intent
				.getStringExtra(ZNOApplication.ExtrasKeys.DB_NAME);

		test = db.getTest(dbName);

	}

}
