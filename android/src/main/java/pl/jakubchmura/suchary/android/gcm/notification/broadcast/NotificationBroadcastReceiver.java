package pl.jakubchmura.suchary.android.gcm.notification.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pl.jakubchmura.suchary.android.gcm.notification.NotificationManager;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_CANCELLED = "notification_cancelled";
    private static final String TAG = "NotificationBrdcstRcvr";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive action = " + action);
        switch (action) {
            case NOTIFICATION_CANCELLED:
                NotificationManager notificationManager = new NotificationManager(context);
                notificationManager.setNotificationDisplayed(false);
                break;
        }
    }
}
