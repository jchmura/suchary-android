package pl.jakubchmura.suchary.android.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import pl.jakubchmura.suchary.android.SucharyApp;

public class Analytics {

    public static void setId(Context context, String id) {
        SucharyApp application = getApplication(context);
        if (application != null) {
            application.getTracker().setClientId(id);
        }
    }

    public static void setScreenName(Context context, String name) {
        SucharyApp application = getApplication(context);
        if (application != null) {
            Tracker tracker = application.getTracker();
            tracker.setScreenName(name);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    public static void setSearchQuery(Context context, String query) {
        SucharyApp application = getApplication(context);
        if (application != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Actionbar")
                    .setAction("Search")
                    .setLabel(query);
            Tracker tracker = application.getTracker();
            tracker.send(builder.build());
        }
    }

    public static void setShuffle(Context context) {
        SucharyApp application = getApplication(context);
        if (application != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Actionbar")
                    .setAction("Shuffle");
            Tracker tracker = application.getTracker();
            tracker.send(builder.build());
        }
    }

    public static void setTime(Context context, String category, String variable, String label, long value) {
        SucharyApp application = getApplication(context);
        if (application != null) {
            HitBuilders.TimingBuilder builder = new HitBuilders.TimingBuilder();
            builder.setCategory(category)
                    .setVariable(variable)
                    .setLabel(label)
                    .setValue(value);
            Tracker tracker = application.getTracker();
            tracker.send(builder.build());
        }
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
