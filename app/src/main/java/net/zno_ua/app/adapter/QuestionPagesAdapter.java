package net.zno_ua.app.adapter;

import android.database.Cursor;
import android.support.v4.app.Fragment;

import net.zno_ua.app.R;
import net.zno_ua.app.model.TestingInfo;
import net.zno_ua.app.provider.Query;
import net.zno_ua.app.fragment.QuestionFragment;

import static net.zno_ua.app.provider.ZNOContract.Subject.ENGLISH;

/**
 * @author vojkovladimir.
 */
public class QuestionPagesAdapter extends CursorFragmentStatePagerAdapter {
    private final String QUESTION_NUMBER_FORMAT;
    private final long mSubjectId;

    public QuestionPagesAdapter(Fragment parentFragment, TestingInfo testingInfo) {
        super(parentFragment.getChildFragmentManager());
        mSubjectId = testingInfo.getSubjectId();
        if (mSubjectId == ENGLISH) {
            QUESTION_NUMBER_FORMAT = parentFragment.getString(R.string.question_en_format);
        } else {
            QUESTION_NUMBER_FORMAT = parentFragment.getString(R.string.question_format);
        }
    }

    @Override
    public Fragment getItem(int position, Cursor cursor) {
        return QuestionFragment.newInstance(
                cursor.getInt(Query.Question.Column.ID),
                cursor.getInt(Query.Question.Column.TYPE),
                cursor.getString(Query.Question.Column.TEXT),
                cursor.getString(Query.Question.Column.ADDITIONAL_TEXT),
                cursor.getString(Query.Question.Column.ANSWERS),
                cursor.getInt(Query.Question.Column.POINT),
                cursor.getString(Query.Question.Column.CORRECT_ANSWER)
        );
    }

    @Override
    public CharSequence getPageTitle(int position, Cursor cursor) {
        return String.format(QUESTION_NUMBER_FORMAT, (position + 1));
    }

}

