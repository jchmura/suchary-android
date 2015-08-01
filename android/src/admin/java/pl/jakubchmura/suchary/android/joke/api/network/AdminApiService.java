package pl.jakubchmura.suchary.android.joke.api.network;


import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface AdminApiService {

    @FormUrlEncoded
    @POST("/token/")
    void getToken(@Field("username") String username, @Field("password") String password, Callback<APIToken> callback);

}
