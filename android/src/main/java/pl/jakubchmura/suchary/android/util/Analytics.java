package pl.jakubchmura.suchary.android.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import pl.jakubchmura.suchary.android.SucharyApp;

public class Analytics {

    private static SucharyApp mApplication;

    public static void init(Context context) {
        mApplication = getApplication(context);
    }

    public static void setId(String id) {
        try {
            if (mApplication != null) {
                mApplication.getTracker().setClientId(id);
            }
        } catch (Throwable th) {
            Crashlytics.logException(th);
        }
    }

    public static void setScreenName(String name) {
        if (mApplication != null) {
            try {
                Tracker tracker = mApplication.getTracker();
                tracker.setScreenName(name);
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            } catch (Throwable th) {
                Crashlytics.logException(th);
            }
        }
    }

    public static void setSearchQuery(String query) {
        if (mApplication != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Actionbar")
                    .setAction("Search")
                    .setLabel(query);
            try {
                Tracker tracker = mApplication.getTracker();
                tracker.send(builder.build());
            } catch (Throwable th) {
                Crashlytics.logException(th);
            }
        }
    }

    public static void clickedShuffle() {
        if (mApplication != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Actionbar")
                    .setAction("Shuffle");
            try {
                Tracker tracker = mApplication.getTracker();
                tracker.send(builder.build());
            } catch (Throwable th) {
                Crashlytics.logException(th);
            }
        }
    }

    public static void setTime(String category, String variable, String label, long value) {
        if (mApplication != null) {
            HitBuilders.TimingBuilder builder = new HitBuilders.TimingBuilder();
            builder.setCategory(category)
                    .setVariable(variable)
                    .setLabel(label)
                    .setValue(value);
            try {
                Tracker tracker = mApplication.getTracker();
                tracker.send(builder.build());
            } catch (Throwable th) {
                Crashlytics.logException(th);
            }
        }
    }

    public static void clickedStarred(String label) {
        if (mApplication != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Expand")
                    .setAction("Favorite")
                    .setLabel(label);
            try {
                Tracker tracker = mApplication.getTracker();
                tracker.send(builder.build());
            } catch (Throwable th) {
                Crashlytics.logException(th);
            }
        }
    }

    public static void clickedOriginal(String label) {
        if (mApplication != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Expand")
                    .setAction("Original")
                    .setLabel(label);
            try {
                Tracker tracker = mApplication.getTracker();
                tracker.send(builder.build());
            } catch (Throwable th) {
                Crashlytics.logException(th);
            }
        }
    }

    public static void clickedShare(String label) {
        if (mApplication != null) {
            HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
            builder.setCategory("Expand")
                    .setAction("Share")
                    .setLabel(label);
            try {
                Tracker tracker = mApplication.getTracker();
                tracker.send(builder.build());
            } catch (Throwable th) {
                Crashlytics.logException(th);
            }
        }
    }

    public static void setError(String description, boolean isFatal) {
        if (mApplication != null) {
            HitBuilders.ExceptionBuilder builder = new HitBuilders.ExceptionBuilder();
            builder.setDescription(description)
                    .setFatal(isFatal);
            try {
                Tracker tracker = mApplication.getTracker();
                tracker.send(builder.build());
            } catch (Throwable th) {
                Crashlytics.logException(th);
            }
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
