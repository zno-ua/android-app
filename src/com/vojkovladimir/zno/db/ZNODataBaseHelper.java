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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.ZNOApplication;
import com.vojkovladimir.zno.api.Api;
import com.vojkovladimir.zno.models.Lesson;
import com.vojkovladimir.zno.models.Question;
import com.vojkovladimir.zno.models.Test;
import com.vojkovladimir.zno.models.TestInfo;

public class ZNODataBaseHelper extends SQLiteOpenHelper {

	private static String LOG_TAG = "ZNODataBase";

	private static final String DATABASE_NAME = "ZNOTests.db";
	private static final int DATABASE_VERSION = 1;

	public static final String TABLE_LESSONS = "lessons";
	public static final String TABLE_TESTS = "tests";

	public static final String KEY_ID = "id";
	public static final String KEY_LESSON_ID = "lesson_id";
	public static final String KEY_LINK = "link";
	public static final String KEY_NAME = "name";
	public static final String KEY_TASK_ALL = "task_all";
	public static final String KEY_TASK_MATCHES = "task_matches";
	public static final String KEY_TASK_OPEN_ANSWER = "task_open_answer";
	public static final String KEY_TASK_TEST = "task_test";
	public static final String KEY_TASK_VARS = "task_vars";
	public static final String KEY_TIME = "time";
	public static final String KEY_YEAR = "year";
	public static final String KEY_LOADED = "loaded";
	public static final String KEY_ANSWERS = "answers";
	public static final String KEY_BALLS = "balls";
	public static final String KEY_CORRECT_ANSWER = "correct_answer";
	public static final String KEY_ID_TEST_QUESTION = "id_test_question";
	public static final String KEY_QUESTION = "question";
	public static final String KEY_TYPE_QUESTION = "type_question";

	private static final String CREATE_TABLE_LESSONS = "CREATE TABLE "
			+ TABLE_LESSONS + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
			+ KEY_LINK + " TEXT, " + KEY_NAME + " TEXT);";

	private static final String CREATE_TABLE_TESTS = "CREATE TABLE "
			+ TABLE_TESTS + " (" + KEY_ID + " INTEGER PRIMARY KEY, "
			+ KEY_LESSON_ID + " INTEGER, " + KEY_NAME + " TEXT, "
			+ KEY_TASK_ALL + " INTEGER, " + KEY_TASK_MATCHES + " INTEGER, "
			+ KEY_TASK_OPEN_ANSWER + " INTEGER, " + KEY_TASK_TEST
			+ " INTEGER, " + KEY_TASK_VARS + " INTEGER, " + KEY_TIME
			+ " INTEGER, " + KEY_YEAR + " INTEGER, " + KEY_LOADED
			+ " INTEGER);";

	private static final String createTableTest(String tableName) {
		return "CREATE TABLE " + tableName + " (" + KEY_ID
				+ " INTEGER PRIMARY KEY, " + KEY_ANSWERS + " TEXT, "
				+ KEY_BALLS + " INTEGER, " + KEY_CORRECT_ANSWER + " STRING, "
				+ KEY_ID_TEST_QUESTION + " INTEGER, " + KEY_QUESTION
				+ " TEXT, "+ KEY_TYPE_QUESTION
				+ " INTEGER" + ");";

	}

