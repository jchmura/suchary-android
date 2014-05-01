package pl.jakubchmura.suchary.android;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;

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
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public abstract class JokesBaseFragment<ActivityClass extends Activity> extends Fragment {

    private static final String TAG = "JokesBaseFragment";
    private static final String PREFS_NAME = GcmIntentService.PREFS_NAME;
    private static final String EDIT_JOKE = GcmIntentService.EDIT_JOKE;
    private static final String DELETE_JOKE = GcmIntentService.DELETE_JOKE;

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
     * Pull to refresh
     */
    protected PullToRefreshLayout mPullToRefresh;

    /**
     * Progress Bar
     */
    protected ProgressBar mProgress;

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
            mPullToRefresh = (PullToRefreshLayout) mRootView.findViewById(R.id.ptr_layout);
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
            mAdapter = (JokeCardArrayAdapter) mCardListView.getAdapter();
            if (mAdapter != null) {
                mAdapter.addAll(cards);
                mAdapter.notifyDataSetChanged();
            } else {
                mAdapter = new JokeCardArrayAdapter(mActivity, cards);
                mCardListView.setAdapter(mAdapter);
                hideProgress();
            }
        }
    }

    /**
     * Add jokes to the beginning of the card list.
     *
     * @param jokes  list of jokes to add
     * @param silent don't move to top after adding
     */
    public void addJokesToTop(List<Joke> jokes, boolean silent) {
        if (mPullToRefresh != null) {
            mPullToRefresh.setRefreshComplete();
        }
        if (mCardListView != null && mAdapter != null) {
            int size = jokes.size();
            int start = mCardListView.getFirstVisiblePosition();
            for (int i = size - 1; i >= 0; i--) {
                JokeCard card = makeCard(jokes.get(i));
                mAdapter.insert(card, 0);
            }
            if (silent && size > 0) {
                mCardListView.setSelection(start + size);
                showCroutonNew(size);
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
        new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE);
        String croutonText = getResources().getQuantityString(R.plurals.new_joke_notification, size);
        final Crouton crouton = Crouton.makeText(mActivity, croutonText, style);
        crouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCardListView.smoothScrollToPosition(0);
                crouton.cancel();
            }
        });
        Crouton.cancelAllCroutons();
        crouton.show();
    }

    /**
     * Replace jokes with modified version of them.
     *
     * @param jokes list of jokes with modification
     */
    public void replaceJokes(List<Joke> jokes) {
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
        if (mAdapter != null) {
            for (String key : keys) {
                JokeCard card = mAdapter.getCard(key);
                if (card != null) {
                    mAdapter.remove(card);
                }
            }
        }
    }

    /**
     * Call when there is no more older jokes to show.
     */
    public void endOfData() {
        mCardListView.setOnScrollListener(null);
    }

    /**
     * Fetching new jokes is completed.
     */
    public void setRefreshComplete() {
        if (mPullToRefresh != null) {
            mPullToRefresh.setRefreshComplete();
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
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                super.onScrollStateChanged(view, scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
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
        });
    }

    public void checkNewJokes() {
        mFetcher.getNewerFromDB();
    }

    public void checkEditedJokes() {
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(PREFS_NAME, 0);
        String edited = sharedPreferences.getString(EDIT_JOKE, "").trim();
        if (!edited.isEmpty()) {
            String[] editedKeys = edited.split(" ");
            mFetcher.updateJokes(editedKeys);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(EDIT_JOKE, "");
            editor.commit();
        }
    }

    public void checkDeletedJoke() {
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(PREFS_NAME, 0);
        String edited = sharedPreferences.getString(DELETE_JOKE, "").trim();
        if (!edited.isEmpty()) {
            String[] deletedKeys = edited.split(" ");
            mFetcher.deleteJokes(deletedKeys);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DELETE_JOKE, "");
            editor.commit();
        }
    }

    protected void setPullable() {
        ActionBarPullToRefresh.from(mActivity)
            .allChildrenArePullable()
            .listener(new OnRefreshListener() {
                @Override
                public void onRefreshStarted(View view) {
                    mFetcher.getNewer();
                }
            })
            .setup(mPullToRefresh);
    }

    protected abstract void hideProgress();

}
