package net.zno_ua.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import net.zno_ua.app.db.ZNODataBaseHelper;

import java.util.Calendar;
import java.util.Random;

public class MainActivityOld extends Activity implements View.OnClickListener {

    ZNOApplication app;
    ZNODataBaseHelper db;
    String[] quotes;
    TextView quote;
    TextView logo;
    Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_old);

        app = ZNOApplication.getInstance();
        db = app.getZnoDataBaseHelper();
        random = new Random(System.currentTimeMillis());

        logo = (TextView) findViewById(R.id.app_logo);
        quote = (TextView) findViewById(R.id.quote);

        quotes = getResources().getStringArray(R.array.quotes);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        if (calendar.get(Calendar.MONTH) >= 7) {
            year++;
        }

        String zno = getString(R.string.zno);
        String logoText = zno + " " + String.valueOf(year);

        int yearColor = getResources().getColor(R.color.blue_light);
        int appNameColor = getResources().getColor(R.color.quote_color);

        ForegroundColorSpan yearColorSpan = new ForegroundColorSpan(yearColor);
        ForegroundColorSpan appNameColorSpan = new ForegroundColorSpan(appNameColor);

        SpannableString spannableString = new SpannableString(logoText);
        spannableString.setSpan(yearColorSpan, 0, zno.length(), 0);
        spannableString.setSpan(appNameColorSpan, zno.length() + 1, logoText.length(), 0);

        Typeface ptSansBold = Typeface.createFromAsset(getAssets(), "fonts/PT_Sans-Web-Bold.ttf");

        logo.setText(spannableString);
        logo.setTypeface(ptSansBold);

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
            dialogBuilder.setMessage(getString(R.string.dialog_unfinished_test_text));
            dialogBuilder.setPositiveButton(R.string.dialog_positive_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    app.startSavedSession(MainActivityOld.this);
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
        quote.setText(Html.fromHtml(quotes[random.nextInt(quotes.length)]));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.begin_testing_btn:
                Intent lessons = new Intent(this, LessonsActivity.class);
                startActivity(lessons);
                break;
            case R.id.records_btn:
                Intent records = new Intent(this, RecordsActivity.class);
                records.setAction(RecordsActivity.Action.VIEW_BEST_SCORES);
                startActivity(records);
                break;
            case R.id.last_passed_tests_btn:
                Intent passedTests = new Intent(this, RecordsActivity.class);
                passedTests.setAction(RecordsActivity.Action.VIEW_PASSED_TESTS);
                startActivity(passedTests);
                break;
        }
    }
}