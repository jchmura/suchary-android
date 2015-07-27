package pl.jakubchmura.suchary.android.joke.card;

import android.app.Activity;

import it.gmariotti.cardslib.library.internal.Card;
import pl.jakubchmura.suchary.android.joke.Joke;


public class CardFactory {

    private static final String TAG = "CardFactory";
    private final Activity mActivity;

    public CardFactory(Activity activity) {
        mActivity = activity;
    }

    public JokeCard getCard(Joke joke) {
        return makeCard(joke);
    }

    public JokeCard getCard(Joke joke, Card.OnSwipeListener onSwipeListener,
                            Card.OnUndoSwipeListListener undoSwipeListListener) {
        JokeCard card = makeCard(joke);
        card.setSwipeable(true);
        card.setId(joke.getKey());
        card.setOnSwipeListener(onSwipeListener);
        card.setOnUndoSwipeListListener(undoSwipeListListener);
        return card;
    }

    protected JokeCard makeCard(final Joke joke) {
        JokeCard card = new JokeCard(mActivity);
        JokeExpand expand = new JokeExpand(mActivity, joke);
        card.addCardExpand(expand);
        card.setJoke(joke);
        return card;
    }
}
