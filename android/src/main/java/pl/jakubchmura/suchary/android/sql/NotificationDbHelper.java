package pl.jakubchmura.suchary.android.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.jakubchmura.suchary.android.joke.Joke;

import static pl.jakubchmura.suchary.android.sql.NotificationContract.FeedEntry.COLUMN_ALL;
import static pl.jakubchmura.suchary.android.sql.NotificationContract.FeedEntry.COLUMN_NAME_JOKE;
import static pl.jakubchmura.suchary.android.sql.NotificationContract.FeedEntry.TABLE_NAME;

public class NotificationDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "NotificationDbHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Notifications.db";
    private static NotificationDbHelper mInstance;

    private NotificationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static NotificationDbHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NotificationDbHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(NotificationContract.SQL_CREATE_ENTRIES);
        } catch (SQLException e) {
            Crashlytics.logException(e);
            throw e;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(NotificationContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void insertJokes(@NotNull List<Joke> jokes) {
        String sql = NotificationContract.SQL_INSERT_ENTRIES;
        SQLiteDatabase db = getWritableDatabase();

        if (db != null) {
            db.beginTransaction();
            SQLiteStatement statement = db.compileStatement(sql);

            for (Joke joke : jokes) {
                statement.bindString(1, joke.getKey());
                try {
                    statement.executeInsert();
                } catch (SQLiteConstraintException e) {
                    Crashlytics.logException(e);
                    Log.e(TAG, "Error inserting joke " + joke, e);
                } finally {
                    statement.clearBindings();
                }
            }

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }

    @NotNull
    public List<String> getJokeKeys() {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<String> jokes = new ArrayList<>();
        if (db != null) {
            Cursor cursor = db.query(TABLE_NAME, COLUMN_ALL, null, null, null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                jokes.add(cursor.getString(cursor.getColumnIndexOrThrow(NotificationContract.FeedEntry.COLUMN_NAME_JOKE)));
                cursor.moveToNext();
            }
            cursor.close();
            db.close();
        }

        return jokes;

    }

    public void removeJokes(List<Joke> jokes) {
        int size = jokes.size();
        if (size == 0) {
            return;
        }

        String[] keys = new String[size];
        for (int i = 0; i < size; i++) {
            keys[i] = jokes.get(i).getKey();
        }

        SQLiteDatabase db = this.getWritableDatabase();
        if (db != null) {
            db.beginTransaction();
            try {
                for (int i = 0; i < keys.length; i += 20) {
                    int end = i + 20;
                    if (end > keys.length) {
                        end = keys.length;
                    }
                    String[] transaction = Arrays.copyOfRange(keys, i, end);
                    db.delete(TABLE_NAME, COLUMN_NAME_JOKE + " IN (" + new String(new char[transaction.length - 1]).replace("\0", "?,") + "?)", transaction);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }

        }
    }

    public void removeAllJokes() {
        SQLiteDatabase db = getReadableDatabase();
        if (db != null) {
            db.delete(TABLE_NAME, null, null);
            db.close();
        }
    }
}
