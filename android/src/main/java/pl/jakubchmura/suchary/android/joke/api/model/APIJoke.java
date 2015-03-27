package pl.jakubchmura.suchary.android.joke.api.model;

import java.util.Date;

import pl.jakubchmura.suchary.android.joke.Joke;

public class APIJoke {

    private String mKey;
    private int mVotes;
    private Date mAdded;
    private String mUrl;
    private String mBody;
    private String mSite;
    private Date mChanged;
    private Date mHidden;

    public APIJoke(String key, int votes, Date added, String url, String body, String site, Date changed, Date hidden) {
        mKey = key;
        mVotes = votes;
        mAdded = added;
        mUrl = url;
        mBody = body;
        mSite = site;
        mChanged = changed;
        mHidden = hidden;
    }

    public String getKey() {
        return mKey;
    }

    public int getVotes() {
        return mVotes;
    }

    public Date getAdded() {
        return mAdded;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getBody() {
        return mBody;
    }

    public String getSite() {
        return mSite;
    }

    public Date getChanged() {
        return mChanged;
    }

    public Date getHidden() {
        return mHidden;
    }

    public Joke getJoke() {
        return new Joke(this);
    }

}
