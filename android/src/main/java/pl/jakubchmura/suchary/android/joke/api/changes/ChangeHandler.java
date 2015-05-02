package pl.jakubchmura.suchary.android.joke.api.changes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import pl.jakubchmura.suchary.android.MainActivity;
import pl.jakubchmura.suchary.android.gcm.NewJokeNotification;
import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.settings.SettingsFragment;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;

public class ChangeHandler {

    private static final String TAG = "ChangeHandler";

    public static final String PREFS_NAME = "gcm_jokes";
    public static final String EDIT_JOKE = "edit_joke";
    public static final String DELETE_JOKE = "delete_joke";
    private Context mContext;

    public ChangeHandler(Context context) {
        mContext = context;
    }

    public void handleAll(ChangeResolver resolver, boolean notify) {
        handleDeletedJokes(resolver.getDeleted(), notify);
        handleEditedJokes(resolver.getEdited(), notify);
        handleAddedJokes(resolver.getAdded(), notify);
    }

    public void handleAllInBackground(final ChangeResolver resolver, final boolean notify) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                handleAll(resolver, notify);
                return null;
            }
        }.execute((Void[]) null);
    }

    public void handleDeletedJokes(List<Joke> jokes, boolean notify) {
        if (jokes.isEmpty()) {
            Log.d(TAG, "There are no jokes to delete");
        } else {
            Log.i(TAG, "About to handle deletion of " + jokes.size() + " jokes with" + (notify? " ": "out ") + "notifying");
            String[] keys = getKeys(jokes);
            deleteJokesFromDatabase(keys);
            if (notify) {
                notifyAbout(MainActivity.ACTION_DELETE_JOKE);
                addJokesToPrefs(keys, DELETE_JOKE);
            }
            Log.d(TAG, "Deletion completed");
        }
    }

    public void handleEditedJokes(List<Joke> jokes, boolean notify) {
        if (jokes.isEmpty()) {
            Log.d(TAG, "There are no jokes to edit");
        } else {
            Log.i(TAG, "About to handle edit of " + jokes.size() + " jokes with" + (notify? " ": "out ") + "notifying");
            String[] keys = getKeys(jokes);
            updateJokesInDatabase(jokes);
            if (notify) {
                notifyAbout(MainActivity.ACTION_EDIT_JOKE);
                addJokesToPrefs(keys, EDIT_JOKE);
            }
            Log.d(TAG, "Edit completed");
        }
    }

    public void handleAddedJokes(List<Joke> jokes, boolean notify) {
        if (jokes.isEmpty()) {
            Log.d(TAG, "There are no jokes to add");
        } else {
            Log.i(TAG, "About to handle addition of " + jokes.size() + " jokes with" + (notify? " ": "out ") + "notifying");
            Joke last = jokes.get(0);
            addJokesToDatabase(jokes);
            if (notify) {
                notifyAbout(MainActivity.ACTION_NEW_JOKE);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                boolean notification = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIF, false);
                if (notification) {
                    NewJokeNotification.notify(mContext, last.getBody(), jokes.size());
                }
            }
            Log.d(TAG, "Addition completed");
        }
    }

    public static String[] getKeys(List<Joke> jokes) {
        String[] keys = new String[jokes.size()];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = jokes.get(i).getKey();
        }
        return keys;
    }

    private void deleteJokesFromDatabase(String[] keys) {
        JokeDbHelper helper = JokeDbHelper.getInstance(mContext);
        helper.deleteJokes(keys);
    }

    private void updateJokesInDatabase(List<Joke> jokes) {
        JokeDbHelper helper = JokeDbHelper.getInstance(mContext);

        int size = jokes.size();
        List<Joke> oldJokes = helper.getJokes(getKeys(jokes), true);

        for (int i = 0; i < size; i++) {
            Joke oldJoke = oldJokes.get(i);
            if (oldJoke != null) jokes.get(i).setStar(oldJoke.isStar());
        }
        helper.updateJokes(jokes);
    }

    private void addJokesToDatabase(List<Joke> jokes) {
        JokeDbHelper helper = JokeDbHelper.getInstance(mContext);
        helper.createJokes(jokes);
    }

    private void notifyAbout(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        mContext.sendBroadcast(intent);
    }

    private void addJokesToPrefs(String[] keys, String preference) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREFS_NAME, 0);
        String deleted = sharedPreferences.getString(preference, "");
        deleted += " " + StringUtils.join(keys, ' ');

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preference, deleted);
        editor.apply();
    }
}
