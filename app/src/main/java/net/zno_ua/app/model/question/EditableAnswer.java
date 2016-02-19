package net.zno_ua.app.model.question;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author vojkovladimir.
 */
public class EditableAnswer implements AnswerItem {
    private final int mType;
    private String mAnswer;

    public EditableAnswer(int type, @Nullable String answer) {
        mType = type;
        mAnswer = answer;
    }

    @NonNull
    @Override
    public String getAnswer() {
        return mAnswer;
    }

    @Override
    public void setAnswer(@NonNull String answer) {
        this.mAnswer = answer;
    }

    public int getType() {
        return mType;
    }
}
