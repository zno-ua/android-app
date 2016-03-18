package net.zno_ua.app.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import static net.zno_ua.app.provider.ZNOContract.*;
import static net.zno_ua.app.provider.ZNODatabase.Tables;

/**
 * @author Vojko Vladimir
 */
public class ZNOProvider extends ContentProvider {

    private static final String EQ = " = ";

    private ZNODatabase mDatabaseHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private interface URI_CODE {
        int SUBJECT = 100;
        int SUBJECT_ID = 101;
        int TEST = 200;
        int TEST_ID = 201;
        int QUESTION = 300;
        int QUESTION_ID = 301;
        int QUESTION_AND_ANSWER = 302;
        int ANSWER = 400;
        int ANSWER_ID = 401;
        int TESTING = 500;
        int TESTING_ID = 501;
        int POINT = 600;
        int POINT_ID = 601;
        int TESTING_RESULT = 701;
        int TESTING_RESULT_ID = 702;
        int TEST_UPDATE = 800;
    }

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(CONTENT_AUTHORITY, PATH_SUBJECT, URI_CODE.SUBJECT);
        matcher.addURI(CONTENT_AUTHORITY, PATH_SUBJECT + "/#", URI_CODE.SUBJECT_ID);
        matcher.addURI(CONTENT_AUTHORITY, PATH_TEST, URI_CODE.TEST);
        matcher.addURI(CONTENT_AUTHORITY, PATH_TEST + "/#", URI_CODE.TEST_ID);
        matcher.addURI(CONTENT_AUTHORITY, PATH_QUESTION, URI_CODE.QUESTION);
        matcher.addURI(CONTENT_AUTHORITY, PATH_QUESTION + "/#", URI_CODE.QUESTION_ID);
        matcher.addURI(CONTENT_AUTHORITY, PATH_QUESTION_AND_ANSWER, URI_CODE.QUESTION_AND_ANSWER);
        matcher.addURI(CONTENT_AUTHORITY, PATH_ANSWER, URI_CODE.ANSWER);
        matcher.addURI(CONTENT_AUTHORITY, PATH_ANSWER + "/#", URI_CODE.ANSWER_ID);
        matcher.addURI(CONTENT_AUTHORITY, PATH_TESTING, URI_CODE.TESTING);
        matcher.addURI(CONTENT_AUTHORITY, PATH_TESTING + "/#", URI_CODE.TESTING_ID);
        matcher.addURI(CONTENT_AUTHORITY, PATH_POINT, URI_CODE.POINT);
        matcher.addURI(CONTENT_AUTHORITY, PATH_POINT + "/#", URI_CODE.POINT_ID);
        matcher.addURI(CONTENT_AUTHORITY, PATH_TESTING_RESULT, URI_CODE.TESTING_RESULT);
        matcher.addURI(CONTENT_AUTHORITY, PATH_TESTING_RESULT + "/#", URI_CODE.TESTING_RESULT_ID);
        matcher.addURI(CONTENT_AUTHORITY, PATH_TEST_UPDATE, URI_CODE.TEST_UPDATE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new ZNODatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case URI_CODE.SUBJECT:
                queryBuilder.setTables(Tables.SUBJECT);
                break;
            case URI_CODE.SUBJECT_ID:
                queryBuilder.setTables(Tables.SUBJECT);
                queryBuilder.appendWhere(Subject._ID + EQ + uri.getLastPathSegment());
                break;
            case URI_CODE.TEST:
                queryBuilder.setTables(Tables.TEST);
                break;
            case URI_CODE.TEST_ID:
                queryBuilder.setTables(Tables.TEST);
                queryBuilder.appendWhere(Test._ID + EQ + uri.getLastPathSegment());
                break;
            case URI_CODE.QUESTION:
                queryBuilder.setTables(Tables.QUESTION);
                break;
            case URI_CODE.QUESTION_AND_ANSWER:
                queryBuilder.setTables(Tables.QUESTION_JOIN_ANSWER);
                break;
            case URI_CODE.QUESTION_ID:
                queryBuilder.setTables(Tables.QUESTION);
                queryBuilder.appendWhere(Question._ID + EQ + uri.getLastPathSegment());
                break;
            case URI_CODE.ANSWER:
                queryBuilder.setTables(Tables.ANSWER);
                break;
            case URI_CODE.ANSWER_ID:
                queryBuilder.setTables(Tables.ANSWER);
                queryBuilder.appendWhere(Answer._ID + EQ + uri.getLastPathSegment());
                break;
            case URI_CODE.TESTING:
                queryBuilder.setTables(Tables.TESTING);
                break;
            case URI_CODE.TESTING_ID:
                queryBuilder.setTables(Tables.TESTING);
                queryBuilder.appendWhere(Testing._ID + EQ + uri.getLastPathSegment());
                break;
            case URI_CODE.POINT:
                queryBuilder.setTables(Tables.POINT);
                break;
            case URI_CODE.POINT_ID:
                queryBuilder.setTables(Tables.POINT);
                queryBuilder.appendWhere(Point._ID + EQ + uri.getLastPathSegment());
                break;
            case URI_CODE.TESTING_RESULT:
                queryBuilder.setTables(Tables.TESTING_RESULT);
                break;
            case URI_CODE.TESTING_RESULT_ID:
                queryBuilder.setTables(Tables.TESTING_RESULT);
                queryBuilder.appendWhere(BaseColumns._ID + EQ + uri.getLastPathSegment());
                break;
            case URI_CODE.TEST_UPDATE:
                queryBuilder.setTables(Tables.TEST_UPDATE);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        final Cursor cursor = queryBuilder.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        if (getContext() != null && getContext().getContentResolver() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String table;
        long id;

        switch (match) {
            case URI_CODE.SUBJECT:
                table = Tables.SUBJECT;
                break;
            case URI_CODE.TEST:
                table = Tables.TEST;
                break;
            case URI_CODE.QUESTION:
                table = Tables.QUESTION;
                break;
            case URI_CODE.ANSWER:
                table = Tables.ANSWER;
                break;
            case URI_CODE.TESTING:
                table = Tables.TESTING;
                break;
            case URI_CODE.POINT:
                table = Tables.POINT;
                break;
            case URI_CODE.TEST_UPDATE:
                table = Tables.TEST_UPDATE;
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        id = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        notifyChange(uri, match);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int deleteCount;
        String table;
        String where = null;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case URI_CODE.SUBJECT:
                table = Tables.SUBJECT;
                break;
            case URI_CODE.SUBJECT_ID:
                table = Tables.SUBJECT;
                where = Subject._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.TEST:
                table = Tables.TEST;
                break;
            case URI_CODE.TEST_ID:
                table = Tables.TEST;
                where = Test._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.QUESTION:
                table = Tables.QUESTION;
                break;
            case URI_CODE.QUESTION_ID:
                table = Tables.QUESTION;
                where = Question._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.ANSWER:
                table = Tables.ANSWER;
                break;
            case URI_CODE.ANSWER_ID:
                table = Tables.ANSWER;
                where = Answer._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.TESTING:
                table = Tables.TESTING;
                delete(Answer.CONTENT_URI, null, null);
                break;
            case URI_CODE.TESTING_ID:
                table = Tables.TESTING;
                where = Testing._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.POINT:
                table = Tables.POINT;
                break;
            case URI_CODE.POINT_ID:
                table = Tables.POINT;
                where = Point._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.TEST_UPDATE:
                table = Tables.TEST_UPDATE;
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        deleteCount = db.delete(table, appendSelection(where, selection), selectionArgs);

        notifyChange(uri, match);

        return deleteCount;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int updateCount;
        String table;
        String where = null;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case URI_CODE.SUBJECT:
                table = Tables.SUBJECT;
                break;
            case URI_CODE.SUBJECT_ID:
                table = Tables.SUBJECT;
                where = Subject._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.TEST:
                table = Tables.TEST;
                break;
            case URI_CODE.TEST_ID:
                table = Tables.TEST;
                where = Test._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.QUESTION:
                table = Tables.QUESTION;
                break;
            case URI_CODE.QUESTION_ID:
                table = Tables.QUESTION;
                where = Question._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.ANSWER:
                table = Tables.ANSWER;
                break;
            case URI_CODE.ANSWER_ID:
                table = Tables.ANSWER;
                where = Answer._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.TESTING:
                table = Tables.TESTING;
                break;
            case URI_CODE.TESTING_ID:
                table = Tables.TESTING;
                where = Testing._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.POINT:
                table = Tables.POINT;
                break;
            case URI_CODE.POINT_ID:
                table = Tables.POINT;
                where = Point._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.TEST_UPDATE:
                table = Tables.TEST_UPDATE;
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        updateCount = db.update(table, values, appendSelection(where, selection), selectionArgs);

        notifyChange(uri, match);

        return updateCount;
    }

    private void notifyChange(Uri uri, int match) {
        switch (match) {
            case URI_CODE.ANSWER:
                notifyChange(QuestionAndAnswer.CONTENT_URI);
                notifyChange(TestingResult.CONTENT_URI);
                break;
            case URI_CODE.TESTING:
                notifyChange(TestingResult.CONTENT_URI);
                break;
        }
        notifyChange(uri);
    }

    @SuppressWarnings("ConstantConditions")
    private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null, false);
    }

    private static String appendSelection(String where, String selection) {
        if (TextUtils.isEmpty(where)) {
            return selection;
        } else {
            if (!TextUtils.isEmpty(selection)) {
                where += " AND " + selection;
            }
            return where;
        }
    }
}
