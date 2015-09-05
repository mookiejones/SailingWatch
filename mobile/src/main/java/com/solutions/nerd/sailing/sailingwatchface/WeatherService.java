package com.solutions.nerd.sailing.sailingwatchface;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;


public class WeatherService extends WearableListenerService
    implements    GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
    {
    private static final String TAG                     = "WeatherService";
        private static final String SINGLE_LOCATION_UPDATE_ACTION = "single_update";
        private static GoogleApiClient mGoogleApiClient;
    private static LocationManager mLocationManager;
    private static Location mLocation;

    private String          mPeerId;
    @Override
    public int onStartCommand( Intent intent, int flags, int startId )
    {
        Log.e(TAG, "onStartCommand");
        if (mLocationManager==null)
            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if ( intent != null )
        {
            if ( WeatherWatchFaceConfigActivity.class.getSimpleName().equals( intent.getAction() ) )
            {
                mPeerId = intent.getStringExtra("PeerId");
                getWeather();

            }
        }

        return super.onStartCommand( intent, flags, startId );
    }

    @Override
    public void onMessageReceived( MessageEvent messageEvent )
    {
        super.onMessageReceived( messageEvent );


        mPeerId = messageEvent.getSourceNodeId();
        Log.d(TAG, "MessageReceived: path:" + messageEvent.getPath() + " peerId: " + mPeerId);

        if ( messageEvent.getPath().equals(Consts.PATH_SERVICE_REQUIRE) )
        {
            startTask();
            getWeather();
        }
    }

    @Override
    public void onConnectedNodes(List<Node> connectedNodes) {
        super.onConnectedNodes(connectedNodes);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(WeatherService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        if (BuildConfig.DEBUG){
            android.os.Debug.waitForDebugger();
        }

    }
    @Override
    public void onConnected(Bundle bundle) {
        getWeather();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + location);
            try {
//                mLocationManager.removeUpdates(this);
            }catch(Exception e){
                Log.e(TAG,e.getMessage());
            }
            mLocation = location;



            List<String> params = new ArrayList<String>();
            params.add(String.valueOf(location.getLatitude()));
            params.add(String.valueOf(location.getLongitude()));

            String[] p=(String[])params.toArray();

            WeatherTask wt = new WeatherTask();
            wt.execute(p);




        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };



        private void requestLocationUpdate() {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);

            PendingIntent singleUpdatePI = PendingIntent.getBroadcast(this.getBaseContext(), 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            IntentFilter locIntentFilter = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);
            SingleUpdateReceiver receiver = new SingleUpdateReceiver();
            getApplicationContext().registerReceiver(receiver, locIntentFilter);
            mLocationManager.requestSingleUpdate(criteria, singleUpdatePI);

        }
        class SingleUpdateReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG,"LocationREcieved");
                // ... never invoked
            }
        }

        private void getWeather() {
            if (mLocationManager == null)
                mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            final Location current = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            WeatherTask wt = new WeatherTask();
            wt.execute(String.valueOf(current.getLatitude()), String.valueOf(current.getLongitude()));
       }
        private void startTask() {
        try {
            Log.d(TAG, "Start Weather AsyncTask");


            if (mLocationManager==null)
                mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            requestLocationUpdate();

            if (mLocation == null)
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);


        }
        catch(Exception e) {
            Log.e(TAG,e.getMessage());
        }


    }


        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

        class WeatherTask extends AsyncTask<String,Void,Void>{
    private static final String base_weather_url =Consts.WEATHER_URL;

            private URL getWeatherURL(final String[] params) throws MalformedURLException {
                String url_string = base_weather_url+ String.valueOf(params[0]) + "," + String.valueOf(params[1])+".json";
                return new URL(url_string);

            }

    String convertStreamToString(InputStream is){
        Scanner s=new Scanner(is,"UTF-8").useDelimiter("\\A");
        return s.hasNext()?s.next():"";
    }




    @Override
    protected Void doInBackground(String... params) {
        // first parameter should be latitude
        // second param should be longitude
        String response = "";



        URL url = null;
        try {
            url = getWeatherURL(params);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            InputStream content = connection.getInputStream();
            response = convertStreamToString(content);
            PutDataMapRequest dataMap = PutDataMapRequest.create("/myapp/myevent");
            dataMap.getDataMap().putString("weather", response);



            DataMap dm=dataMap.getDataMap();
            dm.putString("weather", response);

            dataMap.getDataMap().putString("weather", response);

            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, Consts.PATH_WEATHER_INFO, dm.toByteArray())
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.w(TAG, "GotMESSAGE:" + sendMessageResult.getStatus());
                        }
                    });



        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

}
