package pl.jakubchmura.suchary.android.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;

import java.util.HashSet;
import java.util.List;

import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.api.changes.ChangeResolver;
import pl.jakubchmura.suchary.android.joke.api.model.APIResult;
import pl.jakubchmura.suchary.android.joke.api.network.requests.AllJokesRequest;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;
import pl.jakubchmura.suchary.android.util.NetworkHelper;

public class ResetJokes {

    private static final String TAG = "ResetJokes";
    private static final String REQUEST_CACHE_KEY = "all jokes request from reset jokes";

    public static List<Joke> mJokes;

    private Context mContext;
    private SpiceManager mSpiceManager;
    private AllJokesRequest mAllJokesRequest;
    private PendingRequestListener<APIResult.APIJokes> mAllJokesRequestListener;
    private ProgressDialog mProgressDialog;
    private HashSet<String> mStarred;
    private int mProgressDialogState;
    private int mProgressDialogMaxState;

    public ResetJokes(Context context, SpiceManager spiceManager) {
        mContext = context;
        mSpiceManager = spiceManager;
        mStarred = new HashSet<>();
    }

    public void reset() {
        if (!NetworkHelper.isOnline(mContext)) {
            Log.w(TAG, "No network connectivity, not downloading all jokes for reset");
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.no_network_reset);
            builder.setTitle(R.string.no_network);
            builder.setNeutralButton(R.string.ok, null);
            builder.show();
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                saveStarred();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                downloadAll();
            }
        }.execute((Void) null);
    }

    public void detach() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    public void attach(Context context) {
        mContext = context;
        if (mAllJokesRequest != null && !mAllJokesRequest.isFinished()) {
            showProgressDialog(mProgressDialogState, mProgressDialogMaxState);
            mSpiceManager.addListenerIfPending(APIResult.APIJokes.class, REQUEST_CACHE_KEY, mAllJokesRequestListener);
        }
    }

    private void saveStarred() {
        JokeDbHelper helper = JokeDbHelper.getInstance(mContext);
        List<Joke> starred = helper.getStarred();
        for (Joke joke : starred) {
            mStarred.add(joke.getKey());
        }
        helper.deleteAllJokes();
    }

    private void downloadAll() {
        showProgressDialog(0, 100);
        mAllJokesRequest = new AllJokesRequest(100);
        mAllJokesRequest.setProgressListener(new AllJokesRequest.ProgressListener() {
            @Override
            public void setMaxProgress(int max) {
                mProgressDialogMaxState = max;
                mProgressDialog.setMax(max);
            }

            @Override
            public void incrementProgress(int delta) {
                mProgressDialogState += delta;
                mProgressDialog.setProgress(mProgressDialogState);
            }
        });
        mAllJokesRequestListener = new PendingRequestListener<APIResult.APIJokes>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                mProgressDialog.cancel();
            }

            @Override
            public void onRequestSuccess(APIResult.APIJokes apiJokes) {
                List<Joke> jokes = apiJokes.getJokes();
                mProgressDialog.dismiss();
                for (Joke joke : jokes) {
                    if (mStarred.contains(joke.getKey())) {
                        joke.setStar(true);
                    }
                }
                mJokes = jokes;
                addJokesToDatabase(jokes);
                ChangeResolver.saveLastChange(mContext, apiJokes.getLastChange());
            }

            @Override
            public void onRequestNotFound() {

            }
        };
        mSpiceManager.execute(mAllJokesRequest, REQUEST_CACHE_KEY, DurationInMillis.ALWAYS_EXPIRED, mAllJokesRequestListener);
    }

    private void showProgressDialog(int progress, int max) {
        mProgressDialogState = progress;
        mProgressDialogMaxState = max;
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getResources().getString(R.string.download_jokes_progress_title));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(max);
        mProgressDialog.setProgress(progress);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mAllJokesRequest.cancel();
            }
        });
        mProgressDialog.show();
    }

    private void addJokesToDatabase(List<Joke> jokes) {
        new AsyncTask<List<Joke>, Void, Void>() {
            @SafeVarargs
            @Override
            protected final Void doInBackground(List<Joke>... params) {
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        getClass().getName());
                wl.acquire();

                JokeDbHelper helper = JokeDbHelper.getInstance(mContext);
                helper.createJokes(params[0]);

                wl.release();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mProgressDialog.dismiss();
            }
        }.execute(jokes);
    }
}
