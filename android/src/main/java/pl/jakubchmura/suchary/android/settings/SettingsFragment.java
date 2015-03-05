package pl.jakubchmura.suchary.android.settings;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import pl.jakubchmura.android.colorpicker.ColorPickerPreference;
import pl.jakubchmura.suchary.android.R;

public class SettingsFragment extends PreferenceFragment {

    public static final String KEY_PREF_NOTIF = "pref_notif";
    private static final String KEY_PREF_NOTIF_COLOR = "pref_notif_color";
    private static final String KEY_PREF_RESET = "pref_reset";

    private Settings mActivity;
    private ResetJokes mResetJokes;
    private ColorPickerPreference mColorPreference;

    public SettingsFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (Settings) activity;
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference resetPreference = findPreference(KEY_PREF_RESET);
        if (resetPreference != null) {
            resetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mResetJokes = new ResetJokes(mActivity);
                    mResetJokes.reset();
                    return true;
                }
            });
        }

        mColorPreference = (ColorPickerPreference) findPreference(KEY_PREF_NOTIF_COLOR);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mResetJokes != null) {
            mResetJokes.attach(mActivity);
        }
        if (mColorPreference != null) {
            mColorPreference.attach(mActivity);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mResetJokes != null) {
            mResetJokes.detach();
        }
    }
}
