package com.vojkovladimir.zno.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vojkovladimir.zno.R;
import com.vojkovladimir.zno.ZNOApplication;
import com.vojkovladimir.zno.models.Lesson;
import com.vojkovladimir.zno.models.Question;
import com.vojkovladimir.zno.models.Test;
import com.vojkovladimir.zno.models.TestInfo;
import com.vojkovladimir.zno.service.ApiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ZNODataBaseHelper extends SQLiteOpenHelper {

    private static String LOG_TAG = "MyLogs";

    private static final String DATABASE_NAME = "ZNO.db";
    private static final int DATABASE_VERSION = 1;

    private static final String HREF_REPLACEMENT = "<a href=\"open.image://?src=";
    private static final String HREF = "<a href=\"";

    private static final String TABLE_LESSONS = "lessons";
    private static final String TABLE_TESTS = "tests";
    private static final String TABLE_QUESTIONS = "questions";
    private static final String TABLE_SQLITE_MASTER = "sqlite_master";
    private static final String TABLE_ANDROID_META_DATA = "android_metadata";
    private static final String TABLE_SQLITE_SEQUENCE = "sqlite_sequence";
    private static final String TABLE_USER_ANSWERS = "user_answers";
    private static final String TABLE_USER_RECORDS = "user_records";

    private static final String KEY_ID = "id";
    private static final String KEY_LESSON_ID = "lesson_id";
    private static final String KEY_TEST_ID = "test_id";
    private static final String KEY_ID_ON_TEST = "id_on_test";
    private static final String KEY_ID_TEST_QUESTION = "id_test_question";
    private static final String KEY_LINK = "link";
    private static final String KEY_NAME = "name";
    private static final String KEY_TASK_ALL = "task_all";
    private static final String KEY_TASK_MATCHES = "task_matches";
    private static final String KEY_TASK_OPEN_ANSWER = "task_open_answer";
    private static final String KEY_TASK_TEST = "task_test";
    private static final String KEY_TASK_VARS = "task_vars";
    private static final String KEY_TIME = "time";
    private static final String KEY_YEAR = "year";
    private static final String KEY_LOADED = "loaded";
    private static final String KEY_ANSWERS = "answers";
    private static final String KEY_BALLS = "balls";
    private static final String KEY_ZNO_BALL = "zno_ball";
    private static final String KEY_CORRECT_ANSWER = "correct_answer";
    private static final String KEY_QUESTION = "question";
    private static final String KEY_TYPE_QUESTION = "type_question";
    private static final String KEY_LAST_UPDATE = "last_update";
    private static final String KEY_PARENT_QUESTION = "parent_question";
    private static final String KEY_TYPE = "type";
    private static final String KEY_DATE = "date";
    private static final String KEY_ELAPSED_TIME = "elapsed_time";

    private static final String CREATE_TABLE_LESSONS =
            "CREATE TABLE " + TABLE_LESSONS + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY, "
                    + KEY_LINK + " TEXT, "
                    + KEY_NAME + " TEXT);";

    private static final String CREATE_TABLE_TESTS =
            "CREATE TABLE " + TABLE_TESTS + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY, "
                    + KEY_LESSON_ID + " INTEGER, "
                    + KEY_NAME + " TEXT, "
                    + KEY_TASK_ALL + " INTEGER, "
                    + KEY_TASK_MATCHES + " INTEGER, "
                    + KEY_TASK_OPEN_ANSWER + " INTEGER, "
                    + KEY_TASK_TEST + " INTEGER, "
                    + KEY_TASK_VARS + " INTEGER, "
                    + KEY_TIME + " INTEGER, "
                    + KEY_YEAR + " INTEGER, "
                    + KEY_LAST_UPDATE + " INTEGER, "
                    + KEY_BALLS + " TEXT, "
                    + KEY_LOADED + " INTEGER);";

    private static final String CREATE_TABLE_QUESTIONS =
            "CREATE TABLE " + TABLE_QUESTIONS + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY, "
                    + KEY_ID_ON_TEST + " INTEGER, "
                    + KEY_ID_TEST_QUESTION + " INTEGER, "
                    + KEY_TEST_ID + " INTEGER, "
                    + KEY_PARENT_QUESTION + " TEXT, "
                    + KEY_QUESTION + " TEXT, "
                    + KEY_ANSWERS + " TEXT, "
                    + KEY_CORRECT_ANSWER + " STRING, "
                    + KEY_BALLS + " INTEGER, "
                    + KEY_TYPE_QUESTION + " INTEGER" + ");";

    private static final String CREATE_TABLE_USER_ANSWERS =
            "CREATE TABLE " + TABLE_USER_ANSWERS + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY, "
                    + KEY_LESSON_ID + " INTEGER, "
                    + KEY_TEST_ID + " INTEGER, "
                    + KEY_DATE + " INTEGER, "
                    + KEY_ANSWERS + " TEXT, "
                    + KEY_ELAPSED_TIME + " INTEGER, "
                    + KEY_ZNO_BALL + " REAL);";

    private static final String CREATE_TABLE_USER_RECORDS =
            "CREATE TABLE " + TABLE_USER_RECORDS + " ("
                    + KEY_LESSON_ID + " INTEGER PRIMARY KEY, "
                    + KEY_TEST_ID + " INTEGER, "
                    + KEY_DATE + " INTEGER, "
                    + KEY_ELAPSED_TIME + " INTEGER, "
                    + KEY_ZNO_BALL + " REAL);";

    public ZNODataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LESSONS);
        db.execSQL(CREATE_TABLE_TESTS);
        db.execSQL(CREATE_TABLE_QUESTIONS);
        db.execSQL(CREATE_TABLE_USER_ANSWERS);
        db.execSQL(CREATE_TABLE_USER_RECORDS);

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
        List<String> tables = new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SQLITE_MASTER + " WHERE " + KEY_TYPE + "='table';", null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String tableName = cursor.getString(1);
                if (!tableName.equals(TABLE_ANDROID_META_DATA) && !tableName.equals(TABLE_SQLITE_SEQUENCE))
                    tables.add(tableName);
                cursor.moveToNext();
            }
        }
        cursor.close();

        for (String tableName : tables) {
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
        }
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

                id = lesson.getInt(ApiService.Keys.ID);
                link = lesson.getString(ApiService.Keys.LINK).replace("-", "_");
                name = lesson.getString(ApiService.Keys.NAME);

                values.clear();
                values.put(KEY_ID, id);
                values.put(KEY_LINK, link);
                values.put(KEY_NAME, name);

                status = db.insert(TABLE_LESSONS, null, values);

                if (status == -1) {
                    Log.e(LOG_TAG, "Error while inserting lesson! Lesson id: " + id + ".");
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
        int lastUpdate;

        long status;

        for (int i = 0; i < tests.length(); i++) {
            try {
                test = tests.getJSONObject(i);

                id = test.getInt(ApiService.Keys.ID);
                lessonId = test.getInt(ApiService.Keys.LESSON_ID);
                name = test.getString(ApiService.Keys.NAME);
                taskAll = test.getInt(ApiService.Keys.TASK_ALL);
                taskMatches = test.getInt(ApiService.Keys.TASK_MATCHES);
                taskOpenAnswers = test.getInt(ApiService.Keys.TASK_OPEN_ANSWER);
                taskTest = test.getInt(ApiService.Keys.TASK_TEST);
                taskVars = test.getInt(ApiService.Keys.TASK_VARS);
                time = test.getInt(ApiService.Keys.TIME);
                year = test.getInt(ApiService.Keys.YEAR);
                lastUpdate = test.getInt(ApiService.Keys.LAST_UPDATE);

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
                values.put(KEY_LAST_UPDATE, lastUpdate);

                status = db.insert(TABLE_TESTS, null, values);

                if (status == -1) {
                    Log.e(LOG_TAG, "Error while inserting test! test id: " + id + ".");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateRecords(SQLiteDatabase db, int lessonId, int testId, float newBall, long elapsedTime, long date) {
        Cursor c = db.query(TABLE_USER_RECORDS, new String[]{KEY_ZNO_BALL}, KEY_LESSON_ID + "=" + lessonId, null, null, null, null);
        if (c.moveToFirst() && c.getCount() == 1) {
            int znoBallIndex = c.getColumnIndex(KEY_ZNO_BALL);
            float oldBall = c.getFloat(znoBallIndex);
            if (oldBall <= newBall) {
                ContentValues values = new ContentValues();
                values.put(KEY_TEST_ID, testId);
                values.put(KEY_ZNO_BALL, newBall);
                values.put(KEY_ELAPSED_TIME, elapsedTime);
                values.put(KEY_DATE, date);
                db.update(TABLE_USER_RECORDS, values, KEY_LESSON_ID + "=" + lessonId, null);
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(KEY_LESSON_ID, lessonId);
            values.put(KEY_TEST_ID, testId);
            values.put(KEY_ZNO_BALL, newBall);
            values.put(KEY_ELAPSED_TIME, elapsedTime);
            values.put(KEY_DATE, date);
            db.insert(TABLE_USER_RECORDS, null, values);
        }
    }

//    public void updateTableTests(JSONArray tests) {
//
//    }

    public int saveUserAnswers(int lessonId, int testId, String answers) {
        long row;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LESSON_ID, lessonId);
        values.put(KEY_TEST_ID, testId);
        values.put(KEY_ANSWERS, answers);
        row = db.insert(TABLE_USER_ANSWERS, null, values);
        db.close();
        return (int) row;
    }

    public int updateUserAnswers(int id, String answers) {
        long row;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);
        values.put(KEY_ANSWERS, answers);
        row = db.update(TABLE_USER_ANSWERS, values, KEY_ID + "=" + id, null);
        db.close();
        return (int) row;
    }

    public int completeUserAnswers(int id, float znoBall, long elapsedTime, long date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        long row;
        values.put(KEY_ID, id);
        values.put(KEY_ZNO_BALL, znoBall);
        values.put(KEY_ELAPSED_TIME, elapsedTime);
        values.put(KEY_DATE, date);
        row = db.update(TABLE_USER_ANSWERS, values, KEY_ID + "=" + id, null);
        Cursor c = db.query(TABLE_USER_ANSWERS, new String[]{KEY_LESSON_ID, KEY_TEST_ID}, KEY_ID + "=" + id, null, null, null, null);
        if (c.moveToFirst()) {
            int lessonIdIndex = c.getColumnIndex(KEY_LESSON_ID);
            int testIdIndex = c.getColumnIndex(KEY_TEST_ID);
            int lessonId = c.getInt(lessonIdIndex);
            int testId = c.getInt(testIdIndex);
            updateRecords(db, lessonId, testId, znoBall, elapsedTime, date);
        }
        db.close();
        return (int) row;
    }

    public void deleteUserAnswers(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_USER_ANSWERS, KEY_ID + "=" + id, null);
        db.close();
    }

    public String getUserAnswers(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USER_ANSWERS, new String[]{KEY_ANSWERS}, KEY_ID + "=" + id, null, null, null, null);
        if (c.moveToFirst()) {
            return c.getString(c.getColumnIndex(KEY_ANSWERS));
        } else {
            return null;
        }
    }

    public boolean updateQuestions(int testId, JSONArray questions, JSONArray testBalls) throws JSONException {
        long status;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        db.delete(TABLE_QUESTIONS, KEY_TEST_ID + "=" + testId, null);

        JSONObject question;
        int typeQuestion;
        int balls;

        for (int i = 0; i < questions.length(); i++) {
            question = questions.getJSONObject(i);
            typeQuestion = question.getInt(ApiService.Keys.TYPE_QUESTION);
            balls = question.getInt(ApiService.Keys.BALLS);

            if (!(balls == 0 && typeQuestion == 2)) {
                values.clear();
                values.put(KEY_ID_ON_TEST, question.getInt(ApiService.Keys.ID_ON_TEST));
                values.put(KEY_ID_TEST_QUESTION, question.getInt(ApiService.Keys.ID_TEST_QUESTION));
                values.put(KEY_TEST_ID, testId);
                values.put(KEY_PARENT_QUESTION, question.optString(ApiService.Keys.PARENT_QUESTION, ""));
                values.put(KEY_QUESTION, question.getString(ApiService.Keys.QUESTION).replace(HREF, HREF_REPLACEMENT));
                values.put(KEY_ANSWERS, question.getString(ApiService.Keys.ANSWERS));
                values.put(KEY_CORRECT_ANSWER, question.getString(ApiService.Keys.CORRECT_ANSWER));
                values.put(KEY_BALLS, balls);
                values.put(KEY_TYPE_QUESTION, typeQuestion);

                status = db.insert(TABLE_QUESTIONS, null, values);
                if (status == -1) {
                    Log.e(LOG_TAG, "Error while inserting question, test # " + testId);
                    db.close();
                    return false;
                }
            }
        }

        JSONObject ball;
        String ballsArray = new String();
        values.clear();

        for (int i = 0; i < testBalls.length(); i++) {
            ball = testBalls.getJSONObject(i);
            ballsArray += ball.getString(ApiService.Keys.ZNO_BALL) + "\r";
        }

        values.put(KEY_BALLS, ballsArray);
        values.put(KEY_LOADED, 1);
        status = db.update(TABLE_TESTS, values, KEY_ID + "=" + testId, null);
        db.close();

        if (status == -1) {
            return false;
        } else {
            return true;
        }
    }

    public String[] getTestBalls(int id) {
        String balls;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_TESTS, new String[]{KEY_BALLS}, KEY_ID + "=" + id, null, null, null, null);
        if (c.moveToFirst()) {
            balls = c.getString(c.getColumnIndex(KEY_BALLS));
        } else {
            balls = null;
        }
        db.close();

        return balls.split("\r");
    }

    public ArrayList<Lesson> getLessons() {
        ArrayList<Lesson> lessons = new ArrayList<Lesson>();
        ArrayList<Lesson> lessonsEnd = new ArrayList<Lesson>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_LESSONS, new String[]{KEY_ID, KEY_NAME, KEY_LINK}, null, null, null, null, null);
        Lesson lesson;
        int id;

        if (c.moveToFirst()) {
            int idIndex = c.getColumnIndex(KEY_ID);
            int nameIndex = c.getColumnIndex(KEY_NAME);
            int linkIndex = c.getColumnIndex(KEY_LINK);

            do {
                id = c.getInt(idIndex);
                lesson = new Lesson(id, c.getString(nameIndex), c.getString(linkIndex), getLessonTestsCount(id));
                if (id == 3) {
                    lessonsEnd.add(lesson);
                } else {
                    lessons.add(lesson);
                }
            } while (c.moveToNext());
        }
        lessons.addAll(lessonsEnd);

        return lessons;
    }

    public int getLessonTestsCount(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int count = (db.query(TABLE_TESTS, new String[]{KEY_LESSON_ID}, KEY_LESSON_ID + " = " + id, null, null, null, null)).getCount();
        db.close();
        return count;
    }

    public ArrayList<TestInfo> getLessonTests(int id) {
        ArrayList<TestInfo> testsList = new ArrayList<TestInfo>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_TESTS, new String[]{KEY_ID, KEY_LESSON_ID,
                        KEY_NAME, KEY_TASK_ALL, KEY_TASK_MATCHES, KEY_TASK_OPEN_ANSWER,
                        KEY_TASK_TEST, KEY_TASK_VARS, KEY_TIME, KEY_YEAR, KEY_LOADED},
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

    public Test getTest(int id) {
        ArrayList<Question> questions = new ArrayList<Question>();
        TestInfo testInfo = null;

        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.query(TABLE_QUESTIONS, new String[]{KEY_ID_TEST_QUESTION, KEY_QUESTION, KEY_PARENT_QUESTION,
                        KEY_ANSWERS, KEY_CORRECT_ANSWER, KEY_BALLS, KEY_TYPE_QUESTION, KEY_TEST_ID}, KEY_TEST_ID + "=" + id, null,
                null, null, KEY_ID_ON_TEST);

        if (c.moveToFirst()) {
            int idTestQuestionIndex = c.getColumnIndex(KEY_ID_TEST_QUESTION);
            int questionIndex = c.getColumnIndex(KEY_QUESTION);
            int parentQuestionIndex = c.getColumnIndex(KEY_PARENT_QUESTION);
            int answersIndex = c.getColumnIndex(KEY_ANSWERS);
            int correctAnswerIndex = c.getColumnIndex(KEY_CORRECT_ANSWER);
            int ballsIndex = c.getColumnIndex(KEY_BALLS);
            int typeQuestionIndex = c.getColumnIndex(KEY_TYPE_QUESTION);

            Question question;
            do {
                question = new Question(c.getInt(idTestQuestionIndex),
                        c.getString(questionIndex),
                        c.getString(parentQuestionIndex),
                        c.getString(answersIndex),
                        c.getString(correctAnswerIndex),
                        c.getInt(ballsIndex),
                        c.getInt(typeQuestionIndex),
                        null);
                questions.add(question);
            } while (c.moveToNext());
        }
        c.close();

        c = db.query(TABLE_TESTS, new String[]{KEY_ID, KEY_LESSON_ID,
                        KEY_NAME, KEY_TASK_ALL, KEY_TASK_MATCHES, KEY_TASK_OPEN_ANSWER,
                        KEY_TASK_TEST, KEY_TASK_VARS, KEY_TIME, KEY_YEAR, KEY_LOADED},
                KEY_ID + "=" + id, null, null, null, null);

        if (c.moveToFirst()) {
            int idIndex = c.getColumnIndex(KEY_ID);
            int lessonId = c.getColumnIndex(KEY_LESSON_ID);
            int nameIndex = c.getColumnIndex(KEY_NAME);
            int taskAllIndex = c.getColumnIndex(KEY_TASK_ALL);
            int taskMatchesIndex = c.getColumnIndex(KEY_TASK_MATCHES);
            int taskOpenAnswersIndex = c.getColumnIndex(KEY_TASK_OPEN_ANSWER);
            int taskTestIndex = c.getColumnIndex(KEY_TASK_OPEN_ANSWER);
            int taskVarsIndex = c.getColumnIndex(KEY_TASK_ALL);
            int timeIndex = c.getColumnIndex(KEY_TIME);
            int yearIndex = c.getColumnIndex(KEY_YEAR);
            int loadedIndex = c.getColumnIndex(KEY_LOADED);

            testInfo = new TestInfo(c.getInt(idIndex), c.getInt(lessonId),
                    c.getString(nameIndex), c.getInt(taskAllIndex),
                    c.getInt(taskMatchesIndex), c.getInt(taskOpenAnswersIndex),
                    c.getInt(taskTestIndex), c.getInt(taskVarsIndex),
                    c.getInt(timeIndex), c.getInt(yearIndex),
                    (c.getInt(loadedIndex) == 0) ? false : true);

        }

        return new Test(testInfo, questions);
    }

    private JSONArray loadFromResources(int id) throws IOException, JSONException {
        InputStream is = ZNOApplication.getInstance().getResources().openRawResource(id);
        byte[] buf = new byte[is.available()];
        is.read(buf);
        is.close();

        return new JSONArray(new String(buf));
    }
}
