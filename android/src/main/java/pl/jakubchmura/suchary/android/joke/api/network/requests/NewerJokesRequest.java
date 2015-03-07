package pl.jakubchmura.suchary.android.joke.api.network.requests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.Date;

import pl.jakubchmura.suchary.android.joke.api.model.APIResult;
import pl.jakubchmura.suchary.android.joke.api.network.Jokes;

public class NewerJokesRequest extends RetrofitSpiceRequest<APIResult.APIJokes, Jokes> {

    private Date mDate;

    public NewerJokesRequest(Date date) {
        super(APIResult.APIJokes.class, Jokes.class);
        mDate = date;
    }

    @Override
    public APIResult.APIJokes loadDataFromNetwork() throws Exception {
        String dateString = Jokes.DATE_FORMAT.format(mDate);
        APIResult.APIJokes apiJokes = new APIResult.APIJokes();
        APIResult apiResult;
        int page = 1;
        do {
            apiResult = getService().jokes(dateString, page);
            apiJokes.addAll(apiResult.getResults());
            page++;
        } while (apiResult.getNext() != null);

        return apiJokes;
    }
}
