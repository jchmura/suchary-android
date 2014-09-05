package pl.jakubchmura.suchary.android.joke.api;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.util.Analytics;

public class DownloadAllJokes extends AsyncTask<String, Integer, Void> {

    private static final String TAG = "DownloadAllJokes";
    private Context mContext;
    private DownloadAllJokesCallback mCallback;
    private List<Joke> mResult;
    private long start;

    public DownloadAllJokes(Context context, DownloadAllJokesCallback callback) {
        mContext = context;
        mCallback = callback;
        mResult = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(String... params) {
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        wl.acquire();

        try {
            download(params[0]);
        } catch (MalformedURLException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            wl.release();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        long end = System.currentTimeMillis();
        Analytics.setTime(mContext, "Download", "All jokes", String.valueOf(mResult.size()), end - start);
        mCallback.getAPIAllResult(mResult);
    }

    private void download(String url) throws IOException {
        start = System.currentTimeMillis();
        String jsonString = URLConnectionReader.getText(url);
        APIResult apiResult = null;
        try {
            JSONObject json = new JSONObject(jsonString);
            apiResult = JSONParser.getAPIResult(json);
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        processResult(apiResult);
    }

    private void processResult(APIResult result) throws IOException {
        if (result != null) {
            if (mResult.isEmpty()) {
                mCallback.setMaxProgress(result.getCount());
            }

            if (result.getResults() != null) {
                mCallback.incrementProgress(result.getResults().size());
                mResult.addAll(result.getResults());
                if (result.getNext() != null && !isCancelled()) {
                    download(result.getNext());
                }
            }
        } else {
            mCallback.errorDownloadingAll();
        }
    }

    public interface DownloadAllJokesCallback {
        void setMaxProgress(int i);

        void incrementProgress(int i);

        void getAPIAllResult(List<Joke> result);

        void errorDownloadingAll();
    }
}
