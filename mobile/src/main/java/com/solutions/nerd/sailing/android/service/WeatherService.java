package com.solutions.nerd.sailing.android.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WeatherService extends WearableListenerService
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WeatherService";
    private static GoogleApiClient mGoogleApiClient;
    private static LocationManager mLocationManager;
    private static Location mLocation;
    private String mPeerId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        if (mLocationManager == null)
            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        mGoogleApiClient = new GoogleApiClient.Builder(WeatherService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
        Log.d(TAG, "created API Client");


        if (intent != null) {
            if (WeatherWatchFaceConfigActivity.class.getSimpleName().equals(intent.getAction())) {
                mPeerId = intent.getStringExtra("PeerId");
                getWeather();

            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        mPeerId = messageEvent.getSourceNodeId();
        Log.d(TAG, "MessageReceived: path:" + messageEvent.getPath() + " peerId: " + mPeerId);

        if (messageEvent.getPath().equals(Consts.PATH_SERVICE_REQUIRE)) {
            startTask();
            getWeather();
        }
    }

    @Override
    public void onConnectedNodes(List<Node> connectedNodes) {
        super.onConnectedNodes(connectedNodes);
        Log.d(TAG,"onConnectedNodes:"+connectedNodes);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(WeatherService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        if (mLocationManager == null)
            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        getWeather();

        Criteria criteria=new Criteria();
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(criteria,true),2000,2000,mLocationListener);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
        mLocationManager.removeUpdates(mLocationListener);
    }


    final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG,"Location Changed");


            mLocation = location;
            String lat=String.valueOf(mLocation.getLatitude());
            String lon=String.valueOf(mLocation.getLongitude());


//            WeatherTask wt = new WeatherTask();
 //           wt.execute(lat, lon);
            DataMap dm = new DataMap();

            dm.putDouble("latitude", mLocation.getLatitude());
            dm.putDouble("longitude", mLocation.getLongitude());

            dm.putFloat("speed",mLocation.getSpeed());
            dm.putFloat("bearing", mLocation.getBearing());

            Wearable.MessageApi.sendMessage(mGoogleApiClient, "", Consts.PATH_LOCATION_INFO, dm.toByteArray())
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.w(TAG, "Successfully sent location to wearable:" + sendMessageResult.getStatus());
                        }
                    });


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String msg = String.format("onStatusChanged provider:%s , status:%s",provider,status);

            if(Log.isLoggable(TAG,Log.DEBUG))
                Log.d(TAG, msg );
        }


        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled");
        }
    };


    private void requestLocationUpdate() {
        mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener,null);
    }


    private void getWeather() {
        try {
            if (mLocationManager == null)
                mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            final Location current = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            WeatherTask wt = new WeatherTask();
            wt.execute(String.valueOf(current.getLatitude()), String.valueOf(current.getLongitude()));
        }
        catch(Exception e){
            Log.e(TAG,"Error during getWeather");
            e.printStackTrace();
        }
    }

    private void startTask() {
        try {

            Log.w(TAG,"startTask");

            if(mLocationManager==null)
                mLocationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

             mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

//            requestLocationUpdate();
            String lat=String.valueOf(mLocation.getLatitude());
            String lon=String.valueOf(mLocation.getLongitude());

            WeatherTask wt = new WeatherTask();
            wt.execute(lat, lon);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnection");
    }

    class WeatherTask extends AsyncTask<String, Void, DataMap> {
        private static final String base_weather_url = Consts.WEATHER_URL;

        private URL getWeatherURL(final String[] params) throws MalformedURLException {
            String url_string = base_weather_url + String.valueOf(params[0]) + "," + String.valueOf(params[1]) + ".json";
            return new URL(url_string);

        }

        String convertStreamToString(InputStream is) {
            Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }


        @Override
        protected DataMap doInBackground(String... params) {
            // first parameter should be latitude
            // second param should be longitude
            String response;
            URL url;

            try {
                url = getWeatherURL(params);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "");
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                InputStream content = connection.getInputStream();
                response = convertStreamToString(content);
                PutDataMapRequest dataMap = PutDataMapRequest.create("/myapp/myevent");
                Log.d(TAG,response);
                dataMap.getDataMap().putString("weather", response);

                Pattern pattern = Pattern.compile("icon_url\": *\"([^\"]+)");

                Matcher matcher = pattern.matcher(response);
                String image = "";

                while (matcher.find()) {
                    image = matcher.group(1);
                }

                DataMap dm = dataMap.getDataMap();
                try {
                    if (!image.isEmpty()) {
                        Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(image).getContent());
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        dm.putByteArray("icon", byteArray);

                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }


//            Log.e(TAG,String.format("url:%s\n%s", url, response));
                dm.putString("weather", response);


                dataMap.getDataMap().putString("weather", response);

                return dm;




            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch(Exception e){
                Log.e(TAG,"General Exception");
                e.printStackTrace();
            }

            return null;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param dm The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(DataMap dm) {

            super.onPostExecute(dm);
            if (dm!=null)
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, Consts.PATH_WEATHER_INFO, dm.toByteArray())
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.w(TAG, "Successfully sent weather to wearable:" + sendMessageResult.getStatus());
                        }
                    });
        }
    }

}
