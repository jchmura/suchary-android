package pl.jakubchmura.suchary.android;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.dismissanimation.SwipeDismissAnimation;
import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.card.CardFactory;
import pl.jakubchmura.suchary.android.joke.card.JokeCard;
import pl.jakubchmura.suchary.android.joke.card.JokeExpand;
import pl.jakubchmura.suchary.android.sql.JokeCount;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;
import pl.jakubchmura.suchary.android.util.FontCache;


public class StarredJokesFragment extends JokesBaseFragment<MainActivity> {
    private static final String TAG = "StarredJokesFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";

    protected SwipeDismissAnimation mDismissAnimation;

    public static StarredJokesFragment newInstance(int sectionNumber) {
        StarredJokesFragment fragment = new StarredJokesFragment();
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
            mRootView = inflater.inflate(R.layout.fragment_starred, container, false);
            saved = false;
        } else {
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (parent != null) {
                parent.removeView(mRootView);
            }
        }

        View createdView = createView(saved);
        if (!saved) {
            mDismissAnimation = new SwipeDismissAnimation(mActivity);
            getJokes();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void addJokesToBottom(List<Joke> jokes) {
        super.addJokesToBottom(jokes);
        if (!mAdapter.isEnableUndo()) {
            mAdapter.setEnableUndo(true);
            mDismissAnimation.setup(mAdapter);
        }
    }

    protected void getJokes() {
        new AsyncTask<Void, Void, JokeCount>() {
            @Override
            protected JokeCount doInBackground(Void... params) {
                JokeDbHelper helper = JokeDbHelper.getInstance(mActivity);
                return helper.getJokeCount();
            }

            @Override
            protected void onPostExecute(JokeCount jokeCount) {
                mProgress.setVisibility(View.GONE);
                if (jokeCount.getStarred() == 0) {
                    showPlaceholder();
                    setRetainInstance(false);
                } else {
                    mFetcher.setOnlyStarred(true);
                    mFetcher.fetchNext();
                    setRetainInstance(true);
                }
            }
        }.execute((Void) null);
    }

    @Override
    protected JokeCard makeCard(final Joke joke) {
        CardFactory cardFactory = new CardFactory(mActivity);
        Card.OnSwipeListener swipeListener = new Card.OnSwipeListener() {
            @Override
            public void onSwipe(Card card) {
                joke.setStar(false);
                JokeDbHelper helper = JokeDbHelper.getInstance(mActivity);
                helper.updateJoke(joke);
                if (mAdapter.getCount() == 0) {
                    showPlaceholder();
                    setRetainInstance(false);
                }
            }
        };
        Card.OnUndoSwipeListListener undoSwipeListListener = new Card.OnUndoSwipeListListener() {
            @Override
            public void onUndoSwipe(Card card) {
                joke.setStar(true);
                JokeDbHelper helper = JokeDbHelper.getInstance(mActivity);
                helper.updateJoke(joke);
                if (mAdapter.getCount() > 0) {
                    hidePlaceholder();
                    setRetainInstance(true);
                }

            }
        };
        JokeCard card = cardFactory.getCard(joke, swipeListener, undoSwipeListListener);
        JokeExpand expand = (JokeExpand) card.getCardExpand();
        expand.setDismissAnimation(mDismissAnimation);
        return card;
    }

    protected void showPlaceholder() {
        LinearLayout placeholder = (LinearLayout) mRootView.findViewById(R.id.placeholder);
        placeholder.setVisibility(View.VISIBLE);

        TextView placeholderText = (TextView) placeholder.findViewById(R.id.placeholder_text);
        String typefaceName = "fonts/RobotoCondensed-Light.ttf";
        Typeface typeface = FontCache.get(typefaceName, mActivity);
        placeholderText.setTypeface(typeface);
        mCardListView.setVisibility(View.GONE);
    }

    protected void hidePlaceholder() {
        LinearLayout placeholder = (LinearLayout) mRootView.findViewById(R.id.placeholder);
        placeholder.setVisibility(View.GONE);
        mCardListView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideProgress() {
        mProgress.setVisibility(View.GONE);
        mCardListView.setVisibility(View.VISIBLE);
    }
}
