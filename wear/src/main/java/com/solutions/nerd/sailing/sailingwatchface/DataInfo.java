package com.solutions.nerd.sailing.sailingwatchface;

import com.google.android.gms.wearable.DataMap;

import java.util.Set;


/*

{
"response": {
"version": "0.1",
"termsofService": "http://www.wunderground.com/weather/api/d/terms.html",
"features": {
"conditions": 1
}
},
"current_observation": {
"image": {
"url": "http://icons.wxug.com/graphics/wu2/logo_130x80.png",
"title": "Weather Underground",
"link": "http://www.wunderground.com"
},
"display_location": {
"full": "Dearborn Heights, MI",
"city": "Dearborn Heights",
"state": "MI",
"state_name": "Michigan",
"country": "US",
"country_iso3166": "US",
"zip": "48125",
"magic": "1",
"wmo": "99999",
"latitude": "42.280000",
"longitude": "-83.270000",
"elevation": "189.00000000"
},
"observation_location": {
"full": "APRSWXNET, Dearborn, Michigan",
"city": "APRSWXNET, Dearborn",
"state": "Michigan",
"country": "US",
"country_iso3166": "US",
"latitude": "42.293331",
"longitude": "-83.268669",
"elevation": "659 ft"
},
"estimated": {},
"station_id": "MC3625",
"observation_time": "Last Updated on August 28, 6:32 PM EDT",
"observation_time_rfc822": "Fri, 28 Aug 2015 18:32:00 -0400",
"observation_epoch": "1440801120",
"local_time_rfc822": "Fri, 28 Aug 2015 19:07:57 -0400",
"local_epoch": "1440803277",
"local_tz_short": "EDT",
"local_tz_long": "America/New_York",
"local_tz_offset": "-0400",
"weather": "Mostly Cloudy",
"temperature_string": "73 F (22.8 C)",
"temp_f": 73,
"temp_c": 22.8,
"relative_humidity": "67%",
"wind_string": "Calm",
"wind_dir": "East",
"wind_degrees": 98,
"wind_mph": 0,
"wind_gust_mph": 0,
"wind_kph": 0,
"wind_gust_kph": 0,
"pressure_mb": "1020",
"pressure_in": "30.14",
"pressure_trend": "0",
"dewpoint_string": "61 F (16 C)",
"dewpoint_f": 61,
"dewpoint_c": 16,
"heat_index_string": "NA",
"heat_index_f": "NA",
"heat_index_c": "NA",
"windchill_string": "NA",
"windchill_f": "NA",
"windchill_c": "NA",
"feelslike_string": "73 F (22.8 C)",
"feelslike_f": "73",
"feelslike_c": "22.8",
"visibility_mi": "10.0",
"visibility_km": "16.1",
"solarradiation": "--",
"UV": "0",
"precip_1hr_string": "0.00 in ( 0 mm)",
"precip_1hr_in": "0.00",
"precip_1hr_metric": " 0",
"precip_today_string": " in ( mm)",
"precip_today_in": "",
"precip_today_metric": "--",
"icon": "mostlycloudy",
"icon_url": "http://icons.wxug.com/i/c/k/mostlycloudy.gif",
"forecast_url": "http://www.wunderground.com/US/MI/Dearborn_Heights.html",
"history_url": "http://www.wunderground.com/weatherstation/WXDailyHistory.asp?ID=MC3625",
"ob_url": "http://www.wunderground.com/cgi-bin/findweather/getForecast?query=42.293331,-83.268669",
"nowcast": ""
}
}





 */


/**
 * Created by mookie on 8/28/15.
 */
public class DataInfo {
    public final static int Imperial=0;
    public final static int Metric=1;
    public static final int Nautical=2;

    private String mSunride;
    private String mSunset;
    private String mWeatherCondition;
    private String mWeatherUpdateTime;


    private String mWindDirection;
    private String mWindDegrees;
    private String mWindGust;
    private String mStationID;
    private String mState;
    private String mPressure_IN;
    private String mPressure_MB;
    private String mPressureTrend;
    private String mDewpointString;
    private String mDewpoint_C;
    private String mDewpoint_F;
    private String mIcon;
    private int mUnitType=Nautical;

    enum WeatherType{
        Imperial,
        Metric,
        Nautical
    }
    public DataInfo(){

    }
    public DataInfo(DataMap data){
        getData(data);
    }
    public DataInfo(int unitType){

    }
    public DataInfo(WeatherType unitType){

    }
    public void setData(DataMap data){
        getData(data);

    }

    private void getData(DataMap data){
        Set<String> keys= data.keySet();

    }

    public String getDewpoint_C() {
        return mDewpoint_C;
    }

    public void setDewpoint_C(String mDewpoint_C) {
        this.mDewpoint_C = mDewpoint_C;
    }
}
