package net.zno_ua.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import net.zno_ua.app.model.TestingInfo;
import net.zno_ua.app.view.CursorViewHolder;
import net.zno_ua.app.view.question.QuestionNumberVH;

/**
 * @author vojkovladimir.
 */
public class QuestionsGridAdapter extends CursorRecyclerViewAdapter<CursorViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private final long mSubjectId;
    private final boolean mIsPassed;
    private final QuestionNumberVH.OnQuestionNumberClickListener mOnQuestionNumberClickListener;

    public QuestionsGridAdapter(Context context, TestingInfo testingInfo,
                                QuestionNumberVH.OnQuestionNumberClickListener listener) {
        mLayoutInflater = LayoutInflater.from(context);
        mSubjectId = testingInfo.getSubjectId();
        mIsPassed = testingInfo.isPassed();
        mOnQuestionNumberClickListener = listener;
    }

    @Override
    public void onBindViewHolder(CursorViewHolder viewHolder, Cursor cursor) {
        viewHolder.bind(cursor);
    }

    @Override
    public CursorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new QuestionNumberVH(mLayoutInflater, parent, mSubjectId, mIsPassed, mOnQuestionNumberClickListener);
    }

}
