package com.solutions.nerd.sailing;

//TODO Need to implement selecting the account toggle.
//TODO Need to fix the navigation item list to completely expand items

/**
 * Created by cberman on 12/16/2014.
 */
public class BaseActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        LoginAndAuthHelper.Callbacks {




    private final static String TAG = BaseActivity.class.getSimpleName();
    private ImageView mExpandAccountBoxIndicator;
    private boolean mAccountBoxExpanded = false;
    protected DrawerLayout mDrawerLayout;
    protected ViewGroup mDrawerItemsListContainer;
    private Firebase mFirebaseRef;
    
    // Durations for certain animations we use:
    private static final int HEADER_HIDE_ANIM_DURATION = 300;
    private static final int ACCOUNT_BOX_EXPAND_ANIM_DURATION = 200;

    @SuppressWarnings("SpellCheckingInspection")
    private static final int NAVDRAWER_ITEM_INVALID = -1;

    // the LoginAndAuthHelper handles signing in to Google Play Services and OAuth
    private LoginAndAuthHelper mLoginAndAuthHelper;


    /**
     * IDs for Fragments
     */
    private final static int JOURNEYS_ID = 0;
    private final static int MAINTENANCE_ID = 1;
    private final static int COMPASS_ID = 2;
    private final static int BLOG_ID = 3;
    private final static int SETTINGS_ID = 4;
    private final static int ABOUT_ID = 5;
    private static final long MAIN_CONTENT_FADEIN_DURATION = 250;


