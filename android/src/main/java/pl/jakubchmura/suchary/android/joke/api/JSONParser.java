package pl.jakubchmura.suchary.android.joke.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pl.jakubchmura.suchary.android.joke.api.model.APIJoke;
import pl.jakubchmura.suchary.android.joke.api.model.APIResult;

public class JSONParser {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public static APIJoke getAPIJoke(JSONObject apiJoke) throws JSONException, ParseException {
        String key = apiJoke.getString("key");
        int votes = apiJoke.getInt("votes");
        Date added = DATE_FORMAT.parse(apiJoke.getString("added"));
        String url = apiJoke.getString("url");
        String body = apiJoke.getString("body");
        String site = apiJoke.getString("site");
        Date changed = DATE_FORMAT.parse(apiJoke.getString("changed"));
        Date hidden = apiJoke.isNull("hidden")? null: DATE_FORMAT.parse(apiJoke.getString("hidden"));

        return new APIJoke(key, votes, added, url, body, site, changed, hidden);
    }

    public static APIResult.APIJokes getAPIJokes(JSONObject results) throws JSONException, ParseException {
        JSONArray array = results.getJSONArray("results");
        APIResult.APIJokes jokes = new APIResult.APIJokes();
        for (int i = 0; i < array.length(); i++) {
            jokes.add(getAPIJoke(array.getJSONObject(i)));
        }

        return jokes;
    }

    public static APIResult getAPIResult(JSONObject apiResult) throws JSONException, ParseException {
        int count = apiResult.getInt("count");
        String next = apiResult.isNull("next")? null: apiResult.getString("next");
        String previous = apiResult.isNull("previous")? null: apiResult.getString("previous");
        APIResult.APIJokes results = getAPIJokes(apiResult);

        return new APIResult(count, next, previous, results);
    }
}
