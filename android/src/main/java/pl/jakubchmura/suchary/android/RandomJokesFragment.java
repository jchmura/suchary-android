package pl.jakubchmura.suchary.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import pl.jakubchmura.suchary.android.util.Analytics;


public class RandomJokesFragment extends JokesBaseFragment<MainActivity> {
    private static final String TAG = "RandomJokesFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static RandomJokesFragment newInstance(int sectionNumber) {
        RandomJokesFragment fragment = new RandomJokesFragment();
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
            mRootView = inflater.inflate(R.layout.fragment_random, container, false);
            saved = false;
        }

        View createdView = createView(saved);
        if (!saved) {
            mFetcher.setRandom(true);
            mFetcher.fetchNext();
        }

        setHasOptionsMenu(true);

        return createdView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        setRetainInstance(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.random, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shuffle:
                mPreviousTotal = 0;
                mFetcher.clear();
                mAdapter.clear();
                Analytics.clickedShuffle(mActivity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mCardListView.setVisibility(View.VISIBLE);
    }
}
