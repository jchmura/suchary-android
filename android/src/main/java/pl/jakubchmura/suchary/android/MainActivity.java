package pl.jakubchmura.suchary.android;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.heinrichreimersoftware.materialdrawer.DrawerView;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerItem;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerProfile;

import io.fabric.sdk.android.services.concurrency.AsyncTask;
import pl.jakubchmura.suchary.android.about.AboutActivity;
import pl.jakubchmura.suchary.android.gcm.GcmRegistration;
import pl.jakubchmura.suchary.android.gcm.NewJokeNotification;
import pl.jakubchmura.suchary.android.search.SearchActivity;
import pl.jakubchmura.suchary.android.settings.Settings;
import pl.jakubchmura.suchary.android.sql.JokeCount;
import pl.jakubchmura.suchary.android.sql.JokeDbHelper;
import pl.jakubchmura.suchary.android.util.ActionBarTitle;


public class MainActivity extends AppCompatActivity {

    public static final String ACTION_NEW_JOKE = "action_new_joke";
    public static final String ACTION_EDIT_JOKE = "action_edit_joke";
    public static final String ACTION_DELETE_JOKE = "action_delete_joke";
    public static final String ACTION_JOKE_COUNT = "action_joke_count";

    private static final String DRAWER_LAST_ITEM = "drawer_last_item";

    private static final String TAG = "MainActivity";
    private String mTitle;
    private int mFragmentNumber;
    private JokesBaseFragment<MainActivity> mFragment;
    private DrawerView mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private int mDrawerItemSelected = 0;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "Received action: " + action);
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

    private BroadcastReceiver mDrawerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Drawer received action to update");
            updateDrawerJokeCount();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (DrawerView) findViewById(R.id.drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        setUpDrawer(savedInstanceState);
        ActionBarTitle actionBarTitle = new ActionBarTitle(this);
        actionBarTitle.setTitle(R.string.app_name);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    private boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mDrawer);
    }

    private void selectItem(int position) {
        mDrawer.selectItem(position);
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawer);
        }
        onNavigationDrawerItemSelected(position);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register the device in GCM
        GcmRegistration gcm = new GcmRegistration(this);
        gcm.register();

        // Register the broadcast receivers
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEW_JOKE);
        filter.addAction(ACTION_EDIT_JOKE);
        filter.addAction(ACTION_DELETE_JOKE);
        registerReceiver(mReceiver, filter);

        IntentFilter drawerFilter = new IntentFilter();
        drawerFilter.addAction(ACTION_JOKE_COUNT);
        registerReceiver(mDrawerReceiver, drawerFilter);

        // Cancel the notification about new jokes
        NewJokeNotification.cancel(this);

        updateDrawerJokeCount();
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            unregisterReceiver(mReceiver);
            unregisterReceiver(mDrawerReceiver);
        } catch (RuntimeException e) {
            Log.e(TAG, "Error while unregistering broadcast receiver", e);
            Crashlytics.logException(e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void onNavigationDrawerItemSelected(int position) {
        mDrawerItemSelected = position;
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

    private void setUpDrawer(Bundle savedInstanceState) {
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawer);
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.closeDrawer(mDrawer);

        mDrawer.addItem(new DrawerItem().setTextPrimary(getString(R.string.section_new)).setImage(getResources().getDrawable(R.drawable.ic_whatshot_black_18dp)));
        mDrawer.addItem(new DrawerItem().setTextPrimary(getString(R.string.section_starred)).setImage(getResources().getDrawable(R.drawable.ic_favorite_black_18dp)));
        mDrawer.addItem(new DrawerItem().setTextPrimary(getString(R.string.section_random)).setImage(getResources().getDrawable(R.drawable.ic_shuffle_black_18dp)));

        mDrawer.addDivider();

        mDrawer.addItem(new DrawerItem().setTextPrimary(getString(R.string.navigation_drawer_settings)).setImage(getResources().getDrawable(R.drawable.ic_settings_black_18dp)));
        mDrawer.addItem(new DrawerItem().setTextPrimary(getString(R.string.navigation_drawer_about)).setImage(getResources().getDrawable(R.drawable.ic_help_black_18dp)));

        mDrawer.addProfile(new DrawerProfile().setName(getString(R.string.app_name)).setBackground(getResources().getDrawable(R.drawable.profile_background)));

        mDrawer.setOnItemClickListener(new DrawerItem.OnItemClickListener() {
            @Override
            public void onClick(DrawerItem item, long id, int position) {
                switch (position) {
                    case 0:
                    case 1:
                    case 2:
                        selectItem(position);
                        break;
                    case 4:
                        if (isDrawerOpen()) {
                            mDrawerLayout.closeDrawer(mDrawer);
                        }
                        startActivity(new Intent(MainActivity.this, Settings.class));
                        break;
                    case 5:
                        if (isDrawerOpen()) {
                            mDrawerLayout.closeDrawer(mDrawer);
                        }
                        startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        break;
                }
            }
        });

        if (savedInstanceState != null) {
            mDrawerItemSelected = savedInstanceState.getInt(DRAWER_LAST_ITEM, 0);
        } else {
            selectItem(0);
        }
        mDrawer.selectItem(mDrawerItemSelected);
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
        if (!isDrawerOpen()) {
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

                    if (mDrawerItemSelected == 1)
                    searchView.setQueryHint(getString(R.string.search_hint_starred));

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle other action bar items...
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        Bundle appData = new Bundle();
        appData.putInt(SearchActivity.FRAGMENT_NUMBER, mFragmentNumber);
        startSearch(null, false, appData, false);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DRAWER_LAST_ITEM, mDrawerItemSelected);
    }

    private void setTitle() {
        ActionBarTitle actionBarTitle = new ActionBarTitle(this);
        actionBarTitle.setTitle(mTitle);
    }

    private void updateDrawerJokeCount() {
        Log.d(TAG, "Updating joke count in the drawer");
        new AsyncTask<Void, Void, JokeCount>() {

            @Override
            protected JokeCount doInBackground(Void... voids) {
                JokeDbHelper helper = JokeDbHelper.getInstance(MainActivity.this);
                try {
                    return helper.getJokeCount();
                } catch (Exception e) {
                    Crashlytics.logException(e);
                    throw e;
                }
            }

            @Override
            protected void onPostExecute(JokeCount jokeCount) {
                Log.d(TAG, "New count: " + jokeCount);
                if (mDrawer != null) {
                    Resources resources = getResources();
                    String totalString = resources.getQuantityString(R.plurals.total_joke_count, (int) jokeCount.getTotal(), (int) jokeCount.getTotal());
                    String starredString = resources.getQuantityString(R.plurals.starred_joke_count, (int) jokeCount.getStarred(), (int) jokeCount.getStarred());
                    DrawerProfile profile = mDrawer.getProfiles().get(0).setDescription(totalString + " (" + starredString + ")");
                    mDrawer.clearProfiles();
                    mDrawer.addProfile(profile);
                }
            }
        }.execute((Void) null);
    }
}
