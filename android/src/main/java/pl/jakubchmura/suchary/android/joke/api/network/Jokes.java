package pl.jakubchmura.suchary.android.joke.api.network;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import pl.jakubchmura.suchary.android.joke.api.model.APIJoke;
import pl.jakubchmura.suchary.android.joke.api.model.APIResult;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface Jokes {

    DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Get all the jokes from specified page with specified number of jokes per page
     *
     * @param page page of results to download
     */
    @GET("/")
    APIResult jokes(@Query("page") int page);

    /**
     * Get all the jokes from specified page with specified number of jokes per page
     *
     * @param page  page of results to download
     * @param limit number of jokes per page
     */
    @GET("/")
    APIResult jokes(@Query("page") int page, @Query("limit") int limit);

    /**
     * Get all the jokes from specified page that were added after the specified date
     *
     * @param after date of the oldest joke formatted with {@link pl.jakubchmura.suchary.android.joke.api.network.Jokes#DATE_FORMAT}
     * @param page  page of results to download
     */
    @GET("/")
    APIResult jokes(@Query("after") String after, @Query("page") int page);

    /**
     * Get all the jokes from specified page that were changed after the specified date
     *
     * @param changedAfter date of the oldest changed joke formatted with {@link pl.jakubchmura.suchary.android.joke.api.network.Jokes#DATE_FORMAT}
     */
    @GET("/")
    APIResult changedJokes(@Query("changed_after") String changedAfter, @Query("page") int page);

    /**
     * Get on joke with specified key
     *
     * @param key key of the joke to download
     */
    @GET("/{joke}")
    APIJoke joke(@Path("joke") String key);

}
