package com.vojkovladimir.zno;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.vojkovladimir.zno.models.Test;

public class SplashActivity extends Activity {

    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView logo;
        logo = (TextView) findViewById(R.id.app_logo);
        ZNOApplication.buildLogo(logo, getResources(), getAssets());

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent startedActivity;
                SharedPreferences preferences = getSharedPreferences(ZNOApplication.APP_SETTINGS, Context.MODE_PRIVATE);

                if (preferences.contains(TestActivity.Extra.USER_ANSWERS_ID)) {
                    startedActivity = new Intent(TestActivity.Action.CONTINUE_PASSAGE_TEST);
                    startedActivity.putExtra(Test.TEST_ID, preferences.getInt(Test.TEST_ID, -1));
                    startedActivity.putExtra(TestActivity.Extra.USER_ANSWERS_ID, preferences.getInt(TestActivity.Extra.USER_ANSWERS_ID, -1));
                    startedActivity.putExtra(TestActivity.Extra.QUESTION_NUMBER, preferences.getInt(TestActivity.Extra.QUESTION_NUMBER, -1));
                    startActivity(startedActivity);
                } else {
                    startedActivity = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(startedActivity);
                }

                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}
