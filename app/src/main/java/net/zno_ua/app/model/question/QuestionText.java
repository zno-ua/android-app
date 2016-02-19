package net.zno_ua.app.model.question;

import android.content.Context;
import android.text.TextUtils;

import net.zno_ua.app.R;
import net.zno_ua.app.util.Utils;

/**
 * @author vojkovladimir.
 */
public class QuestionText implements QuestionItem {
    private static final String SRC = "src=\"";
    private final String mText;
    private final String mAdditionalText;
    private final boolean mIsTagged;
    private final boolean mIsEnglish;

    public QuestionText(Context context, String text, String additionalText, boolean isEnglish) {
        mAdditionalText = additionalText;
        mIsEnglish = isEnglish;
        mIsTagged = text.contains("<table");
        if (mIsTagged) {
            final int textSize = (int) (context.getResources().getDimension(R.dimen.abc_text_size_small_material)
                    / context.getResources().getDisplayMetrics().scaledDensity);
            String formattedText = text.replace(SRC, SRC + Utils.getFilesDirPath(context));
            mText = String.format(context.getString(R.string.html_format), textSize, formattedText);
        } else {
            mText = text;
        }
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

    public boolean isTagged() {
        return mIsTagged;
    }

    public boolean isEnglish() {
        return mIsEnglish;
    }
}