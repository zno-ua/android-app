package net.zno_ua.app.viewholder.question;

import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.zno_ua.app.R;
import net.zno_ua.app.activity.ViewImageActivity;
import net.zno_ua.app.model.question.QuestionText;
import net.zno_ua.app.text.ImageGetter;

import static android.text.Html.fromHtml;

/**
 * @author vojkovladimir.
 */
public class QuestionTextVH extends QuestionItemVH<QuestionText>
        implements ViewStub.OnInflateListener {
    private static final WebViewClient WEB_VIEW_CLIENT = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains(ViewImageActivity.DATA_SCHEMA)) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                view.getContext().startActivity(intent);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

    };

    private final ViewStub mVsTextView;
    private final ViewStub mVsWebView;
    private final ViewStub mVsAdditionalText;
    private TextView mTvText;
    private WebView mWvText;
    private Button mBtnAdditionalText;
    private MaterialDialog mDialog;

    public QuestionTextVH(LayoutInflater layoutInflater, ViewGroup parent) {
        super(layoutInflater.inflate(R.layout.view_question_text, parent, false));
        mVsTextView = (ViewStub) itemView.findViewById(R.id.text_view_stub);
        mVsWebView = (ViewStub) itemView.findViewById(R.id.web_view_stub);
        mVsAdditionalText = (ViewStub) itemView.findViewById(R.id.additional_text_view_stub);
        mVsTextView.setOnInflateListener(this);
        mVsWebView.setOnInflateListener(this);
        mVsAdditionalText.setOnInflateListener(this);
    }

    @Override
    public void onInflate(ViewStub stub, View inflated) {
        switch (stub.getId()) {
            case R.id.web_view_stub:
                mWvText = (WebView) inflated;
                mWvText.setFocusable(false);
                mWvText.setFocusableInTouchMode(false);
                mWvText.setWebViewClient(WEB_VIEW_CLIENT);
                break;
            case R.id.text_view_stub:
                mTvText = (TextView) inflated;
                mTvText.setMovementMethod(LinkMovementMethod.getInstance());
                break;
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
        if (item.isTagged()) {
            mVsWebView.setVisibility(View.VISIBLE);
            mWvText.loadDataWithBaseURL(null, item.getText(), "text/html", "utf-8", null);
        } else {
            mVsTextView.setVisibility(View.VISIBLE);
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
