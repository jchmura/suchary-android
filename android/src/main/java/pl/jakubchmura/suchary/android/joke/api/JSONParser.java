package pl.jakubchmura.suchary.android.joke.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.jakubchmura.suchary.android.joke.Joke;

public class JSONParser {
    public static Joke getJoke(JSONObject apiJoke) throws JSONException, ParseException {
        String key = apiJoke.getString("key");
        int votes = apiJoke.getInt("votes");
        String dateString = apiJoke.getString("added");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date date = dateFormat.parse(dateString);
        String url = apiJoke.getString("url");
        String body = apiJoke.getString("body");
        String site = apiJoke.getString("site");

        return new Joke(key, votes, date, url, body, site);
    }

    public static List<Joke> getJokes(JSONArray results) throws JSONException, ParseException {
        List<Joke> jokes = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            jokes.add(getJoke(results.getJSONObject(i)));
        }

        return jokes;
    }

    public static APIResult getAPIResult(JSONObject apiResult) throws JSONException, ParseException {
        int count = apiResult.getInt("count");
        String next;
        if (apiResult.isNull("next")) next = null;
        else next = apiResult.getString("next");
        String previous;
        if (apiResult.isNull("previous")) previous = null;
        else previous = apiResult.getString("previous");
        List<Joke> results = getJokes(apiResult.getJSONArray("results"));

        return new APIResult(count, next, previous, results);
    }
}
