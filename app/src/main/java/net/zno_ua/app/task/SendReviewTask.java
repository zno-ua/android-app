package net.zno_ua.app.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.R;
import net.zno_ua.app.rest.APIClient;
import net.zno_ua.app.rest.model.Review;
import net.zno_ua.app.util.Utils;

import java.lang.ref.WeakReference;

import okhttp3.ResponseBody;
import retrofit2.Response;

import static net.zno_ua.app.rest.ServiceGenerator.createService;

/**
 * @author vojkovladimir.
 */
public class SendReviewTask extends AsyncTask<Review, Void, Boolean> {

    private final WeakReference<CallBack> mCallBackWeakReference;
    private final MaterialDialog mProgressDialog;

    public SendReviewTask(@NonNull Context context, @NonNull CallBack callBack) {
        mCallBackWeakReference = new WeakReference<>(callBack);
        mProgressDialog = new MaterialDialog.Builder(context)
                .content(R.string.message_send_please_wait)
                .progress(true, 0)
                .cancelable(false)
                .build();
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Review... params) {
        final long startTime = System.currentTimeMillis();
        final APIClient apiClient = createService(APIClient.class, BuildConfig.API_KEY);
        boolean isSuccess = false;
        try {
            final Response<ResponseBody> response = apiClient.sendReview(params[0]).execute();
            isSuccess = response.isSuccess() && response.code() == 201;
        } catch (Exception e) {
            e.printStackTrace();
        }
        final long taskTime = System.currentTimeMillis() - startTime;
        if (taskTime < Utils.MIN_TASK_DELAY) {
            try {
                Thread.sleep(Utils.MIN_TASK_DELAY - taskTime);
            } catch (InterruptedException ignored) {
            }
        }
        return isSuccess;
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        mProgressDialog.dismiss();
        final CallBack callBack = mCallBackWeakReference.get();
        if (callBack != null) {
            callBack.onFinish(isSuccess);
        }
    }

    public interface CallBack {
        void onFinish(boolean isSuccess);
    }
}
