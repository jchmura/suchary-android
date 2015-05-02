package pl.jakubchmura.suchary.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.api.changes.ChangeResolver;
import pl.jakubchmura.suchary.android.joke.api.model.APIResult;
import pl.jakubchmura.suchary.android.joke.api.network.requests.AllJokesRequest;
import pl.jakubchmura.suchary.android.settings.ResetJokes;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;
import pl.jakubchmura.suchary.android.util.NetworkHelper;

/**
 * A placeholder fragment containing a simple view.
 */
public class NewJokesFragment extends JokesBaseFragment<MainActivity> {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "NewJokesFragment";
    private static final String REQUEST_CACHE_KEY = "all jokes request from new fragment";

    private ProgressDialog mProgressDialog;
    private int mProgressDialogState;
    private int mProgressDialogMaxState;
    private AllJokesRequest mAllJokesRequest;
    private PendingRequestListener<APIResult.APIJokes> mAllJokesRequestListener;
    private boolean mFirstStart = true;

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
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (parent != null) {
                parent.removeView(mRootView);
            }
        }

        View createdView = createView(saved);

        setPullable();
        if (mAllJokesRequest == null) {
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
        if (mAllJokesRequest != null) {
            if (!mAllJokesRequest.isFinished() && !mFirstStart) {
                Log.d(TAG, "onResume show dialog");
                showProgressDialog(mProgressDialogState, mProgressDialogMaxState);
                mSpiceManager.addListenerIfPending(APIResult.APIJokes.class, REQUEST_CACHE_KEY, mAllJokesRequestListener);
            }
        } else {
            int latches = 3;
            if (mAdapter == null) latches++;
            mCountDownLatch = new CountDownLatch(latches);
            checkNewJokes();
            checkEditedJokes();
            checkDeletedJoke();
            downloadLatestChanges();
        }
        mFirstStart = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void addJokesToBottom(List<Joke> jokes) {
        super.addJokesToBottom(jokes);
        hideProgress();
    }

    @Override
    public boolean showNewJokes() {
        return true;
    }

    protected void getJokes(final boolean saved) {
        if (ResetJokes.mJokes != null) {
            mFetcher.setJokes(ResetJokes.mJokes);
            mFetcher.fetchNext();
        } else {
            JokeDbHelper helper = JokeDbHelper.getInstance(mActivity);
            if (helper.getCount() == 0) downloadJokesFromServer();
            else if (!saved) mFetcher.fetchNext();
        }
    }

    private void downloadJokesFromServer() {
        if (isAdded() && mAllJokesRequest == null) {
            if (!NetworkHelper.isOnline(mActivity)) {
                Log.w(TAG, "No network connectivity, not downloading all jokes");
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage(R.string.no_network_first_download);
                builder.setTitle(R.string.no_network);
                builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mActivity.finish();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mActivity.finish();
                    }
                });
                builder.show();
                return;
            }
            Log.d(TAG, "downloadJokesFromServer show dialog");
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

                }

                @Override
                public void onRequestSuccess(APIResult.APIJokes apiJokes) {
                    List<Joke> jokes = apiJokes.getJokes();
                    mProgressDialog.dismiss();
                    mFetcher.addJokesToDatabase(jokes);
                    mFetcher.setJokes(jokes);
                    mFetcher.fetchNext();
                    ChangeResolver.saveLastChange(getActivity(), apiJokes.getLastChange());
                }

                @Override
                public void onRequestNotFound() {

                }
            };
            mSpiceManager.execute(mAllJokesRequest, REQUEST_CACHE_KEY, DurationInMillis.ALWAYS_EXPIRED, mAllJokesRequestListener);
        }
    }

    private void showProgressDialog(int progress, int max) {
        Log.d(TAG, "show progress dialog, " + progress + " " + max);
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
                mAllJokesRequest.cancel();
            }
        });
        mProgressDialog.show();
    }

    @Override
    protected void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mSwipeRefresh.setVisibility(View.VISIBLE);
    }

    private void downloadLatestChanges() {
        mFetcher.getNewer(mCountDownLatch);
    }
}
