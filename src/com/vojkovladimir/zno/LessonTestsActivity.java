package com.vojkovladimir.zno;

import com.vojkovladimir.zno.db.ZNODataBaseHelper;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class LessonTestsActivity extends ListActivity {

	public static String LOG_TAG = "MyLogs";

	ArrayAdapter<String> testsListAdapter;

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			Log.d(LOG_TAG, "You select test #: " + position);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		String testaTableName = intent
				.getStringExtra(ZNOApplication.ExtrasKeys.TABLE_NAME);
		int idLesson = intent.getIntExtra(ZNOApplication.ExtrasKeys.ID_LESSON,
				-1);
		
		setTitle(testaTableName);

		SQLiteDatabase db = ZNOApplication.getInstance().getZnoDataBaseHelper()
				.getReadableDatabase();
		
		testsListAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		setListAdapter(testsListAdapter);
		getListView().setOnItemClickListener(itemListener);


		String[] columns = { ZNODataBaseHelper.KEY_NAME_TEST };

		Cursor c = db.query(ZNODataBaseHelper.TABLE_TESTS_LIST, columns,
				ZNODataBaseHelper.KEY_ID_LESSON + "=" + idLesson, null, null,
				null, null);

		if (c.moveToFirst()) {
			int nameTestIndex = c
					.getColumnIndex(ZNODataBaseHelper.KEY_NAME_TEST);
			do {
				testsListAdapter.add(c.getString(nameTestIndex));
			} while (c.moveToNext());
		}
	}
}
