package pl.jakubchmura.suchary.android.joke.api.network;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.retrofit.RetrofitObjectPersisterFactory;
import com.octo.android.robospice.retrofit.RetrofitSpiceService;

import pl.jakubchmura.suchary.android.R;
import retrofit.converter.Converter;

public class JokeRetrofitSpiceService extends RetrofitSpiceService {

    @Override
    public void onCreate() {
        super.onCreate();
        addRetrofitInterface(Jokes.class);
    }

    @Override
    protected String getServerUrl() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPref.getBoolean("pref_admin_dev", false)) {
            return getApplicationContext().getString(R.string.api_url_dev);
        } else {
            return getApplicationContext().getString(R.string.api_url);
        }
    }

    @Override
    protected Converter createConverter() {
        return new JokeConverter();
    }

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new RetrofitObjectPersisterFactory(application, getConverter()));
        return cacheManager;
    }
}
