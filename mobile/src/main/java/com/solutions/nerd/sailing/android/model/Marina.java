package com.solutions.nerd.sailing.android.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.android.gms.maps.model.Marker;
//import com.google.maps.android.clustering.ClusterItem;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by cberman on 12/29/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class Marina implements ClusterItem {

    private static final String TAG = Marina.class.getSimpleName();
    private static final String MARINA_ID = "id";
    private static final String MARINA_NAME="name";
    private static final String MARINA_SIMPLENAME="simple_name";
    private static final String MARINA_MCU="mcu";
    private static final String MARINA_CITY="city";
    private static final String MARINA_STATE="state";
    private static final String MARINA_DOCKMASTER="dockmaster";
    private static final String MARINA_DOCKRATE="dockrate";
    private static final String MARINA_LAT="latd";
    private static final String MARINA_LNG="lond";
    private Marker mMarker = null;
    public Marker getMarker(){return mMarker;}
    public void setMarker(Marker marker){mMarker = marker;}
    public String getName(){return name;}
    public String getCity(){return city;}
    public String getState(){return state;}
    public String getDockmaster(){return dockmaster;}
    public String getDockrate(){return dockrate;}
    public String getSimpleName(){return simple_name;}
//    public LatLng getLatLng(){
    //    return new LatLng(latd,lond);
 //   }
    public String getMcu(){return mcu;}
    public void setMcu(String mcu){this.mcu=mcu;}
    private int mId;
    public String name;
    public String mcu;
    public String city;
    public String state;
    public String dockmaster;
    public String dockrate;
    public String simple_name;
    public Double lond;
    public Double latd;
    private  LatLng mPosition;

    //Cache variables
    @JsonIgnore
    private String key;
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Marina(){


    }
    public Marina(HashMap map){
        mId = Integer.parseInt(map.get(MARINA_ID).toString());
        name = map.get(MARINA_NAME).toString();
        simple_name =map.get(MARINA_SIMPLENAME).toString();
        dockmaster=map.get(MARINA_DOCKMASTER).toString();
        dockrate=map.get(MARINA_DOCKRATE).toString();
        mcu=map.get(MARINA_MCU).toString();
        city=map.get(MARINA_CITY).toString();
        state = map.get(MARINA_STATE).toString();
        lond=Double.parseDouble(map.get(MARINA_LNG).toString());
        latd=Double.parseDouble(map.get(MARINA_LAT).toString());
        mPosition = new LatLng(lond,latd);

    }
    public Marina(JSONObject object){
        mId = object.optInt(MARINA_ID);
        name = object.optString(MARINA_NAME);
        simple_name =object.optString(MARINA_SIMPLENAME);
        dockmaster=object.optString(MARINA_DOCKMASTER);
        dockrate=object.optString(MARINA_DOCKRATE);
        mcu=object.optString(MARINA_MCU);
        city=object.optString(MARINA_CITY);
        state = object.optString(MARINA_STATE);
      //  lond=object.optDouble(MARINA_LNG);
     //   latd=object.optDouble(MARINA_LAT);
        mPosition = new LatLng(0,0);
    }


    @Override
    public LatLng getPosition() {
        return new LatLng(latd,lond);

    }
}
