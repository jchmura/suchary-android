package pl.jakubchmura.suchary.android.sql;

public class JokeCount {

    private long mTotal;
    private long mStarred;

    public JokeCount(long total, long starred) {
        mTotal = total;
        mStarred = starred;
    }

    public long getTotal() {
        return mTotal;
    }

    public long getStarred() {
        return mStarred;
    }

    @Override
    public String toString() {
        return "total=" + mTotal + ", starred=" + mStarred;
    }
}
