package pl.jakubchmura.suchary.android.joke.card;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.joke.Joke;

public class JokeCard extends Card {

    private Joke mJoke;

    public JokeCard(Context context) {
        super(context, R.layout.joke_card_body);
        init();
    }

    protected void init() {
        ViewToClickToExpand viewToClickToExpand =
                ViewToClickToExpand.builder()
                        .highlightView(false)
                        .setupCardElement(ViewToClickToExpand.CardElementUI.CARD);
        this.setViewToClickToExpand(viewToClickToExpand);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        TextView bodyView = (TextView) parent.findViewById(R.id.obcy_joke_card_body);
        if (bodyView != null) {
            bodyView.setText(mJoke.getBody());
        }
    }

    public Joke getJoke() {
        return mJoke;
    }

    public void setJoke(Joke joke) {
        mJoke = joke;
    }
}
