package net.zno_ua.app.util;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import net.zno_ua.app.model.TestingInfo;

import static net.zno_ua.app.provider.Query.QuestionAndAnswer;
import static net.zno_ua.app.provider.Query.selectionArgs;

/**
 * @author vojkovladimir.
 */
public class TestingAnswersUtils {
    public static final int NO_POSITION = -1;

    public static int findFirstUnansweredQuestion(Context context, TestingInfo testingInfo) {
        final Cursor cursor = context.getContentResolver().query(QuestionAndAnswer.URI,
                QuestionAndAnswer.PROJECTION, QuestionAndAnswer.SELECTION,
                selectionArgs(testingInfo.getTestingId(), testingInfo.getTestId()),
                QuestionAndAnswer.SORT_ORDER);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.isNull(QuestionAndAnswer.Column.ANSWER)) {
                        return cursor.getPosition();
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        }

        return NO_POSITION;
    }

}
