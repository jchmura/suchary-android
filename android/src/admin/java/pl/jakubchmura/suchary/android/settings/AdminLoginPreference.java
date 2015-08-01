package pl.jakubchmura.suchary.android.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.joke.api.network.APIToken;
import pl.jakubchmura.suchary.android.joke.api.network.AdminApiService;
import pl.jakubchmura.suchary.android.joke.api.network.RestAdminHelper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AdminLoginPreference extends DialogPreference implements Callback<APIToken> {

    private static final String TAG = "AdminLogin";

    public AdminLoginPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.admin_login);
        setPositiveButtonText(R.string.login);
        setNegativeButtonText(R.string.cancel);
        setDialogIcon(null);

        if (!isTokenEmpty(context)) {
            setTitle(R.string.pref_admin_logout_title);
        }
    }

    @Override
    protected void onClick() {
        if (isTokenEmpty(getContext())) {
            super.onClick(); // display the login dialog
        } else {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(APIToken.TOKEN_PREF, "");
            editor.apply();
            setTitle(R.string.pref_admin_login_title);
            Log.i(TAG, "{onClick} Logged out");
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        super.onClick(dialogInterface, which);
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Dialog dialog = getDialog();
            if (dialog != null) {
                String username = ((EditText) dialog.findViewById(R.id.username)).getText().toString();
                String password = ((EditText) dialog.findViewById(R.id.password)).getText().toString();

                AdminApiService service = RestAdminHelper.getService(getContext(), AdminApiService.class);
                service.getToken(username, password, AdminLoginPreference.this);
            }
        }
    }

    @Override
    public void success(APIToken apiToken, Response response) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(APIToken.TOKEN_PREF, apiToken.getToken());
        editor.apply();

        setTitle(R.string.pref_admin_logout_title);
    }

    @Override
    public void failure(RetrofitError error) {
        Log.e(TAG, "{failure}", error);
        setTitle(R.string.pref_admin_login_title);
    }

    private boolean isTokenEmpty(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String token = sharedPref.getString(APIToken.TOKEN_PREF, "");
        return token.isEmpty();
    }
}
