package pl.jakubchmura.suchary.android.joke.api.changes;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.joke.api.model.APIJoke;
import pl.jakubchmura.suchary.android.joke.api.model.APIResult;
import pl.jakubchmura.suchary.android.joke.api.network.Jokes;

public class ChangeResolver {

    public static final String PREFS_CHANGE_DATE = "last_change_date";
    public static final String CHANGE_DATE = "change_date";

    private static final String TAG = "ChangeResolver";

    private APIResult.APIJokes mApiJokes;
    private Date mLastChange;

    public ChangeResolver(APIResult.APIJokes apiJokes, Date lastChange) {
        mApiJokes = apiJokes;
        mLastChange = lastChange;
    }

    public static Date getLastChange(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_CHANGE_DATE, Context.MODE_PRIVATE);
        String lastChangeString = sharedPreferences.getString(CHANGE_DATE, "");
        Date lastChange;
        if (lastChangeString.isEmpty()) {
            lastChange = new Date(0);
        } else {
            try {
                lastChange = Jokes.DATE_FORMAT.parse(lastChangeString);
                lastChange = DateUtils.addSeconds(lastChange, 1);
            } catch (ParseException e) {
                lastChange = new Date(0);
            }
        }
        Log.d(TAG, "Last change is: " + Jokes.DATE_FORMAT.format(lastChange));
        return lastChange;
    }


    public static void saveLastChange(Context context, Date date) {
        if (date == null) {
            Log.d(TAG, "Received a null date, not saving last change");
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_CHANGE_DATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CHANGE_DATE, Jokes.DATE_FORMAT.format(date));
        editor.apply();
        Log.i(TAG, "Last change set to: " + Jokes.DATE_FORMAT.format(date));
    }

    public List<Joke> getDeleted() {
        List<Joke> deletedJokes = new LinkedList<>();
        for (APIJoke apiJoke: mApiJokes) {
            if (isDeleted(apiJoke)) {
                deletedJokes.add(apiJoke.getJoke());
            }
        }
        return deletedJokes;
    }

    public List<Joke> getAdded() {
        List<Joke> addedJokes = new LinkedList<>();
        for (APIJoke apiJoke: mApiJokes) {
            if (isAdded(apiJoke)) {
                addedJokes.add(apiJoke.getJoke());
            }
        }
        return addedJokes;
    }

    public List<Joke> getEdited() {
        List<Joke> editedJokes = new LinkedList<>();
        for (APIJoke apiJoke: mApiJokes) {
            if (isEdited(apiJoke)) {
                editedJokes.add(apiJoke.getJoke());
            }
        }
        return editedJokes;
    }

    private boolean isDeleted(APIJoke apiJoke) {
        return apiJoke.getHidden() != null;
    }

    private boolean isAdded(APIJoke apiJoke) {
        return !isDeleted(apiJoke) && apiJoke.getAdded().after(mLastChange);
    }

    private boolean isEdited(APIJoke apiJoke) {
        return !isAdded(apiJoke);
    }
}
