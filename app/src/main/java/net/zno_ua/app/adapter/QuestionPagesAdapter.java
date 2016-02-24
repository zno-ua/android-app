package net.zno_ua.app.adapter;

import android.database.Cursor;
import android.support.v4.app.Fragment;

import net.zno_ua.app.R;
import net.zno_ua.app.model.TestingInfo;
import net.zno_ua.app.fragment.QuestionFragment;

import static net.zno_ua.app.provider.ZNOContract.Question.TYPE_2;
import static net.zno_ua.app.provider.ZNOContract.Subject.*;
import static net.zno_ua.app.provider.Query.Question.Column;

/**
 * @author vojkovladimir.
 */
public class QuestionPagesAdapter extends CursorFragmentStatePagerAdapter {
    private final String QUESTION_NUMBER_FORMAT;
    private final String STATEMENT_QUESTION;
    private final long mSubjectId;

    public QuestionPagesAdapter(Fragment parentFragment, TestingInfo testingInfo) {
        super(parentFragment.getChildFragmentManager());
        mSubjectId = testingInfo.getSubjectId();
        if (mSubjectId == ENGLISH) {
            QUESTION_NUMBER_FORMAT = parentFragment.getString(R.string.question_en_format);
            STATEMENT_QUESTION = parentFragment.getString(R.string.statement_en);
        } else {
            QUESTION_NUMBER_FORMAT = parentFragment.getString(R.string.question_format);
            STATEMENT_QUESTION = parentFragment.getString(R.string.statement);
        }
    }

    @Override
    public Fragment getItem(int position, Cursor cursor) {
        return QuestionFragment.newInstance(
                cursor.getInt(Column.ID),
                cursor.getInt(Column.TYPE),
                cursor.getString(Column.TEXT),
                cursor.getString(Column.ADDITIONAL_TEXT),
                cursor.getString(Column.ANSWERS),
                cursor.getInt(Column.POINT),
                cursor.getString(Column.CORRECT_ANSWER)
        );
    }

    @Override
    public CharSequence getPageTitle(int position, Cursor cursor) {
        if ((mSubjectId == ENGLISH || mSubjectId == UKRAINIAN)
                && cursor.getInt(Column.TYPE) == TYPE_2 && cursor.getCount() - 1 == cursor.getPosition()) {
            return STATEMENT_QUESTION;
        }
        return String.format(QUESTION_NUMBER_FORMAT, (position + 1));
    }

}

