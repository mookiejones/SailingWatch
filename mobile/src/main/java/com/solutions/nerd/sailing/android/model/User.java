package com.firstmate.android.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

/**
 * Created by cberman on 12/17/2014.
 */
public class User {
    private static final String TAG = User.class.getSimpleName();


    @JsonIgnore
    private String key;
    public User(){
        mMarkers = new HashMap<String,Marker>();

    }

    public String getKey(){
        return key;
    }
    public void setKey(String key){
        this.key=key;
    }

    public HashMap<String, Marker> getMarkers() {
        return mMarkers;
    }

    public void setMarkers(HashMap<String, Marker> mMarkers) {
        this.mMarkers = mMarkers;
    }

    private HashMap<String,Marker> mMarkers;




}

