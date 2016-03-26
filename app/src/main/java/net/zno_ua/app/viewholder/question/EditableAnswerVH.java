package net.zno_ua.app.viewholder.question;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
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
    private final OnAnswerChangeListener mListener;

    private EditableAnswer mEditableAnswer;

    public EditableAnswerVH(LayoutInflater layoutInflater, ViewGroup parent,
                            @NonNull OnAnswerChangeListener listener) {
        super(layoutInflater.inflate(R.layout.view_editable_answer_item, parent, false));
        mTvInputDescription = (TextView) itemView.findViewById(R.id.input_description);
        mTvInput = (TextView) itemView.findViewById(R.id.input);
        mTvInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (view instanceof EditText && !hasFocus) {
                    Utils.hideSoftKeyboard(view);
                }
            }
        });
        mListener = listener;
    }

    public void bind(@NonNull EditableAnswer item) {
        mEditableAnswer = item;
        mTvInput.removeTextChangedListener(mTextWatcher);
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
        mTvInput.addTextChangedListener(mTextWatcher);
    }

    private final TextWatcher mTextWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            String answer = s.toString();
            if (mEditableAnswer.getType() == Question.TYPE_5 && !TextUtils.isEmpty(answer)) {
                try {
                    answer = String.valueOf(Float.parseFloat(answer));
                } catch (NumberFormatException ignored) {
                }
            }
            mListener.onAnswerChanged(getAdapterPosition(), answer);
        }
    };

}