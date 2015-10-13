package com.solutions.nerd.sailing.sailingwatchface;


import android.graphics.Typeface;

import java.util.concurrent.TimeUnit;

class Consts {


    public static final String NO_GPS_MSG="This hardware doesn't have GPS.";
    public static final String PATH_WEATHER="/weather";
    public static final String PATH_LOCATION="/location";

    public static final String KEY_CONFIG_REQUIRE_INTERVAL = "RequireInterval";
    public static final String KEY_CONFIG_TEMPERATURE_SCALE = "TemperatureScale";
    public static final String KEY_WEATHER_CONDITION = "Condition";
    public static final String KEY_WEATHER_SUNRISE = "Sunrise";
    public static final String KEY_WEATHER_SUNSET = "Sunset";
    public static final String KEY_CONFIG_THEME = "Theme";
    public static final String KEY_CONFIG_TIME_UNIT = "TimeUnit";
    public static final String KEY_WEATHER_TEMPERATURE = "Temperature";
    public static final String KEY_WEATHER_UPDATE_TIME = "Update_Time";
    public static final String PATH_CONFIG = "/WeatherWatchFace/Config/";
    public static final String PATH_WEATHER_INFO = "/WeatherWatchFace/WeatherInfo";
    public static final String PATH_LOCATION_INFO = "/WeatherWatchFace/LocationInfo";
    public static final String PATH_WEATHER_REQUIRE = "/WeatherService/Require";
    public static final String COLON_STRING = ":";
    public static final String PACKAGE_NAME = Consts.class.getPackage().getName();

    public static final String TIME_FULL = "%d:%02d:%02d";
    public static final String DATE_FULL="%d / %d / %d";

    public static final String CIRCLE_NAME="circleRadius";
    public static final String ALPHA="alpha";

    public static final String NORTH="N";
    public static final String SOUTH="S";
    public static final String EAST="E";
    public static final String WEST="W";

    public static final String SouthWest="SW";
    public static final String SouthEast="SE";

    protected static final long UPDATE_INTERVAL_MS=3000;
    protected static final long FASTEST_INTERVAL_MS=1000;
    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    public static final long INTERACTIVE_UPDATE_RATE_MS = 1000;

    public static final String[] Directions={"N","NE","E","SE","S","SW","W","NW"};
    public static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    public  static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    public static final int DIGITAL=1;
    public static final int ANALOG=2;
    public static final int MSG_UPDATE_TIME = 0;
    public static final int MSG_GET_WEATHER = 1;
    public static final String time_ambient = "%d:%02d";
    private static final long CYCLE_PERIOD_SECONDS = 5;
    /** Alpha value for drawing time when in mute mode. */
    public static final int MUTE_ALPHA = 100;


    private static final long FPS = 60;
    /** Number of camera angles to precompute. */
    private static final int mNumCameraAngles = (int) (CYCLE_PERIOD_SECONDS * FPS);
    /** Projection transformation matrix. Converts from 3D to 2D. */
    private static final float[] mProjectionMatrix = new float[16];
    /** Expected frame rate in interactive mode. */

    /** Z distance from the camera to the watchface. */
    private static final float EYE_Z = 2.3f;

    /** How long each frame is displayed at expected frame rate. */
    private static final long FRAME_PERIOD_MS = TimeUnit.SECONDS.toMillis(1) / FPS;

public static final String WEAR="wear";
    private static final int TICK_COUNT = 48;
    private static final int RING_THICKNESS = 50;
    private static final int TICK_THICKNESS = 10;
    private static final int LONG_TICK = 30;
    private static final int SHORT_TICK = 10;
    public static final String
            VOICE_TRANSCRIPTION_CAPABILITY_NAME = "voice_transcription";

}
