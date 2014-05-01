package pl.jakubchmura.suchary.android.sql;

import android.provider.BaseColumns;

public class JokeContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public JokeContract() {}

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "suchary";
        public static final String COLUMN_NAME_KEY = "key";
        public static final String COLUMN_NAME_VOTES = "votes";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_BODY = "body";
        public static final String COLUMN_NAME_SITE = "site";
        public static final String COLUMN_NAME_STAR = "star";

        public static final String[] COLUMN_ALL = {
                COLUMN_NAME_KEY,
                COLUMN_NAME_VOTES,
                COLUMN_NAME_DATE,
                COLUMN_NAME_URL,
                COLUMN_NAME_BODY,
                COLUMN_NAME_SITE,
                COLUMN_NAME_STAR
        };
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String DATETIME_TYPE = " DATETIME";
    private static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedEntry.COLUMN_NAME_KEY + TEXT_TYPE + " UNIQUE" + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_VOTES + INT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_DATE + DATETIME_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_URL + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_BODY + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_SITE + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_STAR + INT_TYPE +

            " )";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
}
