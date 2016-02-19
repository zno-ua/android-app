package net.zno_ua.app.model.question;

import android.text.TextUtils;

/**
 * @author vojkovladimir.
 */
public class ChoicesAnswers implements QuestionItem {
    private final int mNumbersCount;
    private final int mLettersCount;
    private final char mFirstLetter;
    private final boolean mEditable;
    private final String mCorrectAnswer;
    private String mAnswer;
    private boolean mIsAnswered;

    public ChoicesAnswers(int numbersCount, int lettersCount, String correctAnswer, String answer,
                          char firstLetter, boolean editable) {
        mNumbersCount = numbersCount;
        mLettersCount = lettersCount;
        mCorrectAnswer = correctAnswer;
        setAnswer(TextUtils.isEmpty(answer) ? emptyAnswer(numbersCount) : answer);
        mFirstLetter = firstLetter;
        mEditable = editable;
    }

    public int getNumbersCount() {
        return mNumbersCount;
    }

    public int getLettersCount() {
        return mLettersCount;
    }

    public String getCorrectAnswer() {
        return mCorrectAnswer;
    }

    public char getFirstLetter() {
        return mFirstLetter;
    }

    public boolean isEditable() {
        return mEditable;
    }

    public String getAnswer() {
        return mAnswer;
    }

    public void setAnswer(String answer) {
        mAnswer = answer;
        if (!mIsAnswered && mAnswer != null && !mAnswer.contains("0")) {
            mIsAnswered = true;
        }
    }

    public static String emptyAnswer(int numbersCount) {
        return new String(new char[numbersCount]).replace("\0", "0");
    }

    public boolean isAnswered() {
        return mIsAnswered;
    }
}
