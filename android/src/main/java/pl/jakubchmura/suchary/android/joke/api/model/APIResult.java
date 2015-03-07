package pl.jakubchmura.suchary.android.joke.api.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import pl.jakubchmura.suchary.android.joke.Joke;

public class APIResult {
    private int mCount;
    private String mNext;
    private String mPrevious;
    private APIJokes mResults;

    public APIResult(int count, String next, String previous, APIJokes results) {
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

    public APIJokes getResults() {
        return mResults;
    }

    @Override
    public String toString() {
        return "mNext: " + mNext + "\nmPrevious: " + mPrevious + "\nmResults: " + Arrays.toString(mResults.toArray());
    }

    public static class APIJokes extends LinkedList<APIJoke> {

        public List<Joke> getJokes() {
            List<Joke> jokes = new LinkedList<>();
            for (APIJoke apiJoke: this) {
                jokes.add(apiJoke.getJoke());
            }
            return jokes;
        }
    }
}
