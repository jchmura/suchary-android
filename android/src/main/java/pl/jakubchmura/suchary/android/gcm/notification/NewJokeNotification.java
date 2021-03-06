package pl.jakubchmura.suchary.android.gcm.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import pl.jakubchmura.suchary.android.MainActivity;
import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.gcm.notification.broadcast.NotificationBroadcastReceiver;


/**
 * Helper class for showing and canceling new joke
 * notifications.
 * <p/>
 * This class makes heavy use of the {@link android.support.v4.app.NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class NewJokeNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "NewJoke";

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     *
     * @see #cancel(android.content.Context)
     */
    static void notify(final Context context, final String text, final int number, boolean onlyAlertOnce) {
        final Resources res = context.getResources();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("restart", true);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Ringtone
        String ringtone = sharedPref.getString("pref_ringtone", "DEFAULT_SOUND");
        builder.setSound(Uri.parse(ringtone));

        // Vibration
        String vibrationPatternString = sharedPref.getString("pref_vibration_pattern", "0 0");
        String[] vibrations = vibrationPatternString.split(" ");
        long[] vibrationPattern = new long[vibrations.length];
        for (int i = 0; i < vibrations.length; i++) {
            vibrationPattern[i] = Long.parseLong(vibrations[i]);
        }
        builder.setVibrate(vibrationPattern);

        // Light
        int lightColor = sharedPref.getInt("pref_notif_color", context.getResources().getColor(android.R.color.white));
        builder.setLights(lightColor, 800, 1000);
        // Small icon, the notification title and text.
        builder.setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(res.getQuantityString(
                        R.plurals.new_joke_notification, number, number))
                .setContentText(text);

        // All fields below this line are optional.

        // Use a default priority (recognized on devices running Android
        // 4.1 or later)
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Ticker text
        builder.setTicker(res.getQuantityString(
                R.plurals.new_joke_notification, number, number));

        // Pending intent
        builder.setContentIntent(
                PendingIntent.getActivity(context, 0,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT)
        );

        // Number
        if (number > 1) {
            builder.setNumber(number);
        }

        // Show expanded text content on devices running Android 4.1 or later.
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(text);
        bigTextStyle.setBigContentTitle(res.getQuantityString(
                R.plurals.new_joke_notification, number, number));
        if (number > 2) {
            String summary_format = res.getString(R.string.new_joke_notification_summary);
            String summary = String.format(summary_format, number - 1);
            bigTextStyle.setSummaryText(summary);
        }
        builder.setStyle(bigTextStyle);

        // Automatically dismiss the notification when it is touched.
        builder.setAutoCancel(true);
        builder.setDeleteIntent(getDeleteIntent(context));

        // Do not vibrate and emit sound when only updating
        builder.setOnlyAlertOnce(onlyAlertOnce);

        // Wearable features
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                        .setBackground(BitmapFactory.decodeResource(context.getResources(), R.drawable.wear_background));
        builder.extend(wearableExtender);

        notify(context, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    private static PendingIntent getDeleteIntent(Context context) {
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.setAction(NotificationBroadcastReceiver.NOTIFICATION_CANCELLED);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, String, int, boolean)}.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }
}