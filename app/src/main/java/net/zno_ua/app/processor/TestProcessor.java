package net.zno_ua.app.processor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;

import net.zno_ua.app.FileManager;
import net.zno_ua.app.rest.GetTestImage;
import net.zno_ua.app.rest.GetTestInfo;
import net.zno_ua.app.rest.GetTestPoints;
import net.zno_ua.app.rest.GetTestQuestions;
import net.zno_ua.app.activity.ViewImageActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.Point;
import static net.zno_ua.app.provider.ZNOContract.Question;
import static net.zno_ua.app.provider.ZNOContract.Test;
import static net.zno_ua.app.provider.ZNOContract.Test.NO_LOADED_DATA;
import static net.zno_ua.app.provider.ZNOContract.Test.buildTestItemUri;

/**
 * @author Vojko Vladimir
 */
public class TestProcessor extends Processor {
    private static final String TAG = "TestProcessor";

    private static final String HREF = "<a href=\"";
    private static final String HREF_REPLACEMENT =
            "<a href=\"" + ViewImageActivity.DATA_SCHEMA + "://?src=";

    private FileManager mFileManager;

    public TestProcessor(Context context) {
        super(context);
        mFileManager = new FileManager(context);
    }

    public void get(long testId) {
        get(testId, true);
    }

