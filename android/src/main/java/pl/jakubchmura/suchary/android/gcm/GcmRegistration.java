package pl.jakubchmura.suchary.android.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.settings.Settings;
import pl.jakubchmura.suchary.android.util.Analytics;
import pl.jakubchmura.suchary.android.util.NetworkHelper;

public class GcmRegistration {
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String TAG = "GcmRegistration";
    private static final String PROPERTY_APP_VERSION = "app_version";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final String SENDER_ID = "375845694760";
    private GoogleCloudMessaging mGcm;
    private String mRegId;
    private Context mContext;

    public GcmRegistration(Context context) {
        mContext = context;
    }

    public boolean register() {
        if (checkPlayServices()) {
            if (NetworkHelper.isOnline(mContext)) {
                mGcm = GoogleCloudMessaging.getInstance(mContext);
                mRegId = getRegistrationId();

                if (mRegId.isEmpty()) {
                    registerInBackground();
                } else {
                    sendIdToBackend("register");
                }
            }
            return true;
        } else {
            Log.v(TAG, "No valid Google Play Services APK found.");
            return false;
        }
    }

    public void unregister() {
        if (checkPlayServices() && NetworkHelper.isOnline(mContext)) {
            mRegId = getRegistrationId();
            if (!mRegId.isEmpty()) {
                sendIdToBackend("unregister");
            }
        } else {
            Log.v(TAG, "No valid Google Play Services APK found.");
        }
    }

    private String getRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.v(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersionCode();
        if (registeredVersion != currentVersion) {
            Log.v(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences() {
        return mContext.getSharedPreferences(Settings.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private int getAppVersionCode() {
        PackageInfo packageInfo = getPackageInfo();
        if (packageInfo != null) {
            return packageInfo.versionCode;
        }
        return -1;
    }

    private String getAppVersionName() {
        PackageInfo packageInfo = getPackageInfo();
        if (packageInfo != null) {
            return packageInfo.versionName;
        }
        return "-1";
    }

    private void registerInBackground() {
        new AsyncTask() {
            @Override
            protected String doInBackground(Object[] params) {
                String msg;
                try {
                    if (mGcm == null) {
                        mGcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    mRegId = mGcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + mRegId;
                    Log.i(TAG, msg);

                    sendIdToBackend("register");

                    storeRegistrationId();
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Analytics.setError(ex.getMessage());
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    private void sendIdToBackend(final String action) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String id = mRegId.substring(0, 10);
                Crashlytics.setUserIdentifier(id);
                Analytics.setId(id);
                String androidID = android.provider.Settings.Secure.getString(mContext.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
                String data = "registration_id=" + mRegId + "&android_id=" + androidID + "&version=" + getAppVersionName();
                HttpURLConnection connection = null;
                DataOutputStream wr = null;
                try {
                    URL url = new URL(getSiteUrl() + action + "/");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setInstanceFollowRedirects(false);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("charset", "utf-8");
                    connection.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
                    connection.setUseCaches(false);

                    wr = new DataOutputStream(connection.getOutputStream());
                    wr.writeBytes(data);

                    int responseCode = connection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        Log.d(TAG, "Server returned HTTP " + responseCode
                                + " " + connection.getResponseMessage());
                        Analytics.setError("GCM backend returned HTTP code " + responseCode);
                    }
                } catch (IOException e) {
                    Crashlytics.logException(e);
                } finally {
                    if (wr != null) {
                        try {
                            wr.flush();
                            wr.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return null;
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersionCode();
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, mRegId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (!GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Log.w(TAG, "This device is not supported.");
                Analytics.setError("This device is not supported by Google Play Services");
            } else {
                Analytics.setError("This device does not have installed Google Play Services");
            }
            return false;
        }
        return true;
    }

    private PackageInfo getPackageInfo() {
        try {
            PackageManager packageManager = mContext.getPackageManager();
            return packageManager.getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        } catch (NullPointerException e) {
            return null;
        }
    }

    private String getSiteUrl() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (sharedPref.getBoolean("pref_admin_dev", false)) {
            return mContext.getString(R.string.site_url_dev);
        } else {
            return mContext.getString(R.string.site_url);
        }
    }

}
