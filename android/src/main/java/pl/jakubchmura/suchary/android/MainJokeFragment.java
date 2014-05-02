package pl.jakubchmura.suchary.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.api.DownloadAllJokes;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainJokeFragment extends JokesBaseFragment<MainActivity> implements DownloadAllJokes.DownloadAllJokesCallback {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "MainJokeFragment";

    private ProgressDialog mProgressDialog;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainJokeFragment newInstance(int sectionNumber) {
        MainJokeFragment fragment = new MainJokeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        boolean saved = true;
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_all, container, false);
            saved = false;
        }

        View createdView = createView(saved);

        setPullable();
        getJokes(saved);

        return createdView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNewJokes();
        checkEditedJokes();
        checkDeletedJoke();
    }

    @Override
    public void addJokesToBottom(List<Joke> jokes) {
        super.addJokesToBottom(jokes);
        hideProgress();
    }

    protected void getJokes(final boolean saved) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... params) {
                JokeDbHelper helper = new JokeDbHelper(mActivity);
                return helper.getCount();
            }
            @Override
            protected void onPostExecute(Long count) {
                if (count == 0) downloadJokesFromServer();
                else if (!saved) mFetcher.fetchNext();
            }
        }.execute((Void)null);
    }

    private void downloadJokesFromServer() {
        final DownloadAllJokes downloadAll = new DownloadAllJokes(mActivity, this);
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setTitle(getResources().getString(R.string.download_jokes_progress_title));
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

        downloadAll.execute("http://suchary.jakubchmura.pl/api/obcy?limit=100");
    }

    @Override
    protected void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mPullToRefresh.setVisibility(View.VISIBLE);
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
        mFetcher.addJokesToDatabase(jokes);
        mFetcher.setJokes(jokes);
        mFetcher.fetchNext();
    }

    @Override
    public void errorDownloadingAll() {}
}
