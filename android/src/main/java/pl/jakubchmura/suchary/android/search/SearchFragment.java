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
        mRootView = inflater.inflate(R.layout.fragment_search, container, false);

        Bundle arguments = getArguments();
        mQuery = arguments.getString("query");
        mStar = arguments.getBoolean("key");

        View createdView = createView(false);
        getJokes();

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
