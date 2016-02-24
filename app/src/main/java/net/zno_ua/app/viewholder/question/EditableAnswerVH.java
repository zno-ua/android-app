package net.zno_ua.app.viewholder.question;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import net.zno_ua.app.R;
import net.zno_ua.app.model.question.EditableAnswer;
import net.zno_ua.app.text.SimpleTextWatcher;
import net.zno_ua.app.util.Utils;

import static net.zno_ua.app.provider.ZNOContract.Question;

/**
 * @author vojkovladimir.
 */
public class EditableAnswerVH extends QuestionItemVH<EditableAnswer> {
    private final TextView mTvInputDescription;
    private final TextView mTvInput;
    @Nullable
    private final OnAnswerChangeListener mOnAnswerChangeListener;

    public EditableAnswerVH(LayoutInflater layoutInflater, ViewGroup parent,
                            @Nullable OnAnswerChangeListener listener) {
        super(layoutInflater.inflate(R.layout.view_editable_answer_item, parent, false));
        mTvInputDescription = (TextView) itemView.findViewById(R.id.input_description);
        mTvInput = (TextView) itemView.findViewById(R.id.input);
        mTvInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (mOnAnswerChangeListener != null) {
                    mOnAnswerChangeListener.onAnswerChanged(getAdapterPosition(), s.toString());

                }
            }
        });
        mTvInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (view instanceof EditText && !hasFocus) {
                    Utils.hideSoftKeyboard(view);
                }
            }
        });
        mOnAnswerChangeListener = listener;
    }

    public void bind(@NonNull EditableAnswer item) {
        if (item.getType() == Question.TYPE_4) {
            mTvInputDescription.setText(R.string.answer_three_correct);
            mTvInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
            mTvInput.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        } else {
            mTvInputDescription.setText(R.string.answer_short);
            mTvInput.setInputType(EditorInfo.TYPE_CLASS_NUMBER
                    | EditorInfo.TYPE_NUMBER_FLAG_SIGNED
                    | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
            mTvInput.setFilters(new InputFilter[0]);
        }
        if (!TextUtils.isEmpty(item.getAnswer())) {
            mTvInput.setText(item.getAnswer());
        }
    }

}