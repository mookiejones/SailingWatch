package com.solutions.nerd.sailing.glass.util;

import android.util.Log;

/**
 * Created by mookie on 10/12/15.
 */
public class LogUtils {
    public static void LogDebug(final String TAG,final String text){
        if (Log.isLoggable(TAG,Log.DEBUG))
            Log.d(TAG,text);
    }

    public static void LogInfo(final String TAG,final String text){
        if (Log.isLoggable(TAG,Log.INFO))
            Log.i(TAG, text);
    }

    public static void LogWarning(final String TAG,final String text){
        if (Log.isLoggable(TAG,Log.WARN))
            Log.w(TAG,text);
    }
}
