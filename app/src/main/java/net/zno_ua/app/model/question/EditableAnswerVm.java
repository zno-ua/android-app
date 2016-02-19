package net.zno_ua.app.model.question;

import android.support.annotation.Nullable;

/**
 * @author vojkovladimir.
 */
public class EditableAnswerVm implements QuestionItem {
    private final int mType;
    private final String mCorrectAnswer;
    private final String mAnswer;

    public EditableAnswerVm(int type, String correctAnswer) {
        mType = type;
        mCorrectAnswer = correctAnswer;
        mAnswer = null;
    }

    public EditableAnswerVm(int type, String correctAnswer, String answer) {
        mType = type;
        mCorrectAnswer = correctAnswer;
        mAnswer = answer;
    }

    @Nullable
    public String getAnswer() {
        return mAnswer;
    }

    public String getCorrectAnswer() {
        return mCorrectAnswer;
    }

    public boolean isCorrect() {
        return mAnswer == null;
    }


    public int getType() {
        return mType;
    }
}
