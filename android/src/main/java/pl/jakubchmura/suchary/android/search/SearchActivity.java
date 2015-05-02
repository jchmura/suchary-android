package pl.jakubchmura.suchary.android.search;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import pl.jakubchmura.suchary.android.R;
import pl.jakubchmura.suchary.android.util.ActionBarTitle;
import pl.jakubchmura.suchary.android.util.ThemedActivity;

public class SearchActivity extends ThemedActivity {

    public static final String FRAGMENT_NUMBER = "fragment_number";

    private SearchFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        handleIntent(getIntent());

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mFragment)
                    .commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        overridePendingTransition(R.anim.anim_in_left, R.anim.anim_out_left);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            if (query != null) {
                query = query.trim();
            }

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            mFragment = (SearchFragment)
                    fm.findFragmentById(R.id.fragment_search);
            boolean star = false;
            if (mFragment == null) {
                star = intent.getIntExtra(FRAGMENT_NUMBER, 0) == 2;

                Bundle args = new Bundle();
                args.putString("query", query);
                args.putBoolean("star", star);

                mFragment = new SearchFragment();
                mFragment.setArguments(args);
            }

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            ActionBarTitle actionBarTitle = new ActionBarTitle(this);
            actionBarTitle.setTitle(query);
            if (star) {
                actionBarTitle.setSubTitle(getResources().getString(R.string.in_favorites));
            }
        } else {
            mFragment = new SearchFragment();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_right);
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

}
