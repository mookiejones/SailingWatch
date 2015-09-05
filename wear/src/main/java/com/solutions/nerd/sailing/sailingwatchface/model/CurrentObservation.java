package com.solutions.nerd.sailing.sailingwatchface.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mookie on 9/4/15.
 */
public class CurrentObservation {
    public Image image;
    public DisplayLocation display_location;
    public ObservationLocation observation_location;


    @SerializedName("station_id")
    public String station_id;


    @SerializedName("observation_time")
    public String observation_time;


    @SerializedName("observation_time_rfc822")
    public String observation_time_rfc822;


    @SerializedName("observation_epoch")
    public String observation_epoch;


    @SerializedName("local_time_rfc822")
    public String local_time_rfc822;


    @SerializedName("local_epoch")
    public String local_epoch;


    @SerializedName("local_tz_short")
    public String local_tz_short;


    @SerializedName("local_tz_long")
    public String local_tz_long;


    @SerializedName("local_tz_offset")
    public String local_tz_offset;

    @SerializedName("weather")
    public String weather;

    @SerializedName("temperature_string")
    public String temperature_string;


    @SerializedName("temp_f")
    public String temp_f;

    @SerializedName("temp_c")
    public String temp_c;

    @SerializedName("relative_humidity")
    public String relative_humidity;


    @SerializedName("wind_string")
    public String wind_string;


    @SerializedName("wind_dir")
    public String wind_dir;


    @SerializedName("wind_degrees")
    public int wind_degrees;


    @SerializedName("wind_mph")
    public String wind_mph;


    @SerializedName("wind_gust_mph")
    public String wind_gust_mph;

    @SerializedName("wind_kph")
    public String wind_kph;




    @SerializedName("wind_gust_kph")
    public String wind_gust_kph;

    @SerializedName("pressure_mb")
    public String pressure_mb;

    @SerializedName("pressure_in")
    public String pressure_in;

    @SerializedName("pressure_trend")
    public String pressure_trend;

    @SerializedName("dewpoint_string")
    public String dewpoint_string;

    @SerializedName("dewpoint_f")
    public String dewpoint_f;

    @SerializedName("dewpoint_c")
    public String dewpoint_c;

    @SerializedName("heat_index_string")
    public String heat_index_string;

    @SerializedName("heat_index_f")
    public String heat_index_f;

    @SerializedName("heat_index_c")
    public String heat_index_c;

    @SerializedName("windchill_string")
    public String windchill_string;

    @SerializedName("windchill_f")
    public String windchill_f;

    @SerializedName("windchill_c")
    public String windchill_c;

    @SerializedName("feelslike_string")
    public String feelslike_string;

    @SerializedName("feelslike_f")
    public String feelslike_f;

    @SerializedName("feelslike_c")
    public String feelslike_c;

    @SerializedName("visibility_mi")
    public String visibility_mi;

    @SerializedName("visibility_km")
    public String visibility_km;

    @SerializedName("solarradiation")
    public String solarradiation;

    @SerializedName("UV")
    public String UV;

    @SerializedName("precip_1hr_string")
    public String precip_1hr_string;

    @SerializedName("precip_1hr_in")
    public String precip_1hr_in;

    @SerializedName("precip_1hr_metric")
    public String precip_1hr_metric;

    @SerializedName("precip_today_string")
    public String precip_today_string;

    @SerializedName("precip_today_in")
    public String precip_today_in;

    @SerializedName("precip_today_metric")
    public String precip_today_metric;


    @SerializedName("icon")
    public String icon;

    @SerializedName("icon_url")
    public String icon_url;

    @SerializedName("forecast_url")
    public String forecast_url;

    @SerializedName("history_url")
    public String history_url;

    @SerializedName("ob_url")
    public String ob_url;

    @SerializedName("nowcast")
    public String nowcast;

}
