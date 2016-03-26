package net.zno_ua.app.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.util.IOUtils;
import net.zno_ua.app.ZNOApplication;
import net.zno_ua.app.helper.PreferencesHelper;
import net.zno_ua.app.provider.Query;
import net.zno_ua.app.provider.ZNOContract;
import net.zno_ua.app.rest.APIClient;
import net.zno_ua.app.rest.APIServiceGenerator;
import net.zno_ua.app.rest.model.Objects;
import net.zno_ua.app.rest.model.Point;
import net.zno_ua.app.rest.model.Question;
import net.zno_ua.app.rest.model.TestInfo;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static net.zno_ua.app.provider.Query.TestUpdate;
import static net.zno_ua.app.provider.Query.selectionArgs;
import static net.zno_ua.app.provider.ZNOContract.Test;
import static net.zno_ua.app.provider.ZNOContract.Test.NO_LOADED_DATA;
import static net.zno_ua.app.provider.ZNOContract.Test.STATUS_IDLE;
import static net.zno_ua.app.provider.ZNOContract.Test.STATUS_UPDATING;
import static net.zno_ua.app.provider.ZNOContract.Test.buildTestItemUri;


/**
 * @author Vojko Vladimir vojkovladimir@gmail.com
 * @since 16.03.16.
 */
public class TestProcessor extends Processor<TestInfo> {

    private final APIClient mApiClient;
    private final PointProcessor mPointProcessor;

    public TestProcessor(@NonNull Context context) {
        super(context);
        mApiClient = APIServiceGenerator.getAPIClient();
        mPointProcessor = new PointProcessor(context);
    }

    @WorkerThread
    public void get(long testId) {
        get(testId, false);
    }

    @WorkerThread
    public void update(long testId) {
        ZNOApplication.log("--UpdateTest: #" + testId + " " + canBeUpdated(testId));
        if (canBeUpdated(testId)) {
            get(testId, true);
        } else {
            if (getStatus(testId) == Test.STATUS_IDLE) {
                requestUpdate(testId);
            }
        }
    }

    @WorkerThread
    private void get(long testId, boolean onlyUpdate) {
        if (BuildConfig.DEBUG) {
            ZNOApplication.log("--GetTest: #" + testId + (onlyUpdate ? " update" : " download"));
        }
        final Call<TestInfo> testInfoCall = mApiClient.getTestInfo(testId);
        try {
            final Response<TestInfo> testInfoResponse = testInfoCall.execute();
            if (testInfoResponse.isSuccess()) {
                final TestInfo testInfo = testInfoResponse.body();
                final Cursor c = query(testInfo);
                if (c != null) {
                    final boolean isQuestionsLoaded;
                    final boolean isImagesLoaded;
                    final boolean isPointsLoaded;
                    if (c.moveToFirst()) {
                        final int result = c.getInt(Query.Test.Column.RESULT);
                        isQuestionsLoaded = Test.testResult(result, Test.QUESTIONS_LOADED);
                        isImagesLoaded = Test.testResult(result, Test.IMAGES_LOADED);
                        isPointsLoaded = Test.testResult(result, Test.POINTS_LOADED);
                    } else {
                        isQuestionsLoaded = false;
                        isImagesLoaded = false;
                        isPointsLoaded = false;
                    }

                    if (onlyUpdate && (isQuestionsLoaded || isImagesLoaded) || !onlyUpdate) {
                        getQuestions(testId, !isQuestionsLoaded, !isImagesLoaded);
                    }

                    if (onlyUpdate && isPointsLoaded || !onlyUpdate) {
                        getPoints(testId);
                    }

                    c.close();
                } else {
                    insert(testInfo);
                    if (!onlyUpdate) {
                        getQuestions(testId, true, true);
                        getPoints(testId);
                    }
                }
                removeFromRequestedUpdates(testId);
            } else {
                if (BuildConfig.DEBUG) {
                    ZNOApplication.log("--GetTestInfo: #" + testId + " failed");
                }
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                ZNOApplication.log("--GetTest: #" + testId + (onlyUpdate ? " update" : " get") + " ex: " + e.toString());
            }
        }
    }

