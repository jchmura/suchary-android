package pl.jakubchmura.suchary.android.joke.api;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import pl.jakubchmura.suchary.android.joke.Joke;

public class DownloadJokes extends AsyncTask<String, Integer, List<Joke>> {

    private static final String TAG = "DownloadJokes";
    private Context mContext;
    private DownloadJokesCallback mCallback;

    public DownloadJokes(Context context, DownloadJokesCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected List<Joke> doInBackground(String... params) {
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        wl.acquire();
        ArrayList<Joke> results = new ArrayList<>();
        for(String url: params) {
            try {
                results.add(download(url));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        wl.release();
        return results;
    }

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
    protected void onPostExecute(List<Joke> result) {
        if (result != null) {
            mCallback.getAPIJokesResult(result);
        }
    }

    public interface DownloadJokesCallback {
        void getAPIJokesResult(List<Joke> result);
    }
}
