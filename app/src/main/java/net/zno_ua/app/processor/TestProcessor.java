package net.zno_ua.app.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import net.zno_ua.app.FileManager;
import net.zno_ua.app.activity.ViewImageActivity;
import net.zno_ua.app.provider.ZNOContract;
import net.zno_ua.app.rest.APIClient;
import net.zno_ua.app.rest.model.Objects;
import net.zno_ua.app.rest.model.Point;
import net.zno_ua.app.rest.model.Question;
import net.zno_ua.app.rest.model.TestInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Response;

import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.Test;
import static net.zno_ua.app.provider.ZNOContract.Test.NO_LOADED_DATA;
import static net.zno_ua.app.provider.ZNOContract.Test.buildTestItemUri;

/**
 * @author Vojko Vladimir
 */
public class TestProcessor {
    private static final String HREF = "<a href=\"";
    private static final String HREF_REPLACEMENT = "<a href=\"" + ViewImageActivity.DATA_SCHEMA
            + "://?src=";
    private static final String FORMULAS_PATH = "/formulas";
    private static final String IMAGE = "IMAGE";
    private static final String IMAGE_REGEX = "\\$" + IMAGE + "\\$";
    private static final String IMAGE_SRC_FORMAT = "<img src=\"/%d" + FORMULAS_PATH + "/%s\"/>";
    private static final String HR = "<hr>";
    private static final String BR = "<br>";

    private final FileManager mFileManager;
    private final APIClient mApiClient;
    private final ContentResolver mContentResolver;

    public TestProcessor(Context context, APIClient apiClient) {
        mContentResolver = context.getContentResolver();
        mFileManager = new FileManager(context);
        mApiClient = apiClient;
    }

    public void get(long testId) {
        get(testId, true);
    }

    public void get(long testId, boolean checkLastUpdate) {
        updateTestStatus(testId, Test.STATUS_DOWNLOADING);
        try {
            boolean isDataOutdated = true;
            final int result = getResult(testId);
            TestInfo testInfo = null;
            /*
            * Check test last update if necessary.
            * */
            if (checkLastUpdate) {
                final Response<TestInfo> testInfoResponse = mApiClient.getTestInfo(testId).execute();
                if (testInfoResponse.isSuccess()) {
                    testInfo = testInfoResponse.body();
                    isDataOutdated = isDataOutdated(testInfo);
                }
            }

            final boolean isQuestionsLoaded = Test.testResult(result, Test.QUESTIONS_LOADED);
            final boolean isImagesLoaded = Test.testResult(result, Test.IMAGES_LOADED);
            if (!isQuestionsLoaded || !isImagesLoaded || isDataOutdated) {
                final Response<Objects<Question>> objectsResponse =
                        mApiClient.getTestQuestions(testId).execute();
                if (objectsResponse.isSuccess()) {
                    final List<Question> questions = objectsResponse.body().objects;
                    boolean isDownloadImagesSuccess = true;
                    for (Question question : questions) {
                        /*
                        * If the questions are not downloaded or downloaded, but the test is
                        * outdated, download the questions.
                        * */
                        if ((!isQuestionsLoaded || isDataOutdated) && question.point != 0) {
                            saveQuestion(testId, question);
                        }

                        /*
                        * If the images are not downloaded or downloaded, but the test is outdated,
                        * download the images.
                        * */
                        if (!isImagesLoaded || isDataOutdated) {
                            isDownloadImagesSuccess &= downloadQuestionImages(testId, question);
                        }
                    }
                    updateTestResult(testId, Test.QUESTIONS_LOADED);
                    if (isDownloadImagesSuccess) {
                        updateTestResult(testId, Test.IMAGES_LOADED);
                    }
                }
            }

            /*
            * If the points are not downloaded or downloaded, but the test is outdated, download the
            * points.
            * */
            if (!Test.testResult(result, Test.POINTS_LOADED) || isDataOutdated) {
                final Response<Objects<Point>> objectsResponse =
                        mApiClient.getTestPoints(testId).execute();
                if (objectsResponse.isSuccess()) {
                    for (Point point : objectsResponse.body().objects) {
                        savePoint(testId, point);
                    }
                    updateTestResult(testId, Test.POINTS_LOADED);
                }
            }

            if (testInfo != null) {
                updateTestInfo(testInfo);
            }
        } catch (IOException ignored) {
        } finally {
            updateTestStatus(testId, Test.STATUS_IDLE);
        }
    }

