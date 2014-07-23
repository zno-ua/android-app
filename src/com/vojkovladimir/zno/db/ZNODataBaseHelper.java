package com.vojkovladimir.zno.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.ZNOApplication;
import com.vojkovladimir.zno.api.Api;
import com.vojkovladimir.zno.models.Lesson;
import com.vojkovladimir.zno.models.TestInfo;

public class ZNODataBaseHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG = "MyLogs";
	private static final String DATABASE_NAME = "ZNOTests.db";
	private static final int DATABASE_VERSION = 1;

	// Table names
	public static final String TABLE_LESSONS_LIST = "lessons_list";
	public static final String TABLE_TESTS_LIST = "tests_list";
	public static final String TABLE_SQLITE_MASTER = "sqlite_master";

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
	public static final String KEY_TBL_NAME = "tbl_name";
	public static final String KEY_ROOT_PAGE = "rootpage";
	public static final String KEY_SQL = "sql";
	public static final String KEY_LOADED = "loaded";

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
			+ " INTEGER, " + KEY_TASK_ANS + " INTEGER, " + KEY_LOADED
			+ " INTEGER);";

	private void createTableTest(String testName) {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("CREATE TABLE " + testName.replace("-", "_") + " (" + KEY_ID
				+ " INTEGER PRIMARY KEY, " + KEY_ID_QUEST + " INTEGER, "
				+ KEY_TYPE + " INTEGER, " + KEY_TEXT + " TEXT, " + KEY_ANSWERS
				+ " TEXT, " + KEY_CORRECT + " TEXT, " + KEY_BALL + " INTEGER"
				+ ");");
		Log.i(LOG_TAG, testName + " table created!");
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
				byte[] buf = new byte[lessonsListIS.available()];
				lessonsListIS.read(buf);
				lessonsListIS.close();

				JSONArray lessonsList = new JSONArray(new String(buf));

				fillTableLessonsList(db, lessonsList);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			InputStream testsListIS = ZNOApplication.getInstance()
					.getResources().openRawResource(R.raw.tests_list);
			try {
				byte[] buf = new byte[testsListIS.available()];
				testsListIS.read(buf);
				testsListIS.close();

				JSONArray testsList = new JSONArray(new String(buf));

				fillTableTestsList(db, testsList);

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

		Log.i(LOG_TAG, "DB created!");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSONS_LIST);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TESTS_LIST);
		onCreate(db);

		Log.i(LOG_TAG, DATABASE_NAME + " upgraded!");
	}

	// Filling a table lessons list

	private void fillTableLessonsList(SQLiteDatabase db, JSONArray jsonArray) {

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

	// Filling a table tests list

	private void fillTableTestsList(SQLiteDatabase db, JSONArray jsonArray) {
		ContentValues values = new ContentValues();
		JSONObject lesson;

		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				lesson = jsonArray.getJSONObject(i);
				values.put(KEY_ID_LESSON, lesson.getInt(Api.Keys.ID_LESSON));
				values.put(KEY_DB_NAME, lesson.getString(Api.Keys.DB_NAME)
						.replace("-", "_"));
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
				values.put(KEY_LOADED, lesson.getInt(Api.Keys.LOADED));

				Log.i(LOG_TAG,
						lesson.getString(Api.Keys.NAME_LESSON)
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

	// Filling a table test

	private void fillTableTest(SQLiteDatabase db, String testTableName,
			JSONArray jsonArray) {
		ContentValues values = new ContentValues();
		JSONObject lesson;

		Log.i(LOG_TAG, "\tfillTableTest(), " + testTableName.replace("-", "_")
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

				db.insert(testTableName.replace("-", "_"), null, values);
			} catch (JSONException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}
	}

	public void fillTableTest(String testTableName, JSONArray jsonArray) {
		SQLiteDatabase db = getWritableDatabase();
		String tableName = testTableName.replace("-", "_");

		Cursor c = db.query(TABLE_TESTS_LIST, new String[] { KEY_DB_NAME,
				KEY_LOADED }, KEY_DB_NAME + "=?", new String[] { tableName },
				null, null, null);

		if (c.moveToNext()) {
			int dbNameIndex = c.getColumnIndex(KEY_DB_NAME);
			int loadedIndex = c.getColumnIndex(KEY_LOADED);

			Log.i(LOG_TAG, c.getString(dbNameIndex) + " == " + tableName);

			if (c.getInt(loadedIndex) == 0) {
				createTableTest(tableName);
				fillTableTest(db, tableName, jsonArray);
				ContentValues values = new ContentValues();
				values.put(KEY_LOADED, 1);
				db.update(TABLE_TESTS_LIST, values, KEY_DB_NAME + "=?",
						new String[] { tableName });
				Log.i(LOG_TAG, "missing");
			} else {
				Log.i(LOG_TAG, "loaded");
				clearTableTest(db, tableName);
				fillTableTest(db, tableName, jsonArray);
			}
		}

	}

	// Methods for cleaning tables

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

	private void clearTableTest(SQLiteDatabase db, String testTableName) {
		db.execSQL("DROP TABLE IF EXISTS " + testTableName);
		Log.i(LOG_TAG, "Table " + testTableName + " cleard.");
	}

	// Methods of access to information from the database

	public ArrayList<TestInfo> getLessonTestsList(int idLesson) {
		ArrayList<TestInfo> testsList = new ArrayList<TestInfo>();

		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TABLE_TESTS_LIST, new String[] { KEY_DB_NAME,
				KEY_NAME_TEST, KEY_YEAR, KEY_TASKS_NUM, KEY_LOADED },
				KEY_ID_LESSON + "=" + idLesson, null, null, null, KEY_YEAR
						+ " DESC");
		TestInfo testInfo;

		if (c.moveToFirst()) {
			int dbNameIndex = c.getColumnIndex(KEY_DB_NAME);
			int nameTestIndex = c.getColumnIndex(KEY_NAME_TEST);
			int yearIndex = c.getColumnIndex(KEY_YEAR);
			int tastsNumIndex = c.getColumnIndex(KEY_TASKS_NUM);
			int loadedIndex = c.getColumnIndex(KEY_LOADED);
			do {
				testInfo = new TestInfo(c.getString(dbNameIndex),
						c.getString(nameTestIndex), c.getInt(yearIndex),
						c.getInt(tastsNumIndex),
						(c.getInt(loadedIndex) == 0) ? false : true);
				testsList.add(testInfo);
			} while (c.moveToNext());
		}

		return testsList;
	}

	public int getTestsCount(int idLesson) {
		SQLiteDatabase db = getWritableDatabase();
		return (db.query(TABLE_TESTS_LIST, new String[] { KEY_ID },
				KEY_ID_LESSON + " = " + idLesson, null, null, null, null))
				.getCount();
	}

	public ArrayList<Lesson> getLessonsList() {
		ArrayList<Lesson> lessonsList = new ArrayList<Lesson>();

		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db
				.query(TABLE_LESSONS_LIST, new String[] { KEY_ID, KEY_NAME },
						null, null, null, null, null);
		String lessonName;
		int idLesson;
		Lesson lesson;

		if (c.moveToFirst()) {
			int lessonIdIndex = c.getColumnIndex(KEY_ID);
			int lessonNameIndex = c.getColumnIndex(KEY_NAME);

			do {
				lessonName = c.getString(lessonNameIndex);
				idLesson = c.getInt(lessonIdIndex);
				lesson = new Lesson(idLesson, lessonName,
						getTestsCount(idLesson));
				lessonsList.add(lesson);
			} while (c.moveToNext());

		}

		return lessonsList;
	}

}
