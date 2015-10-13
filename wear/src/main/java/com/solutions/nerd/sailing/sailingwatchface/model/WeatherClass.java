package com.solutions.nerd.sailing.sailingwatchface.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by mookie on 9/4/15.
 */
public class WeatherClass {

    public Response response;
    private CurrentObservation current_observation;

    public WeatherClass(){}


    public String getWindDirection(){
        return String.format("Direction: %s", current_observation.wind_dir);
    }

    public String windFormat(){
       return String.format("Wind: %s/%s %s",toKnots(current_observation.wind_kph),toKnots(current_observation.wind_gust_kph),current_observation.wind_dir);
    }

    public String windString(){
        String wind_string=current_observation.wind_string;
        String wind=toKnots(current_observation.wind_kph);
        String gust=toKnots(current_observation.wind_gust_kph);
        String dir=current_observation.wind_dir;
        int deg=current_observation.wind_degrees;

        return String.format("Wind: %s %s/%s %s %d°",wind_string,wind,gust,dir,deg);
    }

    public String getWindKnots(){
        return String.format("Wind: %s kts", toKnots(current_observation.wind_kph));

    }

    public String getTempString(){
        return current_observation.temperature_string;
    }
    public String getTemp(){
        return String.format("Temp: %s",current_observation.temp_f);
    }

    public String getWindGustKnots(){
        return String.format("Wind Gust: %s kts", toKnots(current_observation.wind_gust_kph));
    }

    public String getWeatherString(){
        String weather = current_observation.weather;
        String temp=current_observation.temp_f;
        return String.format("%s %s",weather,temp);
    }

    private Bitmap mBitmap;
    public void setIcon(byte[] array){


       mBitmap = BitmapFactory.decodeByteArray(array , 0, array .length);


    }
    public Bitmap getIcon(){
        return mBitmap;
    }

    public String getTime(){
        return current_observation.observation_time;
    }

    public String getZip(){
        return current_observation.display_location.zip;
    }
    public String getState(){
        return current_observation.display_location.state;

    }
    public String getCity(){
        return current_observation.display_location.city;
    }
    private String toKnots(String value){
        double val=Double.parseDouble(value)*0.539957;
        return String.valueOf(val);
    }

}
