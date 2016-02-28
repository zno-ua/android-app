package net.zno_ua.app.service;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

public class GcmInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "Logs";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        final Intent intent = new Intent(this, GcmRegistrationService.class);
        startService(intent);
        Log.d(TAG, "onTokenRefresh");
    }
}
