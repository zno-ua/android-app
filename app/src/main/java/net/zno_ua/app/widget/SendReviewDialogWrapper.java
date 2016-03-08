package net.zno_ua.app.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.zno_ua.app.R;
import net.zno_ua.app.helper.PreferencesHelper;
import net.zno_ua.app.rest.model.Review;
import net.zno_ua.app.util.Utils;

import java.lang.ref.WeakReference;

/**
 * @author vojkovladimir.
 */
public class SendReviewDialogWrapper implements MaterialDialog.SingleButtonCallback {
    private final MaterialDialog mDialog;
    private final TextInputLayout mTlEmail;
    private final TextInputEditText mEtEmail;
    private final TextInputLayout mTlName;
    private final TextInputEditText mEtName;
    private final TextInputLayout mTlMessage;
    private final TextInputEditText mEtMessage;
    private final String mNoNameErrorMessage;
    private final String mNoEmailErrorMessage;
    private final String mNotValidEmailErrorMessage;
    private final String mNoMessageErrorMessage;
    private final WeakReference<Callback> mCallbackWeakReference;
    private final WeakReference<PreferencesHelper> mPreferencesHelperWeakReference;

    public SendReviewDialogWrapper(Context context, @NonNull Callback callback,
                                   @NonNull PreferencesHelper helper) {
        mDialog = new MaterialDialog.Builder(context)
                .title(R.string.send_review)
                .customView(R.layout.dialog_give_review, true)
                .positiveText(R.string.send)
                .negativeText(R.string.cancel)
                .neutralText(R.string.send_later)
                .autoDismiss(false)
                .onAny(this).build();
        //noinspection ConstantConditions
        mTlEmail = (TextInputLayout) mDialog.getCustomView().findViewById(R.id.input_email);
        mEtEmail = (TextInputEditText) mDialog.getCustomView().findViewById(R.id.email);
        mTlName = (TextInputLayout) mDialog.getCustomView().findViewById(R.id.input_name);
        mEtName = (TextInputEditText) mDialog.getCustomView().findViewById(R.id.name);
        mTlMessage = (TextInputLayout) mDialog.getCustomView().findViewById(R.id.input_message);
        mEtMessage = (TextInputEditText) mDialog.getCustomView().findViewById(R.id.message);
        mNoNameErrorMessage = context.getString(R.string.no_name_error_message);
        mNoEmailErrorMessage = context.getString(R.string.no_email_error_message);
        mNotValidEmailErrorMessage = context.getString(R.string.not_valid_email_error_message);
        mNoMessageErrorMessage = context.getString(R.string.no_message_error_message);
        mCallbackWeakReference = new WeakReference<>(callback);
        mPreferencesHelperWeakReference = new WeakReference<>(helper);
    }

    public void setName(String name) {
        mEtName.setText(name);
    }

    public void setEmail(String email) {
        mEtEmail.setText(email);
    }

    public void setMessage(String message) {
        mEtMessage.setText(message);
    }

    @Override
    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
        switch (dialogAction) {
            case POSITIVE:
                send();
                break;
            case NEGATIVE:
                clear();
                if (mPreferencesHelperWeakReference.get() != null) {
                    mPreferencesHelperWeakReference.get().saveMessage(null);
                }
                materialDialog.dismiss();
                break;
            case NEUTRAL:
                clearErrors();
                if (mPreferencesHelperWeakReference.get() != null) {
                    mPreferencesHelperWeakReference.get().saveName(mEtName.getText().toString());
                    mPreferencesHelperWeakReference.get().saveEmail(mEtEmail.getText().toString());
                    mPreferencesHelperWeakReference.get().saveMessage(mEtMessage.getText().toString());
                }
                materialDialog.dismiss();
                break;
        }
    }

    private void send() {
        clearErrors();
        final String name = mEtName.getText().toString();
        final String email = mEtEmail.getText().toString();
        final String message = mEtMessage.getText().toString();
        View focusView = null;
        boolean cancel = false;
        if (TextUtils.isEmpty(name)) {
            mTlName.setError(mNoNameErrorMessage);
            focusView = mTlName;
            cancel = true;
        }
        final boolean isEmailEmpty = TextUtils.isEmpty(email);
        if (isEmailEmpty || !Utils.isValidEmail(email)) {
            mTlEmail.setError(isEmailEmpty ? mNoEmailErrorMessage : mNotValidEmailErrorMessage);
            if (focusView == null) {
                focusView = mTlEmail;
            }
            cancel = true;
        }
        if (TextUtils.isEmpty(message)) {
            mTlMessage.setError(mNoMessageErrorMessage);
            if (focusView == null) {
                focusView = mTlMessage;
            }
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            mDialog.dismiss();
            final Review review = new Review(name, email, message);
            if (mPreferencesHelperWeakReference.get() != null) {
                mPreferencesHelperWeakReference.get().saveReview(review);
            }
            if (mCallbackWeakReference.get() != null) {
                mCallbackWeakReference.get().send(review);
            }
        }
    }

    private void clearErrors() {
        mTlName.setError(null);
        mTlName.setErrorEnabled(false);
        mTlEmail.setError(null);
        mTlEmail.setErrorEnabled(false);
        mTlMessage.setError(null);
        mTlMessage.setErrorEnabled(false);
    }

    public void clear() {
        mEtMessage.getText().clear();
        mEtMessage.clearFocus();
        mEtEmail.clearFocus();
        mEtMessage.clearFocus();
        clearErrors();
    }

    public boolean isShown() {
        return mDialog.isShowing();
    }

    public void show() {
        mDialog.show();
    }

    public interface Callback {
        void send(final @NonNull Review review);
    }
}
