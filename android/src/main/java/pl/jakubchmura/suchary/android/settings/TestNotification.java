package pl.jakubchmura.suchary.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import pl.jakubchmura.suchary.android.R;

public class TestNotification extends BroadcastReceiver {

    private static final String NOTIFICATION_TAG = "TestNotification";
    private final Context mContext;
    private Dialog mDialog;

    public TestNotification(Context context) {
        mContext = context;
    }

    public void test() {
        clearNotification();
        registerListener();
        showDialog();
    }

    private void clearNotification() {
        final NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_TAG, 0);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.test_notification_dialog_title))
                .setMessage(mContext.getString(R.string.test_notification_dialog_body));
        mDialog = builder.create();
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mContext.unregisterReceiver(TestNotification.this);
            }
        });
        mDialog.show();
    }

    private void registerListener() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(this, filter);
    }

    private static android.app.Notification createNotification(Context context) {
        final Resources res = context.getResources();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

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

        //
        builder.setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(res.getString(R.string.test_notification_title))
                .setContentText(res.getString(R.string.test_notification_body));

        // Automatically dismiss the notification when it is touched.
        builder.setAutoCancel(true);

        return builder.build();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Notification notification = createNotification(context);
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_TAG, 0, notification);
        mDialog.dismiss();
        context.unregisterReceiver(this);
    }
}
