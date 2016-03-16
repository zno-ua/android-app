package net.zno_ua.app.service;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.R;

import java.io.IOException;

public class GcmRegistrationService extends IntentService {
    public static final String TOPICS = "/topics/";
    public static final String GLOBAL_NEWS = "news";
    public static final String NEWS = GLOBAL_NEWS + "-v" + BuildConfig.API_VERSION;
    public static final String GLOBAL_UPDATE = "update";
    public static final String UPDATE = GLOBAL_UPDATE + "-v" + BuildConfig.API_VERSION;

    private static final String[] ALL_TOPICS = {
            GLOBAL_NEWS,
            NEWS,
            GLOBAL_UPDATE,
            UPDATE
    };

    public GcmRegistrationService() {
        super(GcmRegistrationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            final InstanceID instanceID = InstanceID.getInstance(this);
            final String senderId = getString(R.string.gcm_defaultSenderId);
            final String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
            final String token = instanceID.getToken(senderId, scope, null);
            subscribeTopics(token);
        } catch (Exception ignored) {
        }
    }

    private void subscribeTopics(String token) throws IOException {
        final GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : ALL_TOPICS) {
            pubSub.subscribe(token, TOPICS + topic, null);
        }
    }

}
