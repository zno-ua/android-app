package com.vojkovladimir.zno;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.vojkovladimir.zno.adapters.TestsListAdapter;
import com.vojkovladimir.zno.api.Api;
import com.vojkovladimir.zno.db.ZNODataBaseHelper;
import com.vojkovladimir.zno.models.TestInfo;

public class LessonTestsActivity extends Activity {

	ZNOApplication app;
	ZNODataBaseHelper db;

	ListView testsListView;
	TestsListAdapter testsListAdapter;
	ArrayList<TestInfo> tests;

	String link;
	String downloadProgressMessage;
	int idLesson;

	final Context context = this;

	OnItemClickListener itemListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			final TestInfo test = tests.get(position);

			if (test.loaded) {
				Intent testActivity = new Intent(getApplicationContext(),
						TestActivity.class);

				testActivity.putExtra(ZNOApplication.ExtrasKeys.TABLE_NAME,
						link + "_" + test.year + "_" + test.id);
				testActivity.putExtra(ZNOApplication.ExtrasKeys.ID_TEST, ""
						+ test.id);
				startActivity(testActivity);
			} else {
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
						context);

				final ProgressDialog downloadProgress = new ProgressDialog(
						context);
				downloadProgress.setMessage(downloadProgressMessage);

				final Listener<JSONObject> listener = new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject responce) {
						try {
							final JSONArray questions = responce
									.getJSONArray(Api.Keys.OBJECTS);
							final String path = responce
									.getString(Api.Keys.IMAGES_PATH);
							final JSONArray images = responce
									.getJSONArray(Api.Keys.IMAGES);
							
							final Runnable invalidateList = new Runnable() {

								@Override
								public void run() {
									invalidate();
								}
							};
							
							final FileManager fm = new FileManager(getApplicationContext());

							Runnable saveTest = new Runnable() {

								@Override
								public void run() {
									for (int i = 0; i < images.length(); i++) {
										try {
											final String name = images.getJSONObject(i).getString(Api.Keys.NAME);
											final Listener<Bitmap> imagesListener = new Listener<Bitmap>() {

												@Override
												public void onResponse(Bitmap image) {
													fm.saveBitmap(path, name, image);
												}
											};
											
											final ErrorListener errorListener = new ErrorListener() {

												@Override
												public void onErrorResponse(VolleyError error) {
													error.printStackTrace();
												}
											};

											app.addToRequestQueue(new ImageRequest(
													Api.SITE_URL + path +"/"+ name,
													imagesListener, 0, 0, null,
													errorListener));
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}

									db.updateTableTest(link, test.year,test.id, questions);
									runOnUiThread(invalidateList);
									downloadProgress.cancel();
								}
							};

							new Thread(saveTest).start();

						} catch (JSONException e) {
							e.printStackTrace();
						}
						downloadProgress.cancel();
					}
				};

				final ErrorListener errorListener = new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						downloadProgress.cancel();
						Toast.makeText(getApplicationContext(),
								error.toString(), Toast.LENGTH_LONG).show();
					}
				};

				OnClickListener onButtonClick = new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							app.addToRequestQueue(Api.getTestRequest(test.id,
									listener, errorListener));
							downloadProgress.show();
							break;
						}
					}
				};

				dialogBuilder.setMessage(R.string.dialog_load_test_text);
				dialogBuilder.setPositiveButton(R.string.dialog_positive_text,
						onButtonClick);
				dialogBuilder.setNegativeButton(R.string.dialog_negative_text,
						null);

				AlertDialog downloadAlert = dialogBuilder.create();

				downloadAlert.show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tests_list);

		app = ZNOApplication.getInstance();
		db = app.getZnoDataBaseHelper();

		Intent intent = getIntent();

		setTitle(intent.getStringExtra(ZNOApplication.ExtrasKeys.LESSON_NAME));
		idLesson = intent.getIntExtra(ZNOApplication.ExtrasKeys.ID_LESSON, -1);
		link = intent.getStringExtra(ZNOApplication.ExtrasKeys.LINK);
		tests = new ArrayList<TestInfo>();
		tests = db.getLessonTests(idLesson);
		testsListAdapter = new TestsListAdapter(this, tests);
		testsListView = (ListView) findViewById(R.id.tests_list_view);
		testsListView.setAdapter(testsListAdapter);
		testsListView.setOnItemClickListener(itemListener);
		downloadProgressMessage = getResources().getString(
				R.string.progress_test_load);
	}

	public void invalidate() {
		tests = db.getLessonTests(idLesson);
		testsListAdapter.setTestsList(tests);
		testsListView.invalidateViews();
	}

}
