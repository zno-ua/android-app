package com.vojkovladimir.zno;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class SplashActivity extends Activity {

	private static int SPLASH_TIME_OUT = 3000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		TextView logo;
		logo = (TextView) findViewById(R.id.app_logo);
		ZNOApplication.buildLogo(logo, getResources(), getAssets());

//		new Handler().postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				Intent i = new Intent(SplashActivity.this, MainActivity.class);
//				startActivity(i);
//				finish();
//			}
//		}, SPLASH_TIME_OUT);
        Intent i = new Intent(TestActivity.Action.VIEW_TEST);
        i.putExtra(TestActivity.Extra.TEST_ID,32);
        i.putExtra(TestActivity.Extra.USER_ANSWERS_ID,4);
        startActivity(i);
        finish();
    }

}
