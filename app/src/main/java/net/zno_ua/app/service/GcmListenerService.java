package net.zno_ua.app.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.R;
import net.zno_ua.app.ZNOApplication;
import net.zno_ua.app.activity.MainActivity;
import net.zno_ua.app.util.Utils;

import java.io.IOException;
import java.util.Arrays;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {

    private static final java.lang.String KEY_TITLE = "title";
    private static final java.lang.String KEY_DESCRIPTION = "description";
    private static final java.lang.String KEY_LINK = "link";
    private static final java.lang.String KEY_TEST_ID = "tests_id";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (from.startsWith(GcmRegistrationService.TOPICS)) {
            final String topic = from.replace(GcmRegistrationService.TOPICS, "");
            onTopicReceived(topic, data);
        }
    }

    private void onTopicReceived(String topic, Bundle data) {
        switch (topic) {
            case GcmRegistrationService.GLOBAL_NEWS:
            case GcmRegistrationService.NEWS:
                onNewsReceived(data);
                break;
            case GcmRegistrationService.GLOBAL_UPDATE:
            case GcmRegistrationService.UPDATE:
                onUpdateReceived(data);
                break;
        }
    }

    private void onNewsReceived(Bundle data) {
        final String title = data.getString(KEY_TITLE);
        final String description = data.getString(KEY_DESCRIPTION);
        final String link = data.getString(KEY_LINK);
        if (BuildConfig.DEBUG) {
            ZNOApplication.log("GCM news received: " + title + ": " + description + " " + link);
        }
    }

    private void onUpdateReceived(Bundle data) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final long[] testIds = mapper.readValue(data.getString(KEY_TEST_ID), long[].class);
            if (BuildConfig.DEBUG) {
                ZNOApplication.log("GCM update received: " + Arrays.toString(testIds));
            }
            if (testIds != null && testIds.length != 0) {
                APIService.updateTests(getBaseContext(), testIds);
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                ZNOApplication.log("GCM update received: " + e.toString());
            }
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_school_white_24dp)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(Utils.DEFAULT_SOUND_URI)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
