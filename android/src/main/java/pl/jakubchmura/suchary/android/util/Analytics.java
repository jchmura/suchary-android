package pl.jakubchmura.suchary.android.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import pl.jakubchmura.suchary.android.SucharyApp;

public class Analytics {

    private static SucharyApp mApplication;

    public static void init(Context context) {
        mApplication = getApplication(context);
    }

    public static void setId(String id) {
        if (mApplication != null) {
            mApplication.getTracker().setClientId(id);
        }
    }

    public static void setScreenName(String name) {
        if (mApplication != null) {
            Tracker tracker = mApplication.getTracker();
            tracker.setScreenName(name);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    public static void setSearchQuery(String query) {
        if (mApplication != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Actionbar")
                    .setAction("Search")
                    .setLabel(query);
            Tracker tracker = mApplication.getTracker();
            tracker.send(builder.build());
        }
    }

    public static void clickedShuffle() {
        if (mApplication != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Actionbar")
                    .setAction("Shuffle");
            Tracker tracker = mApplication.getTracker();
            tracker.send(builder.build());
        }
    }

    public static void setTime(String category, String variable, String label, long value) {
        if (mApplication != null) {
            HitBuilders.TimingBuilder builder = new HitBuilders.TimingBuilder();
            builder.setCategory(category)
                    .setVariable(variable)
                    .setLabel(label)
                    .setValue(value);
            Tracker tracker = mApplication.getTracker();
            tracker.send(builder.build());
        }
    }

    public static void clickedStarred(String label) {
        if (mApplication != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Expand")
                    .setAction("Favorite")
                    .setLabel(label);
            Tracker tracker = mApplication.getTracker();
            tracker.send(builder.build());
        }
    }

    public static void clickedOriginal(String label) {
        if (mApplication != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Expand")
                    .setAction("Original")
                    .setLabel(label);
            Tracker tracker = mApplication.getTracker();
            tracker.send(builder.build());
        }
    }

    public static void clickedShare(String label) {
        if (mApplication != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Expand")
                    .setAction("Share")
                    .setLabel(label);
            Tracker tracker = mApplication.getTracker();
            tracker.send(builder.build());
        }
    }

    public static void setError(String description, boolean isFatal) {
        if (mApplication != null) {
            HitBuilders.ExceptionBuilder builder = new HitBuilders.ExceptionBuilder();
            builder.setDescription(description)
                    .setFatal(isFatal);
            Tracker tracker = mApplication.getTracker();
            tracker.send(builder.build());
        }
    }

    public static void setError(String description) {
        setError(description, false);
    }

    private static SucharyApp getApplication(Context context) {
        SucharyApp application;
        if (context instanceof Activity) {
            application = (SucharyApp) ((Activity) context).getApplication();
        } else if (context instanceof Application) {
            application = (SucharyApp) context;
        } else {
            return null;
        }
        return application;
    }

}
