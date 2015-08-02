package pl.jakubchmura.suchary.android.joke.api.network;

import pl.jakubchmura.suchary.android.joke.api.model.APIJoke;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.PATCH;
import retrofit.http.Path;

public interface AdminJokeService {

    @FormUrlEncoded
    @PATCH("/obcy/{joke}/")
    APIJoke edit(@Path("joke") String key, @Field("body") String body);

    @DELETE("/obcy/{joke}/")
    APIJoke delete(@Path("joke") String key);

}
