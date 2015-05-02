package pl.jakubchmura.suchary.android.joke;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import pl.jakubchmura.suchary.android.JokesBaseFragment;
import pl.jakubchmura.suchary.android.joke.api.changes.ChangeHandler;
import pl.jakubchmura.suchary.android.joke.api.changes.ChangeResolver;
import pl.jakubchmura.suchary.android.joke.api.model.APIResult;
import pl.jakubchmura.suchary.android.joke.api.network.requests.ChangedJokesRequest;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;
import pl.jakubchmura.suchary.android.util.NetworkHelper;

public class JokeFetcher {

    private static final String TAG = "JokeFetcher";
    private static final int LIMIT = 15;

    private Context mContext;
    private SpiceManager mSpiceManager;
    private JokesBaseFragment<?> mCallback;
    private final List<Joke> mJokes;
    private int mServed = 0;
    private boolean mEnd = false;
    private boolean mGettingFromDB;
    private boolean mCanGetOlder = true;
    private boolean mRandom = false;
    private boolean mOnlyStarred = false;

    public JokeFetcher(Context context, SpiceManager spiceManager, JokesBaseFragment<?> callback) {
        mContext = context;
        mSpiceManager = spiceManager;
        mCallback = callback;
        mJokes = new ArrayList<>();
    }

    public void setContext(Context context) {
        mContext = context;
    }

    /**
     * Get next jokes.
     */
    public void fetchNext() {
        if (mGettingFromDB) return;

        if (mCanGetOlder && (mEnd || mServed == mJokes.size())) {
            if (mRandom) {
                getRandom();
            } else {
                getOlder();
            }
            return;
        }

        List<Joke> jokes = new ArrayList<>();
        int newServed = mServed + LIMIT;
        if (newServed > mJokes.size()) {
            newServed = mJokes.size();
            mEnd = true;
        }

        for (int i = mServed; i < newServed; i++) {
            jokes.add(mJokes.get(i));
        }

        mServed = newServed;
        mCallback.addJokesToBottom(jokes);
        if (!mCanGetOlder) {
            mCallback.endOfData();
        }
    }

    /**
     * Get jokes from the database older than the last present.
     */
    private void getOlder() {
        Date last_date = null;
        if (!mJokes.isEmpty()) {
            Joke last = mJokes.get(mJokes.size() - 1);
            last_date = last.getDate();
        }
        getJokesFromDatabaseBefore(last_date, 15);
    }

    /**
     * Download newer jokes from server than the first present.
     *
     * @see #getNewer(CountDownLatch)
     */
    public void getNewer() {
        getNewer(new CountDownLatch(0));
    }

    /**
     * Download newer jokes from server than the first present.
     * Executes {@link #downloadChangedAfter(Date, CountDownLatch)} if network is online.
     */
    public void getNewer(CountDownLatch countDownLatch) {
        if (!NetworkHelper.isOnline(mContext)) {
            mCallback.setRefreshComplete();
            mCallback.showCroutonOffline();
            return;
        }
        downloadChangedAfter(ChangeResolver.getLastChange(mContext), countDownLatch);
    }

    /**
     * Get next random jokes from database
     */
    public void getRandom() {
        new AsyncTask<Void, Integer, List<Joke>>() {
            @Override
            protected final List<Joke> doInBackground(Void... params) {
                mGettingFromDB = true;
                JokeDbHelper helper = JokeDbHelper.getInstance(mContext);
                return helper.getRandom(15);
            }

            @Override
            protected void onPostExecute(List<Joke> jokes) {
                mGettingFromDB = false;
                mJokes.addAll(jokes);
                fetchNext();
            }
        }.execute((Void[]) null);
    }

