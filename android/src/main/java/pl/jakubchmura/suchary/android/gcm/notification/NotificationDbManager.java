package pl.jakubchmura.suchary.android.gcm.notification;

import android.content.Context;

import java.util.List;

import pl.jakubchmura.suchary.android.joke.Joke;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;
import pl.jakubchmura.suchary.android.sql.NotificationDbHelper;

class NotificationDbManager {

    private final NotificationDbHelper mNotificationHelper;
    private final JokeDbHelper mJokeHelper;

    NotificationDbManager(Context context) {
        mNotificationHelper = NotificationDbHelper.getInstance(context);
        mJokeHelper = JokeDbHelper.getInstance(context);
    }

    List<Joke> getAllJokes() {
        List<String> jokeKeys = mNotificationHelper.getJokeKeys();
        return mJokeHelper.getJokes(jokeKeys.toArray(new String[jokeKeys.size()]));
    }

    void insertJokes(List<Joke> jokes) {
        mNotificationHelper.insertJokes(jokes);
    }

    void removeJokes(List<Joke> jokes) {
        mNotificationHelper.removeJokes(jokes);
    }

    void removeAllJokes() {
        mNotificationHelper.removeAllJokes();
    }
}
