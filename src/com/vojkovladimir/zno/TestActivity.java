package com.vojkovladimir.zno;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.fragments.QuestionFragment;
import com.vojkovladimir.zno.models.Test;

public class TestActivity extends Activity {

	public static String LOG_TAG = "MyLogs";

	ZNOApplication app;
	ZNODataBaseHelper db;

	Test test;

	Button next;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		Intent intent = getIntent();

		setTitle(intent.getStringExtra(ZNOApplication.ExtrasKeys.LESSON_NAME)
				+ " " + intent.getIntExtra(ZNOApplication.ExtrasKeys.YEAR, 0));

		app = ZNOApplication.getInstance();
		db = app.getZnoDataBaseHelper();

		String dbName = intent
				.getStringExtra(ZNOApplication.ExtrasKeys.DB_NAME);

		test = db.getTest(dbName);

		next = (Button) findViewById(R.id.test_skip_btn);

		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		transaction.add(R.id.test_question_container, QuestionFragment
				.newIntstance(test.questions.get(3).idQuest, test.tasksNum,
						test.questions.get(3).text));

		transaction.commit();

	}

	public void skip(View v) {

	}

}
