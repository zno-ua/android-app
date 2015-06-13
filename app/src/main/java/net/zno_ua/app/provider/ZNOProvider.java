package net.zno_ua.app.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import static net.zno_ua.app.provider.ZNOContract.Question;
import static net.zno_ua.app.provider.ZNOContract.Subject;
import static net.zno_ua.app.provider.ZNOContract.Test;
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
    }

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ZNOContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "subject" , URI_CODE.SUBJECT);
        matcher.addURI(authority, "subject/#" , URI_CODE.SUBJECT_ID);

        matcher.addURI(authority, "test" , URI_CODE.TEST);
        matcher.addURI(authority, "test/#" , URI_CODE.TEST_ID);

        matcher.addURI(authority, "question" , URI_CODE.QUESTION);
        matcher.addURI(authority, "question/#" , URI_CODE.QUESTION_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new ZNODatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        final int match = sUriMatcher.match(uri);

        switch (match) {
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
            case URI_CODE.QUESTION_ID:
                queryBuilder.setTables(Tables.QUESTION);
                queryBuilder.appendWhere(Question._ID + EQ + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        Cursor cursor = queryBuilder.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case URI_CODE.SUBJECT:
                return Subject.CONTENT_TYPE;
            case URI_CODE.SUBJECT_ID:
                return Subject.CONTENT_ITEM_TYPE;
            case URI_CODE.TEST:
                return Test.CONTENT_TYPE;
            case URI_CODE.TEST_ID:
                return Test.CONTENT_ITEM_TYPE;
            case URI_CODE.QUESTION:
                return Question.CONTENT_TYPE;
            case URI_CODE.QUESTION_ID:
                return Question.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
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
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        id = db.insertOrThrow(table, null, values);

        notifyChange(uri);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
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
                table = Tables.SUBJECT;
                where = Test._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.QUESTION:
                table = Tables.QUESTION;
                break;
            case URI_CODE.QUESTION_ID:
                table = Tables.SUBJECT;
                where = Question._ID + EQ + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        deleteCount = db.delete(table, appendSelection(where, selection), selectionArgs);

        notifyChange(uri);

        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
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
                table = Tables.SUBJECT;
                where = Test._ID + EQ + uri.getLastPathSegment();
                break;
            case URI_CODE.QUESTION:
                table = Tables.QUESTION;
                break;
            case URI_CODE.QUESTION_ID:
                table = Tables.SUBJECT;
                where = Question._ID + EQ + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        updateCount = db.update(table, values, appendSelection(where, selection), selectionArgs);

        notifyChange(uri);

        return updateCount;
    }

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