	public ZNODataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_LESSONS);
		db.execSQL(CREATE_TABLE_TESTS);

		try {
			JSONArray lessons = loadFromResources(R.raw.lessons);
			fillInTableLessons(db, lessons);
		} catch (IOException e) {
			Log.e(LOG_TAG, e.toString());
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.toString());
		}

		try {
			JSONArray tests = loadFromResources(R.raw.tests);
			fillInTableTests(db, tests);
		} catch (IOException e) {
			Log.e(LOG_TAG, e.toString());
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.toString());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSONS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TESTS);
		onCreate(db);
	}

	private void fillInTableLessons(SQLiteDatabase db, JSONArray lessons) {
		ContentValues values = new ContentValues();

		JSONObject lesson;
		int id;
		String link;
		String name;

		long status;

		for (int i = 0; i < lessons.length(); i++) {
			try {
				lesson = lessons.getJSONObject(i);

				id = lesson.getInt(Api.Keys.ID);
				link = lesson.getString(Api.Keys.LINK).replace("-", "_");
				name = lesson.getString(Api.Keys.NAME);

				values.clear();
				values.put(KEY_ID, id);
				values.put(KEY_LINK, link);
				values.put(KEY_NAME, name);

				status = db.insert(TABLE_LESSONS, null, values);

				if (status == -1) {
					Log.e(LOG_TAG, "Error while inserting lesson! Lesson id: "
							+ id + ".");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void fillInTableTests(SQLiteDatabase db, JSONArray tests) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TESTS);
		db.execSQL(CREATE_TABLE_TESTS);
		ContentValues values = new ContentValues();

		JSONObject test;
		int id;
		int lessonId;
		String name;
		int taskAll;
		int taskMatches;
		int taskOpenAnswers;
		int taskTest;
		int taskVars;
		int time;
		int year;

		long status;

		for (int i = 0; i < tests.length(); i++) {
			try {
				test = tests.getJSONObject(i);

				id = test.getInt(Api.Keys.ID);
				lessonId = test.getInt(Api.Keys.LESSON_ID);
				;
				name = test.getString(Api.Keys.NAME);
				taskAll = test.getInt(Api.Keys.TASK_ALL);
				taskMatches = test.getInt(Api.Keys.TASK_MATCHES);
				taskOpenAnswers = test.getInt(Api.Keys.TASK_OPEN_ANSWER);
				taskTest = test.getInt(Api.Keys.TASK_TEST);
				taskVars = test.getInt(Api.Keys.TASK_VARS);
				time = test.getInt(Api.Keys.TIME);
				year = test.getInt(Api.Keys.YEAR);

				values.clear();
				values.put(KEY_ID, id);
				values.put(KEY_LESSON_ID, lessonId);
				values.put(KEY_NAME, name);
				values.put(KEY_TASK_ALL, taskAll);
				values.put(KEY_TASK_MATCHES, taskMatches);
				values.put(KEY_TASK_OPEN_ANSWER, taskOpenAnswers);
				values.put(KEY_TASK_TEST, taskTest);
				values.put(KEY_TASK_VARS, taskVars);
				values.put(KEY_TIME, time);
				values.put(KEY_YEAR, year);
				values.put(KEY_LOADED, 0);

				status = db.insert(TABLE_TESTS, null, values);

				if (status == -1) {
					Log.e(LOG_TAG, "Error while inserting test! test id: " + id
							+ ".");
				}
			} catch (JSONException e) {
			}
		}
	}

	public void updateTableTests(JSONArray tests) {
		SQLiteDatabase db = getWritableDatabase();
		fillInTableTests(db, tests);
	}

	private void fillInTableTest(SQLiteDatabase db, JSONArray questions,
			String tableName) {
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
		db.execSQL(createTableTest(tableName));
		ContentValues values = new ContentValues();

		JSONObject questionItem;
		String answers;
		int balls;
		String correctAnswer;
		int id;
		int idTestQuestion;
		String question;
		int typeQuestion;

		long status;
		try {
			for (int i = 0; i < questions.length(); i++) {
				questionItem = questions.getJSONObject(i);

				answers = questionItem.getString(Api.Keys.ANSWERS);
				balls = questionItem.getInt(Api.Keys.BALLS);
				correctAnswer = questionItem.getString(Api.Keys.CORRECT_ANSWER);
				id = questionItem.getInt(Api.Keys.ID_ON_TEST);
				idTestQuestion = questionItem.getInt(Api.Keys.ID_TEST_QUESTION);
				question = questionItem.getString(Api.Keys.QUESTION);
				typeQuestion = questionItem.getInt(Api.Keys.TYPE_QUESTION);

				values.clear();
				values.put(KEY_ANSWERS, answers);
				values.put(KEY_BALLS, balls);
				values.put(KEY_CORRECT_ANSWER, correctAnswer);
				values.put(KEY_ID, id);
				values.put(KEY_ID_TEST_QUESTION, idTestQuestion);
				values.put(KEY_QUESTION, question);
				values.put(KEY_TYPE_QUESTION, typeQuestion);

				status = db.insert(tableName, null, values);

				if (status == -1) {
					Log.e(LOG_TAG, "Error while inserting question! "
							+ tableName + " id: " + id + ".");
				}
			}

		} catch (JSONException e) {

		}

	}

	public void updateTableTest(String lesson, int year, int id,
			JSONArray questions) {
		SQLiteDatabase db = getWritableDatabase();

		Cursor c = db.query(TABLE_TESTS, new String[] { KEY_ID, KEY_LOADED },
				KEY_ID + "=" + id, null, null, null, null);

		if (c.moveToNext()) {
			int loadedIndex = c.getColumnIndex(KEY_LOADED);

			if (c.getInt(loadedIndex) == 0) {
				ContentValues values = new ContentValues();
				values.put(KEY_LOADED, 1);
				db.update(TABLE_TESTS, values, KEY_ID + "=" + id, null);
			}
		}
		fillInTableTest(db, questions, lesson + "_" + year + "_" + id);
	}

	public ArrayList<Lesson> getLessons() {
		ArrayList<Lesson> lessons = new ArrayList<Lesson>();

		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TABLE_LESSONS, new String[] { KEY_ID, KEY_NAME,
				KEY_LINK }, null, null, null, null, null);
		Lesson lesson;
		int id;

		if (c.moveToFirst()) {
			int idIndex = c.getColumnIndex(KEY_ID);
			int nameIndex = c.getColumnIndex(KEY_NAME);
			int linkIndex = c.getColumnIndex(KEY_LINK);

			do {
				id = c.getInt(idIndex);
				lesson = new Lesson(id, c.getString(nameIndex),
						c.getString(linkIndex), getLessonTestsCount(id));
				lessons.add(lesson);
			} while (c.moveToNext());
		}

		return lessons;
	}

	public int getLessonTestsCount(int id) {
		SQLiteDatabase db = getWritableDatabase();
		return (db.query(TABLE_TESTS, new String[] { KEY_LESSON_ID },
				KEY_LESSON_ID + " = " + id, null, null, null, null)).getCount();
	}

	public ArrayList<TestInfo> getLessonTests(int id) {
		ArrayList<TestInfo> testsList = new ArrayList<TestInfo>();

		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TABLE_TESTS, new String[] { KEY_ID, KEY_LESSON_ID,
				KEY_NAME, KEY_TASK_ALL, KEY_TASK_MATCHES, KEY_TASK_OPEN_ANSWER,
				KEY_TASK_TEST, KEY_TASK_VARS, KEY_TIME, KEY_YEAR, KEY_LOADED },
				KEY_LESSON_ID + "=" + id, null, null, null, KEY_YEAR + " DESC");
		TestInfo testInfo;

		if (c.moveToFirst()) {
			int idIndex = c.getColumnIndex(KEY_ID);
			int nameIndex = c.getColumnIndex(KEY_NAME);
			int taskAllIndex = c.getColumnIndex(KEY_TASK_ALL);
			int taskMatchesIndex = c.getColumnIndex(KEY_TASK_MATCHES);
			int taskOpenAnswersIndex = c.getColumnIndex(KEY_TASK_OPEN_ANSWER);
			int taskTestIndex = c.getColumnIndex(KEY_TASK_OPEN_ANSWER);
			int taskVarsIndex = c.getColumnIndex(KEY_TASK_ALL);
			int timeIndex = c.getColumnIndex(KEY_TIME);
			int yearIndex = c.getColumnIndex(KEY_YEAR);
			int loadedIndex = c.getColumnIndex(KEY_LOADED);

			do {
				testInfo = new TestInfo(c.getInt(idIndex), id,
						c.getString(nameIndex), c.getInt(taskAllIndex),
						c.getInt(taskMatchesIndex),
						c.getInt(taskOpenAnswersIndex),
						c.getInt(taskTestIndex), c.getInt(taskVarsIndex),
						c.getInt(timeIndex), c.getInt(yearIndex),
						(c.getInt(loadedIndex) == 0) ? false : true);
				testsList.add(testInfo);
			} while (c.moveToNext());
		}

		ArrayList<TestInfo> part1 = new ArrayList<TestInfo>();
		ArrayList<TestInfo> part2 = new ArrayList<TestInfo>();

		for (TestInfo currTest : testsList) {
			if (currTest.loaded) {
				part1.add(currTest);
			} else {
				part2.add(currTest);
			}
		}

		testsList = new ArrayList<TestInfo>();
		testsList.addAll(part1);
		testsList.addAll(part2);

		return testsList;
	}

	public Test getTest(String tableName) {
		ArrayList<Question> questionsAll = new ArrayList<Question>();
		ArrayList<Question> questions = new ArrayList<Question>();
		TestInfo testInfo = null;
		int id = Integer.parseInt(tableName.substring(
				tableName.lastIndexOf('_') + 1, tableName.length()));

		SQLiteDatabase db = getWritableDatabase();

		Cursor c = db.query(tableName, new String[] { KEY_ID,
				KEY_ID_TEST_QUESTION, KEY_QUESTION, KEY_ANSWERS,
				KEY_CORRECT_ANSWER, KEY_BALLS, KEY_TYPE_QUESTION },
				null, null, null, null, null);

		if (c.moveToFirst()) {
			int idIndex = c.getColumnIndex(KEY_ID);
			int idTestQuestionIndex = c.getColumnIndex(KEY_ID_TEST_QUESTION);
			int questionIndex = c.getColumnIndex(KEY_QUESTION);
			int answersIndex = c.getColumnIndex(KEY_ANSWERS);
			int correctAnswerIndex = c.getColumnIndex(KEY_CORRECT_ANSWER);
			int ballsIndex = c.getColumnIndex(KEY_BALLS);
			int typeQuestionIndex = c.getColumnIndex(KEY_TYPE_QUESTION);

			Question question;
			do {
				question = new Question(c.getInt(idIndex), c
						.getInt(idTestQuestionIndex), c
						.getString(questionIndex), c.getString(answersIndex), c
						.getString(correctAnswerIndex), c.getInt(ballsIndex), c
						.getInt(typeQuestionIndex),null);
				
				questionsAll.add(question);
				if(question.idTestQuestion!=0){
					questions.add(question);
				}
			} while (c.moveToNext());
		}

		c = db.query(TABLE_TESTS, new String[] { KEY_ID, KEY_LESSON_ID,
				KEY_NAME, KEY_TASK_ALL, KEY_TASK_MATCHES, KEY_TASK_OPEN_ANSWER,
				KEY_TASK_TEST, KEY_TASK_VARS, KEY_TIME, KEY_YEAR, KEY_LOADED },
				KEY_ID + "=" + id, null, null, null, null);

		if (c.moveToFirst()) {
			int idIndex = c.getColumnIndex(KEY_ID);
			int nameIndex = c.getColumnIndex(KEY_NAME);
			int taskAllIndex = c.getColumnIndex(KEY_TASK_ALL);
			int taskMatchesIndex = c.getColumnIndex(KEY_TASK_MATCHES);
			int taskOpenAnswersIndex = c.getColumnIndex(KEY_TASK_OPEN_ANSWER);
			int taskTestIndex = c.getColumnIndex(KEY_TASK_OPEN_ANSWER);
			int taskVarsIndex = c.getColumnIndex(KEY_TASK_ALL);
			int timeIndex = c.getColumnIndex(KEY_TIME);
			int yearIndex = c.getColumnIndex(KEY_YEAR);
			int loadedIndex = c.getColumnIndex(KEY_LOADED);

			testInfo = new TestInfo(c.getInt(idIndex), id,
					c.getString(nameIndex), c.getInt(taskAllIndex),
					c.getInt(taskMatchesIndex), c.getInt(taskOpenAnswersIndex),
					c.getInt(taskTestIndex), c.getInt(taskVarsIndex),
					c.getInt(timeIndex), c.getInt(yearIndex),
					(c.getInt(loadedIndex) == 0) ? false : true);

		}

		return new Test(testInfo, questionsAll,questions);
	}

	private JSONArray loadFromResources(int id) throws IOException,
			JSONException {
		InputStream is = ZNOApplication.getInstance().getResources()
				.openRawResource(id);
		byte[] buf = new byte[is.available()];
		is.read(buf);
		is.close();

		return new JSONArray(new String(buf));
	}
}
