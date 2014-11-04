package pl.jakubchmura.suchary.android;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import pl.jakubchmura.suchary.android.about.AboutActivity;
import pl.jakubchmura.suchary.android.settings.Settings;
import pl.jakubchmura.suchary.android.util.ActionBarTitle;
import pl.jakubchmura.suchary.android.util.DrawerAdapter;
import pl.jakubchmura.suchary.android.util.FontCache;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    private static final String TAG = "NavigationDrawer";

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private DrawerAdapter<String> mDrawerAdapter;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 1;
    private View mVerticalSpace;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }
        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mVerticalSpace = new View(getActivity());
        mVerticalSpace.setMinimumHeight((int) getResources().getDimension(R.dimen.drawer_item_padding_top));
        setUpHeader();
        setUpFooter();
        mDrawerAdapter = new DrawerAdapter<>(
                getActivity(),
                R.layout.drawer_list_item,
                R.id.text,
                R.id.icon,
                new String[]{
                        getString(R.string.section_new),
                        getString(R.string.section_starred),
                        getString(R.string.section_random)
                }, new int[]{
                        R.drawable.ic_whatshot_black_18dp,
                        R.drawable.ic_favorite_black_18dp,
                        R.drawable.ic_shuffle_black_18dp
                }
        );
        mDrawerListView.setAdapter(mDrawerAdapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActionBarActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void setUpHeader() {
        mDrawerListView.addHeaderView(mVerticalSpace);
    }

    private void setUpFooter() {
        mDrawerListView.addFooterView(mVerticalSpace);

//        View bar = LayoutInflater.from(getActivity()).inflate(R.layout.drawer_list_item, null);
        View bar = new View(getActivity());
        bar.setBackgroundResource(R.color.drawer_footer_divider);
        bar.setMinimumHeight(2);
        mDrawerListView.addFooterView(bar);
        mDrawerListView.addFooterView(mVerticalSpace);


        Typeface typeface = FontCache.get("fonts/Roboto-Light.ttf", getActivity());

        View settings = LayoutInflater.from(getActivity()).inflate(R.layout.drawer_list_item, null);
        if (settings != null) {
            ImageView icon = (ImageView) settings.findViewById(R.id.icon);
            icon.setImageResource(R.drawable.ic_settings_black_18dp);
            TextView text = (TextView) settings.findViewById(R.id.text);
            text.setText(R.string.navigation_drawer_settings);
            text.setTypeface(typeface);
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawerLayout != null) {
                        mDrawerLayout.closeDrawer(mFragmentContainerView);
                    }
                    Intent intent = new Intent(getActivity(), Settings.class);
                    startActivity(intent);
                }
            });
            settings.setMinimumHeight((int) getResources().getDimension(R.dimen.drawer_item_height));
            mDrawerListView.addFooterView(settings);
        }

        View about = LayoutInflater.from(getActivity()).inflate(R.layout.drawer_list_item, null);
        if (about != null) {
            ImageView icon = (ImageView) about.findViewById(R.id.icon);
            icon.setImageResource(R.drawable.ic_help_black_18dp);
            TextView text = (TextView) about.findViewById(R.id.text);
            text.setText(R.string.navigation_drawer_about);
            text.setTypeface(typeface);
            about.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawerLayout != null) {
                        mDrawerLayout.closeDrawer(mFragmentContainerView);
                    }
                    Intent intent = new Intent(getActivity(), AboutActivity.class);
                    startActivity(intent);
                }
            });
            about.setMinimumHeight((int) getResources().getDimension(R.dimen.drawer_item_height));
            mDrawerListView.addFooterView(about);
        }
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position - 1);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        ActionBarTitle actionBarTitle = new ActionBarTitle((ActionBarActivity) getActivity());
        actionBarTitle.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return getActionBarActivity().getSupportActionBar();
    }

    private ActionBarActivity getActionBarActivity() {
        return (ActionBarActivity) getActivity();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }
}
