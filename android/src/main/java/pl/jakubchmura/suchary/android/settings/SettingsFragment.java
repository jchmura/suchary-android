package pl.jakubchmura.suchary.android.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.gcm.GcmRegistration;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_PREF_NOTIF = "pref_notif";
    private Settings mActivity;

    public SettingsFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (Settings) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPrefs = getPreferenceScreen().getSharedPreferences();
        if (sharedPrefs != null) {
            sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPrefs = getPreferenceScreen().getSharedPreferences();
        if (sharedPrefs != null) {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_NOTIF)) {
            boolean notification = sharedPreferences.getBoolean(KEY_PREF_NOTIF, false);
            GcmRegistration gcm = new GcmRegistration(mActivity);
            if (notification) {
                gcm.register();
            } else {
                gcm.unregister();
            }
        }
    }


}
