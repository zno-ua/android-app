package net.zno_ua.app.viewholder.question;

import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.zno_ua.app.R;
import net.zno_ua.app.model.question.QuestionText;
import net.zno_ua.app.text.ImageGetter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.Html.fromHtml;

/**
 * @author vojkovladimir.
 */
public class QuestionTextVH extends QuestionItemVH<QuestionText>
        implements ViewStub.OnInflateListener {
    private static final String TEXT_REGEX = "(.*?)<table.*?<td.*?>(.*?)</td>.*?<td.*?>(.*?)</td>.*?";
    private static final Pattern PATTERN = Pattern.compile(TEXT_REGEX, Pattern.DOTALL);
    private final TextView mTvText;
    private final TextView mTvText1;
    private final TextView mTvText2;
    private final View mTable;
    private final ViewStub mVsAdditionalText;
    private Button mBtnAdditionalText;
    private MaterialDialog mDialog;

    public QuestionTextVH(LayoutInflater layoutInflater, ViewGroup parent) {
        super(layoutInflater.inflate(R.layout.view_question_text, parent, false));
        mTvText = (TextView) itemView.findViewById(R.id.text);
        mTvText.setMovementMethod(LinkMovementMethod.getInstance());
        mTvText1 = (TextView) itemView.findViewById(R.id.text1);
        mTvText1.setMovementMethod(LinkMovementMethod.getInstance());
        mTvText2 = (TextView) itemView.findViewById(R.id.text2);
        mTvText2.setMovementMethod(LinkMovementMethod.getInstance());
        mTable = itemView.findViewById(R.id.table);
        mVsAdditionalText = (ViewStub) itemView.findViewById(R.id.additional_text_view_stub);
        mVsAdditionalText.setOnInflateListener(this);
    }

    @Override
    public void onInflate(ViewStub stub, View inflated) {
        switch (stub.getId()) {
            case R.id.additional_text_view_stub:
                mBtnAdditionalText = (Button) inflated;
                mBtnAdditionalText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mDialog != null && !mDialog.isShowing()) {
                            mDialog.show();
                        }
                    }
                });
                break;
        }
    }

    public void bind(QuestionText item) {
        final Matcher matcher = PATTERN.matcher(item.getText());
        if (matcher.matches()) {
            final String text = matcher.group(1);
            if (TextUtils.isEmpty(text)) {
                mTvText.setVisibility(View.GONE);
            } else {
                mTvText.setVisibility(View.VISIBLE);
                mTvText.setText(fromHtml(matcher.group(1), new ImageGetter(mTvText), null));
            }
            mTvText1.setText(fromHtml(matcher.group(2), new ImageGetter(mTvText), null));
            mTvText2.setText(fromHtml(matcher.group(3), new ImageGetter(mTvText), null));
            mTable.setVisibility(View.VISIBLE);
        } else {
            mTable.setVisibility(View.GONE);
            mTvText.setText(fromHtml(item.getText(), new ImageGetter(mTvText), null));
        }
        if (item.hasAdditionalText()) {
            mVsAdditionalText.setVisibility(View.VISIBLE);
            int textForReadingResId;
            int closeResId;
            if (item.isEnglish()) {
                mBtnAdditionalText.setText(R.string.read_text_en);
                textForReadingResId = R.string.text_for_reading_en;
                closeResId = R.string.close_en;
            } else {
                textForReadingResId = R.string.text_for_reading;
                closeResId = R.string.close;
            }
            mDialog = new MaterialDialog.Builder(itemView.getContext())
                    .title(textForReadingResId)
                    .positiveText(closeResId)
                    .build();
            ImageGetter imageGetter = new ImageGetter(mDialog.getContentView());
            Spanned content = Html.fromHtml(item.getAdditionalText(), imageGetter, null);
            mDialog = mDialog.getBuilder().content(content).build();
        }
    }

}
