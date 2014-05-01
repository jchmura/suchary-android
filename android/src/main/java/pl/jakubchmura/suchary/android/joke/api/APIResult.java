package pl.jakubchmura.suchary.android.joke.api;

import java.util.Arrays;
import java.util.List;

import pl.jakubchmura.suchary.android.joke.Joke;

public class APIResult {
    private int mCount;
    private String mNext;
    private String mPrevious;
    private List<Joke> mResults;

    public APIResult(int count, String next, String previous, List<Joke> results) {
        mCount = count;
        mNext = next;
        mPrevious = previous;
        mResults = results;
    }

    public int getCount() {
        return mCount;
    }

    public String getNext() {
        return mNext;
    }

    public String getPrevious() {
        return mPrevious;
    }

    public List<Joke> getResults() {
        return mResults;
    }

    @Override
    public String toString() {
        return "mNext: " + mNext + "\nmPrevious: " + mPrevious + "\nmResults: " + Arrays.toString(mResults.toArray());
    }
}
