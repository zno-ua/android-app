package net.zno_ua.app.rest;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.RequestFuture;

import net.zno_ua.app.ZNOApplication;

import java.util.concurrent.ExecutionException;

/**
 * @author Vojko Vladimir
 */
public abstract class RESTMethod<T> {

    protected abstract Request<T> getRequest(RequestFuture<T> requestFuture);

    public T getResponse() throws ExecutionException, InterruptedException {
        RequestFuture<T> requestFuture = RequestFuture.newFuture();
        Request<T> request = getRequest(requestFuture);

        request.setRetryPolicy(getRetryPolicy());

        ZNOApplication.getInstance().addToRequestQueue(request);

        return requestFuture.get();
    }

    protected RetryPolicy getRetryPolicy() {
        return new DefaultRetryPolicy(5000, 5, 1.2f);
    }
}