    private void removeFromRequestedUpdates(long testId) {
        Log.d("Logs", "remove from request " + testId);
        getContentResolver().delete(TestUpdate.URI, TestUpdate.SELECTION, selectionArgs(testId));
    }

    @WorkerThread
    public void delete(long testId) {
        new QuestionProcessor(getContext(), testId).delete(testId);
        mPointProcessor.delete(testId);
        updateTestResult(testId, NO_LOADED_DATA, false);
    }

    private void getQuestions(long testId, boolean updateQuestions, boolean downloadImages)
            throws IOException {
        final Call<Objects<Question>> questionsCall = mApiClient.getTestQuestions(testId);
        final Response<Objects<Question>> questionsResponse = questionsCall.execute();
        if (questionsResponse.isSuccess()) {
            final QuestionProcessor questionProcessor = new QuestionProcessor(getContext(), testId);
            questionProcessor.prepare(updateQuestions, downloadImages);
            questionProcessor.process(questionsResponse.body().get());
            updateTestResult(testId, Test.QUESTIONS_LOADED, true);
        } else {
            if (BuildConfig.DEBUG) {
                ZNOApplication.log("--GetTestQuestions: #" + testId + " failed");
            }
        }
    }

    private void getPoints(long testId) throws IOException {
        final Call<Objects<Point>> pointsCall = mApiClient.getTestPoints(testId);
        final Response<Objects<Point>> pointsResponse = pointsCall.execute();
        if (pointsResponse.isSuccess()) {
            mPointProcessor.process(pointsResponse.body().get());
            updateTestResult(testId, Test.POINTS_LOADED, true);
        } else {
            if (BuildConfig.DEBUG) {
                ZNOApplication.log("--GetTestPoints: #" + testId + " failed");
            }
        }
    }

    @Override
    protected void insert(@NonNull TestInfo testInfo) {
        getContentResolver().insert(Query.Test.URI, createContentValuesForInsert(testInfo));
        removeFromRequestedUpdates(testInfo.getId());
    }

