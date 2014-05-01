package pl.jakubchmura.suchary.android.util;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkHelper {
    /**
     * Check if device is online
     *
     * @param context context of the application
     * @return whether is online
     */
    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }
}
