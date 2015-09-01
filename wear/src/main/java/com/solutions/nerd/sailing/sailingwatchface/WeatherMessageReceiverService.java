package com.solutions.nerd.sailing.sailingwatchface;



        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.net.Uri;
        import android.os.Bundle;
        import android.util.Log;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.common.api.Result;
        import com.google.android.gms.common.api.ResultCallback;
        import com.google.android.gms.common.api.Status;

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



    private static double latitude;
    private static double longitude;
    private static double bearing;
    private static float altitude;
    private double speed;
    private long time;

    private static String condition;
    private static String temperature;
    private static long sunrise;
    private static long sunset;
    private static int temperature_scale;
    private static int theme = 3;
    private static int time_unit;
    private static int interval;
    private static boolean alreadyInitialize;
    private static String path;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!hasGPS()){
            Log.d(TAG,"This hardware doesn't have GPS");
        }else{
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)  // used for data layer API
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }

    private boolean hasGPS(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);

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


        if (path.equals(Consts.PATH_WEATHER_INFO)) {

            if (dataMap.containsKey(Consts.KEY_WEATHER_CONDITION)) {
                condition = dataMap.getString(Consts.KEY_WEATHER_CONDITION);
            }

            if (dataMap.containsKey(Consts.KEY_WEATHER_TEMPERATURE)) {
                String temp = dataMap.getString(Consts.KEY_WEATHER_TEMPERATURE);
                Log.e(TAG,"Temperature is "+temp);
                temperature = dataMap.getString(Consts.KEY_WEATHER_TEMPERATURE);
            }

            if (dataMap.containsKey(Consts.KEY_WEATHER_SUNRISE)) {
                sunrise = dataMap.getLong(Consts.KEY_WEATHER_SUNRISE);
            }

            if (dataMap.containsKey(Consts.KEY_WEATHER_SUNSET)) {
                sunset = dataMap.getLong(Consts.KEY_WEATHER_SUNSET);
            }

            config.putLong(Consts.KEY_WEATHER_UPDATE_TIME, System.currentTimeMillis());
            config.putString(Consts.KEY_WEATHER_CONDITION, condition);
            config.putString(Consts.KEY_WEATHER_TEMPERATURE, temperature);
            config.putLong(Consts.KEY_WEATHER_SUNRISE, sunrise);
            config.putLong(Consts.KEY_WEATHER_SUNSET, sunset);
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
                        Log.d(TAG, "SaveConfig: " + dataItemResult.getStatus() + ", " + dataItemResult.getDataItem().getUri());

                        mGoogleApiClient.disconnect();
                    }
                });
    }

    protected void fetchConfig(DataMap config) {
        if (config.containsKey(Consts.KEY_WEATHER_CONDITION)) {
            condition = config.getString(Consts.KEY_WEATHER_CONDITION);
        }

        if (config.containsKey(Consts.KEY_WEATHER_TEMPERATURE)) {
            temperature = config.getString(Consts.KEY_WEATHER_TEMPERATURE);
        }

        if (config.containsKey(Consts.KEY_WEATHER_SUNRISE)) {
            sunrise = config.getLong(Consts.KEY_WEATHER_SUNRISE);
        }

        if (config.containsKey(Consts.KEY_WEATHER_SUNSET)) {
            sunset = config.getLong(Consts.KEY_WEATHER_SUNSET);
        }

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

    }
}

