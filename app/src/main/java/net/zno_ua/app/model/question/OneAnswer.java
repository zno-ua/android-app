package net.zno_ua.app.model.question;

/**
 * @author vojkovladimir.
 */
public class OneAnswer implements QuestionItem {
    private final int mCorrect;
    private final boolean mEditable;
    private final char mFirstLetter;
    private int mPosition;
    private String mText;
    private boolean mIsSelected = false;

    public OneAnswer(String text, int position, int correct, boolean editable, char firstLetter) {
        mPosition = position;
        mText = text;
        mCorrect = correct;
        mEditable = editable;
        mFirstLetter = firstLetter;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public int getCorrect() {
        return mCorrect;
    }

    public boolean isCorrect() {
        return mPosition == mCorrect;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public boolean isEditable() {
        return mEditable;
    }

    public char getFirstLetter() {
        return mFirstLetter;
    }
}