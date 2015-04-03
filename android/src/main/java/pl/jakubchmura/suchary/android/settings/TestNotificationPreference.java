package pl.jakubchmura.suchary.android.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class TestNotificationPreference extends Preference {

    public TestNotificationPreference(Context context) {
        super(context);
    }

    public TestNotificationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestNotificationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onClick() {
        super.onClick();
        TestNotification testNotification = new TestNotification(getContext());
        testNotification.test();
    }
}
