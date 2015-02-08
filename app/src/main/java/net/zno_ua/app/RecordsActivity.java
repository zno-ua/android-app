package net.zno_ua.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import net.zno_ua.app.adapters.RecordsAdapter;
import net.zno_ua.app.db.ZNODataBaseHelper;
import net.zno_ua.app.models.PassedTest;
import net.zno_ua.app.models.Test;

public class RecordsActivity extends Activity {

    public interface Action {
        String VIEW_PASSED_TESTS = "net.zno_ua.app.VIEW_PASSED_TESTS";
        String VIEW_BEST_SCORES = "net.zno_ua.app.VIEW_BEST_SCORES";
    }

    ZNOApplication app;
    ZNODataBaseHelper db;

    RecordsAdapter adapter;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        list = (ListView) findViewById(R.id.records_list_view);
        app = ZNOApplication.getInstance();
        db = app.getZnoDataBaseHelper();

        String action = getIntent().getAction();

        if (action.equals(Action.VIEW_BEST_SCORES)) {
            adapter = new RecordsAdapter(this, db.getRecords());
            list.setAdapter(adapter);
            list.setSelector(android.R.color.transparent);
            setTitle(R.string.records);
        } else if (action.equals(Action.VIEW_PASSED_TESTS)) {
            setTitle(R.string.last_passed_tests);
            adapter = new RecordsAdapter(this, db.getPassedTests());
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    PassedTest passedTest = (PassedTest) adapter.getItem(i);
                    Intent viewTest = new Intent(RecordsActivity.this, TestActivity.class);
                    viewTest.setAction(TestActivity.Action.VIEW_TEST);
                    viewTest.putExtra(Test.TEST_ID, passedTest.testId);
                    viewTest.putExtra(TestActivity.Extra.USER_ANSWERS_ID, passedTest.id);
                    startActivity(viewTest);
                }
            });
        }

        if (adapter != null && adapter.getCount() == 0) {
            list.setVisibility(View.GONE);
            TextView notice = (TextView) findViewById(R.id.empty_list_notice);
            notice.setText(getString(R.string.empty_list));
            notice.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
