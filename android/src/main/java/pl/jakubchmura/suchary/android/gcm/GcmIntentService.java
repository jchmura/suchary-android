package pl.jakubchmura.suchary.android.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.jakubchmura.suchary.android.MainActivity;
import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.api.model.APIJoke;
import pl.jakubchmura.suchary.android.joke.api.model.APIResult;
import pl.jakubchmura.suchary.android.joke.api.network.JokeRetrofitSpiceService;
import pl.jakubchmura.suchary.android.joke.api.network.requests.NewerJokesRequest;
import pl.jakubchmura.suchary.android.joke.api.network.requests.SingleJokeRequest;
import pl.jakubchmura.suchary.android.settings.SettingsFragment;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;

/**
 * An {@link android.app.IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class GcmIntentService extends IntentService {

    public static final String PREFS_NAME = "gcm_jokes";
    public static final String EDIT_JOKE = "edit_joke";
    public static final String DELETE_JOKE = "delete_joke";
    private static final String TAG = "IntentService";
    private static boolean mHandling = false;
    private Intent mIntent;
    private SpiceManager mSpiceManager = new SpiceManager(JokeRetrofitSpiceService.class);

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mSpiceManager.start(this);
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
                        case "message":
                            handleMessage(extras);
                            break;
                        default:
                            finish();
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        mSpiceManager.shouldStop();
        super.onDestroy();
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
            SingleJokeRequest request = new SingleJokeRequest(key);
            mSpiceManager.execute(request, new RequestListener<APIJoke>() {
                @Override
                public void onRequestFailure(SpiceException spiceException) {

                }

                @Override
                public void onRequestSuccess(APIJoke apiJoke) {
                    Joke joke = apiJoke.getJoke();
                    addEditedJokeToPrefs(joke);
                    updateJokeInDatabase(joke);
                }
            });
        }
    }

    private void handleDeleteJoke(Bundle extras) {
        String key = extras.getString("key");
        if (key != null) {
            removeJokeInDatabase(key);
            addDeletedJokeToPrefs(key);
        }
    }

    private void handleMessage(Bundle extras) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setVibrate(new long[]{0, 300, 400, 300});
        builder.setSmallIcon(R.drawable.ic_stat_notify);
        builder.setContentTitle(extras.getString("title"));
        builder.setContentText(extras.getString("text"));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(extras.getString("text"))
                .setBigContentTitle(extras.getString("title")));
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify("Suchary message", 0, builder.build());
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
        }.execute((Void) null);
    }

    private void downloadNewer(Date date) {
        NewerJokesRequest request = new NewerJokesRequest(date);
        mSpiceManager.execute(request, date, DurationInMillis.ALWAYS_EXPIRED, new RequestListener<APIResult.APIJokes>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                finish();
            }

            @Override
            public void onRequestSuccess(APIResult.APIJokes apiJokes) {
                List<Joke> jokes = apiJokes.getJokes();
                if (jokes.size() > 0) {
                    jokes.remove(jokes.size() - 1);
                }
                if (!jokes.isEmpty()) {
                    Joke last = jokes.get(jokes.size() - 1);
                    addJokesToDatabase(jokes);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(GcmIntentService.this);
                    boolean notification = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIF, false);
                    if (notification) {
                        NewJokeNotification.notify(GcmIntentService.this, last.getBody(), jokes.size());
                    }
                } else {
                    finish();
                }
            }
        });
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
                List<Joke> oldJokes = helper.getJokes(keys, true);

                for (int i = 0; i < size; i++) {
                    Joke oldJoke = oldJokes.get(i);
                    if (oldJoke != null) newJokes.get(i).setStar(oldJoke.isStar());
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
        editor.apply();
    }

    private void addDeletedJokeToPrefs(String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        String deleted = sharedPreferences.getString(DELETE_JOKE, "");
        deleted += " " + key;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DELETE_JOKE, deleted);
        editor.apply();
    }

    private void finish() {
        mHandling = false;
        GcmBroadcastReceiver.completeWakefulIntent(mIntent);
    }
}
