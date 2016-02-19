package net.zno_ua.app.view.question;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.model.question.OneAnswer;
import net.zno_ua.app.text.ImageGetter;

import static android.text.Html.fromHtml;

/**
 * @author vojkovladimir.
 */
public class OneAnswerVH extends QuestionItemVH<OneAnswer> {
    private static final String LETTER_FORMAT = "%c.";
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnOneAnswerSelectListener != null) {
                mOnOneAnswerSelectListener.onOneAnswerSelected(getAdapterPosition());
            }
        }
    };
    private final int colorRed;
    private final int colorGreen;
    private final int colorBlack;
    private final TextView mTvLetter;
    private final TextView mTvText;
    @Nullable
    private final OnOneAnswerSelectListener mOnOneAnswerSelectListener;

    public OneAnswerVH(LayoutInflater layoutInflater, ViewGroup parent,
                       @Nullable OnOneAnswerSelectListener listener) {
        super(layoutInflater.inflate(R.layout.view_one_answer_item, parent, false));
        mTvLetter = (TextView) itemView.findViewById(R.id.letter);
        mTvText = (TextView) itemView.findViewById(R.id.text);
        colorRed = ContextCompat.getColor(parent.getContext(), R.color.red_500);
        colorGreen = ContextCompat.getColor(parent.getContext(), R.color.green_500);
        colorBlack = ContextCompat.getColor(parent.getContext(), R.color.primary_text_color);
        mOnOneAnswerSelectListener = listener;
    }

    public void bind(@NonNull OneAnswer item) {
        mTvLetter.setText(String.format(LETTER_FORMAT, item.getFirstLetter() + item.getPosition()));
        mTvText.setText(fromHtml(item.getText(), new ImageGetter(mTvText), null));
        if (item.isEditable()) {
            itemView.setSelected(item.isSelected());
            itemView.setOnClickListener(mOnClickListener);
        } else {
            itemView.setOnClickListener(null);
            itemView.setClickable(false);
            if (item.isCorrect()) {
                setTextColor(colorGreen);
            } else if (item.isSelected()) {
                setTextColor(colorRed);
            } else {
                setTextColor(colorBlack);
            }
        }
    }

    private void setTextColor(@ColorInt int color) {
        mTvLetter.setTextColor(color);
        mTvText.setTextColor(color);
    }

    public interface OnOneAnswerSelectListener {
        void onOneAnswerSelected(int adapterPosition);
    }
}
