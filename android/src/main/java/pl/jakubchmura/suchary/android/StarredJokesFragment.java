package pl.jakubchmura.suchary.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.listener.UndoBarController;
import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.card.CardFactory;
import pl.jakubchmura.suchary.android.joke.card.JokeCard;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;


public class StarredJokesFragment extends JokesBaseFragment<MainActivity> {
    private static final String TAG = "StarredJokesFragment";
    private static final String ARG_SECTION_NUMBER = "section_number";

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
        }
        View createdView = createView(saved);
        if (!saved) {
            JokeDbHelper helper = new JokeDbHelper(mActivity);
            List<Joke> jokes = helper.getStarred();
            mFetcher.setJokes(jokes);
            mFetcher.setFetchGetOlder(false);
            mFetcher.fetchNext();
        }
        if (mAdapter != null){
            mAdapter.setEnableUndo(true);
            UndoBarController mUndoBarController = mAdapter.getUndoBarController();
            mUndoBarController.hideUndoBar(true);
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
    public void addJokesToBottom(List<Joke> jokes) {
        super.addJokesToBottom(jokes);
        mAdapter.setEnableUndo(true);
    }

    @Override
    protected JokeCard makeCard(final Joke joke) {
        CardFactory cardFactory = new CardFactory(mActivity);
        Card.OnSwipeListener swipeListener = new Card.OnSwipeListener(){
            @Override
            public void onSwipe(Card card) {
                joke.setStar(false);
                JokeDbHelper helper = new JokeDbHelper(mActivity);
                helper.updateJoke(joke);
            }
        };
        Card.OnUndoSwipeListListener undoSwipeListListener = new Card.OnUndoSwipeListListener(){
            @Override
            public void onUndoSwipe(Card card) {
                joke.setStar(true);
                JokeDbHelper helper = new JokeDbHelper(mActivity);
                helper.updateJoke(joke);
            }
        };
        return cardFactory.getCard(joke, swipeListener, undoSwipeListListener);
    }
}
