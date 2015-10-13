package com.solutions.nerd.sailing.sailingwatchface.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mookie on 9/4/15.
 */
public class ObservationLocation {
    @SerializedName("full")
    public String full;


    @SerializedName("city")
    public String city;


    @SerializedName("state")
    public String state;

    @SerializedName("country")
    public String country;


    @SerializedName("country_iso3166")
    public String country_iso3166;


    @SerializedName("latitude")
    public String latitude;


    @SerializedName("longitude")
    public String longitude;


    @SerializedName("elevation")
    public String elevation;

    public ObservationLocation(){}
}
