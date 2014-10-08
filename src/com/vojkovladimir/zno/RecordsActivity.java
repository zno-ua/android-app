package com.vojkovladimir.zno;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.ListView;

import com.vojkovladimir.zno.adapters.RecordsAdapter;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;

public class RecordsActivity extends Activity {

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

        adapter = new RecordsAdapter(this, db.getRecords());
        list.setAdapter(adapter);
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