    /**
     * Download from server newer jokes than indicated.
     */
    private void downloadChangedAfter(final Date date, CountDownLatch countDownLatch) {
        ChangedJokesRequest request = new ChangedJokesRequest(date, countDownLatch);
        mSpiceManager.execute(request, date, DurationInMillis.ALWAYS_EXPIRED, new RequestListener<APIResult.APIJokes>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                Log.e(TAG, "request failed", spiceException);
            }

            @Override
            public void onRequestSuccess(APIResult.APIJokes apiJokes) {
                ChangeResolver resolver = new ChangeResolver(apiJokes, date);

                showNewerJokes(resolver.getAdded());
                mCallback.replaceJokes(resolver.getEdited());
                deleteJokes(ChangeHandler.getKeys(resolver.getDeleted()));

                ChangeHandler handler = new ChangeHandler(mContext);
                handler.handleAllInBackground(resolver, false);
                ChangeResolver.saveLastChange(mContext, apiJokes.getLastChange());
            }
        });
    }

    /**
     * Get newer jokes from database than the first present.
     */
    public void getNewerFromDB() {
        if (!mJokes.isEmpty()) {
            Joke first = mJokes.get(0);
            getJokesFromDatabaseAfter(first.getDate(), null);
        } else {
            mCallback.getCountDownLatch().countDown();
        }
    }

    /**
     * Get jokes from the database that are older than indicated.
     *
     * @param date  get older jokes than this date
     * @param limit how many jokes to get
     */
    public void getJokesFromDatabaseBefore(final Date date, final Integer limit) {
        new AsyncTask<Void, Integer, Void>() {
            @Override
            protected final Void doInBackground(Void... params) {
                Log.d(TAG, "getJokesFromDatabaseBefore " + date + " with limit " + limit + ". Show only starred? " + mOnlyStarred);
                mGettingFromDB = true;
                JokeDbHelper helper = JokeDbHelper.getInstance(mContext);
                List<Joke> jokes = helper.getBefore(date, limit, mOnlyStarred);
                mJokes.addAll(jokes);

                if (jokes.isEmpty()) {
                    mCanGetOlder = false;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mEnd = false;
                mGettingFromDB = false;
                fetchNext();
            }
        }.execute((Void[]) null);
    }

    /**
     * Get jokes from database and add them to the top.
     *
     * @param date  get newer jokes than this date
     * @param limit how many jokes to get
     */
    public void getJokesFromDatabaseAfter(final Date date, final Integer limit) {
        new AsyncTask<Void, Integer, List<Joke>>() {
            @Override
            protected final List<Joke> doInBackground(Void... params) {
                Log.d(TAG, "getJokesFromDatabaseAfter " + date + " with limit " + limit);
                mGettingFromDB = true;
                JokeDbHelper helper = JokeDbHelper.getInstance(mContext);
                List<Joke> jokes = helper.getAfter(date, limit);
                Log.d(TAG, "Found " + jokes.size() + " new jokes in DB");
                return jokes;
            }

            @Override
            protected void onPostExecute(List<Joke> jokes) {
                mGettingFromDB = false;
                mCallback.addJokesToTop(jokes, false);
                mServed += jokes.size();
                jokes.addAll(mJokes);
                mJokes.clear();
                mJokes.addAll(jokes);
                mCallback.getCountDownLatch().countDown();
            }
        }.execute((Void[]) null);
    }

    /**
     * Add new jokes to the database.
     *
     * @param jokes List of jokes to add
     */
    public void addJokesToDatabase(List<Joke> jokes) {
        new AsyncTask<List<Joke>, Integer, Void>() {
            @SafeVarargs
            @Override
            protected final Void doInBackground(List<Joke>... params) {
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        getClass().getName());
                wl.acquire();
                JokeDbHelper helper = JokeDbHelper.getInstance(mContext);
                helper.createJokes(params[0]);
                wl.release();
                return null;
            }
        }.execute(jokes);
    }

    private void showNewerJokes(List<Joke> jokes) {
        List<Joke> newJokes = new LinkedList<>();
        Collections.sort(jokes, Collections.reverseOrder());
        synchronized (mJokes) {
            for (Joke joke : jokes) {
                if (!mJokes.contains(joke)) {
                    newJokes.add(joke);
                    mJokes.add(0, joke);
                }
            }
            mServed += newJokes.size();
        }

        mCallback.addJokesToTop(newJokes, true);
    }

    /**
     * Update jokes with newest version.
     *
     * @param keys keys of jokes to update
     */
    public void updateJokes(String[] keys) {
        updateJokesFromDatabase(keys);
    }

    /**
     * Update jokes with the version kept in the database.
     *
     * @param keys keys of jokes to update
     */
    private void updateJokesFromDatabase(String[] keys) {
        new AsyncTask<String, Integer, List<Joke>>() {
            @Override
            protected final List<Joke> doInBackground(String... keys) {
                JokeDbHelper helper = JokeDbHelper.getInstance(mContext);
                List<Joke> jokes = helper.getJokes(keys);
                Log.d(TAG, "Found " + jokes.size() + " updated in DB");
                return jokes;
            }

            @Override
            protected void onPostExecute(List<Joke> jokes) {
                mCallback.replaceJokes(jokes);
                mCallback.getCountDownLatch().countDown();
            }
        }.execute(keys);
    }

    /**
     * Delete jokes from memory and screen.
     *
     * @param keys keys of jokes to delete
     */
    public void deleteJokes(String[] keys) {
        Log.d(TAG, "Trying to delete " + keys.length + " jokes from the view");
        for (String key : keys) {
            deleteJoke(key);
        }
        mCallback.deleteJokes(keys);
        mCallback.getCountDownLatch().countDown();
    }

    /**
     * Delete joke from memory.
     *
     * @param key key of joke to delete
     */
    private void deleteJoke(String key) {
        Joke joke = getJoke(key);
        if (joke != null) {
            mJokes.remove(joke);
            mServed--;
        }
    }

    /**
     * Get the jokes that are kept in memory.
     *
     * @return list of jokes
     */
    public List<Joke> getJokes() {
        return mJokes;
    }

    /**
     * Replace jokes in memory with given list
     *
     * @param jokes jokes to replace with
     */
    public void setJokes(List<Joke> jokes) {
        mServed = 0;
        mJokes.clear();
        mJokes.addAll(jokes);
        mEnd = false;
    }

    /**
     * Get the joke from memory
     *
     * @param key key of joke to return
     * @return the joke found or null
     */
    @Nullable
    public Joke getJoke(String key) {
        for (Joke joke : mJokes) {
            if (key.equals(joke.getKey())) return joke;
        }
        return null;
    }

    /**
     * Clear the jokes from memory
     */
    public void clear() {
        mServed = 0;
        mJokes.clear();
        mEnd = false;
    }

    /**
     * Set if can get older jokes than those already displayed
     *
     * @param can whether can or not
     */
    public void setFetchGetOlder(boolean can) {
        mCanGetOlder = can;
    }

    public void setRandom(boolean random) {
        mRandom = random;
    }

    public void setOnlyStarred(boolean onlyStarred) {
        mOnlyStarred = onlyStarred;
    }
}
