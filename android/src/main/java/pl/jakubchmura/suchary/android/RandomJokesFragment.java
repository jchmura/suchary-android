package pl.jakubchmura.suchary.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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
            mRootView = inflater.inflate(R.layout.fragment_new, container, false);
            saved = false;
        }

        View createdView = createView(saved);
        if (!saved) {
            mFetcher.setRandom(true);
            mFetcher.fetchNext();
        }

        setPullable();

        return createdView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity.onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        setRetainInstance(true);
    }

    protected void setPullable() {
        mSwipeRefresh = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe);
        mSwipeRefresh.setColorScheme(R.color.holo_orange, R.color.holo_blue,
                                     R.color.holo_orange, R.color.holo_blue);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPreviousTotal = 0;
                mFetcher.clear();
                mAdapter.clear();
                mSwipeRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    protected void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mSwipeRefresh.setVisibility(View.VISIBLE);
    }
}
