package pl.jakubchmura.suchary.android;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Space;

import com.octo.android.robospice.SpiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.LifecycleCallback;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardViewNative;
import it.gmariotti.cardslib.library.view.listener.SwipeOnScrollListener;
import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.JokeFetcher;
import pl.jakubchmura.suchary.android.joke.api.changes.ChangeHandler;
import pl.jakubchmura.suchary.android.joke.api.network.JokeRetrofitSpiceService;
import pl.jakubchmura.suchary.android.joke.card.CardFactory;
import pl.jakubchmura.suchary.android.joke.card.JokeCard;
import pl.jakubchmura.suchary.android.joke.card.JokeCardArrayAdapter;

public abstract class JokesBaseFragment<ActivityClass extends Activity> extends Fragment {

    private static final String TAG = "JokesBaseFragment";
    private static final String PREFS_NAME = ChangeHandler.PREFS_NAME;
    private static final String EDIT_JOKE = ChangeHandler.EDIT_JOKE;
    private static final String DELETE_JOKE = ChangeHandler.DELETE_JOKE;
    private boolean showCroutonNew = false;
    private Crouton mCroutonOffline = null;


    /**
     * Activity which attached this fragment
     */
    protected ActivityClass mActivity;

    /**
     * Root view of everything in fragment
     */
    protected View mRootView;

    /**
     * Card list view
     */
    protected CardListView mCardListView;

    /**
     * ListView footer
     */
    protected ProgressBar mFooterView;

    /**
     * ListView header
     */
    protected Space mHeaderView;

    /**
     * Adapter for the {@link #mCardListView}
     */
    protected JokeCardArrayAdapter mAdapter;

    /**
     * Number of shown cards. Used in {@link #setScrollListener()}
     */
    protected int mPreviousTotal;

    /**
     * Fetcher used to get new jokes
     */
    protected JokeFetcher mFetcher;

    /**
     * Swipe to refresh
     */
    protected SwipeRefreshLayout mSwipeRefresh;

    /**
     * Progress Bar
     */
    protected ProgressBar mProgress;

    /**
     * Crouton informing about new jokes
     */
    private Crouton mCroutonNew;

    /**
     * SpiceManager used for downloading content from the server
     */
    protected SpiceManager mSpiceManager = new SpiceManager(JokeRetrofitSpiceService.class);

    /**
     * CyclicBarrier used for checking the database for changes before requesting them from the server
     */
    protected CountDownLatch mCountDownLatch = new CountDownLatch(0);

    public JokesBaseFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (ActivityClass) activity;
        if (mFetcher != null) {
            mFetcher.setContext(mActivity);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mSpiceManager.start(getActivity());
    }

    @Override
    public void onStop() {
        if (mSpiceManager.isStarted()) {
            mSpiceManager.shouldStop();
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Crouton.cancelAllCroutons();
    }

    /**
     * Create basic elements in view. Call in onCreateView of subclasses.
     */
    public View createView(boolean saved) {
        if (!saved) {
            mCardListView = (CardListView) mRootView.findViewById(R.id.cardList);
            mProgress = (ProgressBar) mRootView.findViewById(R.id.progress);
            mFetcher = new JokeFetcher(mActivity, mSpiceManager, this);

            setScrollListener();
        }

        return mRootView;
    }

    /**
     * Add jokes to the end of the card list.
     *
     * @param jokes list of jokes to add
     */
    public void addJokesToBottom(List<Joke> jokes) {
        List<Card> cards = makeCardArray(jokes);

        if (mCardListView != null) {
            if (mAdapter != null) {
                mAdapter.addAll(cards);
                mAdapter.notifyDataSetChanged();
            } else {
                mFooterView = new ProgressBar(mActivity);
                mCardListView.addFooterView(mFooterView);
                hideProgress();

                mAdapter = new JokeCardArrayAdapter(mActivity, cards);
                mCardListView.setAdapter(mAdapter);
                mCountDownLatch.countDown();
            }
        }
    }

    /**
     * Whether to show new jokes at the top
     *
     * @return can add new jokes to the top
     */
    protected boolean showNewJokes() {
        return false;
    }

    /**
     * Add jokes to the beginning of the card list.
     * @param jokes list of jokes to add
     * @param move  move to top after adding
     */
    public void addJokesToTop(List<Joke> jokes, boolean move) {
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setRefreshing(false);
        }
        if (mCardListView != null && mAdapter != null) {
            int size = jokes.size();
            int start = mCardListView.getFirstVisiblePosition();
            for (int i = size - 1; i >= 0; i--) {
                JokeCard card = makeCard(jokes.get(i));
                mAdapter.insert(card, 0);
            }
            if (move && size > 0) {
                mCardListView.setSelection(start + size);
                if (showCroutonNew) {
                    showCroutonNew(size);
                    showCroutonNew = false;
                }
            }
        }
    }

