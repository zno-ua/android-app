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
import com.vojkovladimir.zno.service.ApiService;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    ZNOApplication app;
    ZNODataBaseHelper db;
    String[] quotes;
    TextView quote;
    TextView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        quote = (TextView) findViewById(R.id.quote);
        logo = (TextView) findViewById(R.id.app_logo);
        ZNOApplication.buildLogo(logo, getResources(), getAssets());
        quotes = getResources().getStringArray(R.array.quotes);
        app = ZNOApplication.getInstance();
        db = app.getZnoDataBaseHelper();
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
        if (app.hasSavedSession()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage(getString(R.string.unfinished_test_alert_text));
            dialogBuilder.setPositiveButton(R.string.dialog_positive_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    app.startSavedSession(MainActivity.this);
                }
            });
            dialogBuilder.setNegativeButton(R.string.dialog_negative_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    int userAnswersId = app.getSavedSessionUserAnswersId();
                    db.deleteUserAnswers(userAnswersId);
                    app.removeSavedSession();
                }
            });
            dialogBuilder.create().show();
        }
    }

    public void refreshQuote() {
        quote.setText(Html.fromHtml(quotes[new Random().nextInt(quotes.length)]));
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