package com.solutions.nerd.sailing.android;


class Consts {
    public static final String SINGLE_LOCATION_UPDATE_ACTION = "single_update";

    public static final String WEATHER_URL= "http://api.wunderground.com/api/b52ad4185dacf690/conditions/q/";
    public static final String KEY_LOCATION_LATITUDE="Latitude";
    public static final String KEY_LOCATION_LONGITUDE="Longitude";
    public static final String KEY_LOCATION_BEARING="Bearing";
    public static final String KEY_LOCATION_SPEED="Speed";
    public static final  String KEY_WEATHER_CONDITION   = "Condition";
    public static final  String KEY_WEATHER_SUNRISE     = "Sunrise";
    public static final  String KEY_WEATHER_SUNSET      = "Sunset";
    public static final  String KEY_WEATHER_TEMPERATURE = "Temperature";
    public static final  String PATH_WEATHER_INFO       = "/WeatherWatchFace/WeatherInfo";
    public static final  String PATH_LOCATION_INFO       = "/WeatherWatchFace/LocationInfo";
    public static final  String PATH_SERVICE_REQUIRE    = "/WeatherService/Require";
    public static final  String PATH_SERVICE_WEATHER    = "/WeatherService/Weather";
    public static final String image_regex="icon_url\":\\s*\"([^\"]+)\n";

}
