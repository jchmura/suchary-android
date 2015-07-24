package pl.jakubchmura.suchary.android.gcm.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.jakubchmura.suchary.android.joke.Joke;

public class NotificationManager {

    private static final String TAG = "NotificationManager";
    private static final String PREFS_NAME = "notifications";
    private static final String NOTIFICATION_DISPLAYED = "notification_displayed";

    private final Context mContext;
    private final NotificationDbManager mDbManager;

    public NotificationManager(Context context) {
        mContext = context.getApplicationContext();
        mDbManager = new NotificationDbManager(context);
    }

    public void newJokes(List<Joke> jokes) {
        if (jokes.size() == 0) {
            Log.d(TAG, "{newJokes} No new jokes, doing nothing");
            return;
        }

        Log.d(TAG, "{newJokes} " + jokes.size() + " new jokes arrived, displaying notification");
        mDbManager.insertJokes(jokes);
        display(jokes, false);
    }

    public void editJokes(List<Joke> jokes) {
        if (jokes.size() == 0) {
            Log.d(TAG, "{editJokes} No edited jokes, doing nothing");
            return;
        }
        mDbManager.insertJokes(jokes);

        if (isNotificationDisplayed()) {
            Log.d(TAG, "{editJokes} " + jokes.size() + " new jokes arrived, updating notification");
            List<Joke> total = new ArrayList<>(jokes);
            total.addAll(mDbManager.getAllJokes());
            display(total, true);
        } else {
            Log.d(TAG, "{editJokes} notification is not displayed, not updating it");
        }
    }

    public void removeJokes(List<Joke> jokes) {
        if (jokes.size() == 0) {
            Log.d(TAG, "{removeJokes} No removed jokes, doing nothing");
            return;
        }
        mDbManager.removeJokes(jokes);
        if (isNotificationDisplayed()) {
            Log.d(TAG, "{removeJokes} " + jokes.size() + " jokes to remove arrived, updating notification");
            List<Joke> current = mDbManager.getAllJokes();
            current.retainAll(jokes);

            if (current.size() == 0) {
                Log.d(TAG, "{removeJokes} no remaining jokes, hiding the notification");
                clear();
            } else {
                Log.d(TAG, "{removeJokes} there is " + current.size() + " remaining jokes in the notification");
                display(current, true);
            }
        } else {
            Log.d(TAG, "{removeJokes} notification is not displayed, not updating it");
        }
    }

    public void clear() {
        Log.i(TAG, "clear");
        setNotificationDisplayed(false);
        NewJokeNotification.cancel(mContext);
        mDbManager.removeAllJokes();
    }


    private void display(List<Joke> jokes, boolean onlyAlertOnce) {
        Log.d(TAG, "display() called with " + "jokes = [" + jokes + "], onlyAlertOnce = [" + onlyAlertOnce + "]");

        setNotificationDisplayed(true);
        Collections.sort(jokes);
        String first = jokes.get(0).getBody();
        NewJokeNotification.notify(mContext, first, jokes.size(), onlyAlertOnce);
    }

    public void setNotificationDisplayed(boolean displayed) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NOTIFICATION_DISPLAYED, displayed);
        editor.apply();
    }

    public boolean isNotificationDisplayed() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        return sharedPreferences.getBoolean(NOTIFICATION_DISPLAYED, false);
    }
}
