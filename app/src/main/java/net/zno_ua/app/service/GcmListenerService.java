package net.zno_ua.app.service;

import android.os.Bundle;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.ZNOApplication;
import net.zno_ua.app.helper.NotificationHelper;

import java.util.Arrays;

public class GcmListenerService extends com.google.android.gms.gcm.GcmListenerService {

    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_LINK = "link";
    private static final String KEY_TEST_ID = "tests_id";

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
        if (!(data.containsKey(KEY_TITLE) || data.containsKey(KEY_DESCRIPTION)
                || data.containsKey(KEY_LINK))) {
            return;
        }
        final String title = data.getString(KEY_TITLE);
        final String description = data.getString(KEY_DESCRIPTION);
        final String link = data.getString(KEY_LINK);

        NotificationHelper.notifyNews(getBaseContext(), title, description, link);

        if (BuildConfig.DEBUG) {
            ZNOApplication.log("GCM news received: " + title + ": " + description + " " + link);
        }
    }

    private void onUpdateReceived(Bundle data) {
        if (!data.containsKey(KEY_TEST_ID)) {
            return;
        }
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final long[] testIds = mapper.readValue(data.getString(KEY_TEST_ID), long[].class);
            if (BuildConfig.DEBUG) {
                ZNOApplication.log("GCM update received: " + Arrays.toString(testIds));
            }
            if (testIds != null && testIds.length != 0) {
                APIService.updateTests(getBaseContext(), testIds);
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                ZNOApplication.log("GCM update received EXCEPTION:\n\t" + e.toString());
            }
        }
    }

}
