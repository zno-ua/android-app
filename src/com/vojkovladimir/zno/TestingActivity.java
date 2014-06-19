package com.vojkovladimir.zno;

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

import com.vojkovladimir.zno.db.ZNODataBaseHelper;

public class TestingActivity extends ListActivity {

	public static String LOG_TAG = "MyLogs";

	ArrayAdapter<String> lessonsListAdapter;
	int[] lessonsIds;

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			try {
				Log.d(LOG_TAG, "You select lesson with id: "
						+ lessonsIds[position]);

				Intent testsList = new Intent(getApplicationContext(),
						LessonTestsActivity.class);
				testsList.putExtra(ZNOApplication.ExtrasKeys.ID_LESSON,
						lessonsIds[position]);
				testsList.putExtra(ZNOApplication.ExtrasKeys.TABLE_NAME,
						lessonsListAdapter.getItem(position));

				startActivity(testsList);
			} catch (ArrayIndexOutOfBoundsException e) {
				Log.e(LOG_TAG, "You try to select lesson #" + position
						+ " from list.");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		lessonsListAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);

		setListAdapter(lessonsListAdapter);
		getListView().setOnItemClickListener(itemListener);

		SQLiteDatabase db = ZNOApplication.getInstance().getZnoDataBaseHelper()
				.getReadableDatabase();

		Cursor c = db
				.query("lessons_list  inner join tests_list on lessons_list.id = tests_list.id_lesson",
						new String[] { "lessons_list.id", "name" }, null, null,
						"lessons_list.id", null, null);

		lessonsIds = new int[c.getCount()];

		if (c.moveToFirst()) {
			int lessonIdIndex = c.getColumnIndex(ZNODataBaseHelper.KEY_ID);
			int lessonNameIndex = c.getColumnIndex(ZNODataBaseHelper.KEY_NAME);

			for (int i = 0; i < lessonsIds.length; i++, c.moveToNext()) {
				lessonsListAdapter.add(c.getString(lessonNameIndex));
				lessonsIds[i] = c.getInt(lessonIdIndex);
			}
		}
	}
}
