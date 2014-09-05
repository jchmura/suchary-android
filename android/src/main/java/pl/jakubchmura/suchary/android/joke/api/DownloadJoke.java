package pl.jakubchmura.suchary.android.joke.api;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.util.Analytics;

public class DownloadJoke extends AsyncTask<String, Integer, Joke> {

    private static final String TAG = "DownloadJoke";
    private Context mContext;
    private DownloadJokeCallback mCallback;
    private long start;

    public DownloadJoke(Context context, DownloadJokeCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    @Nullable
    protected Joke doInBackground(String... params) {
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        wl.acquire();
        Joke joke = null;
        start = System.currentTimeMillis();
        try {
            joke = download(params[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            wl.release();
        }
        return joke;
    }

    @Nullable
    private Joke download(String url) throws IOException {
        String jsonString = URLConnectionReader.getText(url);
        try {
            JSONObject json = new JSONObject(jsonString);
            return JSONParser.getJoke(json);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Joke result) {
        if (result != null) {
            long end = System.currentTimeMillis();
            Analytics.setTime(mContext, "Download", "Joke", "1", end - start);
            mCallback.getAPIJokeResult(result);
        }
    }

    public interface DownloadJokeCallback {
        void getAPIJokeResult(Joke result);
    }
}
