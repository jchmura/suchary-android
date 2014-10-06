package pl.jakubchmura.suchary.android;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class SucharyApp extends Application {
    private Tracker mTracker = null;
    private GoogleAnalytics mAnalytics;

    public synchronized Tracker getTracker() {
        if (mTracker == null) {
            mTracker = mAnalytics.newTracker(R.xml.analytics);
        }
        return mTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAnalytics = GoogleAnalytics.getInstance(this);
        Crashlytics.start(this);
        Crashlytics.log(Log.ERROR, "SucharyApp", "Testing crashlytics logging");
    }
}
