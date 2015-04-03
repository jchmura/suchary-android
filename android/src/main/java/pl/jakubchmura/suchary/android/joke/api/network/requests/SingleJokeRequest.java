package pl.jakubchmura.suchary.android.joke.api.network.requests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import pl.jakubchmura.suchary.android.joke.api.model.APIJoke;
import pl.jakubchmura.suchary.android.joke.api.network.Jokes;

public class SingleJokeRequest extends RetrofitSpiceRequest<APIJoke, Jokes> {

    private String mKey;

    public SingleJokeRequest(String key) {
        super(APIJoke.class, Jokes.class);
        mKey = key;
    }

    @Override
    public APIJoke loadDataFromNetwork() throws Exception {
        return getService().joke(mKey);
    }
}
