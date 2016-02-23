package net.zno_ua.app.service;

import android.content.Context;
import android.content.Intent;

/**
 * @author Vojko Vladimir
 */
public class APIServiceHelper {

    public static void restartPendingRequests(Context context) {
        context.startService(getIntent(context).setAction(APIService.Action.RESTART_PENDING_REQUESTS));
    }

    public static void getTest(Context context, long testId) {
        context.startService(getIntent(context)
                .putExtra(APIService.Extra.METHOD, APIService.Method.GET)
                .putExtra(APIService.Extra.RESOURCE_TYPE, APIService.ResourceType.TEST)
                .putExtra(APIService.Extra.ID, testId));
    }

    public static void deleteTest(Context context, long testId) {
        context.startService(getIntent(context)
                .putExtra(APIService.Extra.METHOD, APIService.Method.DELETE)
                .putExtra(APIService.Extra.RESOURCE_TYPE, APIService.ResourceType.TEST)
                .putExtra(APIService.Extra.ID, testId));
    }

    private static Intent getIntent(Context context) {
        return new Intent(context, APIService.class);
    }

}
