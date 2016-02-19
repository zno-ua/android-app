package net.zno_ua.app.view.question;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.model.question.EditableAnswerVm;

import static net.zno_ua.app.provider.ZNOContract.Question;

/**
 * @author vojkovladimir.
 */
public class EditableAnswerVmVH extends QuestionItemVH<EditableAnswerVm> {
    private final int mColorRed;
    private final int mColorGreen;
    private final TextView mTvAnswer;

    public EditableAnswerVmVH(LayoutInflater layoutInflater, ViewGroup parent) {
        super(layoutInflater.inflate(R.layout.view_editable_answer_vm_item, parent, false));
        mTvAnswer = (TextView) itemView.findViewById(R.id.answer);
        mColorRed = ContextCompat.getColor(itemView.getContext(), R.color.red_500);
        mColorGreen = ContextCompat.getColor(itemView.getContext(), R.color.green_500);
    }

    public void bind(@NonNull EditableAnswerVm item) {
        if (item.isCorrect()) {
            bindCorrectAnswer(item);
        } else {
            bindUserAnswer(item);
        }
    }

    private void bindCorrectAnswer(EditableAnswerVm item) {
        final SpannableString spannableString;
        Object span;
        if (item.getType() == Question.TYPE_4) {
            mTvAnswer.setText(R.string.correct_combination);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < item.getCorrectAnswer().length(); i++) {
                stringBuilder.append(" ").append(item.getCorrectAnswer().charAt(i));
            }

            span = new ForegroundColorSpan(mColorGreen);
            spannableString = new SpannableString(stringBuilder.toString());
            spannableString.setSpan(span, 0, spannableString.length(), Spannable.SPAN_POINT_MARK);
        } else {
            mTvAnswer.setText(R.string.correct_answer);
            mTvAnswer.append(" ");
            span = new ForegroundColorSpan(mColorGreen);
            spannableString = new SpannableString(item.getCorrectAnswer());
            spannableString.setSpan(span, 0, item.getCorrectAnswer().length(),
                    Spannable.SPAN_POINT_MARK);
        }

        mTvAnswer.append(spannableString);
    }

    private void bindUserAnswer(EditableAnswerVm item) {
        final SpannableString spannableString;
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        Object span;
        if (item.getType() == Question.TYPE_4) {
            mTvAnswer.setText(R.string.user_combination);
            mTvAnswer.append(" ");
            final char[] chars = item.getCorrectAnswer().toCharArray();
            char c;
            int index;
            final String mAnswer = item.getAnswer();
            final String mCorrectAnswer = item.getCorrectAnswer();
            //noinspection ConstantConditions
            for (int i = 0; i < mAnswer.length(); i++) {
                c = mAnswer.charAt(i);
                builder.insert(i * 2, c + " ");
                index = mCorrectAnswer.indexOf(c);

                if (index == -1 || chars[index] != c) {
                    span = new ForegroundColorSpan(mColorRed);
                } else {
                    chars[index] = ' ';
                    span = new ForegroundColorSpan(mColorGreen);
                }

                builder.setSpan(span, i * 2, i * 2 + 1, Spannable.SPAN_POINT_MARK);
            }
            spannableString = new SpannableString(builder);
        } else {
            mTvAnswer.setText(R.string.user_answer);
            mTvAnswer.append(" ");
            if (TextUtils.equals(item.getCorrectAnswer(), item.getAnswer())) {
                span = new ForegroundColorSpan(mColorGreen);
            } else {
                span = new ForegroundColorSpan(mColorRed);
            }
            spannableString = new SpannableString(item.getAnswer());
            //noinspection ConstantConditions
            spannableString.setSpan(span, 0, item.getAnswer().length(), Spannable.SPAN_POINT_MARK);
        }

        mTvAnswer.append(spannableString);
    }
}
