package net.zno_ua.app.service;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Request;

import net.zno_ua.app.rest.RESTClient;

/**
 * @author Vojko Vladimir
 */
public class ZNOApiServiceHelper {

    private static ZNOApiServiceHelper mInstance = null;

    private Context mContext;

    private ZNOApiServiceHelper(Context context) {
        mContext = context;
    }

    public void restartPendingRequests() {
        mContext.startService(getIntent().setAction(ZNOApiService.Action.RESTART_PENDING_REQUESTS));
    }

    public void getTest(long testId) {
        mContext.startService(getIntent()
                .putExtra(ZNOApiService.Extra.METHOD, Request.Method.GET)
                .putExtra(ZNOApiService.Extra.RESOURCE_TYPE, RESTClient.ResourceType.TEST)
                .putExtra(ZNOApiService.Extra.ID, testId));
    }

    public void deleteTest(long testId) {
        mContext.startService(getIntent()
                .putExtra(ZNOApiService.Extra.METHOD, Request.Method.DELETE)
                .putExtra(ZNOApiService.Extra.RESOURCE_TYPE, RESTClient.ResourceType.TEST)
                .putExtra(ZNOApiService.Extra.ID, testId));
    }

    private Intent getIntent() {
        return new Intent(mContext, ZNOApiService.class);
    }

    public static ZNOApiServiceHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ZNOApiServiceHelper(context);
        }

        return mInstance;
    }
}
