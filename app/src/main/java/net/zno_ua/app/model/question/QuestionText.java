package net.zno_ua.app.model.question;

import android.text.TextUtils;

/**
 * @author vojkovladimir.
 */
public class QuestionText implements QuestionItem {
    private final String mText;
    private final String mAdditionalText;
    private final boolean mIsEnglish;

    public QuestionText(String text, String additionalText, boolean isEnglish) {
        mAdditionalText = additionalText;
        mIsEnglish = isEnglish;
        mText = text;
    }

    public String getText() {
        return mText;
    }

    public String getAdditionalText() {
        return mAdditionalText;
    }

    public boolean hasAdditionalText() {
        return !TextUtils.isEmpty(mAdditionalText);
    }

    public boolean isEnglish() {
        return mIsEnglish;
    }
}