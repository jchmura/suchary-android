package pl.jakubchmura.suchary.android.search;

import android.app.Activity;
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
    private final String mQuery;
    private final boolean mStar;

    public SearchFragment() {
        mQuery = "";
        mStar = false;
    }

    public SearchFragment(String query, boolean star) {
        mQuery = query;
        mStar = star;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_search, container, false);

        View createdView = createView(false);
        JokeDbHelper helper = new JokeDbHelper(mActivity);
        mFetcher.setJokes(helper.searchBody(mQuery, mStar));
        mFetcher.setFetchGetOlder(false);
        mFetcher.fetchNext();


        return createdView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
    }
}
