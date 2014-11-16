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
import com.vojkovladimir.zno.models.PassedTest;
import com.vojkovladimir.zno.models.Question;
import com.vojkovladimir.zno.models.Record;
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

    private static final String AND = " AND ";
    private static final String AS = " AS ";
    private static final String ON = " ON ";
    private static final String SELECT = "SELECT ";
    private static final String FROM = "FROM ";
    private static final String INNER = "INNER ";
    private static final String JOIN = "JOIN ";
    private static final String WHERE = " WHERE ";
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
    private static final String KEY_NAME_ROD = "name_rod";
    private static final String KEY_TASK_ALL = "task_all";
    private static final String KEY_TIME = "time";
    private static final String KEY_YEAR = "year";
    private static final String KEY_LOADED = "loaded";
    private static final String KEY_ANSWERS = "answers";
    private static final String KEY_BALLS = "balls";
    private static final String KEY_ZNO_BALL = "zno_ball";
    private static final String KEY_TEST_BALL = "test_ball";
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
                    + KEY_NAME + " TEXT, "
                    + KEY_NAME_ROD + " TEXT);";

    private static final String CREATE_TABLE_TESTS =
            "CREATE TABLE " + TABLE_TESTS + " ("
                    + KEY_ID + " INTEGER PRIMARY KEY, "
                    + KEY_LESSON_ID + " INTEGER, "
                    + KEY_NAME + " TEXT, "
                    + KEY_TASK_ALL + " INTEGER, "
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
                    + KEY_TEST_BALL + " INTEGER, "
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
        String nameRod;

        long status;

        for (int i = 0; i < lessons.length(); i++) {
            try {
                lesson = lessons.getJSONObject(i);

                id = lesson.getInt(ApiService.Keys.ID);
                link = lesson.getString(ApiService.Keys.LINK).replace("-", "_");
                name = lesson.getString(ApiService.Keys.NAME);
                nameRod = lesson.getString(ApiService.Keys.NAME_ROD);

                values.clear();
                values.put(KEY_ID, id);
                values.put(KEY_LINK, link);
                values.put(KEY_NAME, name);
                values.put(KEY_NAME_ROD, nameRod);

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

        for (int i = 0; i < tests.length(); i++) {
            try {
                insertTest(db, tests.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertTest(SQLiteDatabase db, JSONObject test) throws JSONException {
        ContentValues values = new ContentValues();

        int id;
        int lessonId;
        String name;
        int taskAll;
        int time;
        int year;
        int lastUpdate;

        id = test.getInt(ApiService.Keys.ID);
        lessonId = test.getInt(ApiService.Keys.LESSON_ID);
        name = test.getString(ApiService.Keys.NAME);
        taskAll = test.getInt(ApiService.Keys.TASK_ALL);
        time = test.getInt(ApiService.Keys.TIME);
        year = test.getInt(ApiService.Keys.YEAR);
        lastUpdate = test.getInt(ApiService.Keys.LAST_UPDATE);

        values.clear();
        values.put(KEY_ID, id);
        values.put(KEY_LESSON_ID, lessonId);
        values.put(KEY_NAME, name);
        values.put(KEY_TASK_ALL, taskAll);
        values.put(KEY_TIME, time);
        values.put(KEY_YEAR, year);
        values.put(KEY_LOADED, 0);
        values.put(KEY_LAST_UPDATE, lastUpdate);

        db.insert(TABLE_TESTS, null, values);
    }

    private void updateRecords(SQLiteDatabase db, int lessonId, int testId, float newBall, long elapsedTime, long date) {
        String[] projection = {KEY_ZNO_BALL};
        String selection = KEY_LESSON_ID + "=" + lessonId;
        Cursor c = db.query(TABLE_USER_RECORDS, projection, selection, null, null, null, null);
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

    public int saveUserAnswers(int lessonId, int testId, String answers) {
        long row;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LESSON_ID, lessonId);
        values.put(KEY_TEST_ID, testId);
        values.put(KEY_ANSWERS, answers);
        row = db.insert(TABLE_USER_ANSWERS, null, values);
        return (int) row;
    }

    public void updateUserAnswers(int id, String answers) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ANSWERS, answers);
        db.update(TABLE_USER_ANSWERS, values, KEY_ID + "=" + id, null);
    }

    public void completeUserAnswers(int id, int testBall, float znoBall, long elapsedTime, long date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ZNO_BALL, znoBall);
        values.put(KEY_TEST_BALL, testBall);
        values.put(KEY_ELAPSED_TIME, elapsedTime);
        values.put(KEY_DATE, date);
        db.update(TABLE_USER_ANSWERS, values, KEY_ID + "=" + id, null);
        String[] projection = {KEY_LESSON_ID, KEY_TEST_ID};
        String selection = KEY_ID + "=" + id;
        Cursor c = db.query(TABLE_USER_ANSWERS, projection, selection, null, null, null, null);
        if (c.moveToFirst()) {
            int lessonIdIndex = c.getColumnIndex(KEY_LESSON_ID);
            int testIdIndex = c.getColumnIndex(KEY_TEST_ID);
            int lessonId = c.getInt(lessonIdIndex);
            int testId = c.getInt(testIdIndex);
            updateRecords(db, lessonId, testId, znoBall, elapsedTime, date);
        }
    }

    public void deleteUserAnswers(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_USER_ANSWERS, KEY_ID + "=" + id, null);
    }

    public String getUserAnswers(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {KEY_ANSWERS};
        String selection = KEY_ID + "=" + id;
        Cursor c = db.query(TABLE_USER_ANSWERS, projection, selection, null, null, null, null);
        if (c.moveToFirst()) {
            return c.getString(c.getColumnIndex(KEY_ANSWERS));
        } else {
            return null;
        }
    }

    public Record getResult(int id) {
        Record result = null;
        String TEST_NAME = "test_name";
        String T = "T";
        String L = "L";
        String A = "A";
        String query =
                SELECT + L + "." + KEY_NAME + ", " +
                        KEY_YEAR + ", " +
                        T + "." + KEY_NAME + AS + TEST_NAME + ", " +
                        KEY_DATE + ", " +
                        KEY_ELAPSED_TIME + ", " +
                        KEY_ZNO_BALL + ", " +
                        KEY_TEST_BALL + " " +
                        FROM + TABLE_USER_ANSWERS + AS + A + " " +
                        INNER + JOIN + TABLE_TESTS + AS + T +
                        ON + T + "." + KEY_ID + "=" + KEY_TEST_ID + " " +
                        INNER + JOIN + TABLE_LESSONS + AS + L +
                        ON + L + "." + KEY_ID + "=" + T + "." + KEY_LESSON_ID +
                        WHERE + " " + A + "." + KEY_ID + "=" + id;

        Cursor c = getReadableDatabase().rawQuery(query, null);
        if (c.moveToFirst()) {
            result = new Record();
            result.lessonName = c.getString(0);
            result.year = c.getInt(1);
            result.session = parseSession(c.getString(2));
            result.date = c.getLong(3);
            result.elapsedTime = c.getInt(4);
            result.znoBall = c.getFloat(5);
            result.testBall = c.getInt(6);
        }

        return result;
    }

    public ArrayList<Integer> getTestsForUpdate(JSONArray tests) throws JSONException {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Integer> ids = new ArrayList<Integer>();

        JSONObject test;
        int id;
        long remoteLastUpdate;
        long lastUpdate;
        String[] projection = {KEY_ID, KEY_LOADED, KEY_LAST_UPDATE};
        String selection;
        Cursor c;

        for (int i = 0; i < tests.length(); i++) {
            test = tests.getJSONObject(i);
            id = test.getInt(ApiService.Keys.ID);
            selection = String.format(KEY_ID + "=%d" + AND + KEY_LOADED + "=1", id);
            c = db.query(TABLE_TESTS, projection, selection, null, null, null, null);
            if (c.moveToFirst()) {
                remoteLastUpdate = test.getLong(ApiService.Keys.LAST_UPDATE);
                lastUpdate = c.getLong(c.getColumnIndex(KEY_LAST_UPDATE));
                if (remoteLastUpdate > lastUpdate) {
                    ids.add(id);
                }
            }
        }

        return ids;
    }

    public void updateTest(int id, JSONArray questions, JSONArray testBalls)
            throws JSONException {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        db.delete(TABLE_QUESTIONS, KEY_TEST_ID + "=" + id, null);

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
                values.put(KEY_TEST_ID, id);
                values.put(KEY_PARENT_QUESTION, question.optString(ApiService.Keys.PARENT_QUESTION, ""));
                values.put(KEY_QUESTION, question.getString(ApiService.Keys.QUESTION).replace(HREF, HREF_REPLACEMENT));
                values.put(KEY_ANSWERS, question.getString(ApiService.Keys.ANSWERS));
                values.put(KEY_CORRECT_ANSWER, question.getString(ApiService.Keys.CORRECT_ANSWER));
                values.put(KEY_BALLS, balls);
                values.put(KEY_TYPE_QUESTION, typeQuestion);

                db.insert(TABLE_QUESTIONS, null, values);
            }
        }

        JSONObject ball;
        String ballsArray = "";
        values.clear();

        for (int i = 0; i < testBalls.length(); i++) {
            ball = testBalls.getJSONObject(i);
            ballsArray += ball.getString(ApiService.Keys.ZNO_BALL) + "\r";
        }

        values.put(KEY_BALLS, ballsArray);
        values.put(KEY_LOADED, 1);
        values.put(KEY_LAST_UPDATE, System.currentTimeMillis() / 1000);
        db.update(TABLE_TESTS, values, KEY_ID + "=" + id, null);
    }

    public void updateTests(JSONArray tests) throws JSONException {
        SQLiteDatabase db = getReadableDatabase();

        JSONObject test;
        int id;
        String[] projection = {KEY_ID, KEY_LOADED, KEY_LAST_UPDATE};
        String selection;
        Cursor c;

        for (int i = 0; i < tests.length(); i++) {
            test = tests.getJSONObject(i);
            id = test.getInt(ApiService.Keys.ID);
            selection = String.format(KEY_ID + "=%d", id);
            c = db.query(TABLE_TESTS, projection, selection, null, null, null, null);
            if (!c.moveToFirst()) {
                insertTest(db, test);
            }
        }
    }

    public String[] getTestBalls(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {KEY_BALLS};
        String selection = KEY_ID + "=" + id;
        Cursor c = db.query(TABLE_TESTS, projection, selection, null, null, null, null);
        if (c.moveToFirst()) {
            return c.getString(c.getColumnIndex(KEY_BALLS)).split("\r");
        } else {
            return null;
        }
    }

    public ArrayList<Lesson> getLessons() {
        ArrayList<Lesson> lessons = new ArrayList<Lesson>();

        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {KEY_ID, KEY_NAME, KEY_NAME_ROD, KEY_LINK};
        Cursor c = db.query(TABLE_LESSONS, projection, null, null, null, null, null);
        Lesson lesson;
        Lesson worldHist = null;

        if (c.moveToFirst()) {

            do {
                lesson = new Lesson();
                lesson.id = c.getInt(0);
                lesson.name = c.getString(1);
                lesson.nameRod = c.getString(2).replace("_", "-");
                lesson.link = c.getString(3);
                lesson.testsCount = getLessonTestsCount(lesson.id);

                if (lesson.id == 3) {
                    worldHist = lesson;
                } else {
                    lessons.add(lesson);
                }

            } while (c.moveToNext());

            if (worldHist != null) {
                lessons.add(worldHist);
            }
        }

        return lessons;
    }

    public Lesson getLesson(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {KEY_ID, KEY_NAME, KEY_NAME_ROD, KEY_LINK};
        String selection = KEY_ID + "=" + id;
        Cursor c = db.query(TABLE_LESSONS, projection, selection, null, null, null, null);
        c.moveToFirst();
        Lesson lesson = new Lesson();

        lesson.id = c.getInt(0);
        lesson.name = c.getString(1);
        lesson.nameRod = c.getString(2);
        lesson.link = c.getString(3);

        return lesson;
    }

    public int getLessonTestsCount(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] projection = {KEY_LESSON_ID};
        String selection = KEY_LESSON_ID + " = " + id;
        Cursor c = db.query(TABLE_TESTS, projection, selection, null, null, null, null);
        // Hide math 2014 test
        if (id == 4) {
            return c.getCount() - 1;
        }
        return c.getCount();
    }

    public ArrayList<TestInfo> getLessonTests(int id) {
        ArrayList<TestInfo> tests = new ArrayList<TestInfo>();
        ArrayList<TestInfo> notLoaded = new ArrayList<TestInfo>();

        SQLiteDatabase db = getWritableDatabase();
        String[] projection = {KEY_ID, KEY_LESSON_ID, KEY_NAME, KEY_TASK_ALL, KEY_TIME, KEY_YEAR, KEY_LOADED};
        String selection = KEY_LESSON_ID + "=" + id;
        String ordering = KEY_YEAR + " DESC";
        Cursor c = db.query(TABLE_TESTS, projection, selection, null, null, null, ordering);

        if (c.moveToFirst()) {
            int idIndex = c.getColumnIndex(KEY_ID);
            int nameIndex = c.getColumnIndex(KEY_NAME);
            int taskAllIndex = c.getColumnIndex(KEY_TASK_ALL);
            int timeIndex = c.getColumnIndex(KEY_TIME);
            int yearIndex = c.getColumnIndex(KEY_YEAR);
            int loadedIndex = c.getColumnIndex(KEY_LOADED);

            TestInfo testInfo;

            do {
                testInfo = new TestInfo();

                testInfo.id = c.getInt(idIndex);
                // Hide math 2014 test
                if (testInfo.id == 43) {
                    continue;
                }
                testInfo.lessonId = id;
                testInfo.name = c.getString(nameIndex);
                testInfo.taskAll = c.getInt(taskAllIndex);
                testInfo.time = c.getInt(timeIndex);
                testInfo.year = c.getInt(yearIndex);
                testInfo.loaded = c.getInt(loadedIndex) != 0;

                if (testInfo.loaded) {
                    tests.add(testInfo);
                } else {
                    notLoaded.add(testInfo);
                }
            } while (c.moveToNext());
        }

        tests.addAll(notLoaded);

        return tests;
    }

    public Test getTest(int id) {
        ArrayList<Question> questions = new ArrayList<Question>();

        SQLiteDatabase db = getWritableDatabase();
        String[] projection = {KEY_ID_TEST_QUESTION, KEY_QUESTION, KEY_PARENT_QUESTION,
                KEY_ANSWERS, KEY_CORRECT_ANSWER, KEY_BALLS, KEY_TYPE_QUESTION, KEY_TEST_ID};
        String selection = KEY_TEST_ID + "=" + id;
        Cursor c = db.query(TABLE_QUESTIONS, projection, selection, null, null, null, KEY_ID_ON_TEST);

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
                question = new Question();

                question.id = c.getInt(idTestQuestionIndex);
                question.question = c.getString(questionIndex);
                question.parentQuestion = c.getString(parentQuestionIndex);
                question.answers = c.getString(answersIndex);
                question.correctAnswer = c.getString(correctAnswerIndex);
                question.balls = c.getInt(ballsIndex);
                question.type = c.getInt(typeQuestionIndex);
                question.makeUserAnswer();

                questions.add(question);
            } while (c.moveToNext());
        }
        c.close();

        projection = new String[]{KEY_ID, KEY_LESSON_ID, KEY_NAME, KEY_TASK_ALL, KEY_TIME,
                KEY_YEAR, KEY_LOADED};
        selection = KEY_ID + "=" + id;
        c = db.query(TABLE_TESTS, projection, selection, null, null, null, null);

        if (c.moveToFirst()) {
            int idIndex = c.getColumnIndex(KEY_ID);
            int lessonId = c.getColumnIndex(KEY_LESSON_ID);
            int nameIndex = c.getColumnIndex(KEY_NAME);
            int taskAllIndex = c.getColumnIndex(KEY_TASK_ALL);
            int timeIndex = c.getColumnIndex(KEY_TIME);
            int yearIndex = c.getColumnIndex(KEY_YEAR);
            int loadedIndex = c.getColumnIndex(KEY_LOADED);

            TestInfo testInfo = new TestInfo();

            testInfo.id = c.getInt(idIndex);
            testInfo.lessonId = c.getInt(lessonId);
            testInfo.name = c.getString(nameIndex);
            testInfo.taskAll = c.getInt(taskAllIndex);
            testInfo.time = c.getInt(timeIndex);
            testInfo.year = c.getInt(yearIndex);
            testInfo.loaded = c.getInt(loadedIndex) != 0;

            return new Test(testInfo, questions);
        }

        return null;
    }

    public ArrayList<Record> getRecords() {
        ArrayList<Record> records = new ArrayList<Record>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cRecords;
        String[] projection = {KEY_LESSON_ID, KEY_TEST_ID, KEY_DATE, KEY_ELAPSED_TIME, KEY_ZNO_BALL};
        String selection;

        cRecords = db.query(TABLE_USER_RECORDS, projection, null, null, null, null, null, null);
        if (cRecords.moveToFirst()) {
            Record record;
            Cursor lesson;
            Cursor test;

            int lessonId;
            int testId;
            String testName;

            do {
                record = new Record();
                lessonId = cRecords.getInt(cRecords.getColumnIndex(KEY_LESSON_ID));
                projection = new String[]{KEY_NAME};
                selection = KEY_ID + "=" + lessonId;
                lesson = db.query(TABLE_LESSONS, projection, selection, null, null, null, null);

                if (lesson.moveToFirst()) {
                    record.lessonName = lesson.getString(lesson.getColumnIndex(KEY_NAME));
                } else {
                    continue;
                }

                testId = cRecords.getInt(cRecords.getColumnIndex(KEY_TEST_ID));
                projection = new String[]{KEY_NAME, KEY_YEAR};
                selection = KEY_ID + "=" + testId;
                test = db.query(TABLE_TESTS, projection, selection, null, null, null, null);

                if (test.moveToFirst()) {
                    testName = test.getString(test.getColumnIndex(KEY_NAME));
                    record.year = test.getInt(test.getColumnIndex(KEY_YEAR));
                    record.session = parseSession(testName);
                } else {
                    continue;
                }

                record.date = cRecords.getLong(cRecords.getColumnIndex(KEY_DATE));
                record.elapsedTime = cRecords.getLong(cRecords.getColumnIndex(KEY_ELAPSED_TIME));
                record.znoBall = cRecords.getFloat(cRecords.getColumnIndex(KEY_ZNO_BALL));

                records.add(record);
            } while (cRecords.moveToNext());
        }


        return records;
    }

    private int parseSession(String testName) {
        String SESSION = ZNOApplication.getInstance().getResources().getString(R.string.session_text);
        if (testName.contains("(I " + SESSION + ")")) {
            return 1;
        } else if (testName.contains("(II " + SESSION + ")")) {
            return 2;
        }
        return 0;
    }

    public ArrayList<PassedTest> getPassedTests() {
        ArrayList<PassedTest> passedTests = new ArrayList<PassedTest>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor recRows;
        String[] projection = {KEY_ID, KEY_LESSON_ID, KEY_TEST_ID, KEY_DATE, KEY_ELAPSED_TIME,
                KEY_ZNO_BALL};
        String selection;
        String ordering = KEY_DATE + " DESC";
        String SESSION = ZNOApplication.getInstance().getResources().getString(R.string.session_text);

        recRows = db.query(TABLE_USER_ANSWERS, projection, null, null, null, null, ordering);
        if (recRows.moveToFirst()) {
            Cursor lesson;
            Cursor test;
            PassedTest passedTest;

            int lessonId;
            String testName;

            do {
                passedTest = new PassedTest();
                passedTest.id = recRows.getInt(recRows.getColumnIndex(KEY_ID));
                lessonId = recRows.getInt(recRows.getColumnIndex(KEY_LESSON_ID));
                projection = new String[]{KEY_NAME};
                selection = KEY_ID + "=" + lessonId;
                lesson = db.query(TABLE_LESSONS, projection, selection, null, null, null, null);

                if (lesson.moveToFirst()) {
                    passedTest.lessonName = lesson.getString(lesson.getColumnIndex(KEY_NAME));
                } else {
                    continue;
                }

                passedTest.testId = recRows.getInt(recRows.getColumnIndex(KEY_TEST_ID));
                projection = new String[]{KEY_NAME, KEY_YEAR};
                selection = KEY_ID + "=" + passedTest.testId;
                test = db.query(TABLE_TESTS, projection, selection, null, null, null, null);

                if (test.moveToFirst()) {
                    testName = test.getString(test.getColumnIndex(KEY_NAME));
                    passedTest.year = test.getInt(test.getColumnIndex(KEY_YEAR));
                    if (testName.contains("(I " + SESSION + ")")) {
                        passedTest.session = 1;
                    } else if (testName.contains("(II " + SESSION + ")")) {
                        passedTest.session = 2;
                    } else {
                        passedTest.session = 0;
                    }
                } else {
                    continue;
                }

                passedTest.date = recRows.getLong(recRows.getColumnIndex(KEY_DATE));
                passedTest.elapsedTime = recRows.getLong(recRows.getColumnIndex(KEY_ELAPSED_TIME));
                passedTest.znoBall = recRows.getFloat(recRows.getColumnIndex(KEY_ZNO_BALL));

                passedTests.add(passedTest);
            } while (recRows.moveToNext());
        }

        return passedTests;
    }

    private JSONArray loadFromResources(int id) throws IOException, JSONException {
        InputStream is = ZNOApplication.getInstance().getResources().openRawResource(id);
        byte[] buf = new byte[is.available()];
        is.read(buf);
        is.close();

        return new JSONArray(new String(buf));
    }
}