    private boolean downloadQuestionImages(long testId, Question question) throws IOException {
        boolean imagesLoaded = true;
        String localPath = "/" + testId;
        if (!TextUtils.isEmpty(question.imagesRelativeUrl)) {
            imagesLoaded = downloadAndSaveImage(localPath, question.imagesRelativeUrl, question.images);
        }

        if (!TextUtils.isEmpty(question.imagesFormulasUrl)) {
            localPath += FORMULAS_PATH;
            imagesLoaded &= downloadAndSaveImage(localPath, question.imagesFormulasUrl, question.imagesFormulas);
        }

        return imagesLoaded;
    }

    private boolean downloadAndSaveImage(String localPath, String relativeUrl, String[] images)
            throws IOException {
        boolean imagesLoaded = true;
        Response<ResponseBody> imageResponse;
        InputStream inputStream;
        for (String name : images) {
            if (!mFileManager.isFileExists(localPath, name)) {
                imageResponse = mApiClient.getImage(relativeUrl, name).execute();
                if (imageResponse.isSuccess()) {
                    inputStream = imageResponse.body().byteStream();
                    imagesLoaded &= mFileManager.saveFile(localPath, name, inputStream);
                }
            }
        }
        return imagesLoaded;
    }

    private int getResult(long testId) {
        final Cursor cursor = mContentResolver.query(buildTestItemUri(testId),
                new String[]{Test.RESULT}, null, null, null);
        int result = Test.NO_LOADED_DATA;
        if (cursor != null) {
            result = cursor.moveToFirst() ? cursor.getInt(0) : Test.NO_LOADED_DATA;
            cursor.close();
        }
        return result;
    }