    public void get(long testId, boolean checkLastUpdate) {
        updateTestStatus(testId, Test.STATUS_DOWNLOADING);

        Cursor cursor;
        boolean isDataOutdated = true;

        cursor = getContentResolver()
                .query(buildTestItemUri(testId), new String[]{Test.RESULT}, null, null, null);

        final int result;
        if (cursor == null) {
            result = Test.NO_LOADED_DATA;
        } else {
            result = cursor.moveToFirst() ? cursor.getInt(0) : Test.NO_LOADED_DATA;
            cursor.close();
        }

        if (checkLastUpdate) {
            /*
            * Check test last update.
            * */
            try {
                JSONObject testInfo = new GetTestInfo(testId).getResponse();
                cursor = getContentResolver().query(buildTestItemUri(testId),
                        new String[]{Test.LAST_UPDATE, Test.RESULT}, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        isDataOutdated = testInfo.getLong(Keys.LAST_UPDATE) != cursor.getLong(0);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                LOG_E(e);
            }
        }

        /*
        * If the questions are not downloaded or downloaded, but the test is outdated, download the
        * questions.
        * */
        if (!Test.testResult(result, Test.QUESTIONS_LOADED) || isDataOutdated) {
            try {
                JSONObject response = new GetTestQuestions(testId).getResponse();
                saveQuestions(testId, response.getJSONArray(Keys.OBJECTS));
                updateTestResult(testId, Test.QUESTIONS_LOADED);
            } catch (Exception e) {
                LOG_E(e);
            }
        }

        cursor = getContentResolver().query(Question.CONTENT_URI,
                new String[]{Question.IMAGES_RELATIVE_URL, Question.IMAGES},
                Question.IMAGES_RELATIVE_URL + " IS NOT NULL AND " + Question.TEST_ID + " =?",
                new String[]{valueOf(testId)}, null);

        /*
        * If the images are not downloaded or downloaded, but the test is outdated, download the
        * images.
        * */
        if (cursor != null) {
            if (!Test.testResult(result, Test.IMAGES_LOADED) || isDataOutdated) {
                try {
                    boolean imagesLoaded = true;
                    if (cursor.moveToFirst() && cursor.getCount() != 0) {
                        do {
                            String path = cursor.getString(0);
                            JSONArray names = new JSONArray(cursor.getString(1));

                            Bitmap image;
                            String name;
                            for (int i = 0; i < names.length(); i++) {
                                name = names.getJSONObject(i).getString(Keys.NAME);

                                if (!mFileManager.isFileExists(path, name) || isDataOutdated) {
                                    image = new GetTestImage(path, name).getResponse();
                                    imagesLoaded &= mFileManager.saveBitmap(path, name, image);
                                }
                            }
                        } while (cursor.moveToNext() && imagesLoaded);
                    }
                    if (imagesLoaded)
                        updateTestResult(testId, Test.IMAGES_LOADED);
                } catch (Exception e) {
                    LOG_E(e);
                }
            }
            cursor.close();
        }

        /*
        * If the points are not downloaded or downloaded, but the test is outdated, download the
        * points.
        * */
        if (!Test.testResult(result, Test.POINTS_LOADED) || isDataOutdated) {
            try {
                JSONObject response = new GetTestPoints(testId).getResponse();
                savePoints(testId, response.getJSONArray(Keys.OBJECTS));
                updateTestResult(testId, Test.POINTS_LOADED);
            } catch (Exception e) {
                LOG_E(e);
            }
        }

        updateTestStatus(testId, Test.STATUS_IDLE);

    }

    public void delete(long testId) {
        updateTestStatus(testId, Test.STATUS_DELETING);
        Cursor cursor;

        /* Removing images */
        cursor = getContentResolver().query(Question.CONTENT_URI,
                new String[]{Question.IMAGES_RELATIVE_URL, Question.IMAGES},
                Question.IMAGES_RELATIVE_URL + " IS NOT NULL AND " + Question.TEST_ID + " =?",
                new String[]{valueOf(testId)}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst() && cursor.getCount() != 0) {
                    do {
                        String path = cursor.getString(0);
                        JSONArray names = new JSONArray(cursor.getString(1));

                        for (int i = 0; i < names.length(); i++) {
                            mFileManager.deleteFile(path, names.getJSONObject(i).getString(Keys.NAME));
                        }
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                LOG_E(e);
            }
            cursor.close();
        }

        getContentResolver()
                .delete(Point.CONTENT_URI, Point.TEST_ID + " =?", new String[]{valueOf(testId)});
        getContentResolver()
                .delete(Question.CONTENT_URI, Question.TEST_ID + " =?", new String[]{valueOf(testId)});

        ContentValues values = new ContentValues();
        values.put(Test.RESULT, NO_LOADED_DATA);
        getContentResolver().update(buildTestItemUri(testId), values, null, null);

        updateTestStatus(testId, Test.STATUS_IDLE);
    }

    private void saveQuestions(long testId, JSONArray questions) throws JSONException {
        JSONObject question;

        for (int i = 0; i < questions.length(); i++) {
            question = questions.getJSONObject(i);
            if (!(question.getInt(Keys.BALLS) == 0 && question.getInt(Keys.BALLS) == 0))
                saveQuestion(testId, question);
        }
    }

    private void saveQuestion(long testId, JSONObject question) throws JSONException {
        ContentValues values = new ContentValues();

        values.put(Question.TEST_ID, testId);
        values.put(Question.POSITION_ON_TEST, question.getInt(Keys.ID_ON_TEST));
        values.put(Question.TYPE, question.getInt(Keys.TYPE_QUESTION));
        values.put(Question.TEXT, question.getString(Keys.QUESTION).replace(HREF, HREF_REPLACEMENT));
        values.put(Question.ADDITIONAL_TEXT, question.optString(Keys.PARENT_QUESTION, null));
        values.put(Question.ANSWERS, cleanAnswers(question.getString(Keys.ANSWERS)));
        values.put(Question.CORRECT_ANSWER, question.getString(Keys.CORRECT_ANSWER));
        values.put(Question.POINT, question.getInt(Keys.BALLS));
        values.put(Question.IMAGES, question.optString(Keys.IMAGES, null));
        values.put(Question.IMAGES_RELATIVE_URL, question.optString(Keys.IMAGES_RELATIVE_URL, null));

        int rowsUpdated = getContentResolver().update(Question.CONTENT_URI,
                values, Question._ID + "=?", new String[]{question.getString(Keys.ID)});
        if (0 == rowsUpdated) {
            values.put(Question._ID, question.getLong(Keys.ID));
            getContentResolver().insert(Question.CONTENT_URI, values);
        }
    }

    private static String cleanAnswers(String answers) {
        return answers.replaceFirst("^.*?(\\.\\s|\\s)", "")
                .replaceAll("(\r\n|\n).*?(\\.\\s|\\s)", "\r\n");
    }

    private void savePoints(long testId, JSONArray points) throws JSONException {
        for (int i = 0; i < points.length(); i++) {
            savePoint(testId, points.getJSONObject(i));
        }
    }

    private void savePoint(long testId, JSONObject point) throws JSONException {
        int testPoint = point.getInt(Keys.TEST_BALL);
        ContentValues values = new ContentValues();
        values.put(Point.TEST_ID, testId);
        values.put(Point.TEST_POINT, testPoint);
        values.put(Point.RATING_POINT, point.getDouble(Keys.ZNO_BALL));

        int rowsUpdated = getContentResolver().update(Point.CONTENT_URI, values,
                Point.TEST_ID + "=? AND " + Point.TEST_POINT + "=?",
                new String[]{valueOf(testId), valueOf(testPoint)});
        if (rowsUpdated == 0) {
            getContentResolver().insert(Point.CONTENT_URI, values);
        }
    }

    private void updateTestStatus(long testId, int status) {
        ContentValues values = new ContentValues();
        values.put(Test.STATUS, status);
        getContentResolver().update(buildTestItemUri(testId), values, null, null);
    }

    private void updateTestResult(long testId, int result) {
        Cursor cursor = getContentResolver()
                .query(buildTestItemUri(testId), new String[]{Test.RESULT}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result |= cursor.getInt(0);
            }
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(Test.RESULT, result);
        getContentResolver().update(buildTestItemUri(testId), values, null, null);
    }

    private static void LOG_E(Throwable throwable) {
        Log.e(TAG, "Error: ", throwable);
    }
}
