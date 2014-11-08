package pl.jakubchmura.suchary.android;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;

import pl.jakubchmura.suchary.android.gcm.GcmRegistration;
import pl.jakubchmura.suchary.android.gcm.NewJokeNotification;
import pl.jakubchmura.suchary.android.search.SearchActivity;
import pl.jakubchmura.suchary.android.util.ActionBarTitle;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String ACTION_NEW_JOKE = "action_new_joke";
    public static final String ACTION_EDIT_JOKE = "action_edit_joke";
    public static final String ACTION_DELETE_JOKE = "action_delete_joke";

    public static final int TOOLBAR_HEIGHT = 56;
    public static final int MAXIMUM_DRAWER_WIDTH = 320;

    private static final String TAG = "MainActivity";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private String mTitle;
    private int mFragmentNumber;
    private JokesBaseFragment<MainActivity> mFragment;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && mFragment != null) {
                switch (action) {
                    case ACTION_NEW_JOKE:
                        mFragment.checkNewJokes();
                        break;
                    case ACTION_EDIT_JOKE:
                        mFragment.checkEditedJokes();
                        break;
                    case ACTION_DELETE_JOKE:
                        mFragment.checkDeletedJoke();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        setNavigationDrawerWidth();
    }

    private void setNavigationDrawerWidth() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int dpWidth = (int) ((displayMetrics.widthPixels / displayMetrics.density) + 0.5);

        View view = mNavigationDrawerFragment.getView();
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = dpWidth - TOOLBAR_HEIGHT;
        if (layoutParams.width > MAXIMUM_DRAWER_WIDTH) {
            layoutParams.width = MAXIMUM_DRAWER_WIDTH;
        }
        layoutParams.width = (int) ((layoutParams.width * displayMetrics.density) + 0.5);
        view.setLayoutParams(layoutParams);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register the device in GCM
        GcmRegistration gcm = new GcmRegistration(this);
        gcm.register();

        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEW_JOKE);
        filter.addAction(ACTION_EDIT_JOKE);
        filter.addAction(ACTION_DELETE_JOKE);
        registerReceiver(mReceiver, filter);

        // Cancel the notification about new jokes
        NewJokeNotification.cancel(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            unregisterReceiver(mReceiver);
        } catch (RuntimeException e) {
            Log.e(TAG, "Error while unregistering broadcast receiver", e);
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position) {
            default:
            case 0:
                mFragment = NewJokesFragment.newInstance(position + 1);
                break;
            case 1:
                mFragment = StarredJokesFragment.newInstance(position + 1);
                break;
            case 2:
                mFragment = RandomJokesFragment.newInstance(position + 1);
                break;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, mFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        mFragmentNumber = number;
        switch (number) {
            case 1:
                mTitle = getString(R.string.section_new);
                break;
            case 2:
                mTitle = getString(R.string.section_starred);
                break;
            case 3:
                mTitle = getString(R.string.section_random);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
        }
        setTitle();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final MenuItem searchItem = menu.findItem(R.id.search);
            if (searchItem != null) {
                SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
                if (searchView != null) {
                    // Assumes current activity is the searchable activity
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

                    // Listener
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            searchItem.collapseActionView();
                            Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                            searchIntent.putExtra(SearchManager.QUERY, query);
                            searchIntent.setAction(Intent.ACTION_SEARCH);
                            searchIntent.putExtra(SearchActivity.FRAGMENT_NUMBER, mFragmentNumber);
                            startActivity(searchIntent);
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            return false;
                        }
                    });
                }
            }

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        Bundle appData = new Bundle();
        appData.putInt(SearchActivity.FRAGMENT_NUMBER, mFragmentNumber);
        startSearch(null, false, appData, false);
        return true;
    }

    private void setTitle() {
        ActionBarTitle actionBarTitle = new ActionBarTitle(this);
        actionBarTitle.setTitle(mTitle);
    }
}
