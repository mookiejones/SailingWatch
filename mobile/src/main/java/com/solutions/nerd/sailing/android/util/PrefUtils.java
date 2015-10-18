package com.solutions.nerd.sailing.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.solutions.nerd.sailing.android.Config;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.TimeZone;

/**
 * Created by cberman on 12/16/2014.
 */
public class PrefUtils {
    private static final String TAG = PrefUtils.class.getSimpleName();


    public static final String PREF_SHOW_MARINAS = "show_marinas";

    public static final String PREF_SHOW_WEATHER = "show_weather";
    public static final String PREF_SHOW_BRIDGES="show_bridges";
    public static final String PREF_SHOW_POI="show_poi";
    public static final String PREF_SHOW_RAMPS="show_ramps";
    public static final String PREF_SHOW_NAVAIDS="show_navaids";
    /**
     * Boolean preference that indicates whether we installed the boostrap data or not.
     */
    public static final String PREF_DATA_BOOTSTRAP_DONE = "pref_data_bootstrap_done";

    /**
     * Boolean indicating whether we should attempt to sign in on startup (default true).
     */
    public static final String PREF_USER_REFUSED_SIGN_IN = "pref_user_refused_sign_in";

    /**
     * Boolean indicating whether the debug build warning was already shown.
     */
    public static final String PREF_DEBUG_BUILD_WARNING_SHOWN = "pref_debug_build_warning_shown";

    /** Boolean indicating whether ToS has been accepted */
    public static final String PREF_TOS_ACCEPTED = "pref_tos_accepted";

    /** Boolean indicating whether user has answered if they are local or remote. */
    public static final String PREF_ANSWERED_LOCAL_OR_REMOTE = "pref_answered_local_or_remote";

    /** Long indicating when a sync was last ATTEMPTED (not necessarily succeeded) */
    public static final String PREF_LAST_SYNC_ATTEMPTED = "pref_last_sync_attempted";

    /** Long indicating when a sync last SUCCEEDED */
    public static final String PREF_LAST_SYNC_SUCCEEDED = "pref_last_sync_succeeded";

    /** Sync interval that's currently configured */
    public static final String PREF_CUR_SYNC_INTERVAL = "pref_cur_sync_interval";


    public static final String PREF_UPDATE_DISTANCE = "pref_update_distance";
    public static final String PREF_UPDATE_TIME = "pref_update_time";
    /**
     * Boolean indicating whether we performed the (one-time) welcome flow.
     */
    public static final String PREF_WELCOME_DONE = "pref_welcome_done";

    /** Boolean indicating if we can collect and Analytics */
    public static final String PREF_ANALYTICS_ENABLED = "pref_analytics_enabled";
    private static final String PREF_GOT_GOOGLE_ID = "pref_got_google_id";
    private static final String PREF_USER_ID = "pref_user_id";
    private static final String PREF_GOT_USER_ID = "pref_got_user_id";
    private static final String PREF_ID_TYPE = "pref_id_type";
    public static final String PREF_OVERLAY_ALPHA ="pref_overlay_alpha" ;
    public static final String PREF_UPDATE_ACCURACY ="pref_update_accuracy" ;


    public static void markDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DATA_BOOTSTRAP_DONE, true).commit();
    }

    public static boolean isDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DATA_BOOTSTRAP_DONE, false);
    }
    /**
     * Integer preference that indicates what conference year the application is configured
     * for. Typically, if this isn't an exact match, preferences should be wiped to re-run
     * setup.
     */
    public static final String PREF_CONFERENCE_YEAR = "pref_conference_year";

    public static void init(final Context context) {
        // Check what year we're configured for
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int conferenceYear = sp.getInt(PREF_CONFERENCE_YEAR, 0);
        if (conferenceYear != Config.CONFERENCE_YEAR) {
            Log.d(TAG, "App not yet set up for " + PREF_CONFERENCE_YEAR + ". Resetting data.");
            // Application is configured for a different conference year. Reset preferences.
            sp.edit().clear().putInt(PREF_CONFERENCE_YEAR, Config.CONFERENCE_YEAR).commit();
        }
    }


    public static void markUserRefusedSignIn(final Context context) {
        markUserRefusedSignIn(context, true);
    }

    public static void markUserRefusedSignIn(final Context context, final boolean refused) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_USER_REFUSED_SIGN_IN, refused).commit();
    }


    public static boolean hasUserRefusedSignIn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_USER_REFUSED_SIGN_IN, false);
    }

    public static boolean wasDebugWarningShown(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DEBUG_BUILD_WARNING_SHOWN, false);
    }

    public static void markDebugWarningShown(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DEBUG_BUILD_WARNING_SHOWN, true).commit();
    }


    public static boolean gotUserID(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_GOT_USER_ID,false);
    }
    public static void markUserID(final Context context,String id, String id_type){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit()
                .putString(PREF_ID_TYPE,id_type)
                .putString(PREF_USER_ID,id)
                .commit();
    }
    public static void markGotUserID(final Context context,boolean value){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_GOT_USER_ID,value).commit();
    }

    public static String getGoogleID(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_USER_ID,"");
    }
    public static boolean isTosAccepted(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_TOS_ACCEPTED, false);
    }

    public static void markTosAccepted(final Context context) {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(PREF_TOS_ACCEPTED, true).commit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean hasAnsweredLocalOrRemote(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ANSWERED_LOCAL_OR_REMOTE, false);
    }

    public static void markAnsweredLocalOrRemote(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_ANSWERED_LOCAL_OR_REMOTE, true).commit();
    }

    public static boolean isWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    public static void markWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, true).commit();
    }

    public static long getLastSyncAttemptedTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_ATTEMPTED, 0L);
    }

    public static void markSyncAttemptedNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_SYNC_ATTEMPTED, UIUtils.getCurrentTime(context)).commit();
    }

    public static long getLastSyncSucceededTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_SUCCEEDED, 0L);
    }

    public static void markSyncSucceededNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_SYNC_SUCCEEDED, UIUtils.getCurrentTime(context)).commit();
    }
    /**
     * Boolean preference that when checked, indicates that the user would like to see times
     * in their local timezone throughout the app.
     */
    public static final String PREF_LOCAL_TIMES = "pref_local_times";

    public static TimeZone getDisplayTimeZone(Context context) {
        TimeZone defaultTz = TimeZone.getDefault();
        return (isUsingLocalTime(context) && defaultTz != null)
                ? defaultTz : Config.CONFERENCE_TIMEZONE;
    }

    public static boolean isUsingLocalTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_LOCAL_TIMES, false);
    }

    public static void setUsingLocalTime(final Context context, final boolean usingLocalTime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_LOCAL_TIMES, usingLocalTime).commit();
    }
    public static boolean isAnalyticsEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ANALYTICS_ENABLED, true);
    }


    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

}
