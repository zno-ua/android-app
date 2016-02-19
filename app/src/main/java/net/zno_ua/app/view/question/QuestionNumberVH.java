package net.zno_ua.app.view.question;

import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.view.CursorViewHolder;

import static net.zno_ua.app.provider.Query.QuestionAndAnswer.Column;
import static net.zno_ua.app.provider.ZNOContract.Question.TYPE_2;
import static net.zno_ua.app.provider.ZNOContract.Question.TYPE_3;
import static net.zno_ua.app.provider.ZNOContract.Subject.ENGLISH;


/**
 * @author vojkovladimir.
 */
public class QuestionNumberVH extends CursorViewHolder {

    private static final String NUMBER_FORMAT = "%02d";
    private final String mStatementText;
    private final TextView mTvQuestionNumber;
    private final boolean mIsPassed;
    private final int colorRed;
    private final int colorGreen;
    private final OnQuestionNumberClickListener mOnQuestionNumberClickListener;

    public QuestionNumberVH(LayoutInflater inflater, ViewGroup parent, long subjectId,
                            boolean isPassed, OnQuestionNumberClickListener listener) {
        super(inflater.inflate(R.layout.view_question_number, parent, false));
        mTvQuestionNumber = (TextView) itemView.findViewById(R.id.question_number);
        if (subjectId == ENGLISH) {
            mStatementText = itemView.getContext().getString(R.string.statement_question_number_en);
        } else {
            mStatementText = itemView.getContext().getString(R.string.statement_question_number);
        }
        mIsPassed = isPassed;
        colorRed = ContextCompat.getColor(parent.getContext(), R.color.red_500);
        colorGreen = ContextCompat.getColor(parent.getContext(), R.color.green_500);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnQuestionNumberClickListener.onQuestionNumberClicked(getAdapterPosition());
            }
        });
        mOnQuestionNumberClickListener = listener;
    }

    @Override
    public void bind(Cursor cursor) {
        final int type = cursor.getInt(Column.TYPE);
        if (type == TYPE_2 && cursor.getCount() - 1 == cursor.getPosition()) {
            mTvQuestionNumber.setText(mStatementText);
        } else {
            mTvQuestionNumber.setText(String.format(NUMBER_FORMAT, cursor.getPosition() + 1));
        }
        final String answer = cursor.getString(Column.ANSWER);
        if (mIsPassed) {
            final int textColor;
            if (answer == null || !answer.equals(cursor.getString(Column.CORRECT_ANSWER))) {
                textColor = colorRed;
            } else {
                textColor = colorGreen;
            }
            mTvQuestionNumber.setTextColor(textColor);
        } else {
            if (type == TYPE_3) {
                mTvQuestionNumber.setSelected(answer != null && !answer.contains("0"));
            } else {
                mTvQuestionNumber.setSelected(answer != null);
            }
        }
    }

    public interface OnQuestionNumberClickListener {
        void onQuestionNumberClicked(int position);
    }
}
