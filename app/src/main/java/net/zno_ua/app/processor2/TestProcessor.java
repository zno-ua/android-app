package net.zno_ua.app.processor2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.zno_ua.app.FileManager;
import net.zno_ua.app.provider.Query;
import net.zno_ua.app.provider.ZNOContract;
import net.zno_ua.app.rest.model.TestInfo;

import java.util.List;

import static java.lang.String.valueOf;
import static net.zno_ua.app.provider.ZNOContract.*;
import static net.zno_ua.app.provider.ZNOContract.Test.NO_LOADED_DATA;
import static net.zno_ua.app.provider.ZNOContract.Test.buildTestItemUri;


/**
 * @author Vojko Vladimir vojkovladimir@gmail.com
 * @since 16.03.16.
 */
public class TestProcessor extends Processor<TestInfo> {

    private final FileManager mFileManager;

    public TestProcessor(@NonNull Context context) {
        super(context);
        mFileManager = new FileManager(context);
    }

    @Override
    public void process(@Nullable List<TestInfo> data) {
        super.process(data);
    }

    public void delete(long testId) {
        updateTestStatus(testId, Test.STATUS_DELETING);
        mFileManager.deleteTestDirectory(testId);
        getContentResolver().delete(Point.CONTENT_URI, Point.TEST_ID + "=?", new String[]{valueOf(testId)});
        getContentResolver().delete(Question.CONTENT_URI, Question.TEST_ID + "=?", new String[]{valueOf(testId)});
        updateTestResult(testId, NO_LOADED_DATA);
        updateTestStatus(testId, Test.STATUS_IDLE);
    }

    @Override
    protected void insert(@NonNull TestInfo testInfo) {
        getContentResolver().insert(Query.Test.URI, createContentValuesForInsert(testInfo));
    }

    @Override
    protected void update(@NonNull TestInfo testInfo) {
        final String selection = Query.Test.SELECTION_ID;
        final String[] selectionArgs = Query.selectionArgs(testInfo.getId());
        final ContentValues values = createContentValuesForUpdate(testInfo);
        getContentResolver().update(Query.Test.URI, values, selection, selectionArgs);
    }

    @Override
    protected Cursor query(@NonNull TestInfo testInfo) {
        return getContentResolver().query(Query.Test.URI, Query.Test.PROJECTION, null, null, null);
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

    @NonNull
    @Override
    protected String createSelectionArg(@NonNull TestInfo testInfo) {
        return String.valueOf(testInfo.getId());
    }

    private void updateTestStatus(long testId, int status) {
        final ContentValues values = new ContentValues();
        values.put(Test.STATUS, status);
        getContentResolver().update(buildTestItemUri(testId), values, null, null);
    }

    private void updateTestResult(long testId, int result) {
        final Cursor cursor = getContentResolver().query(buildTestItemUri(testId),
                new String[]{Test.RESULT}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result |= cursor.getInt(0);
            }
            cursor.close();
        }

        final ContentValues values = new ContentValues();
        values.put(Test.RESULT, result);
        getContentResolver().update(buildTestItemUri(testId), values, null, null);
    }
}
