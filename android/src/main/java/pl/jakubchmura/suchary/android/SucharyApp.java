package pl.jakubchmura.suchary.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.api.changes.ChangeResolver;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;
import pl.jakubchmura.suchary.android.util.Analytics;

public class SucharyApp extends Application {

    private static final String TAG = "SucharyApp";

    private Tracker mTracker = null;
    private GoogleAnalytics mAnalytics;

    public synchronized Tracker getTracker() {
        if (mTracker == null) {
            mTracker = mAnalytics.newTracker(R.xml.analytics);
            mTracker.enableExceptionReporting(true);
        }
        return mTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAnalytics = GoogleAnalytics.getInstance(this);
        Analytics.init(this);
        Crashlytics.start(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        checkLastChangeValue();
    }

    private void checkLastChangeValue() {
        final SharedPreferences sharedPreferences = getSharedPreferences(ChangeResolver.PREFS_CHANGE_DATE, Context.MODE_PRIVATE);
        String lastChangeString = sharedPreferences.getString(ChangeResolver.CHANGE_DATE, "");
        if (lastChangeString.isEmpty()) {
            Log.w(TAG, "Last change date is not set, setting to the date of newest joke");
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    JokeDbHelper dbHelper = new JokeDbHelper(SucharyApp.this);
                    Joke newest = dbHelper.getNewest();
                    if (newest != null) {
                        ChangeResolver.saveLastChange(SucharyApp.this, newest.getDate());
                    }
                    return null;
                }
            }.execute((Void)null);
        }
    }
}