    private boolean isDataOutdated(TestInfo testInfo) {
        final Cursor cursor = mContentResolver.query(buildTestItemUri(testInfo.id),
                new String[]{Test.LAST_UPDATE}, null, null, null);
        boolean isDataOutdated = true;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                isDataOutdated = testInfo.lastUpdate != cursor.getLong(0);
            }
            cursor.close();
        }
        return isDataOutdated;
    }

    public void delete(long testId) {
        updateTestStatus(testId, Test.STATUS_DELETING);
        mFileManager.deleteTestDirectory(testId);

        mContentResolver.delete(ZNOContract.Point.CONTENT_URI, ZNOContract.Point.TEST_ID
                + " =?", new String[]{valueOf(testId)});
        mContentResolver.delete(ZNOContract.Question.CONTENT_URI, ZNOContract.Question.TEST_ID
                + " =?", new String[]{valueOf(testId)});

        final ContentValues values = new ContentValues();
        values.put(Test.RESULT, NO_LOADED_DATA);
        mContentResolver.update(buildTestItemUri(testId), values, null, null);
        updateTestStatus(testId, Test.STATUS_IDLE);
    }

    private void saveQuestion(long testId, Question question) {
        prepareQuestion(testId, question);
        final ContentValues values = new ContentValues();
        values.put(ZNOContract.Question.TEST_ID, testId);
        values.put(ZNOContract.Question.POSITION_ON_TEST, question.positionOnTest);
        values.put(ZNOContract.Question.TYPE, question.type);
        values.put(ZNOContract.Question.TEXT, question.text);
        values.put(ZNOContract.Question.ADDITIONAL_TEXT, question.additionalText);
        values.put(ZNOContract.Question.ANSWERS, cleanAnswers(question.answers));
        values.put(ZNOContract.Question.CORRECT_ANSWER, question.correctAnswer);
        values.put(ZNOContract.Question.POINT, question.point);

        final int rowsUpdated = mContentResolver.update(ZNOContract.Question.CONTENT_URI,
                values, ZNOContract.Question._ID + "=?", new String[]{String.valueOf(question.id)});
        if (0 == rowsUpdated) {
            values.put(ZNOContract.Question._ID, question.id);
            mContentResolver.insert(ZNOContract.Question.CONTENT_URI, values);
        }
    }

    private void prepareQuestion(long testId, Question question) {
        final String localPath = "/" + testId;
        if (question.imagesRelativeUrl != null) {
            question.text = question.text.replace(question.imagesRelativeUrl, localPath);
            question.answers = question.answers.replace(question.imagesRelativeUrl, localPath);
        }

        String imageName;
        String imageSrc;
        if (question.imagesFormulasUrl != null && question.imagesFormulas != null) {
            for (int position = 0; position < question.imagesFormulas.length; position++) {
                imageName = question.imagesFormulas[position];
                imageSrc = String.format(Locale.US, IMAGE_SRC_FORMAT, testId, imageName);
                if (question.text.contains(IMAGE)) {
                    question.text = question.text.replaceFirst(IMAGE_REGEX, imageSrc);
                } else if (question.answers.contains(IMAGE)) {
                    question.answers = question.answers.replaceFirst(IMAGE_REGEX, imageSrc);
                }
            }
        }
        question.text = question.text.replace(HREF, HREF_REPLACEMENT).replace(HR, BR);
    }

    private static String cleanAnswers(String answers) {
        return answers.replaceFirst("^.*?(\\.\\s|\\s)", "")
                .replaceAll("(\r\n|\n).*?(\\.\\s|\\s)", "\r\n");
    }

    private void savePoint(long testId, Point point) {
        final ContentValues values = new ContentValues();
        values.put(ZNOContract.Point.TEST_ID, testId);
        values.put(ZNOContract.Point.TEST_POINT, point.testPoint);
        values.put(ZNOContract.Point.RATING_POINT, point.ratingPoint);

        final int rowsUpdated = mContentResolver.update(ZNOContract.Point.CONTENT_URI, values,
                ZNOContract.Point.TEST_ID + "=? AND " + ZNOContract.Point.TEST_POINT + "=?",
                new String[]{valueOf(testId), valueOf(point.testPoint)});
        if (rowsUpdated == 0) {
            mContentResolver.insert(ZNOContract.Point.CONTENT_URI, values);
        }
    }

    private void updateTestStatus(long testId, int status) {
        final ContentValues values = new ContentValues();
        values.put(Test.STATUS, status);
        mContentResolver.update(buildTestItemUri(testId), values, null, null);
    }

    private void updateTestResult(long testId, int result) {
        final Cursor cursor = mContentResolver.query(buildTestItemUri(testId),
                new String[]{Test.RESULT}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result |= cursor.getInt(0);
            }
            cursor.close();
        }

        final ContentValues values = new ContentValues();
        values.put(Test.RESULT, result);
        mContentResolver.update(buildTestItemUri(testId), values, null, null);
    }

    private void updateTestInfo(@NonNull TestInfo testInfo) {
        final ContentValues values = new ContentValues();
        values.put(Test.SUBJECT_ID, testInfo.subjectId);
        values.put(Test.QUESTIONS_COUNT, testInfo.questionsCount);
        values.put(Test.TYPE, testInfo.type);
        values.put(Test.SESSION, testInfo.session);
        values.put(Test.LEVEL, testInfo.level);
        values.put(Test.TIME, testInfo.time);
        values.put(Test.YEAR, testInfo.year);
        values.put(Test.LAST_UPDATE, testInfo.lastUpdate);
        mContentResolver.update(buildTestItemUri(testInfo.id), values, null, null);
    }

}
