package pl.jakubchmura.suchary.android.joke.api.network.requests;

import android.util.Log;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import pl.jakubchmura.suchary.android.joke.api.model.APIResult;
import pl.jakubchmura.suchary.android.joke.api.network.Jokes;

public class ChangedJokesRequest extends RetrofitSpiceRequest<APIResult.APIJokes, Jokes> {

    public static final String TAG = "ChangedJokesRequest";
    private Date mDate;
    private final CountDownLatch mCountDownLatch;

    public ChangedJokesRequest(Date date, CountDownLatch countDownLatch) {
        super(APIResult.APIJokes.class, Jokes.class);
        mDate = date;
        mCountDownLatch = countDownLatch;
    }

    @Override
    public APIResult.APIJokes loadDataFromNetwork() throws Exception {
        Log.d(TAG, "Waiting for " + mCountDownLatch.getCount() + " operations to finish before starting download");
        mCountDownLatch.await();
        String dateString = Jokes.DATE_FORMAT.format(mDate);
        Log.d(TAG, "Downloading jokes changed afer " + dateString);
        APIResult.APIJokes apiJokes = new APIResult.APIJokes();
        APIResult apiResult;
        int page = 1;
        do {
            apiResult = getService().changedJokes(dateString, page);
            apiJokes.addAll(apiResult.getResults());
            page++;
        } while (apiResult.getNext() != null);

        return apiJokes;
    }
}
