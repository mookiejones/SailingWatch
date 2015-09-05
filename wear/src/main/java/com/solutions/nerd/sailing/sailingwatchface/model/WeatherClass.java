package com.solutions.nerd.sailing.sailingwatchface.model;

/**
 * Created by mookie on 9/4/15.
 */
public class WeatherClass {

    public Response response;
    public CurrentObservation current_observation;

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

        return String.format("Wind: %s %s/%s %s %d",wind_string,wind,gust,dir,deg);
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

    private String toKnots(String value){
        double val=Double.parseDouble(value)*0.539957;
        return String.valueOf(val);
    }

}
