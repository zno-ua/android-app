package net.zno_ua.app.model.question;

import android.support.annotation.NonNull;

/**
 * @author vojkovladimir.
 */
public interface AnswerItem extends QuestionItem {
    String getAnswer();
    void setAnswer(@NonNull String answer);
}
