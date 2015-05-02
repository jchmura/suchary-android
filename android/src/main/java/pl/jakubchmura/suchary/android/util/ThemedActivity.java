package pl.jakubchmura.suchary.android.util;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;

import java.util.Arrays;

import pl.jakubchmura.suchary.android.R;

import static pl.jakubchmura.suchary.android.util.Versions.isLollipop;

public class ThemedActivity extends AppCompatActivity {

    private int mOldPrimaryColor;
    private static int defaultNavBarColor = -1;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_navbar_color")) {
                setNavigationBarColor();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setTheme(getThemeId());
            mOldPrimaryColor = getPrimaryColor();


            if (isLollipop()) {
                if (!hasDrawer()) {
                    setStatusBarColor(getPrimaryDarkColor());
                }
                setNavigationBarColor();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPref.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
            }
        } catch (Exception e) {
            Log.w("ThemedActivity", "Couldn't set theme for the app", e);
            Crashlytics.logException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (themeChanged()) {
            finish();
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
    }

    public int getThemeId() {
        int index = getIndex();
        String themeName = "Theme" + (index + 1);
        return getResources().getIdentifier(themeName, "style", getPackageName());
    }

    public int getPrimaryColor() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getInt("pref_theme_color", R.color.primary_4);
    }

    public int getPrimaryDarkColor() {
        int index = getIndex();
        String darkColorName = "primary_dark_" + (index+1);
        return getResources().getIdentifier(darkColorName, "color", getPackageName());
    }

    public int getAccentColor() {
        int index = getIndex();
        String darkColorName = "accent_" + (index+1);
        return getResources().getIdentifier(darkColorName, "color", getPackageName());
    }

    public boolean themeChanged() {
        return mOldPrimaryColor != getPrimaryColor();
    }

    protected boolean hasDrawer() {
        return false;
    }

    private int getIndex() {
        int primaryColor = getPrimaryColor();
        String primaryColorAsString = String.format("#%06X", 0xFFFFFF & primaryColor);
        String[] primaryColors = getResources().getStringArray(R.array.theme_primary_colors);
        int index = Arrays.asList(primaryColors).indexOf(primaryColorAsString);
        if (index == -1) {
            index = 3;
        }
        return index;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(color));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setNavigationBarColor() {
        Window window = getWindow();
        if (defaultNavBarColor == -1) {
            defaultNavBarColor = window.getNavigationBarColor();
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean colorNavBar = sharedPref.getBoolean("pref_navbar_color", false);

        if (colorNavBar) {
            window.setNavigationBarColor(getPrimaryColor());
        } else {
            window.setNavigationBarColor(defaultNavBarColor);
        }
    }
}
