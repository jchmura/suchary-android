package pl.jakubchmura.suchary.android.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.PowerManager;

import java.util.HashSet;
import java.util.List;

import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.api.DownloadAllJokes;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;

public class ResetJokes implements DownloadAllJokes.DownloadAllJokesCallback {

    public static List<Joke> mJokes;

    private final Context mContext;
    private ProgressDialog mProgressDialog;
    private HashSet<String> mStarred;

    public ResetJokes(Context context) {
        mContext = context;
        mStarred = new HashSet<>();
    }

    public void reset() {
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

    private void saveStarred() {
        JokeDbHelper helper = new JokeDbHelper(mContext);
        List<Joke> starred = helper.getStarred();
        for (Joke joke : starred) {
            mStarred.add(joke.getKey());
        }
        helper.deleteAllJokes();
    }

    private void downloadAll() {
        DownloadAllJokes downloadAll = new DownloadAllJokes(mContext, this);
        showDeterminateProgress(downloadAll);
        downloadAll.execute("http://suchary.jakubchmura.pl/api/obcy?limit=100");
    }

    private void showDeterminateProgress(final DownloadAllJokes downloadAll) {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle(mContext.getResources().getString(R.string.download_jokes_progress_title));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setProgress(0);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadAll.cancel(true);
            }
        });
        mProgressDialog.show();
    }

    @Override
    public void setMaxProgress(int max) {
        mProgressDialog.setMax(max);
    }

    @Override
    public void incrementProgress(int delta) {
        mProgressDialog.incrementProgressBy(delta);
    }

    @Override
    public void getAPIAllResult(List<Joke> jokes) {
        mProgressDialog.dismiss();
        for (Joke joke : jokes) {
            if (mStarred.contains(joke.getKey())) {
                joke.setStar(true);
            }
        }
        mJokes = jokes;
        addJokesToDatabase(jokes);
    }

    @Override
    public void errorDownloadingAll() {
        mProgressDialog.cancel();
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

                JokeDbHelper helper = new JokeDbHelper(mContext);
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
