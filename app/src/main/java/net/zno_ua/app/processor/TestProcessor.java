package net.zno_ua.app.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import net.zno_ua.app.provider.Query;
import net.zno_ua.app.rest.APIClient;
import net.zno_ua.app.rest.ServiceGenerator;
import net.zno_ua.app.rest.model.Objects;
import net.zno_ua.app.rest.model.Point;
import net.zno_ua.app.rest.model.Question;
import net.zno_ua.app.rest.model.TestInfo;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static net.zno_ua.app.provider.ZNOContract.Test;
import static net.zno_ua.app.provider.ZNOContract.Test.NO_LOADED_DATA;
import static net.zno_ua.app.provider.ZNOContract.Test.buildTestItemUri;


/**
 * @author Vojko Vladimir vojkovladimir@gmail.com
 * @since 16.03.16.
 */
public class TestProcessor extends Processor<TestInfo> {

    private final APIClient mApiClient;
    private final QuestionProcessor mQuestionProcessor;
    private final PointProcessor mPointProcessor;

    public TestProcessor(@NonNull Context context) {
        super(context);
        mApiClient = ServiceGenerator.create();
        mQuestionProcessor = new QuestionProcessor(context);
        mPointProcessor = new PointProcessor(context);
    }

    @WorkerThread
    public void get(long testId) {
        final Call<TestInfo> testInfoCall = mApiClient.getTestInfo(testId);
        try {
            final Response<TestInfo> testInfoResponse = testInfoCall.execute();
            if (testInfoResponse.isSuccess()) {
                final TestInfo testInfo = testInfoResponse.body();
                final Cursor c = query(testInfo);
                if (c != null) {
                    final int result = c.moveToFirst() ? c.getInt(Query.Test.Column.RESULT)
                            : Test.NO_LOADED_DATA;
                    final boolean isQuestionsLoaded = Test.testResult(result, Test.QUESTIONS_LOADED);
                    final boolean isImagesLoaded = Test.testResult(result, Test.IMAGES_LOADED);
                    if (!(isQuestionsLoaded && isImagesLoaded)) {
                        getQuestions(testId, !isQuestionsLoaded, !isImagesLoaded);
                    }

                    final boolean isPointsLoaded = Test.testResult(result, Test.POINTS_LOADED);
                    if (!isPointsLoaded) {
                        getPoints(testId);
                    }
                    if (!c.isClosed()) c.close();
                }
            }
        } catch (IOException ignored) {
        }
    }

    @WorkerThread
    public void delete(long testId) {
        mQuestionProcessor.delete(testId);
        mPointProcessor.delete(testId);
        updateTestResult(testId, NO_LOADED_DATA, false);
    }

    private void getQuestions(long testId, boolean updateQuestions, boolean downloadImages)
            throws IOException {
        final Call<Objects<Question>> questionsCall = mApiClient.getTestQuestions(testId);
        final Response<Objects<Question>> questionsResponse = questionsCall.execute();
        if (questionsResponse.isSuccess()) {
            mQuestionProcessor.prepare(testId, updateQuestions, downloadImages);
            mQuestionProcessor.process(questionsResponse.body().get());

            updateTestResult(testId, Test.QUESTIONS_LOADED, true);
        }
    }

    private void getPoints(long testId) throws IOException {
        final Call<Objects<Point>> pointsCall = mApiClient.getTestPoints(testId);
        final Response<Objects<Point>> pointsResponse = pointsCall.execute();
        if (pointsResponse.isSuccess()) {
            mPointProcessor.process(pointsResponse.body().get());

            updateTestResult(testId, Test.POINTS_LOADED, true);
        }
    }

    @Override
    protected void insert(@NonNull TestInfo testInfo) {
        getContentResolver().insert(Query.Test.URI, createContentValuesForInsert(testInfo));
    }

    @Override
    protected void update(@NonNull TestInfo testInfo, @NonNull Cursor cursor) {
        if (cursor.getInt(Query.Test.Column.RESULT) != NO_LOADED_DATA) {
            final Call<Objects<Question>> call = mApiClient.getTestQuestions(testInfo.getId());
            try {
                final Response<Objects<Question>> response = call.execute();
                if (response.isSuccess()) {
                    mQuestionProcessor.prepare(testInfo.getId(), true, true);
                    mQuestionProcessor.process(response.body().get());
                    finishUpdate(testInfo);
                }
            } catch (IOException ignored) {
            }
        } else {
            finishUpdate(testInfo);
        }
    }

    private void finishUpdate(@NonNull TestInfo testInfo) {
        final String selection = Query.Test.SELECTION_ID;
        final String[] selectionArgs = Query.selectionArgs(testInfo.getId());
        final ContentValues values = createContentValuesForUpdate(testInfo);
        getContentResolver().update(Query.Test.URI, values, selection, selectionArgs);
    }

    @Override
    protected Cursor query(@NonNull TestInfo testInfo) {
        return getContentResolver().query(buildTestItemUri(testInfo.getId()), Query.Test.PROJECTION, null, null, null);
    }

    @Override
    protected void cleanUp(@Nullable List<TestInfo> data) {
        final String selection = Query.Test.SELECTION_NOT_IN + createPlaceHolders(data);
        final String[] selectionArgs = createSelectionArgs(data);
        getContentResolver().delete(Query.Test.URI, selection, selectionArgs);
    }

    @Override
    protected boolean shouldUpdate(@NonNull TestInfo testInfo, @NonNull Cursor cursor) {
        return testInfo.getLastUpdate() > cursor.getLong(Query.Test.Column.LAST_UPDATE);
    }

    @Override
    public ContentValues createContentValuesForInsert(TestInfo testInfo) {
        final ContentValues values = createContentValuesForUpdate(testInfo);
        values.put(Test._ID, testInfo.getId());
        return values;
    }

    @Override
    public ContentValues createContentValuesForUpdate(TestInfo testInfo) {
        final ContentValues values = new ContentValues();
        values.put(Test.SUBJECT_ID, testInfo.getSubjectId());
        values.put(Test.QUESTIONS_COUNT, testInfo.getQuestionsCount());
        values.put(Test.TYPE, testInfo.getType());
        values.put(Test.SESSION, testInfo.getSession());
        values.put(Test.LEVEL, testInfo.getLevel());
        values.put(Test.TIME, testInfo.getTime());
        values.put(Test.YEAR, testInfo.getYear());
        values.put(Test.LAST_UPDATE, testInfo.getLastUpdate());
        return values;
    }

    @Override
    protected String createSelectionArg(@NonNull TestInfo testInfo) {
        return String.valueOf(testInfo.getId());
    }

    @WorkerThread
    public static void updateTestResult(ContentResolver contentResolver, long testId, int result, boolean multiply) {
        if (multiply) {
            final Cursor cursor = contentResolver.query(buildTestItemUri(testId),
                    new String[]{Test.RESULT}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    result |= cursor.getInt(0);
                }
                cursor.close();
            }
        }

        final ContentValues values = new ContentValues();
        values.put(Test.RESULT, result);
        contentResolver.update(buildTestItemUri(testId), values, null, null);
    }

    @WorkerThread
    private void updateTestResult(long testId, int result, boolean multiply) {
        updateTestResult(getContentResolver(), testId, result, multiply);
    }
}