    /**
     * Show Crouton informing about new jokes
     *
     * @param size how many jokes was added
     */
    private void showCroutonNew(int size) {
        @SuppressLint("ResourceAsColor") Style style = new Style.Builder().setBackgroundColor(R.color.deep_orange_500).build();
        Configuration configuration = new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build();
        String croutonText = mActivity.getResources().getQuantityString(R.plurals.new_joke_notification, size, size);
        mCroutonNew = Crouton.makeText(mActivity, croutonText, style, (android.view.ViewGroup) mRootView);
        mCroutonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCardListView.smoothScrollToPosition(0);
                mCroutonNew.hide();
            }
        });
        mCroutonNew.setConfiguration(configuration);
        Crouton.cancelAllCroutons();
        mCroutonNew.show();
    }

    public void showCroutonOffline() {
        if (mCroutonOffline == null) {
            @SuppressLint("ResourceAsColor") Style style = new Style.Builder().setBackgroundColor(R.color.indigo_600).build();
            mCroutonOffline = Crouton.makeText((Activity) mActivity, R.string.no_internet_connection, style);
            mCroutonOffline.setLifecycleCallback(new LifecycleCallback() {
                @Override
                public void onDisplayed() {
                }

                @Override
                public void onRemoved() {
                    mCroutonOffline = null;
                }
            });
            mCroutonOffline.show();
        }
    }

    /**
     * Replace jokes with modified version of them.
     *
     * @param jokes list of jokes with modification
     */
    public void replaceJokes(List<Joke> jokes) {
        if (mAdapter == null) {
            Log.d(TAG, "Adapter is not set yet, cards can't be replaced");
            return;
        }

        int successfullyReplaced = 0;
        for (Joke joke : jokes) {
            JokeCard card = mAdapter.getCard(joke.getKey());
            if (card != null) {
                JokeCard newCard = makeCard(joke);
                CardViewNative cardView = (CardViewNative) card.getCardView();
                if (cardView != null) {
                    cardView.replaceCard(newCard);
                    card.setJoke(joke);
                    mAdapter.notifyDataSetChanged();
                    successfullyReplaced++;
                }
            }
        }
        Log.d(TAG, "Successfully replaced " + successfullyReplaced + " joke(s) out of " + jokes.size());
    }

    /**
     * Delete jokes from list view.
     *
     * @param keys keys of jokes to delete
     */
    public void deleteJokes(String[] keys) {
        if (mAdapter == null) {
            Log.d(TAG, "Adapter is not set yet, cards can't be deleted");
            return;
        }

        int successfullyDeleted = 0;
        for (String key : keys) {
            JokeCard card = mAdapter.getCard(key);
            if (card != null) {
                mAdapter.remove(card);
                successfullyDeleted++;
            }
        }
        Log.d(TAG, "Successfully deleted " + successfullyDeleted + " joke(s) out of " + keys.length);
    }

    /**
     * Call when there is no more older jokes to show.
     */
    public void endOfData() {
        mCardListView.setOnScrollListener(null);
        mCardListView.removeFooterView(mFooterView);
    }

    /**
     * Fetching new jokes is completed.
     */
    public void setRefreshComplete() {
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    /**
     * Make cards for the list of jokes.
     *
     * @param jokes jokes to make the cards of
     * @return list of generated cards
     */
    protected List<Card> makeCardArray(List<Joke> jokes) {
        ArrayList<Card> cards = new ArrayList<>();
        for (Joke joke : jokes) {
            Card card = makeCard(joke);
            cards.add(card);
        }
        return cards;
    }

    /**
     * Make card for the given joke.
     *
     * @param joke joke to make the card of
     * @return Card of the joke
     */
    protected JokeCard makeCard(Joke joke) {
        CardFactory cardFactory = new CardFactory(mActivity);
        return cardFactory.getCard(joke);
    }

    /**
     * Set scroll listener to fetch new jokes when coming to the bottom of the list.
     */
    protected void setScrollListener() {
        mPreviousTotal = 0;
        mCardListView.setOnScrollListener(new SwipeOnScrollListener() {
            private int visibleThreshold = 3;
            private boolean loading = true;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                // fix SwipeRefreshLayout starting too soon
                if (mSwipeRefresh != null) {
                    int topRowVerticalPosition = (mCardListView == null || mCardListView.getChildCount() == 0) ? 0 : mCardListView.getChildAt(0).getTop();
                    mSwipeRefresh.setEnabled(topRowVerticalPosition >= 0);
                }

                if (firstVisibleItem == 1 && mCroutonNew != null) {
                    mCroutonNew.hide();
                }

                if (loading) {
                    if (totalItemCount > mPreviousTotal) {
                        loading = false;
                        mPreviousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <=
                        (firstVisibleItem + visibleThreshold)) {
                    mFetcher.fetchNext();
                    loading = true;
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                super.onScrollStateChanged(view, scrollState);
            }
        });
    }

    /**
     * Check for newer jokes in DB.
     */
    public void checkNewJokes() {
        if (showNewJokes()) {
            showCroutonNew = true;
            mFetcher.getNewerFromDB();
        } else {
            mCountDownLatch.countDown();
        }
    }

    /**
     * Check if jokes were edited and update then.
     */
    public void checkEditedJokes() {
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(PREFS_NAME, 0);
        String edited = sharedPreferences.getString(EDIT_JOKE, "").trim();
        if (!edited.isEmpty()) {
            String[] editedKeys = edited.split(" ");
            mFetcher.updateJokes(editedKeys);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(EDIT_JOKE, "");
            editor.apply();
        } else {
            mCountDownLatch.countDown();
        }
    }

    /**
     * Check if jokes were set to be deleted and perform the removal.
     */
    public void checkDeletedJoke() {
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(PREFS_NAME, 0);
        String edited = sharedPreferences.getString(DELETE_JOKE, "").trim();
        if (!edited.isEmpty()) {
            String[] deletedKeys = edited.split(" ");
            mFetcher.deleteJokes(deletedKeys);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DELETE_JOKE, "");
            editor.apply();
        } else {
            mCountDownLatch.countDown();
        }
    }

    /**
     * Enable the pull to refresh swipe. When performed, it fetches to server.
     */
    protected void setPullable() {
        mSwipeRefresh = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe);
        mSwipeRefresh.setColorSchemeResources(R.color.light_blue_500, R.color.deep_orange_500);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mFetcher.getNewer();
            }
        });
    }


    public CountDownLatch getCountDownLatch() {
        return mCountDownLatch;
    }

    /**
     * Hide the progress dialog and show the jokes.
     */
    protected abstract void hideProgress();

}
