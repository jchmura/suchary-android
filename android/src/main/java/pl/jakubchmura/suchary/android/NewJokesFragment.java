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
import pl.jakubchmura.suchary.android.settings.ResetJokes;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewJokesFragment extends JokesBaseFragment<MainActivity>
        implements DownloadAllJokes.DownloadAllJokesCallback {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "NewJokesFragment";

    private ProgressDialog mProgressDialog;
    private DownloadAllJokes mDownloadAllTask;
    private int mProgressDialogState;
    private int mProgressDialogMaxState;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static NewJokesFragment newInstance(int sectionNumber) {
        NewJokesFragment fragment = new NewJokesFragment();
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
            mRootView = inflater.inflate(R.layout.fragment_new, container, false);
            saved = false;
        } else {
            ((ViewGroup) mRootView.getParent()).removeView(mRootView);
        }

        View createdView = createView(saved);

        setPullable();
        if (mDownloadAllTask == null) {
            getJokes(saved);
        }

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
        if (mDownloadAllTask != null && mDownloadAllTask.getStatus() != AsyncTask.Status.FINISHED) {
            showProgressDialog(mProgressDialogState, mProgressDialogMaxState);
            mDownloadAllTask.attach(mActivity, this);
        } else {
            checkNewJokes();
            checkEditedJokes();
            checkDeletedJoke();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (mDownloadAllTask != null) {
            mDownloadAllTask.detach();
        }
    }

    @Override
    public void addJokesToBottom(List<Joke> jokes) {
        super.addJokesToBottom(jokes);
        hideProgress();
    }

    protected void getJokes(final boolean saved) {
        if (ResetJokes.mJokes != null) {
            mFetcher.setJokes(ResetJokes.mJokes);
            mFetcher.fetchNext();
        } else {
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
            }.execute((Void) null);
        }
    }

    private void downloadJokesFromServer() {
        if (isAdded() && mDownloadAllTask == null) {
            mDownloadAllTask = new DownloadAllJokes(mActivity, this);
            showProgressDialog(0, 100);
            mDownloadAllTask.execute(getString(R.string.api_url) + "?limit=100");
        }
    }

    private void showProgressDialog(int progress, int max) {
        mProgressDialogState = progress;
        mProgressDialogMaxState = max;
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setTitle(getResources().getString(R.string.download_jokes_progress_title));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(max);
        mProgressDialog.setProgress(progress);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mDownloadAllTask.cancel(true);
            }
        });
        mProgressDialog.show();
    }

    @Override
    protected void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mSwipeRefresh.setVisibility(View.VISIBLE);
    }

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

    @Override
    public void getAPIAllResult(List<Joke> jokes) {
        mProgressDialog.dismiss();
        mFetcher.addJokesToDatabase(jokes);
        mFetcher.setJokes(jokes);
        mFetcher.fetchNext();
    }

    @Override
    public void errorDownloadingAll() {
    }
}
