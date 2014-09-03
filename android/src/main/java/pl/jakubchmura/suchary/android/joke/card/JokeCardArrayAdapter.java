package pl.jakubchmura.suchary.android.joke.card;

import android.content.Context;

import java.util.HashMap;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;

public class JokeCardArrayAdapter extends CardArrayAdapter {
    protected HashMap<String, JokeCard> mCards;

    public JokeCardArrayAdapter(Context context, List<Card> cards) {
        super(context, cards);
        populateCards();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        populateCards();
    }

    protected void populateCards() {
        mCards = new HashMap<>();

        for (int i = 0; i < getCount(); i++) {
            JokeCard card = (JokeCard) getItem(i);
            String key = card.getJoke().getKey();
            mCards.put(key, card);
        }
    }

    public JokeCard getCard(String key) {
        return mCards.get(key);
    }
}
