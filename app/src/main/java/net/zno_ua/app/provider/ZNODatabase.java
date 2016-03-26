package net.zno_ua.app.provider;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import net.zno_ua.app.util.IOUtils;

import static net.zno_ua.app.provider.ZNOContract.Answer;
import static net.zno_ua.app.provider.ZNOContract.Question;
import static net.zno_ua.app.provider.ZNOContract.Subject;
import static net.zno_ua.app.provider.ZNOContract.Test;
import static net.zno_ua.app.provider.ZNOContract.TestingColumns;

/**
 * @author Vojko Vladimir
 */
public class ZNODatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "ZNO.db";
    private static final int DATABASE_VERSION = 2;

    interface Tables {
        String SUBJECT = "subject";
        String TEST = "test";
        String QUESTION = "question";
        String TESTING = "testing";
        String ANSWER = "answer";
        String POINT = "point";
        String QUESTION_JOIN_ANSWER = QUESTION + " LEFT JOIN " + ANSWER + " ON "
                + QUESTION + "." + Question._ID + " = " + ANSWER + "." + Answer.QUESTION_ID
                + " AND " + Answer.TESTING_ID + " = ?";
        String TESTING_RESULT = TESTING + " INNER JOIN " + TEST + " ON " + TEST + "." + Test._ID
                + "=" + TESTING + "." + TestingColumns.TEST_ID
                + " INNER JOIN " + SUBJECT + " ON " + SUBJECT + "." + Subject._ID + "="
                + TEST + "." + Test.SUBJECT_ID;
        String TEST_UPDATE = "test_update";
    }

    public ZNODatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade(DATABASE_VERSION);
        IOUtils.cleanOldImagesDir(context);
    }

}
