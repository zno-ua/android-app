package net.zno_ua.app.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import net.zno_ua.app.R;
import net.zno_ua.app.util.Utils;

/**
 * @author vojkovladimir.
 */
public class NotificationHelper {

    private static final int ID_NEWS = 0x1;

    public static void notifyNews(@NonNull Context context, String title, String description, String link) {
        final Intent newsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, newsIntent, 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_school_white_24dp)
                .setContentTitle(title)
                .setContentText(description)
                .setTicker(title)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(description))
                .setContentIntent(contentIntent);
        sendNotification(context, ID_NEWS, builder);
    }

    private static void sendNotification(@NonNull Context context, int id,
                                         @NonNull NotificationCompat.Builder builder) {
        if (PreferencesHelper.getInstance(context).playNotificationSound()) {
            builder.setSound(Utils.DEFAULT_SOUND_URI);
        }
        final NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
