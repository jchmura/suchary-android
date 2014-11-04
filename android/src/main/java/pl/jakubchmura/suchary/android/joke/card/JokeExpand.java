package pl.jakubchmura.suchary.android.joke.card;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.dismissanimation.SwipeDismissAnimation;
import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;
import pl.jakubchmura.suchary.android.util.Analytics;
import pl.jakubchmura.suchary.android.util.FontCache;

public class JokeExpand extends CardExpand {

    private Joke mJoke;
    private Context mContext;
    private SwipeDismissAnimation mDismissAnimation;

    public JokeExpand(Context context, Joke joke) {
        super(context, R.layout.joke_card_expand);
        mContext = context;
        mJoke = joke;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        String typefaceName = "fonts/RobotoCondensed-Regular.ttf";
        Typeface typeface = FontCache.get(typefaceName, mContext);

        TextView tvDate = (TextView) view.findViewById(R.id.date);
        if (tvDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
            tvDate.setText(dateFormat.format(mJoke.getDate()));
            tvDate.setTypeface(typeface);
        }

        ImageView ivStar = (ImageView) view.findViewById(R.id.star);
        if (ivStar != null) {
            colorStar(ivStar);
            ivStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Analytics.clickedStarred(mJoke.getKey());
                    mJoke.setStar(!mJoke.isStar());
                    colorStar((ImageView) v);
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            JokeDbHelper helper = new JokeDbHelper(mContext);
                            helper.updateJoke(mJoke);
                            return null;
                        }
                    }.execute((Void) null);
                    if (mDismissAnimation != null && mParentCard != null) {
//                        mDismissAnimation.animateDismiss(mParentCard);
                    }
                }
            });
            ivStar.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext, R.string.joke_star, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        ImageView ivShare = (ImageView) view.findViewById(R.id.share);
        if (ivShare != null) {
            ivShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Analytics.clickedShare(mJoke.getKey());
                    Intent share = new Intent();
                    share.setAction(Intent.ACTION_SEND);
                    share.putExtra(Intent.EXTRA_TEXT, mJoke.getBody());
                    share.setType("text/plain");
                    mContext.startActivity(Intent.createChooser(share,
                            mContext.getResources().getText(R.string.joke_share)));
                }
            });
            ivShare.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext, R.string.joke_share, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

        ImageView ivSource = (ImageView) view.findViewById(R.id.source);
        if (ivSource != null) {
            ivSource.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Analytics.clickedOriginal(mJoke.getKey());
                    Uri uri = Uri.parse(mJoke.getUrl());
                    openBrowser(uri);
                }

                private void openBrowser(Uri uri) {
                    Intent browse = new Intent(Intent.ACTION_VIEW, uri);
                    browse.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    browse.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(browse);
                }
            });
            ivSource.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext, R.string.joke_source, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }

    public void setDismissAnimation(final SwipeDismissAnimation dismissAnimation) {
        mDismissAnimation = dismissAnimation;
    }

    private void colorStar(ImageView view) {
        if (mJoke.isStar()) {
            view.setImageResource(R.drawable.fav_on_button);
        } else {
            view.setImageResource(R.drawable.fav_off_button);
        }
    }
}
