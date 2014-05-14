package pl.jakubchmura.suchary.android.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.jakubchmura.suchary.android.MainActivity;
import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.api.DownloadAllJokes;
import pl.jakubchmura.suchary.android.joke.api.DownloadJoke;
import pl.jakubchmura.suchary.android.joke.api.DownloadJokes;
import pl.jakubchmura.suchary.android.settings.SettingsFragment;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;

/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class GcmIntentService extends IntentService implements DownloadJokes.DownloadJokesCallback,
        DownloadJoke.DownloadJokeCallback, DownloadAllJokes.DownloadAllJokesCallback {

    private static final String TAG = "IntentService";
    private Intent mIntent;
    private static boolean mHandling = false;

    public static final String PREFS_NAME = "gcm_jokes";
    public static final String EDIT_JOKE = "edit_joke";
    public static final String DELETE_JOKE = "delete_joke";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mIntent = intent;
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            switch (messageType) {
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                    String type = extras.getString("type");
                    Log.i(TAG, "Received GCM message: " + type);
                    switch (type) {
                        case "new":
                            handleNewJokes();
                            break;
                        case "edit":
                            handleEditJoke(extras);
                            break;
                        case "delete":
                            handleDeleteJoke(extras);
                            break;
                        default:
                            finish();
                    }
                    break;
            }
        }
    }

    private void handleNewJokes() {
        if (mHandling) {
            GcmBroadcastReceiver.completeWakefulIntent(mIntent);
            return;
        }
        mHandling = true;
        getNewer();
    }


    private void handleEditJoke(Bundle extras) {
        String key = extras.getString("key");
        if (key != null) {
            String url = "http://suchary.jakubchmura.pl/api/obcy/" + key;
            DownloadJoke downloadJoke = new DownloadJoke(this, this);
            downloadJoke.execute(url);
        }
    }

    private void handleDeleteJoke(Bundle extras) {
        String key = extras.getString("key");
        if (key != null) {
            removeJokeInDatabase(key);
            addDeletedJokeToPrefs(key);
        }
    }

    @Override
    public void getAPIJokesResult(List<Joke> jokes) {
        addJokesToDatabase(jokes);
    }

    @Override
    public void getAPIJokeResult(Joke joke) {
        addEditedJokeToPrefs(joke);
        updateJokeInDatabase(joke);
    }

    private void getNewer() {
        new AsyncTask<Void, Integer, Joke>() {
            @Override
            protected final Joke doInBackground(Void... params) {
                JokeDbHelper helper = new JokeDbHelper(GcmIntentService.this);
                Joke newestJoke = helper.getNewest();
                if (newestJoke != null) {
                    return newestJoke;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Joke joke) {
                if (joke != null) {
                    downloadNewer(joke.getDate());
                } else {
                    finish();
                }
            }
        }.execute((Void)null);
    }

    private void downloadNewer(Date date) {
        String url = "http://suchary.jakubchmura.pl/api/obcy";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        String first_date = dateFormat.format(date);
        try {
            url += "?after=" + URLEncoder.encode(first_date, "UTF-8");
            DownloadAllJokes download = new DownloadAllJokes(this, this);
            download.execute(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            finish();
        }
    }


    @Override
    public void getAPIAllResult(List<Joke> jokes) {
        if (jokes.size() > 0) {
            jokes.remove(jokes.size() - 1);
        }
        if (!jokes.isEmpty()) {
            Joke last = jokes.get(jokes.size()-1);
            addJokesToDatabase(jokes);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            boolean notification = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIF, false);
            if (notification) {
                NewJokeNotification.notify(this, last.getBody(), jokes.size());
            }
        } else {
            finish();
        }
    }

    private void addJokesToDatabase(List<Joke> jokes) {
        new AsyncTask<List<Joke>, Integer, Void>() {
            @SafeVarargs
            @Override
            protected final Void doInBackground(List<Joke>... params) {
                JokeDbHelper helper = new JokeDbHelper(GcmIntentService.this);
                helper.createJokes(params[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_NEW_JOKE);
                sendBroadcast(intent);
                finish();
            }
        }.execute(jokes);
    }

    private void updateJokesInDatabase(List<Joke> jokes) {
        new AsyncTask<List<Joke>, Integer, Void>() {
            @SafeVarargs
            @Override
            protected final Void doInBackground(List<Joke>... params) {
                JokeDbHelper helper = new JokeDbHelper(GcmIntentService.this);

                List<Joke> newJokes = params[0];
                int size = newJokes.size();
                String[] keys = new String[size];
                
                for (int i = 0; i < size; i++) {
                    keys[i] = newJokes.get(i).getKey();
                }
                List<Joke> oldJokes = helper.getJokes(keys);

                for (int i = 0; i < size; i++) {
                    newJokes.get(i).setStar(oldJokes.get(i).isStar());
                }
                helper.updateJokes(newJokes);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_EDIT_JOKE);
                sendBroadcast(intent);
                GcmBroadcastReceiver.completeWakefulIntent(mIntent);
            }
        }.execute(jokes);
    }

    private void removeJokeInDatabase(String key) {
        new AsyncTask<String, Integer, Void>() {
            @Override
            protected final Void doInBackground(String... params) {
                JokeDbHelper helper = new JokeDbHelper(GcmIntentService.this);
                helper.deleteJoke(params[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_DELETE_JOKE);
                sendBroadcast(intent);
                GcmBroadcastReceiver.completeWakefulIntent(mIntent);
            }
        }.execute(key);
    }

    private void updateJokeInDatabase(Joke joke) {
        List<Joke> jokes = new ArrayList<>();
        jokes.add(joke);
        updateJokesInDatabase(jokes);
    }

    private void addEditedJokeToPrefs(Joke joke) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        String edited = sharedPreferences.getString(EDIT_JOKE, "");
        edited += " " + joke.getKey();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EDIT_JOKE, edited);
        editor.commit();
    }

    private void addDeletedJokeToPrefs(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        String deleted = sharedPreferences.getString(DELETE_JOKE, "");
        deleted += " " + key;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DELETE_JOKE, deleted);
        editor.commit();
    }

    private void finish() {
        mHandling = false;
        GcmBroadcastReceiver.completeWakefulIntent(mIntent);
    }

    @Override
    public void errorDownloadingAll() {
        finish();
    }

    @Override
    public void setMaxProgress(int i) {}

    @Override
    public void incrementProgress(int i) {}
}
