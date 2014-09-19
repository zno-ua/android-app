package com.vojkovladimir.zno;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

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
        findViewById(R.id.begin_testing_btn).setOnClickListener(this);
    }

    protected void onStart() {
        super.onStart();
        refreshQuote();
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
                Intent testingActivity = new Intent(this, TestingActivity.class);
                startActivity(testingActivity);
                break;
        }
    }
}