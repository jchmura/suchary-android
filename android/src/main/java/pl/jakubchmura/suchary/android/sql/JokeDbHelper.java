package pl.jakubchmura.suchary.android.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.jakubchmura.suchary.android.joke.Joke;

import static pl.jakubchmura.suchary.android.sql.JokeContract.FeedEntry.COLUMN_ALL;
import static pl.jakubchmura.suchary.android.sql.JokeContract.FeedEntry.COLUMN_NAME_BODY;
import static pl.jakubchmura.suchary.android.sql.JokeContract.FeedEntry.COLUMN_NAME_DATE;
import static pl.jakubchmura.suchary.android.sql.JokeContract.FeedEntry.COLUMN_NAME_KEY;
import static pl.jakubchmura.suchary.android.sql.JokeContract.FeedEntry.COLUMN_NAME_SITE;
import static pl.jakubchmura.suchary.android.sql.JokeContract.FeedEntry.COLUMN_NAME_STAR;
import static pl.jakubchmura.suchary.android.sql.JokeContract.FeedEntry.COLUMN_NAME_URL;
import static pl.jakubchmura.suchary.android.sql.JokeContract.FeedEntry.COLUMN_NAME_VOTES;
import static pl.jakubchmura.suchary.android.sql.JokeContract.FeedEntry.TABLE_NAME;


public class JokeDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "JokeDbHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Suchary.db";

    public JokeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(JokeContract.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(JokeContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void createJoke(Joke joke){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_KEY, joke.getKey());
        values.put(COLUMN_NAME_VOTES, joke.getVotes());
        values.put(COLUMN_NAME_BODY, joke.getBody());
        values.put(COLUMN_NAME_SITE, joke.getSite());
        values.put(COLUMN_NAME_URL, joke.getUrl());
        values.put(COLUMN_NAME_DATE, joke.getDateAsString());
        values.put(COLUMN_NAME_STAR, joke.getStar());

        if (db != null) {
            db.insert(
                TABLE_NAME,
                null,
                values);
            db.close();
        }
    }

    public void createJokes(List<Joke> list) {
        for (Joke joke: list) {
            createJoke(joke);
        }
    }

    @Nullable
    public Joke getJoke(String key) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor;
        if (db != null) {
            cursor = db.query(
                    TABLE_NAME,
                    COLUMN_ALL,
                    COLUMN_NAME_KEY + "=?",
                    new String[]{key},
                    null,
                    null,
                    null
            );


            cursor.moveToFirst();

            Joke joke = new Joke(cursor);
            cursor.close();
            db.close();
            return joke;
        }
        return null;
    }

    public List<Joke> getJokes(String[] keys) {
        List<Joke> jokes = new ArrayList<>();
        for (String key: keys) {
            jokes.add(getJoke(key));
        }
        return jokes;
    }

    public List<Joke> getJokes(String selection, String[] selectionArgs, String order, String limit) {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<Joke> jokes = new ArrayList<>();
        if (db != null) {
            Cursor cursor = db.query(
                    TABLE_NAME,
                    COLUMN_ALL,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    order,
                    limit
            );

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                jokes.add(new Joke(cursor));
                cursor.moveToNext();
            }

            cursor.close();
            db.close();
        }

        return jokes;

    }

    public List<Joke> getAllJokes() {
        return getJokes(null, null, null, null);
    }

    public List<Joke> getBefore(Date date, Integer count) {
        String selection = null;
        String[] selectionArgs = null;
        if (date != null) {
            selection = COLUMN_NAME_DATE + " < ?";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            selectionArgs = new String[]{dateFormat.format(date)};
        }
        String limit;
        if (count != null) limit = String.valueOf(count);
        else limit = null;

        return getJokes(selection, selectionArgs, COLUMN_NAME_DATE + " DESC", limit);
    }

    public List<Joke> getAfter(Date date, Integer count) {
        String selection = null;
        String[] selectionArgs = null;
        if (date != null) {
            selection = COLUMN_NAME_DATE + " > ?";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            selectionArgs = new String[]{dateFormat.format(date)};
        }
        String limit = null;
        if (count != null) limit = String.valueOf(count);

        return getJokes(selection, selectionArgs, COLUMN_NAME_DATE + " DESC", limit);
    }

    @Nullable
    public Joke getNewest() {
        List<Joke> newestJokes = getAfter(null, 1);
        if (newestJokes != null) {
            return newestJokes.get(0);
        }
        return null;
    }

    public List<Joke> getRandom(int limit) {
        return getJokes(null, null, "Random()", String.valueOf(limit));
    }

    public List<Joke> searchBody(String query, boolean star) {
        String selection = "(" + COLUMN_NAME_BODY + " LIKE ?" + " OR " + COLUMN_NAME_BODY + " LIKE ?)";
        if (star) {
            selection += "AND ( " + COLUMN_NAME_STAR + " = 1)";
        }
        String[] selectionArgs = new String[] {query + "%", "% " + query + "%"};
        return getJokes(selection, selectionArgs, null, null);
    }

    public void updateJoke(Joke joke) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_KEY, joke.getKey());
        values.put(COLUMN_NAME_VOTES, joke.getVotes());
        values.put(COLUMN_NAME_BODY, joke.getBody());
        values.put(COLUMN_NAME_SITE, joke.getSite());
        values.put(COLUMN_NAME_URL, joke.getUrl());
        values.put(COLUMN_NAME_DATE, joke.getDateAsString());
        values.put(COLUMN_NAME_STAR, joke.getStar());

        if (db != null) {
            db.update(
                    TABLE_NAME,
                    values,
                    COLUMN_NAME_KEY + "=?",
                    new String[]{joke.getKey()}
            );
            db.close();
        }
    }

    public void updateJokes(List<Joke> jokes) {
        for (Joke joke: jokes) {
            updateJoke(joke);
        }
    }

    public List<Joke> getStarred() {
        String selection = COLUMN_NAME_STAR + " = 1";

        return getJokes(selection, null, COLUMN_NAME_DATE + " DESC", null);
    }

    public long getCount() {
        SQLiteDatabase db = getReadableDatabase();
        long count = 0;
        if (db != null) {
            count = DatabaseUtils.queryNumEntries(db, TABLE_NAME, null);
            db.close();
        }
        return count;
    }

    public void deleteJoke(String key) {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.delete(TABLE_NAME, COLUMN_NAME_KEY + " = ?", new String[]{key});
        }
    }
}
