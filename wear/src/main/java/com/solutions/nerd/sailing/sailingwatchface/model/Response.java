package com.solutions.nerd.sailing.sailingwatchface.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mookie on 9/4/15.
 */
public class Response {
    @SerializedName("version")
    public String version;

    @SerializedName("termsofService")
    public String termsofService;

    public Features features;

}
