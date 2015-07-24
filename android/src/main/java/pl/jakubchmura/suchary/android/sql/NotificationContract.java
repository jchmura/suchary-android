package pl.jakubchmura.suchary.android.sql;

import android.provider.BaseColumns;

public class NotificationContract {

    public NotificationContract() {
    }

    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "notifications";
        public static final String COLUMN_NAME_JOKE = "joke";

        public static final String[] COLUMN_ALL = {
                COLUMN_NAME_JOKE
        };
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedEntry.COLUMN_NAME_JOKE + " REFERENCES " + JokeContract.FeedEntry.TABLE_NAME + "(" + JokeContract.FeedEntry.COLUMN_NAME_KEY + ")" +
                    " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    public static final String SQL_INSERT_ENTRIES =
            "INSERT INTO " + FeedEntry.TABLE_NAME + " (" + FeedEntry.COLUMN_NAME_JOKE + ") VALUES (?)";
}
