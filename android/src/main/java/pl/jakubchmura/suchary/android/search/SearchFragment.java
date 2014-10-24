package pl.jakubchmura.suchary.android.search;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.jakubchmura.suchary.android.JokesBaseFragment;
import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;
import pl.jakubchmura.suchary.android.util.Analytics;

/**
 * A placeholder fragment containing a simple view.
 */
public class SearchFragment extends JokesBaseFragment<SearchActivity> {

    private static final String TAG = "SearchFragment";
    private String mQuery;
    private boolean mStar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        boolean saved = true;
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_search, container, false);
            saved = false;
        } else {
            ((ViewGroup) mRootView.getParent()).removeView(mRootView);
        }

        Bundle arguments = getArguments();
        mQuery = arguments.getString("query");
        mStar = arguments.getBoolean("star");

        Analytics.setSearchQuery(mQuery);

        View createdView = createView(saved);
        if (!saved) {
            getJokes();
        }

        return createdView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
    }

    @Override
    protected void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mCardListView.setVisibility(View.VISIBLE);
    }

    protected void getJokes() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                JokeDbHelper helper = new JokeDbHelper(mActivity);
                mFetcher.setJokes(helper.searchBody(mQuery, mStar));
                mFetcher.setFetchGetOlder(false);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mFetcher.fetchNext();
            }
        }.execute((Void) null);
    }
}
