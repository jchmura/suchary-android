package pl.jakubchmura.suchary.android.sql;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import pl.jakubchmura.suchary.android.MainActivity;
import pl.jakubchmura.suchary.android.joke.Joke;
import roboguice.util.temp.Strings;

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

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String TAG = "JokeDbHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Suchary.db";
    private static JokeDbHelper mInstance;

    private Context mContext;

    private JokeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static JokeDbHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new JokeDbHelper(context.getApplicationContext());
        }
        return mInstance;
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

    public synchronized void createJoke(Joke joke) {
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
            notifyCount();
        }
    }

    public synchronized void createJokes(List<Joke> list) {
        String sql = JokeContract.SQL_INSERT_ENTRIES;
        Set<Joke> jokes = new TreeSet<>(list);
        SQLiteDatabase db = getWritableDatabase();

        if (db != null) {
            db.beginTransaction();
            SQLiteStatement statement = db.compileStatement(sql);

            for (Joke joke : jokes) {
                statement.bindString(1, joke.getKey());
                statement.bindString(2, String.valueOf(joke.getVotes()));
                statement.bindString(3, joke.getDateAsString());
                statement.bindString(4, joke.getUrl());
                statement.bindString(5, joke.getBody());
                statement.bindString(6, joke.getSite());
                statement.bindString(7, String.valueOf(joke.getStar()));
                try {
                    statement.executeInsert();
                } catch (SQLiteConstraintException e) {
                    Crashlytics.setString("Joke key", joke.getKey());
                    Crashlytics.logException(e);
                    Log.e(TAG, "Error inserting joke " + joke, e);
                } finally {
                    statement.clearBindings();
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            db.close();
            notifyCount();
        }
    }

    @Nullable
    public synchronized Joke getJoke(String key) {
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

            Joke joke = null;
            if (cursor.moveToFirst()) {
                joke = new Joke(cursor);
            }
            cursor.close();
            db.close();
            return joke;

        }
        return null;
    }

    @NotNull
    public List<Joke> getJokes(String[] keys, boolean sameLength) {
        List<Joke> jokes = new ArrayList<>();
        for (String key : keys) {
            Joke joke = getJoke(key);
            if (joke != null || sameLength) {
                jokes.add(joke);
            }
        }
        return jokes;
    }

    @NotNull
    public List<Joke> getJokes(String[] keys) {
        return getJokes(keys, false);
    }

    @NotNull
    public synchronized List<Joke> getJokes(String selection, String[] selectionArgs, String order, String limit) {
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

    @NotNull
    public List<Joke> getAllJokes() {
        return getJokes(null, null, null, null);
    }

    @NotNull
    public List<Joke> getBefore(Date date, Integer count, boolean onlyStarred) {
        List<String> selection = new LinkedList<>();
        List<String> selectionArgs = new LinkedList<>();
        if (date != null) {
            selection.add(COLUMN_NAME_DATE + " < ?");
            selectionArgs.add(DATE_FORMAT.format(date));
        }
        if (onlyStarred) {
            selection.add(COLUMN_NAME_STAR + " = ?");
            selectionArgs.add("1");
        }
        String limit;
        if (count != null) limit = String.valueOf(count);
        else limit = null;

        return getJokes(Strings.join(" AND ", selection), selectionArgs.toArray(new String[selectionArgs.size()]), COLUMN_NAME_DATE + " DESC", limit);
    }

    @NotNull
    public List<Joke> getAfter(Date date, Integer count) {
        String selection = null;
        String[] selectionArgs = null;
        if (date != null) {
            selection = COLUMN_NAME_DATE + " > ?";
            selectionArgs = new String[]{DATE_FORMAT.format(date)};
        }
        String limit = null;
        if (count != null) limit = String.valueOf(count);

        return getJokes(selection, selectionArgs, COLUMN_NAME_DATE + " DESC", limit);
    }

    @Nullable
    public Joke getNewest() {
        List<Joke> newestJokes = getAfter(null, 1);
        if (!newestJokes.isEmpty()) {
            return newestJokes.get(0);
        }
        return null;
    }

    @NotNull
    public List<Joke> getRandom(int limit) {
        return getJokes(null, null, "Random()", String.valueOf(limit));
    }

    @NotNull
    public List<Joke> searchBody(String query, boolean star) {
        String selection = "( " + COLUMN_NAME_BODY + " LIKE ? COLLATE NOCASE)";
        if (star) {
            selection += " AND " + COLUMN_NAME_STAR + " = 1";
        }
        String[] selectionArgs = new String[]{"%" + query.trim() + "%"};
        return getJokes(selection, selectionArgs, null, null);
    }

    public synchronized void updateJoke(Joke joke) {
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
            notifyCount();
        }
    }

    public void updateJokes(List<Joke> jokes) {
        for (Joke joke : jokes) {
            updateJoke(joke);
        }
    }

    @NotNull
    public List<Joke> getStarred() {
        String selection = COLUMN_NAME_STAR + " = 1";

        return getJokes(selection, null, COLUMN_NAME_DATE + " DESC", null);
    }

    public synchronized long getCount() {
        SQLiteDatabase db = getReadableDatabase();
        long count = 0;
        if (db != null) {
            count = DatabaseUtils.queryNumEntries(db, TABLE_NAME, null);
            db.close();
        }
        return count;
    }

    public synchronized JokeCount getJokeCount() {
        SQLiteDatabase db = getReadableDatabase();
        long total = 0;
        long starred = 0;
        if (db != null) {
            total = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
            starred = DatabaseUtils.queryNumEntries(db, TABLE_NAME, COLUMN_NAME_STAR + " = 1");
            db.close();
        }
        return new JokeCount(total, starred);
    }

    public synchronized void deleteJoke(String key) {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.delete(TABLE_NAME, COLUMN_NAME_KEY + " = ?", new String[]{key});
            db.close();
            notifyCount();
        }
    }

    public synchronized void deleteJokes(String[] keys) {
        if (keys.length == 0) {
            return;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            db.delete(TABLE_NAME, COLUMN_NAME_KEY + " IN (" + new String(new char[keys.length - 1]).replace("\0", "?,") + "?)", keys);
            db.close();
            notifyCount();
        }
    }

    public synchronized void deleteAllJokes() {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            db.delete(TABLE_NAME, null, null);
            db.close();
            notifyCount();
        }
    }

    private void notifyCount() {
        Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_JOKE_COUNT);
        mContext.sendBroadcast(intent);
    }
}
