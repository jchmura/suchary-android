package pl.jakubchmura.suchary.android.joke.api.network.requests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import pl.jakubchmura.suchary.android.joke.api.model.APIResult;
import pl.jakubchmura.suchary.android.joke.api.network.Jokes;

public class AllJokesRequest extends RetrofitSpiceRequest<APIResult.APIJokes, Jokes> {

    private int mLimit = -1;
    private ProgressListener mProgressListener;
    private boolean mFinished = false;

    public AllJokesRequest() {
        super(APIResult.APIJokes.class, Jokes.class);
    }

    public AllJokesRequest(int limit) {
        this();
        mLimit = limit;
    }

    @Override
    public APIResult.APIJokes loadDataFromNetwork() throws Exception {
        APIResult.APIJokes apiJokes = new APIResult.APIJokes();
        APIResult apiResult;
        int page = 1;
        do {
            if (mLimit > 0) {
                apiResult = getService().jokes(mLimit, page);
            } else {
                apiResult = getService().jokes(page);
            }
            APIResult.APIJokes pageJokes = apiResult.getResults();
            if (mProgressListener != null) {
                mProgressListener.setMaxProgress(apiResult.getCount());
                mProgressListener.incrementProgress(pageJokes.size());
            }
            apiJokes.addAll(pageJokes);
            page++;
        } while (apiResult.getNext() != null);

        mFinished = true;
        return apiJokes;
    }

    public void setProgressListener(ProgressListener progressListener) {
        mProgressListener = progressListener;
    }

    public boolean isFinished() {
        return mFinished;
    }

    public static interface ProgressListener {
        void setMaxProgress(int max);

        void incrementProgress(int delta);
    }
}
