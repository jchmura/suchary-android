package pl.jakubchmura.suchary.android;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.Space;

import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;
import it.gmariotti.cardslib.library.view.listener.SwipeOnScrollListener;
import pl.jakubchmura.suchary.android.gcm.GcmIntentService;
import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.JokeFetcher;
import pl.jakubchmura.suchary.android.joke.card.CardFactory;
import pl.jakubchmura.suchary.android.joke.card.JokeCard;
import pl.jakubchmura.suchary.android.joke.card.JokeCardArrayAdapter;

public abstract class JokesBaseFragment<ActivityClass extends Activity> extends Fragment {

    private static final String TAG = "JokesBaseFragment";
    private static final String PREFS_NAME = GcmIntentService.PREFS_NAME;
    private static final String EDIT_JOKE = GcmIntentService.EDIT_JOKE;
    private static final String DELETE_JOKE = GcmIntentService.DELETE_JOKE;
    private boolean showCrouton = false;

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

    public JokesBaseFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (ActivityClass) activity;
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
            mFetcher = new JokeFetcher(mActivity, this);

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
//                mHeaderView = new Space(mActivity);
//                mHeaderView.setMinimumHeight(10);
//                mCardListView.addHeaderView(mHeaderView);
//
                mFooterView = new ProgressBar(mActivity);
                mCardListView.addFooterView(mFooterView);
                hideProgress();

                mAdapter = new JokeCardArrayAdapter(mActivity, cards);
                mCardListView.setAdapter(mAdapter);
            }
        }
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
                if (showCrouton) {
                    showCroutonNew(size);
                    showCrouton = false;
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
        @SuppressLint("ResourceAsColor") Style style = new Style.Builder().setBackgroundColor(R.color.holo_blue).build();
        Configuration configuration = new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build();
        String croutonText = mActivity.getResources().getQuantityString(R.plurals.new_joke_notification, size, size);
        mCroutonNew = Crouton.makeText(mActivity, croutonText, style);
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

    /**
     * Replace jokes with modified version of them.
     *
     * @param jokes list of jokes with modification
     */
    public void replaceJokes(List<Joke> jokes) {
        if (mAdapter == null) {
            return;
        }

        for (Joke joke : jokes) {
            JokeCard card = mAdapter.getCard(joke.getKey());
            if (card != null) {
                JokeCard newCard = makeCard(joke);
                CardView cardView = card.getCardView();
                if (cardView != null) {
                    cardView.replaceCard(newCard);
                    card.setJoke(joke);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * Delete jokes from list view.
     *
     * @param keys keys of jokes to delete
     */
    public void deleteJokes(String[] keys) {
        if (mAdapter == null) {
            return;
        }

        for (String key : keys) {
            JokeCard card = mAdapter.getCard(key);
            if (card != null) {
                mAdapter.remove(card);
            }
        }
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
        showCrouton = true;
        mFetcher.getNewerFromDB();
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
        }
    }

    /**
     * Enable the pull to refresh swipe. When performed, it fetches to server.
     */
    protected void setPullable() {
        mSwipeRefresh = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe);
        mSwipeRefresh.setColorScheme(R.color.holo_orange, R.color.holo_blue,
                R.color.holo_orange, R.color.holo_blue);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mFetcher.getNewer();
            }
        });
    }

    /**
     * Hide the progress dialog and show the jokes.
     */
    protected abstract void hideProgress();

}
