package pl.jakubchmura.suchary.android.about;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.util.ActionBarTitle;

public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new AboutFragment())
                    .commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarTitle actionBarTitle = new ActionBarTitle(this);
        actionBarTitle.setTitle(R.string.title_activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class AboutFragment extends Fragment {

        public AboutFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_about, container, false);

            TextView application = (TextView) rootView.findViewById(R.id.about_application);
            application.setMovementMethod(LinkMovementMethod.getInstance());

            TextView author = (TextView) rootView.findViewById(R.id.about_author);
            author.setMovementMethod(LinkMovementMethod.getInstance());

            TextView libraries = (TextView) rootView.findViewById(R.id.about_libraries);
            libraries.setMovementMethod(LinkMovementMethod.getInstance());

            return rootView;
        }
    }
}
