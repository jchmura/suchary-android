package pl.jakubchmura.suchary.android.joke.card;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;

import it.gmariotti.cardslib.library.internal.Card;
import pl.jakubchmura.suchary.android.R;
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
        card.setOnLongClickListener(new Card.OnLongCardClickListener() {
            @Override
            public boolean onLongClick(Card card, View view) {
                createEditJokeDialog(joke).show();
                return true;
            }
        });
        return card;
    }

    private Dialog createEditJokeDialog(final Joke joke) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mActivity.getString(R.string.manage_joke));
        builder.setItems(new String[]{mActivity.getString(R.string.edit_joke), mActivity.getString(R.string.delete_joke)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                JokeAdmin jokeAdmin = new JokeAdmin(mActivity, joke);
                switch (which) {
                    case 0:
                        jokeAdmin.edit();
                        break;
                    case 1:
                        jokeAdmin.delete();
                }
            }
        });
        return builder.create();
    }
}