    @Override
    protected void update(@NonNull TestInfo testInfo, @NonNull Cursor cursor) {
        if (cursor.getInt(Query.Test.Column.RESULT) != NO_LOADED_DATA) {
            updateTestStatus(testInfo.getId(), STATUS_UPDATING);
            final Call<Objects<Question>> call = mApiClient.getTestQuestions(testInfo.getId());
            try {
                final Response<Objects<Question>> response = call.execute();
                if (response.isSuccess()) {
                    final QuestionProcessor questionProcessor =
                            new QuestionProcessor(getContext(), testInfo.getId());
                    questionProcessor.prepare(true, true);
                    questionProcessor.process(response.body().get());
                    finishUpdate(testInfo);
                }
                removeFromRequestedUpdates(testInfo.getId());
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    ZNOApplication.log("--GetQuestions: #" + testInfo.getId() + " ex: " + e.toString());
                }
            }
            updateTestStatus(testInfo.getId(), STATUS_IDLE);
        } else {
            finishUpdate(testInfo);
        }
    }

    private void finishUpdate(@NonNull TestInfo testInfo) {
        final String selection = Query.Test.SELECTION_ID;
        final String[] selectionArgs = Query.selectionArgs(testInfo.getId());
        final ContentValues values = createContentValuesForUpdate(testInfo);
        getContentResolver().update(Query.Test.URI, values, selection, selectionArgs);
        removeFromRequestedUpdates(testInfo.getId());
    }

    @Override
    protected Cursor query(@NonNull TestInfo testInfo) {
        return query(testInfo.getId());
    }

    protected Cursor query(long testId) {
        return getContentResolver().query(buildTestItemUri(testId), Query.Test.PROJECTION, null,
                null, null);
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
        values.put(Test.STATUS, Test.STATUS_IDLE);
        values.put(Test.RESULT, Test.NO_LOADED_DATA);
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
    public static void updateTestResult(ContentResolver contentResolver, long testId, int result,
                                        boolean multiply) {
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
    public void updateTestStatus(long testId, int status) {
        final ContentValues values = new ContentValues(1);
        values.put(ZNOContract.Test.STATUS, status);
        getContentResolver().update(buildTestItemUri(testId), values, null, null);
    }

    @WorkerThread
    private void updateTestResult(long testId, int result, boolean multiply) {
        updateTestResult(getContentResolver(), testId, result, multiply);
    }

    @WorkerThread
    public boolean canBeUpdated(long testId) {
        return (getStatus(testId) == Test.STATUS_IDLE || getStatus(testId) == Test.STATUS_UPDATING)
                && !isTestPassing(testId);
    }

    @WorkerThread
    public int getStatus(long testId) {
        int status = Test.STATUS_IDLE;
        final Cursor c = query(testId);
        if (c != null) {
            if (c.moveToFirst()) {
                status = c.getInt(Query.Test.Column.STATUS);
            }
            c.close();
        }

        return status;
    }

    @WorkerThread
    private boolean isTestPassing(long testId) {
        boolean isPassing = false;
        final Cursor c = getContentResolver().query(Query.Testing.URI, Query.Testing.PROJECTION,
                Query.Testing.SELECTION_PASSING, selectionArgs(testId), null);
        if (c != null) {
            if (c.moveToFirst()) {
                isPassing = true;
            }
            c.close();
        }
        return isPassing;
    }

    @WorkerThread
    public void requestUpdate(long testId) {
        final Cursor c = getContentResolver().query(TestUpdate.URI, TestUpdate.PROJECTION,
                TestUpdate.SELECTION, selectionArgs(testId), null);
        boolean exists = false;
        if (c != null) {
            if (c.moveToFirst()) {
                exists = true;
            }
            c.close();
        }
        if (!exists) {
            final ContentValues values = new ContentValues(1);
            values.put(ZNOContract.TestUpdate.TEST_ID, testId);
            getContentResolver().insert(TestUpdate.URI, values);
        }
    }

    @WorkerThread
    public void checkForUpdates() {
        if (BuildConfig.DEBUG) {
            ZNOApplication.log("--CheckForUpdates started--");
        }
        final Call<Objects<TestInfo>> testsCall = APIServiceGenerator.getAPIClient().getTestsInfo();
        try {
            final Response<Objects<TestInfo>> testsResponse = testsCall.execute();
            if (testsResponse.isSuccess()) {
                process(testsResponse.body().get());
                PreferencesHelper.getInstance(getContext()).saveLastUpdateTime(System.currentTimeMillis());
            } else {
                if (BuildConfig.DEBUG) {
                    ZNOApplication.log("--CheckForUpdates request failed --");
                }
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                ZNOApplication.log("--CheckForUpdates ex: " + e.toString() + " --");
            }
        }
        if (BuildConfig.DEBUG) {
            ZNOApplication.log("--CheckForUpdates finished--");
        }
    }

    @WorkerThread
    public void cleanUp() {
        getContentResolver().delete(ZNOContract.Testing.CONTENT_URI, null, null);
        getContentResolver().delete(ZNOContract.Question.CONTENT_URI, null, null);
        getContentResolver().delete(ZNOContract.Point.CONTENT_URI, null, null);
        final ContentValues values = new ContentValues(1);
        values.put(Test.RESULT, Test.NO_LOADED_DATA);
        getContentResolver().update(Test.CONTENT_URI, values, null, null);
        final Cursor c = getContentResolver().query(Test.CONTENT_URI, new String[]{Test._ID}, null,
                null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    IOUtils.deleteTestDirectory(c.getLong(0));
                } while (c.moveToNext());
            }
            c.close();
        }

    }
}