    private LinearLayout mAccountListContainer;
    private Handler mHandler;


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }


    private void getActionBarToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);
        MarinasUtil.init(this);
        AnalyticsManager.initializeAnalyticsTracker(getApplicationContext());

        mFirebaseRef = new Firebase(FirebaseData.FIREBASE_URL);
        PrefUtils.init(this);

        final boolean tosAccepted = PrefUtils.isTosAccepted(this);

        // Check if the EULA has been accepted; if not, show it.
        if (!tosAccepted) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }

        // Enable or disable each Activity depending on the form factor. This is necessary
        // because this app uses many implicit intents where we don't name the exact Activity
        // in the Intent, so there should only be one enabled Activity that handles each
        // Intent in the app.


        if (tosAccepted && !PrefUtils.gotUserID(this)) {
            startLoginProcess();
        }




        setupAccountBox();
    }

    /**
     * Returns the default account on the device. We use the rule that the first account
     * should be the default. It's arbitrary, but the alternative would be showing an account
     * chooser popup which wouldn't be a smooth first experience with the app. Since the user
     * can easily switch the account with the nav drawer, we opted for this implementation.
     */
    private String getDefaultAccount() {
        // Choose first account on device.
        Log.d(TAG, "Choosing default account (first account on device)");
        AccountManager am = AccountManager.get(this);
        Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        if (accounts.length == 0) {
            // No Google accounts on device.
            Log.w(TAG, "No Google accounts on device; not setting default account.");
            return null;
        }

        Log.d(TAG, "Default account is: " + accounts[0].name);
        return accounts[0].name;
    }

    private void complainMustHaveGoogleAccount() {
        Log.d(TAG, "Complaining about missing Google account.");
        new AlertDialog.Builder(this)
                .setTitle(R.string.google_account_required_title)
                .setMessage(R.string.google_account_required_message)
                .setPositiveButton(R.string.add_account, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        promptAddAccount();
                    }
                })
                .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    private void promptAddAccount() {
        Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
        intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[]{"com.google"});
        startActivity(intent);
        finish();
    }

    private void startLoginProcess() {
        Log.d(TAG, "Starting login process.");
        if (!AccountUtils.hasActiveAccount(this)) {
            Log.d(TAG, "No Active Account, attempt to pick a default.");
            String defaultAccount = getDefaultAccount();
            if (defaultAccount == null) {
                Log.e(TAG, "Failed to pick default account (no accounts). Failing");
                complainMustHaveGoogleAccount();
                return;
            }
            Log.d(TAG, "Default to :" + defaultAccount);
            AccountUtils.setActiveAccount(this, defaultAccount);
        }
        if (!AccountUtils.hasActiveAccount(this)) {
            Log.d(TAG, "Can't proceed with login -- no account chosen.");
            return;
        } else {
            Log.d(TAG, "Chosen account : " + AccountUtils.getActiveAccountName(this));
        }

        String accountName = AccountUtils.getActiveAccountName(this);
        Log.d(TAG, "Chosen account: " + AccountUtils.getActiveAccountName(this));

        if (mLoginAndAuthHelper != null && mLoginAndAuthHelper.getAccountName().equals(accountName)) {
            Log.d(TAG, "Tearing down old helper, was " + mLoginAndAuthHelper.getAccountName());
            if (mLoginAndAuthHelper.isStarted()) {
                Log.d(TAG, "Stopping old Helper");
                mLoginAndAuthHelper.stop();
            }
            mLoginAndAuthHelper = null;
        }

        Log.d(TAG, "Creating and starting new Helper with account: " + accountName);
        mLoginAndAuthHelper = new LoginAndAuthHelper(this, this, accountName);
        mLoginAndAuthHelper.start();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        View mainContent = findViewById(R.id.container);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        } else {
            Log.w(TAG, "No View with ID main_content to fade in.");
        }
    }

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;


    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";



        private int mCurrentPosition = -1;
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        String fragmentName = "";
        if (position ==mCurrentPosition)
            return;
        mCurrentPosition = position;
        Fragment fragment = null;
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);

        // Make sure selected fragment isnt allready active;

        switch (position) {
            case MAINTENANCE_ID:

                fragment = PlaceholderFragment.newInstance(position + 1);
                break;
            case JOURNEYS_ID:
//                fragment = MapFragment.getInstance();
                Map_Fragment mapFragment = new Map_Fragment();
                fragment = (Fragment)mapFragment;
                fragmentName = "Map_Fragment";
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                sp.registerOnSharedPreferenceChangeListener(mapFragment);
                fragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.container, fragment)
                        .commit();
                return;
            case COMPASS_ID:
                fragment = Compass_Fragment.newInstance(position + 1);
                break;
            case ABOUT_ID:
                break;
            case BLOG_ID:
                break;
            case SETTINGS_ID:
                fragment = new SettingsActivity();
                fragmentName="SettingsActivity";

                break;
        }

        if (fragment != null) {
            fragment.setArguments(args);
            fragmentManager.beginTransaction().addToBackStack(fragmentName).replace(R.id.container, fragment)
                    .commit();
        }
    }

    public void onSectionAttached(int number) {
        String[] titles = getResources().getStringArray(R.array.titles);

        mTitle =titles[number];

    }


    /**
     * Sets up the account box. This account box is the area at the top of the nav drawer that
     * shows which account the user is logged in as, and lets them switch accounts. It also
     * shows the user's Google+ cover photo as background.
     */
    private void setupAccountBox() {
        mAccountListContainer = (LinearLayout) findViewById(R.id.account_list);
        Account chosenAccount = AccountUtils.getActiveAccount(this);
        if (mAccountListContainer == null) {
            // This  activity does not have an account box.
            return;
        }

        final View chosenAccountView = findViewById(R.id.chosen_account_view);
        if (chosenAccount == null) {
            // No Account logged in. hide account box;
            chosenAccountView.setVisibility(View.GONE);
            mAccountListContainer.setVisibility(View.GONE);
            return;
        } else {
            chosenAccountView.setVisibility(View.VISIBLE);
            mAccountListContainer.setVisibility(View.INVISIBLE);
        }

        AccountManager am = AccountManager.get(this);
        Account[] accountArray = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        List<Account> accounts = new ArrayList<>(Arrays.asList(accountArray));
        accounts.remove(chosenAccount);

        final ImageView coverImageView = (ImageView) chosenAccountView.findViewById(R.id.profile_cover_image);
        final ImageView profileImageView = (ImageView) chosenAccountView.findViewById(R.id.profile_image);
        final TextView nameTextView = (TextView) chosenAccountView.findViewById(R.id.profile_name_text);
        final TextView email = (TextView) chosenAccountView.findViewById(R.id.profile_email_text);
        mExpandAccountBoxIndicator = (ImageView) findViewById(R.id.expand_account_box_indicator);

        final String name = AccountUtils.getPlusName(this);

        if (name == null) {
            nameTextView.setVisibility(View.GONE);
        } else {
            nameTextView.setVisibility(View.VISIBLE);
            nameTextView.setText(name);
        }
        final String imageUrl = AccountUtils.getPlusImageUrl(this);
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(profileImageView);
        }

        final String coverImageUrl = AccountUtils.getPlusCoverUrl(this);
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

        setupAccountBoxToggle();

        populateAccountList(accounts);

    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses
     * of BaseActivity override this to indicate what nav drawer item corresponds to them
     * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    private void populateAccountList(List<Account> accounts) {
        mAccountListContainer.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        for (Account account : accounts) {
            View itemView = layoutInflater.inflate(R.layout.list_item_account, mAccountListContainer, false);
            ((TextView) itemView.findViewById(R.id.profile_email_text)).setText(account.name);
            final String accountName = account.name;
            String imageUrl = AccountUtils.getPlusImageUrl(this, accountName);
            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(this).load(imageUrl).into((ImageView) itemView.findViewById(R.id.profile_image));
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    if (activeNetwork == null || !activeNetwork.isConnected()) {
                        // if there's no network, don't try to change the selected account
                        Toast.makeText(BaseActivity.this, R.string.no_connection_cant_login, Toast.LENGTH_SHORT).show();
                        mDrawerLayout.closeDrawer(Gravity.START);
                    } else {
                        Log.d(TAG, "User requested switch to account: " + accountName);
                        AccountUtils.setActiveAccount(BaseActivity.this, accountName);
                        onAccountChangeRequested();
                        startLoginProcess();
                        mAccountBoxExpanded = false;
                        setupAccountBoxToggle();
                        mDrawerLayout.closeDrawer(Gravity.START);
                        setupAccountBox();
                    }
                }
            });
            mAccountListContainer.addView(itemView);
        }
    }

    protected void onAccountChangeRequested() {
        // override if you want to be notified when another account has been selected account has changed
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Verifies the proper version of Google Play Services Exists on the device.
        PlayServicesUtils.checkGooglePlaySevices(this);

        // watch for sync state changes

//        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING | ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
//        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);

    }

    public void restoreActionBar() {
/*        ActionBar actionBar = getSupportActionBar();
        Toolbar tb = (Toolbar)this.findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setTitle(mTitle);
*/

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = (Fragment)new SettingsActivity();
            fragmentManager.beginTransaction().addToBackStack(fragment.getTag()).replace(R.id.container,fragment).commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPlusInfoLoaded(String accountName) {
        setupAccountBox();
        populateNavDrawer();
    }

    @Override
    public void onAuthSuccess(String accountName, boolean newlyAuthenticated) {
        Log.d(TAG, "onAuthSuccess, account " + accountName + ", newlyAuthenticated=" + newlyAuthenticated);

        refreshAccountDependantData();

        if (newlyAuthenticated) {
            Log.d(TAG, "Enabling auto sync on content provider for account " + accountName);
            //    SyncHelper.updateSyncInterval(this, account);
            //    SyncHelper.requestManualSync(account);
        }

        setupAccountBox();
        populateNavDrawer();
        //       registerGCMClient();

    }


    private void populateNavDrawer() {

        // decide which items will show
        if (AccountUtils.hasActiveAccount(this)) {
            // Only Logged in users can save data , so if there is no active account,
            // there is no data
            Log.d(TAG, "has active account");

        } else {
            Log.d(TAG, "no active account");
        }
    }


    @Override
    public void onAuthFailure(String accountName) {
        Log.d(TAG, "Auth failed for account " + accountName);
        refreshAccountDependantData();
    }

    void refreshAccountDependantData() {
        // Force local data refresh for data that depends on the logged user:
        Log.d(TAG, "Refreshing MySchedule data");
//        getContentResolver().notifyChange(ScheduleContract.MySchedule.CONTENT_URI, null, false);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }


        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    @Override
    public void onStart() {
        super.onStart();



        // Set up a notification to let us know when were connected or disconnected from the firebase servers
        ValueEventListener mConnectedListener = mFirebaseRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                Log.d(TAG, connected ? "Connected to firebase." : "Disconnected from firebase.");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // no op

            }
        });
    }

    private Object userData;

    private void setupAccountBoxToggle() {
        int selfItem = getSelfNavDrawerItem();
        if (mDrawerLayout == null || selfItem == NAVDRAWER_ITEM_INVALID) {
            // this Activity does not have a nav drawer
            return;
        }
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