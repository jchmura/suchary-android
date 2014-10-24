package pl.jakubchmura.suchary.android.settings;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.util.ActionBarTitle;

public class Settings extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }

        ActionBarTitle actionBarTitle = new ActionBarTitle(this);
        actionBarTitle.setTitle(R.string.title_activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
