package com.solutions.nerd.sailing.sailingwatchface.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mookie on 9/4/15.
 */
public class DisplayLocation {

    @SerializedName("full")
    public String full;
    @SerializedName("city")
    public String city;
    @SerializedName("state")
    public String state;
    @SerializedName("state_name")
    public String state_name;

    @SerializedName("country")
    public String country;

    @SerializedName("country_iso3166")
    public String country_iso3166;

    @SerializedName("zip")
    public String zip;


    @SerializedName("magic")
    public String magic;

    @SerializedName("wmo")
    public String wmo;

    @SerializedName("latitude")
    public String latitude;

    @SerializedName("longitude")
    public String longitude;


    @SerializedName("evolution")
    public String evolution;

    public DisplayLocation(){}

}
