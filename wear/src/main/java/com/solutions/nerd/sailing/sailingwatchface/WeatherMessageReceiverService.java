package com.solutions.nerd.sailing.sailingwatchface;



        import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class WeatherMessageReceiverService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = WeatherMessageReceiverService.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private static final String WEATHER_KEY="weather";
    private static final String BEARING_KEY="bearing";
    private static final String LATITUDE_KEY="latitude";
    private static final String LONGITUDE_KEY="longitude";
    private static final String SPEED_KEY="speed";

    private static int temperature_scale;
    private static int theme = 3;
    private static int time_unit;
    private static int interval;
    private static boolean alreadyInitialize;
    private static String path;

    @Override
    public void onCreate() {
        super.onCreate();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)  // used for data layer API
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        mGoogleApiClient.connect();

    }

    private boolean hasGPS(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .build();
        }

        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();

        DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());

        path = messageEvent.getPath();


        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        DataMap config = putDataMapRequest.getDataMap();

        DataInfo info = new DataInfo(config);

        if(path.equals(Consts.PATH_LOCATION_INFO)){
            if(dataMap.containsKey(LATITUDE_KEY))
                config.putDouble(LATITUDE_KEY,dataMap.getDouble(LATITUDE_KEY));
            if(dataMap.containsKey(LONGITUDE_KEY))
                config.putDouble(LONGITUDE_KEY,dataMap.getDouble(LONGITUDE_KEY));
            if(dataMap.containsKey(SPEED_KEY))
                config.putFloat(SPEED_KEY,dataMap.getFloat(SPEED_KEY));
            if(dataMap.containsKey(BEARING_KEY))
                config.putFloat(BEARING_KEY,dataMap.getFloat(BEARING_KEY));
        }


        if (path.equals(Consts.PATH_WEATHER_INFO)) {
            if (dataMap.containsKey(WEATHER_KEY)){
                String weather = dataMap.getString(WEATHER_KEY);
                config.putString(WEATHER_KEY, weather);
            }

            config.putLong(Consts.KEY_WEATHER_UPDATE_TIME, System.currentTimeMillis());
        } else {
            if (!alreadyInitialize) {
                Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
                    @Override
                    public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                        Uri uri = new Uri.Builder()
                                .scheme(Consts.WEAR)
                                .path(path)
                                .authority(getLocalNodeResult.getNode().getId())
                                .build();

                        Wearable.DataApi.getDataItem(mGoogleApiClient, uri)
                                .setResultCallback(
                                        new ResultCallback<DataApi.DataItemResult>() {
                                            @Override
                                            public void onResult(DataApi.DataItemResult dataItemResult) {
                                                if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
                                                    fetchConfig(DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap());
                                                }

                                                alreadyInitialize = true;
                                            }
                                        }
                                );
                    }
                });

                while (!alreadyInitialize) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (dataMap.containsKey(Consts.KEY_CONFIG_TEMPERATURE_SCALE)) {
                temperature_scale = dataMap.getInt(Consts.KEY_CONFIG_TEMPERATURE_SCALE);
            }

            if (dataMap.containsKey(Consts.KEY_CONFIG_THEME)) {
                theme = dataMap.getInt(Consts.KEY_CONFIG_THEME);
            }

            if (dataMap.containsKey(Consts.KEY_CONFIG_TIME_UNIT)) {
                time_unit = dataMap.getInt(Consts.KEY_CONFIG_TIME_UNIT);
            }

            if (dataMap.containsKey(Consts.KEY_CONFIG_REQUIRE_INTERVAL)) {
                interval = dataMap.getInt(Consts.KEY_CONFIG_REQUIRE_INTERVAL);
            }


            config.putInt(Consts.KEY_CONFIG_TEMPERATURE_SCALE, temperature_scale);
            config.putInt(Consts.KEY_CONFIG_THEME, theme);
            config.putInt(Consts.KEY_CONFIG_TIME_UNIT, time_unit);
            config.putInt(Consts.KEY_CONFIG_REQUIRE_INTERVAL, interval);
        }

        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        mGoogleApiClient.disconnect();
                    }
                });
    }

    private void fetchConfig(DataMap config) {


        if (config.containsKey(Consts.KEY_CONFIG_TEMPERATURE_SCALE)) {
            temperature_scale = config.getInt(Consts.KEY_CONFIG_TEMPERATURE_SCALE);
        }

        if (config.containsKey(Consts.KEY_CONFIG_THEME)) {
            theme = config.getInt(Consts.KEY_CONFIG_THEME);
        }

        if (config.containsKey(Consts.KEY_CONFIG_TIME_UNIT)) {
            time_unit = config.getInt(Consts.KEY_CONFIG_TIME_UNIT);
        }

        if (config.containsKey(Consts.KEY_CONFIG_REQUIRE_INTERVAL)) {
            interval = config.getInt(Consts.KEY_CONFIG_REQUIRE_INTERVAL);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "connection to location client suspended");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectionFailed:"+connectionResult);
    }
}

