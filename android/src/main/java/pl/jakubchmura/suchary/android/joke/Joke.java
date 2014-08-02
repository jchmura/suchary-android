package pl.jakubchmura.suchary.android.joke;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pl.jakubchmura.suchary.android.sql.JokeContract;

public class Joke implements Parcelable {
    public static final Creator<Joke> CREATOR = new Creator<Joke>() {
        @Override
        public Joke createFromParcel(Parcel source) {
            return new Joke(source);
        }

        @Override
        public Joke[] newArray(int size) {
            return new Joke[0];
        }
    };
    private String mKey;
    private int mVotes;
    private Date mDate;
    private String mUrl;
    private String mBody;
    private String mSite;
    private boolean mStar;

    public Joke() {
    }

    public Joke(Cursor cursor) {
        mKey = cursor.getString(cursor.getColumnIndexOrThrow(JokeContract.FeedEntry.COLUMN_NAME_KEY));
        mVotes = cursor.getInt(cursor.getColumnIndexOrThrow(JokeContract.FeedEntry.COLUMN_NAME_VOTES));
        mUrl = cursor.getString(cursor.getColumnIndexOrThrow(JokeContract.FeedEntry.COLUMN_NAME_URL));
        mBody = cursor.getString(cursor.getColumnIndexOrThrow(JokeContract.FeedEntry.COLUMN_NAME_BODY));
        mSite = cursor.getString(cursor.getColumnIndexOrThrow(JokeContract.FeedEntry.COLUMN_NAME_SITE));
        int starInt = cursor.getInt(cursor.getColumnIndexOrThrow(JokeContract.FeedEntry.COLUMN_NAME_STAR));
        mStar = starInt == 1;

        String dateString = cursor.getString(cursor.getColumnIndexOrThrow(JokeContract.FeedEntry.COLUMN_NAME_DATE));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            mDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            mDate = new Date();
        }
    }

    public Joke(String key, int votes, Date date, String url, String body, String site, boolean star) {
        mKey = key;
        mVotes = votes;
        mDate = date;
        mUrl = url;
        mBody = body;
        mSite = site;
        mStar = star;
    }

    public Joke(String key, int votes, Date date, String url, String body, String site) {
        this(key, votes, date, url, body, site, false);
    }

    public Joke(Parcel in) {
        int[] intArray = new int[2];
        in.readIntArray(intArray);
        mVotes = intArray[0];
        mStar = intArray[1] == 1;
        DateFormat format = DateFormat.getDateTimeInstance();
        String[] stringArray = new String[6];
        in.readStringArray(stringArray);
        mKey = stringArray[0];
        try {
            mDate = format.parse(stringArray[1]);
        } catch (ParseException e) {
            e.printStackTrace();
            mDate = new Date();
        }
        mUrl = stringArray[3];
        mBody = stringArray[4];
        mSite = stringArray[5];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(new int[]{mVotes, (mStar) ? 1 : 0});
        DateFormat format = DateFormat.getDateTimeInstance();
        String dateString = format.format(mDate);
        String[] stringArray = {mKey, dateString, mUrl, mBody, mSite};
        dest.writeStringArray(stringArray);
    }

    public String getDateAsString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(mDate);
    }

    @Override
    public String toString() {
        return "joke: " + mBody;
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        this.mKey = key;
    }

    public int getVotes() {
        return mVotes;
    }

    public void setVotes(int mVotes) {
        this.mVotes = mVotes;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        this.mBody = body;
    }

    public String getSite() {
        return mSite;
    }

    public void setSite(String site) {
        this.mSite = site;
    }

    public boolean isStar() {
        return mStar;
    }

    public int getStar() {
        return mStar ? 1 : 0;
    }

    public void setStar(boolean star) {
        this.mStar = star;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Joke)) return false;
        Joke other = (Joke) obj;
        return mKey.equals(other.getKey());
    }

    @Override
    public int hashCode() {
        final int prime = 19937;
        return prime * mKey.hashCode();
    }
}
