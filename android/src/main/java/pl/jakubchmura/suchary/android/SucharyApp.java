package pl.jakubchmura.suchary.android;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import pl.jakubchmura.suchary.android.util.Analytics;

public class SucharyApp extends Application {
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
    }
}
