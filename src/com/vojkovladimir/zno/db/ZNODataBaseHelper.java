package com.vojkovladimir.zno.db;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vojkovladimir.zno.ZNOApplication;
import com.vojkovladimir.zno.api.Api;
import com.vojkovladimir.zno.R;

public class ZNODataBaseHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG = "MyLogs";
	private static final String DATABASE_NAME = "ZNOTests.db";
	private static final int DATABASE_VERSION = 1;

	// Table names
	public static final String TABLE_LESSONS_LIST = "lessons_list";
	public static final String TABLE_TESTS_LIST = "tests_list";

	// Column names
	public static final String KEY_ID = "id";
	public static final String KEY_ID_QUEST = "id_quest";
	public static final String KEY_ID_LESSON = "id_lesson";
	public static final String KEY_LINK = "link";
	public static final String KEY_NAME = "name";
	public static final String KEY_NAME_ROD = "name_rod";
	public static final String KEY_DB_NAME = "db_name";
	public static final String KEY_NAME_LESSON = "name_lesson";
	public static final String KEY_LINK_LESSON = "link_lesson";
	public static final String KEY_NAME_TEST = "name_test";
	public static final String KEY_YEAR = "year";
	public static final String KEY_TIME = "time";
	public static final String KEY_TASK_BLOCKS = "task_blocks";
	public static final String KEY_TASKS_NUM = "tasks_num";
	public static final String KEY_TASK_TEST = "task_test";
	public static final String KEY_TASK_TEXTS = "task_texts";
	public static final String KEY_TASK_VIDPOV = "task_vidpov";
	public static final String KEY_TASK_VARS = "task_vars";
	public static final String KEY_TASK_ANS = "task_ans";
	public static final String KEY_TYPE = "type";
	public static final String KEY_TEXT = "text";
	public static final String KEY_ANSWERS = "answers";
	public static final String KEY_CORRECT = "correct";
	public static final String KEY_BALL = "ball";

	// Table Create Statements
	private static final String CREATE_TABLE_LESSONS_LIST = "CREATE TABLE "
			+ TABLE_LESSONS_LIST + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
			+ KEY_LINK + " TEXT, " + KEY_NAME + " TEXT, " + KEY_NAME_ROD
			+ " TEXT);";
	private static final String CREATE_TABLE_TESTS_LIST = "CREATE TABLE "
			+ TABLE_TESTS_LIST + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
			+ KEY_ID_LESSON + " INTEGER, " + KEY_DB_NAME + " TEXT, "
			+ KEY_NAME_LESSON + " TEXT, " + KEY_LINK_LESSON + " TEXT,"
			+ KEY_NAME_TEST + " TEXT, " + KEY_YEAR + " INTEGER, " + KEY_TIME
			+ " INTEGER, " + KEY_TASK_BLOCKS + " INTEGER, " + KEY_TASKS_NUM
			+ " INTEGER, " + KEY_TASK_TEST + " INTEGER, " + KEY_TASK_TEXTS
			+ " INTEGER, " + KEY_TASK_VIDPOV + " INTEGER, " + KEY_TASK_VARS
			+ " INTEGER, " + KEY_TASK_ANS + " INTEGER);";

	private void createTableTest(String testName) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("CREATE TABLE " + testName + " (" + KEY_ID
				+ " INTEGER PRIMARY KEY, " + KEY_ID_QUEST + " INTEGER, "
				+ KEY_TYPE + " INTEGER, " + KEY_TEXT + " TEXT, " + KEY_ANSWERS
				+ " TEXT, " + KEY_CORRECT + " TEXT, " + KEY_BALL + " INTEGER"
				+ ");");
	}

	public ZNODataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.execSQL(CREATE_TABLE_LESSONS_LIST);
			db.execSQL(CREATE_TABLE_TESTS_LIST);

			InputStream lessonsListIS = ZNOApplication.getInstance()
					.getResources().openRawResource(R.raw.lessons_list);
			try {
				byte [] buf = new byte[lessonsListIS.available()];
				lessonsListIS.read(buf);
				lessonsListIS.close();
				
				JSONArray lessonsList = new JSONArray(new String(buf));
				
				fillTableLessonsList(db,lessonsList);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			InputStream testsListIS = ZNOApplication.getInstance()
					.getResources().openRawResource(R.raw.tests_list);
			try {
				byte [] buf = new byte[lessonsListIS.available()];
				testsListIS.read(buf);
				testsListIS.close();
				
				JSONArray lessonsList = new JSONArray(new String(buf));
				
				fillTableTestsList(db,lessonsList);
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

		} catch (SQLException e) {
			Log.e(LOG_TAG,
					"Error in creating " + DATABASE_NAME + ":\n"
							+ e.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSONS_LIST);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TESTS_LIST);
		onCreate(db);

		Log.i(LOG_TAG, DATABASE_NAME + " upgraded!");
	}
	
	private void fillTableLessonsList(SQLiteDatabase db,JSONArray jsonArray) {
		
		ContentValues values = new ContentValues();
		JSONObject lesson;

		Log.i(LOG_TAG,
				"\tfillTableLessonsList(), Tests count = " + jsonArray.length());

		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				lesson = jsonArray.getJSONObject(i);
				values.put(KEY_LINK, lesson.getString(Api.Keys.LINK));
				values.put(KEY_NAME, lesson.getString(Api.Keys.NAME));
				values.put(KEY_NAME_ROD, lesson.getString(Api.Keys.NAME_ROD));

				Log.i(LOG_TAG,
						lesson.getString(Api.Keys.NAME)
								+ "inserted with status = "
								+ db.insert(TABLE_LESSONS_LIST, null, values));
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage());
			}

		}
	}
	
	public void fillTableLessonsList(JSONArray jsonArray) {
		SQLiteDatabase db = getWritableDatabase();
		
		clearTableLessonsList(db);
		fillTableLessonsList(db, jsonArray);
	}
	
	private void fillTableTestsList(SQLiteDatabase db,JSONArray jsonArray) {
		ContentValues values = new ContentValues();
		JSONObject lesson;

		Log.i(LOG_TAG,
				"\tfillTableTestsList(), Tests count = " + jsonArray.length());

		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				lesson = jsonArray.getJSONObject(i);
				values.put(KEY_ID_LESSON, lesson.getInt(Api.Keys.ID_LESSON));
				values.put(KEY_DB_NAME, lesson.getString(Api.Keys.DB_NAME));
				values.put(KEY_NAME_LESSON,
						lesson.getString(Api.Keys.NAME_LESSON));
				values.put(KEY_LINK_LESSON,
						lesson.getString(Api.Keys.LINK_LESSON));
				values.put(KEY_NAME_TEST, lesson.getString(Api.Keys.NAME_TEST));
				values.put(KEY_YEAR, lesson.getInt(Api.Keys.YEAR));
				values.put(KEY_TIME, lesson.getInt(Api.Keys.TIME));
				values.put(KEY_TASK_BLOCKS, lesson.getInt(Api.Keys.TASK_BLOCKS));
				values.put(KEY_TASKS_NUM, lesson.getInt(Api.Keys.TASKS_NUM));
				values.put(KEY_TASK_TEST, lesson.getInt(Api.Keys.TASK_TEST));
				values.put(KEY_TASK_TEXTS, lesson.getInt(Api.Keys.TASK_TEXTS));
				values.put(KEY_TASK_VIDPOV, lesson.getInt(Api.Keys.TASK_VIDPOV));
				values.put(KEY_TASK_VARS, lesson.getInt(Api.Keys.TASK_VARS));
				values.put(KEY_TASK_ANS, lesson.getInt(Api.Keys.TASK_ANS));

				Log.i(LOG_TAG,
						lesson.getString(Api.Keys.DB_NAME)
								+ "inserted with status = "
								+ db.insert(TABLE_TESTS_LIST, null, values));
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage());
			}

		}
	}
	
	public void fillTableTestsList(JSONArray jsonArray) {
		SQLiteDatabase db = getWritableDatabase();
		
		clearTableTestsList(db);

		fillTableTestsList(db, jsonArray);
	}
	
	private void fillTableTest(SQLiteDatabase db, String testTableName, JSONArray jsonArray) {
		ContentValues values = new ContentValues();
		JSONObject lesson;

		Log.i(LOG_TAG, "\tfillTableTest(), " + testTableName
				+ " Tests count = " + jsonArray.length());

		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				lesson = jsonArray.getJSONObject(i);
				values.put(KEY_ID, lesson.getInt(Api.Keys.ID));
				values.put(KEY_ID_QUEST, lesson.getInt(Api.Keys.ID_QUEST));
				values.put(KEY_TYPE, lesson.getInt(Api.Keys.TYPE));
				values.put(KEY_TEXT, lesson.getString(Api.Keys.TEXT));
				values.put(KEY_ANSWERS, lesson.getString(Api.Keys.ANSWERS));
				values.put(KEY_CORRECT, lesson.getString(Api.Keys.CORRECT));
				values.put(KEY_BALL, lesson.getInt(Api.Keys.BALL));

				db.insert(testTableName, null, values);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
	}

	public void fillTableTest(String testTableName, JSONArray jsonArray) {
		SQLiteDatabase db = getWritableDatabase();
		
		clearTableTest(db,testTableName);
		fillTableTest(db, testTableName, jsonArray);
	}

	private void clearTableLessonsList(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSONS_LIST);
		db.execSQL(CREATE_TABLE_LESSONS_LIST);
		Log.i(LOG_TAG, "Table " + TABLE_LESSONS_LIST + " cleard.");
	}

	private void clearTableTestsList(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TESTS_LIST);
		db.execSQL(CREATE_TABLE_TESTS_LIST);
		Log.i(LOG_TAG, "Table " + TABLE_TESTS_LIST + " cleard.");
	}

	private void clearTableTest(SQLiteDatabase db,String testTableName) {
		db.execSQL("DROP TABLE IF EXISTS " + testTableName);
		Log.i(LOG_TAG, "Table " + testTableName + " cleard.");
	}

}
