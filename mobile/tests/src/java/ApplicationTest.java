package com.solutions.nerd.sailing.sailingwatchface;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.util.Calendar;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest e{
    public ApplicationTest() {
        super(Application.class);


// allocate a Calendar to calculate local time using the UTC time and time zone
        Calendar mCalendar = Calendar.getInstance();

        mCalendar.setTimeInMillis(System.currentTimeMillis());

        float mSeconds=mCalendar.get(Calendar.SECOND);
        float mMinutes=mCalendar.get(Calendar.MINUTE);
        float mMilliseconds=mCalendar.get(Calendar.MILLISECOND);
        float mHours=mCalendar.get(Calendar.HOUR);
        String s=String.format("%s:%02.0f", mHours, mMinutes);
        Log.d(ApplicationTest.class.getSimpleName(), s);
    }
}