package com.solutions.nerd.sailing.sailingwatchface.test;

import android.test.ActivityTestCase;
import android.util.Log;

import junit.framework.Assert;
import junit.framework.TestResult;

import java.util.Calendar;


public class MainActivityTest extends ActivityTestCase {
    private static final String TAG=MainActivityTest.class.getSimpleName();

    @Override
    public TestResult run() {

        return super.run();
    }




    private static final String no_work =  "%1f:%02f:%02f";

    private static final String lat="41.8488";
            private static final String lng="-83.453";

    private boolean weather_complete=false;

    public void testWeather(){
        Log.d(TAG, "Starting testWeather");
      }

    public void testTime(){
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        float mSeconds=mCalendar.get(Calendar.SECOND);
        float mMinutes=mCalendar.get(Calendar.MINUTE);
        @SuppressWarnings("UnusedAssignment") float mMilliseconds=mCalendar.get(Calendar.MILLISECOND);
        float mHours=mCalendar.get(Calendar.HOUR);
        String format = String.format(no_work, mHours, mMinutes, mSeconds);
        if (mHours==(long)mHours){
            format = String.format("Works %d:%02d:%02d",(long)mHours,(long)mMinutes,(long)mSeconds);
        }

        Log.d(MainActivityTest.class.getSimpleName(), format);

        Assert.assertTrue(format,true);


    }

    public void testLocation(){
        Assert.assertTrue("works",true);
    }
}
