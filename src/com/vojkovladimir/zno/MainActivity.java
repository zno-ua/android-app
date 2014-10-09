package com.vojkovladimir.zno;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.fragments.TestTimerFragment;
import com.vojkovladimir.zno.models.Test;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    ZNODataBaseHelper db;
    String[] quotes;
    String quoteTitle;
    TextView quote;
    TextView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quote = (TextView) findViewById(R.id.quote);
        logo = (TextView) findViewById(R.id.app_logo);
        ZNOApplication.buildLogo(logo, getResources(), getAssets());
        quotes = getResources().getStringArray(R.array.quotes_2011);
        quoteTitle = getResources().getString(R.string.quotes_2011_title);
        db = ZNOApplication.getInstance().getZnoDataBaseHelper();
        findViewById(R.id.begin_testing_btn).setOnClickListener(this);
        findViewById(R.id.records_btn).setOnClickListener(this);
        findViewById(R.id.last_passed_tests_btn).setOnClickListener(this);
    }

    protected void onStart() {
        super.onStart();
        refreshQuote();
    }

    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences preferences = getSharedPreferences(ZNOApplication.APP_SETTINGS, Context.MODE_PRIVATE);
        if (preferences.contains(TestActivity.Extra.USER_ANSWERS_ID)) {
            final int userAnswersId = preferences.getInt(TestActivity.Extra.USER_ANSWERS_ID, -1);
            final int questionNumber = preferences.getInt(TestActivity.Extra.QUESTION_NUMBER, -1);
            final int testId = preferences.getInt(Test.TEST_ID, -1);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage(getString(R.string.unfinished_test_alert_text));
            dialogBuilder.setPositiveButton(R.string.dialog_positive_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent startedActivity;
                    startedActivity = new Intent(TestActivity.Action.CONTINUE_PASSAGE_TEST);
                    startedActivity.putExtra(Test.TEST_ID, testId);
                    startedActivity.putExtra(TestActivity.Extra.USER_ANSWERS_ID, userAnswersId);
                    startedActivity.putExtra(TestActivity.Extra.QUESTION_NUMBER, questionNumber);
                    if (preferences.contains(TestTimerFragment.MILLIS_LEFT)) {
                        startedActivity.putExtra(TestTimerFragment.MILLIS_LEFT, preferences.getLong(TestTimerFragment.MILLIS_LEFT, -1));
                    }
                    startActivity(startedActivity);
                }
            });
            dialogBuilder.setNegativeButton(R.string.dialog_negative_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    db.deleteUserAnswers(userAnswersId);
                    SharedPreferences preferences = getSharedPreferences(ZNOApplication.APP_SETTINGS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(Test.TEST_ID);
                    editor.remove(TestActivity.Extra.USER_ANSWERS_ID);
                    editor.remove(TestActivity.Extra.QUESTION_NUMBER);
                    if (preferences.contains(TestTimerFragment.MILLIS_LEFT)) {
                        editor.remove(TestTimerFragment.MILLIS_LEFT);
                    }
                    editor.apply();
                }
            });
            dialogBuilder.create().show();
        }
    }

    public void refreshQuote() {
        String text = quoteTitle + "<br>";
        Random rand = new Random();
        int num = rand.nextInt(quotes.length);
        text += quotes[num];
        quote.setText(Html.fromHtml(text));

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.begin_testing_btn:
                Intent lessons = new Intent(this, LessonsActivity.class);
                startActivity(lessons);
                break;
            case R.id.records_btn:
                Intent records = new Intent(RecordsActivity.Action.VIEW_BEST_SCORES);
                startActivity(records);
                break;
            case R.id.last_passed_tests_btn:
                Intent passedTests = new Intent(RecordsActivity.Action.VIEW_PASSED_TESTS);
                startActivity(passedTests);
                break;
        }
    }
}