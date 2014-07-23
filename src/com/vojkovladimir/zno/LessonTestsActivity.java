package com.vojkovladimir.zno;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.vojkovladimir.zno.models.TestInfo;

public class LessonTestsActivity extends Activity {

	public static String LOG_TAG = "MyLogs";
	final int DIALOG_TEST_LOAD = 1;

	ListView testsListView;
	TestsListAdapter testsListAdapter;
	ArrayList<TestInfo> testsList;

	OnClickListener dialogListener = new OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case Dialog.BUTTON_POSITIVE:
				break;
			case Dialog.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			if(!testsList.get(position).loaded){
				onDialogCreate(DIALOG_TEST_LOAD).show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tests_list);

		Intent intent = getIntent();
		String testTableName = intent
				.getStringExtra(ZNOApplication.ExtrasKeys.TABLE_NAME);
		int idLesson = intent.getIntExtra(ZNOApplication.ExtrasKeys.ID_LESSON,
				-1);
		setTitle(testTableName);
		
		testsList = ZNOApplication.getInstance().getZnoDataBaseHelper().getLessonTestsList(idLesson);
		testsListAdapter = new TestsListAdapter(this, testsList);
		testsListView = (ListView) findViewById(R.id.tests_list_view);
		testsListView.setAdapter(testsListAdapter);
		testsListView.setOnItemClickListener(itemListener);
	}

	private Dialog onDialogCreate(int id) {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		if (id == DIALOG_TEST_LOAD) {
			dialogBuilder.setMessage(R.string.dialog_load_test_text);
			dialogBuilder.setPositiveButton(R.string.dialog_positive_text,
					dialogListener);
			dialogBuilder.setNegativeButton(R.string.dialog_negative_text,
					dialogListener);
		}
		return dialogBuilder.create();
	}

}
