package com.solutions.nerd.sailing.glass.util;
import android.util.Log;

public class UnitUtils {

    public static final double mPerSec2KM = 3.6;
    public static final double mPerSec2MPH=2.23694;
    public static final double mPerSec2NM=1.94384449;

    public enum UnitType{Metric,Imperial,Nautical}
    public static String getVelocity(UnitType unit,float value)
    {
        Log.i("Velocity ", String.valueOf(value));
        if (unit==UnitType.Metric)
            return String.format("%.2f kph",value * mPerSec2KM);
        if (unit==UnitType.Imperial)
            return String.format("%.2f mph",value * mPerSec2MPH);
        if (unit==UnitType.Nautical)
            return String.format("%.2f knots",value * mPerSec2NM);

        return "";
    }

    public static void getDistance(UnitType unit,float value)
    {

    }
}