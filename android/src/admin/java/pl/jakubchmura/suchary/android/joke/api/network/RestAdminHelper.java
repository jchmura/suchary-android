package pl.jakubchmura.suchary.android.joke.api.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import pl.jakubchmura.suchary.android.R;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class RestAdminHelper {

    private static final String TAG = "RestAdminHelper";

    public static <T> T getService(Context context, Class<T> serviceClass) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getEndpoint(context))
                .setRequestInterceptor(getRequestInterceptor(context))
                .build();

        return restAdapter.create(serviceClass);
    }

    private static String getEndpoint(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean("pref_admin_dev", false)) {
            return context.getString(R.string.api_url_dev);
        } else {
            return context.getString(R.string.api_url);
        }
    }

    private static RequestInterceptor getRequestInterceptor(final Context context) {
        return new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                String token = sharedPref.getString(APIToken.TOKEN_PREF, "");
                if (token.isEmpty()) {
                    Log.w(TAG, "{intercept} token is empty, the request will likely fail");
                } else {
                    request.addHeader("Authorization", "Token " + token);
                }
            }
        };
    }

}
