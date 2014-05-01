package pl.jakubchmura.suchary.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


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
            mRootView = inflater.inflate(R.layout.fragment_all, container, false);
            saved = false;
        }

        LinearLayout undoLayout = (LinearLayout) mActivity.findViewById(R.id.list_card_undobar);
        undoLayout.setVisibility(View.GONE);

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
        ActionBarPullToRefresh.from(mActivity)
            .allChildrenArePullable()
            .listener(new OnRefreshListener() {
                @Override
                public void onRefreshStarted(View view) {
                    mPreviousTotal = 0;
                    mFetcher.clear();
                    mAdapter.clear();
                    mPullToRefresh.setRefreshComplete();
                }
            })
            .setup(mPullToRefresh);
    }

    @Override
    protected void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mPullToRefresh.setVisibility(View.VISIBLE);
    }
}
