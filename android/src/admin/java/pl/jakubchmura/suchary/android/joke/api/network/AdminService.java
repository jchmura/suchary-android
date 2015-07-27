package pl.jakubchmura.suchary.android.joke.api.network;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface AdminService {

    @FormUrlEncoded
    @POST("/edit")
    Response editJoke(@Field("key") String key, @Field("body") String body);

    @FormUrlEncoded
    @POST("/delete")
    Response deleteJoke(@Field("key") String key);

}
