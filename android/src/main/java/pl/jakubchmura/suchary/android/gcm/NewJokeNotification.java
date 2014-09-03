package pl.jakubchmura.suchary.android.gcm;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import pl.jakubchmura.suchary.android.MainActivity;
import pl.jakubchmura.suchary.android.R;


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
    public static void notify(final Context context, final String text,
                              final int number) {
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
        boolean vibration = sharedPref.getBoolean("pref_vibration", false);
        long[] vibrationPattern = {0, 0};
        if (vibration) {
            vibrationPattern = new long[]{0, 300, 400, 300};
        }
        builder.setVibrate(vibrationPattern);

        // Light
        builder.setLights(context.getResources().getColor(R.color.notification_lights), 800, 1000);
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

        if (number > 1) {
            // Number
            builder.setNumber(number);
        }

        // Show expanded text content on devices running Android 4.1 or
        // later.
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

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(android.content.Context, String, int)}.
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }
}