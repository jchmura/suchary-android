package pl.jakubchmura.suchary.android.settings;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.util.ActionBarTitle;
import pl.jakubchmura.suchary.android.util.ThemedActivity;

public class Settings extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarTitle actionBarTitle = new ActionBarTitle(this);
        actionBarTitle.setTitle(R.string.title_activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
