package com.vojkovladimir.zno;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		String lessonName = intent.getStringExtra(ZNOApplication.ExtrasKeys.LESSON_NAME);
		int year = intent.getIntExtra(ZNOApplication.ExtrasKeys.YEAR,0);
		setTitle(lessonName+" "+year);
	}

}
