package com.solutions.nerd.sailing.android.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;

import com.google.android.gms.auth.GoogleAuthUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {
    // variables that control the Action Bar auto hide behavior (aka "quick recall")
    private ObjectAnimator mStatusBarColorAnimator;
    private boolean mActionBarAutoHideEnabled = false;
    private int mActionBarAutoHideSensivity = 0;
    private int mActionBarAutoHideMinY = 0;
    private int mActionBarAutoHideSignal = 0;
    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;
    private boolean mActionBarShown = true;
    private LinearLayout mAccountListContainer;
    private ImageView mExpandAccountBoxIndicator;
    private boolean mAccountBoxExpanded = false;
    private ViewGroup mDrawerItemsListContainer;

    // Durations for certain animations we use:
    private static final int HEADER_HIDE_ANIM_DURATION = 300;
    private static final int ACCOUNT_BOX_EXPAND_ANIM_DURATION = 200;


    // delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;


    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";


    /**
     * Drawer Titles
     */
    private String[] mTitles;
    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

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
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }
    protected void onActionBarShowOrHide(boolean shown){
        if (mStatusBarColorAnimator!=null){
            mStatusBarColorAnimator.cancel();
        }

    }

    protected void autoShowOrHideActionBar(boolean show){
        if (show==mActionBarShown){
            return;
        }
        mActionBarShown = show;
        onActionBarShowOrHide(show);
    }

    protected void onNavDrawerStateChanged(boolean isOpen, boolean isAnimating){
        if (mActionBarAutoHideEnabled && isOpen){
            autoShowOrHideActionBar(true);
        }
    }


    protected boolean isNavDrawerOpen(){
        return mDrawerLayout!=null&&mDrawerLayout.isDrawerOpen(Gravity.START);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }
    private ListView mDrawerList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_navigation_drawer,container,false);

       mDrawerListView = (ListView)rootView.findViewById(R.id.nav);

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mTitles = getResources().getStringArray(R.array.titles);


        // Set the adapter for the list view
        mDrawerListView.setAdapter(new ArrayAdapter<String>(this.getActivity(),
                R.layout.drawer_list_item, mTitles));

        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return rootView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }
    protected void onNavDrawerSlide(float offset){}

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
        Toolbar mToolBar = (Toolbar) getActivity().findViewById(R.id.toolbar);



        final Resources r = getActivity().getResources();


        // implement items from base activity
        final Activity a = getActivity();
        mDrawerLayout.setStatusBarBackgroundColor(r.getColor(R.color.colorPrimaryDark));

        ScrimInsetsScrollView navDrawer = (ScrimInsetsScrollView)mDrawerLayout.findViewById(R.id.navdrawer);
        if (navDrawer!=null){
            final View chosenAccountContentView = a.findViewById(R.id.chosen_account_content_view);
            final View chosenAccountView = a.findViewById(R.id.chosen_account_view);
            final int navDrawerChosenAccountHeight = getResources().getDimensionPixelSize(R.dimen.navdrawer_chosen_account_height);
            navDrawer.setOnInsetsCallback(new ScrimInsetsScrollView.OnInsetsCallback() {
                @Override
                public void onInsetsChanged(Rect insets) {
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
                            chosenAccountContentView.getLayoutParams();
                    lp.topMargin = insets.top;
                    chosenAccountContentView.setLayoutParams(lp);

                    ViewGroup.LayoutParams lp2 = chosenAccountView.getLayoutParams();
                    lp2.height = navDrawerChosenAccountHeight + insets.top;
                    chosenAccountView.setLayoutParams(lp2);
                }
            });
        }

        if (mToolBar !=null){
            mToolBar.setNavigationIcon(R.drawable.ic_drawer);
            mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(Gravity.START);
                    Toast.makeText(getActivity(),"On Click",Toast.LENGTH_SHORT).show();
                }
            });
        }


        mDrawerLayout.setDrawerListener( new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                onNavDrawerSlide(slideOffset);
            }


            @Override
            public void onDrawerClosed(View drawerView) {
                // run deferred action, if we have one
                if (mDeferredOnDrawerClosedRunnable!=null){
                    mDeferredOnDrawerClosedRunnable.run();
                    mDeferredOnDrawerClosedRunnable=null;
                }
                if (mAccountBoxExpanded){
                    mAccountBoxExpanded=false;
                    setupAccountBoxToggle();
                }
                onNavDrawerStateChanged(false,false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                onNavDrawerStateChanged(isNavDrawerOpen(),newState!=DrawerLayout.STATE_IDLE);

            }
            @Override
            public void onDrawerOpened(View drawerView) {
                onNavDrawerStateChanged(true,false);
            }

        });






        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                mToolBar,
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }





            protected void enableActionBarAutoHide(final ListView listView){
                initActionBarAutoHide();
            }
            protected void initActionBarAutoHide(){

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        setupFirebase();
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
            mCallbacks.onNavigationDrawerItemSelected(position);
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
//            showGlobalContextActionBar();
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


    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
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


    private void setupFirebase(){
    }



    /**
     * Sets up the account box. This account box is the area at the top of the nav drawer that
     * shows which account the user is logged in as, and lets them switch accounts. It also
     * shows the user's Google+ cover photo as background.
     */
    private void setupAccountBox() {
        mAccountListContainer = (LinearLayout) getActivity().findViewById(R.id.account_list);
        Account chosenAccount = AccountUtils.getActiveAccount(getActivity());
        if (mAccountListContainer == null) {
            // This  activity does not have an account box.
            return;
        }

        final View chosenAccountView = getActivity().findViewById(R.id.chosen_account_view);
        if (chosenAccount == null) {
            // No Account logged in. hide account box;
            chosenAccountView.setVisibility(View.GONE);
            mAccountListContainer.setVisibility(View.GONE);
            return;
        } else {
            chosenAccountView.setVisibility(View.VISIBLE);
            mAccountListContainer.setVisibility(View.INVISIBLE);
        }

        AccountManager am = AccountManager.get(getActivity());
        Account[] accountArray = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        List<Account> accounts = new ArrayList<>(Arrays.asList(accountArray));
        accounts.remove(chosenAccount);

        ImageView coverImageView = (ImageView) chosenAccountView.findViewById(R.id.profile_cover_image);
        ImageView profileImageView = (ImageView) chosenAccountView.findViewById(R.id.profile_image);
        TextView nameTextView = (TextView) chosenAccountView.findViewById(R.id.profile_name_text);
        TextView email = (TextView) chosenAccountView.findViewById(R.id.profile_email_text);
        mExpandAccountBoxIndicator = (ImageView) getActivity().findViewById(R.id.expand_account_box_indicator);

        String name = AccountUtils.getPlusName(getActivity());

        if (name == null) {
            nameTextView.setVisibility(View.GONE);
        } else {
            nameTextView.setVisibility(View.VISIBLE);
            nameTextView.setText(name);
        }
        String imageUrl = AccountUtils.getPlusImageUrl(getActivity());
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(profileImageView);
        }

        String coverImageUrl = AccountUtils.getPlusCoverUrl(getActivity());
        if (coverImageUrl != null) {
            Glide.with(this).load(coverImageUrl).into(coverImageView);
        } else {
            coverImageView.setImageResource(R.drawable.default_cover);
        }
        email.setText(chosenAccount.name);

        if (accounts.isEmpty()) {
            // Theres only one account on the device, so no need for a switcher.
            mExpandAccountBoxIndicator.setVisibility(View.GONE);
            mAccountListContainer.setVisibility(View.GONE);
            chosenAccountView.setEnabled(false);
            return;
        }

        chosenAccountView.setEnabled(true);

        mExpandAccountBoxIndicator.setVisibility(View.VISIBLE);
        chosenAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAccountBoxExpanded = !mAccountBoxExpanded;
                setupAccountBoxToggle();
            }
        });

     //   setupAccountBoxToggle();

     //   populateAccountList(accounts);

    }


    private void setupAccountBoxToggle() {

//        int selfItem = getSelfNavDrawerItem();
 //       if (mDrawerLayout == null || selfItem == NAVDRAWER_ITEM_INVALID) {
  //          // this Activity does not have a nav drawer
  //          return;
  //      }
        mExpandAccountBoxIndicator.setImageResource(mAccountBoxExpanded
                ? R.drawable.ic_drawer_accounts_collapse
                : R.drawable.ic_drawer_accounts_expand);
        int hideTranslateY = -mAccountListContainer.getHeight() / 4; // last 25% of animation
        if (mAccountBoxExpanded && mAccountListContainer.getTranslationY() == 0) {
            // initial setup
            mAccountListContainer.setAlpha(0);
            mAccountListContainer.setTranslationY(hideTranslateY);
        }

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mDrawerItemsListContainer.setVisibility(mAccountBoxExpanded
                        ? View.INVISIBLE : View.VISIBLE);
                mAccountListContainer.setVisibility(mAccountBoxExpanded
                        ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }
        });

        if (mAccountBoxExpanded) {
            mAccountListContainer.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator.ofFloat(mAccountListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListContainer, View.TRANSLATION_Y, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.playSequentially(
                    ObjectAnimator.ofFloat(mDrawerItemsListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    subSet);
            set.start();
        } else {
            mDrawerItemsListContainer.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator.ofFloat(mAccountListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListContainer, View.TRANSLATION_Y,
                            hideTranslateY)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.playSequentially(
                    subSet,
                    ObjectAnimator.ofFloat(mDrawerItemsListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.start();
        }

        set.start();
    }


}
