package net.zno_ua.app.model.question;

import android.support.annotation.NonNull;

/**
 * @author vojkovladimir.
 */
public class StatementAnswer implements AnswerItem {
    private int mPoint;
    private final int mMaxPoint;

    public StatementAnswer(int maxPoint, int point) {
        mMaxPoint = maxPoint;
        mPoint = point == -1 ? maxPoint / 2 : point;
    }

    public int getPoint() {
        return mPoint;
    }

    public void setPoint(int mPoint) {
        this.mPoint = mPoint;
    }

    public int getMaxPoint() {
        return mMaxPoint;
    }

    @Override
    public String getAnswer() {
        return String.valueOf(mPoint);
    }

    @Override
    public void setAnswer(@NonNull String answer) {
        mPoint = Integer.parseInt(answer);
    }
}
