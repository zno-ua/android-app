package com.vojkovladimir.zno;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class TestingActivity extends Activity{
	
	public static String LOG_TAG = "MyLogs";

	ListView lessonsList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_testing);
		
		lessonsList = (ListView)findViewById(R.id.lessons_list_view);
	}
}
