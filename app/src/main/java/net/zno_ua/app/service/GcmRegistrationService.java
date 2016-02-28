package net.zno_ua.app.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import net.zno_ua.app.R;

import java.io.IOException;

public class GcmRegistrationService extends IntentService {

    private static final String TAG = "Logs";
    private static final String[] TOPICS = {"global", "test", "foo-bar"};

    public GcmRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            final InstanceID instanceID = InstanceID.getInstance(this);
            final String senderId = getString(R.string.gcm_defaultSenderId);
            String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "sender_id: " + senderId);
            Log.i(TAG, "GCM token: " + token);
            subscribeTopics(token);
        } catch (Exception ignored) {
        }
    }

    private void subscribeTopics(String token) throws IOException {
        final GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

}
