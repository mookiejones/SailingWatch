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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.List;


public class WeatherService extends WearableListenerService
    implements    GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ISailingDataListener
    {
    private static final String TAG                     = "WeatherService";
        private static final String SINGLE_LOCATION_UPDATE_ACTION = "single_update";
        private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private Location mLocation;
    private String          mPeerId;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private static boolean is_listening=false;
    private static WeatherClass.WeatherObject mWeather;



    @Override
    public int onStartCommand( Intent intent, int flags, int startId )
    {
        Log.e(TAG,"onStartCommand");
        if ( intent != null )
        {
            if ( WeatherWatchFaceConfigActivity.class.getSimpleName().equals( intent.getAction() ) )
            {
                mPeerId = intent.getStringExtra("PeerId");
                startTask();
            }
        }

        return super.onStartCommand( intent, flags, startId );
    }


    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onMessageReceived( MessageEvent messageEvent )
    {
        super.onMessageReceived( messageEvent );


        mPeerId = messageEvent.getSourceNodeId();
        Log.d( TAG, "MessageReceived: path:" + messageEvent.getPath()+" peerId: "+mPeerId );

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
        String [] myData = new String[]{"data1", "data2", "data3"};
        new DataTask (this, myData, this).execute();
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
                e.printStackTrace();
            }
            mLocation = location;
            WeatherClass wc = new WeatherClass(new WeatherClass.WeatherListener() {
                @Override
                public void Callback(WeatherClass.WeatherObject weather) {
                    mWeather=weather;
                    DataMap config = mWeather.map();


                }});

            List<String> params = new ArrayList<String>();
            params.add(String.valueOf(location.getLatitude()));
            params.add(String.valueOf(location.getLongitude()));

            String[] p=(String[])params.toArray();


            wc.execute(p);

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

        @Override
        public void unregisterReceiver(BroadcastReceiver receiver) {
            super.unregisterReceiver(receiver);
        }

        @Override
        public boolean stopService(Intent name) {
            return super.stopService(name);
        }

        /**
         * Called when all clients have disconnected from a particular interface
         * published by the service.  The default implementation does nothing and
         * returns false.
         *
         * @param intent The Intent that was used to bind to this service,
         *               as given to {@link Context#bindService
         *               Context.bindService}.  Note that any extras that were included with
         *               the Intent at that point will <em>not</em> be seen here.
         * @return Return true if you would like to have the service's
         * {@link #onRebind} method later called when new clients bind to it.
         */
        @Override
        public boolean onUnbind(Intent intent) {
//            mLocationManager.removeUpdates(mLocationListener);
            return super.onUnbind(intent);

        }

        public void requestLocationUpdate() {
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

        private void getWeather(){
            final Location current =mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            final double lat=current.getLatitude();
            final double lon = current.getLongitude();
            final float bearing = current.getBearing();
            final float speed=current.getSpeed();





            WeatherClass wc = new WeatherClass(new WeatherClass.WeatherListener() {
                @Override
                public void Callback(WeatherClass.WeatherObject weather) {
                    DataMap config = weather.map();
                    PutDataRequest request = PutDataRequest.create("/weather");
                    request.setData(config.toByteArray());

                    config.putDouble(Consts.KEY_LOCATION_LATITUDE, lat);
                    config.putDouble(Consts.KEY_LOCATION_LONGITUDE, lon);



                    config.putFloat(Consts.KEY_LOCATION_BEARING, bearing);
                    config.putFloat(Consts.KEY_LOCATION_SPEED, speed);
                    config.putString(Consts.KEY_WEATHER_TEMPERATURE, weather.getTemperature());

                    Wearable.DataApi.putDataItem(mGoogleApiClient, request);

                    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                            .putDataItem(mGoogleApiClient, request);


                    mWeather=weather;

                }});

            wc.execute(String.valueOf(current.getLatitude()), String.valueOf(current.getLongitude()));


        }
        private void startTask() {
        try {
            Log.d(TAG, "Start Weather AsyncTask");


            if (mLocationManager==null)
                mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            requestLocationUpdate();
            is_listening = true;

            double lat;
            double lon;


            if (mLocation == null)
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (mWeather!=null){
                LogDebug(TAG, "Callback2");

                DataMap config = new DataMap();

                // Positional Information
                config.putFloat(Consts.KEY_LOCATION_BEARING, mLocation.getBearing());
                config.putFloat(Consts.KEY_LOCATION_SPEED, mLocation.getSpeed());

                // Temperature
                if (mWeather !=null)
                    config.putString(Consts.KEY_WEATHER_TEMPERATURE, mWeather.getTemperature(2));
                PutDataRequest request = PutDataRequest.create("/weather");
                request.setData(config.toByteArray());

                Wearable.DataApi.putDataItem(mGoogleApiClient,request);

                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                        .putDataItem(mGoogleApiClient, request);



                Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, Consts.PATH_WEATHER_INFO, config.toByteArray())
                        .setResultCallback(
                                new ResultCallback<MessageApi.SendMessageResult>() {
                                    @Override
                                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                        Log.d(TAG, "SendUpdateMessage: " + sendMessageResult.getStatus());
                                    }
                                }
                        );



            }
            lat = mLocation.getLatitude();
            lon = mLocation.getLongitude();

            WeatherClass wc = new WeatherClass(new WeatherClass.WeatherListener() {
                @Override
                public void Callback(WeatherClass.WeatherObject weather) {
                    final DataMap weatherConfig = new DataMap();
                    weatherConfig.putString("HERE","MOOKIE");
                    mWeather=weather;

                    Wearable.MessageApi.sendMessage(mGoogleApiClient,mPeerId,Consts.PATH_WEATHER_INFO,weatherConfig.toByteArray())
                            .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                    Log.w(TAG,"GotMESSAGE:" + sendMessageResult.getStatus());
                                }
                            });

            }});

            wc.execute(String.valueOf(lat), String.valueOf(lon));
//            Task task = new Task();
//            task.execute(null);

        }
        catch(Exception e) {
            e.printStackTrace();
        }


    }


    private void SendMessage(){

    }


    static void LogDebug(String tag,String msg){
        if (Log.isLoggable(tag,Log.DEBUG))
            Log.d(tag,msg);
    }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

        @Override
        public void onDataReceived(String[] data) {
            Log.d(TAG,"onDataRecieved");
        }

/*

        private class Task extends AsyncTask{




        @Override
        protected Object doInBackground( Object[] params )
        {
            try
            {
                Log.d(TAG, "Task Running");
                //     RoboGuice.getInjector( WeatherService.this.getApplicationContext() ).injectMembers( this );

                if ( !mGoogleApiClient.isConnected() )
                { mGoogleApiClient.connect(); }

                String mlat=String.valueOf(mLocation.getLatitude());
                String mlon=String.valueOf(mLocation.getLongitude());


                DataMap config = new DataMap();
                WeatherClass.WeatherListener listener= new WeatherClass.WeatherListener() {
                    @Override
                    public void Callback(WeatherClass.WeatherObject weather) {
                        Log.e(TAG,String.format("WeatherListener Callback \n\n\n%s",weather));
                    }
                };
                String[] locParams=new String[]{mlat,mlon};

                final WeatherClass wc = new WeatherClass(listener);






                //real
*/
/*
                config.putInt( Consts.KEY_WEATHER_TEMPERATURE, info.getTemperature() );
                config.putString(Consts.KEY_WEATHER_CONDITION, info.getCondition());
                config.putLong(Consts.KEY_WEATHER_SUNSET, info.getSunset());
                config.putLong(Consts.KEY_WEATHER_SUNRISE, info.getSunrise());
*//*




                //test
                //Random random = new Random();
                //config.putInt("Temperature",random.nextInt(100));
                //config.putString("Condition", new String[]{"clear","rain","snow","thunder","cloudy"}[random.nextInt
                // (4)]);
                PutDataRequest request = PutDataRequest.create("/weather");
                request.setData(config.toByteArray());

                Wearable.DataApi.putDataItem(mGoogleApiClient, request);

                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                        .putDataItem(mGoogleApiClient, request);
                Wearable.MessageApi.sendMessage( mGoogleApiClient, mPeerId, Consts.PATH_WEATHER_INFO, config.toByteArray() )
                        .setResultCallback(
                                new ResultCallback<MessageApi.SendMessageResult>()
                                {
                                    @Override
                                    public void onResult( MessageApi.SendMessageResult sendMessageResult )
                                    {
                                        Log.d( TAG, "SendUpdateMessage: " + sendMessageResult.getStatus() );
                                    }
                                }
                        );
            }
            catch ( Exception e )
            {
                Log.e(TAG, "Task Fail: " + e);
            }
            return null;
        }

    }

*/
        class DataTask extends AsyncTask<Node,Void,Void>{
            private final String[] contents;
            private ISailingDataListener listener;
            Context c;

            public DataTask (Context c, String [] contents, ISailingDataListener listener) {
                this.c = c;
                this.contents = contents;
                this.listener = listener;
            }


            /**
             * Override this method to perform a computation on a background thread. The
             * specified parameters are the parameters passed to {@link #execute}
             * by the caller of this task.
             * <p/>
             * This method can call {@link #publishProgress} to publish updates
             * on the UI thread.
             *
             * @param params The parameters of the task.
             * @return A result, defined by the subclass of this task.
             * @see #onPreExecute()
             * @see #onPostExecute
             * @see #publishProgress
             */
            @Override
            protected Void doInBackground(Node... params) {
                PutDataMapRequest dataMap = PutDataMapRequest.create ("/myapp/myevent");
                dataMap.getDataMap().putStringArray("contents", contents);

                PutDataRequest request = dataMap.asPutDataRequest();

                DataApi.DataItemResult dataItemResult = Wearable.DataApi
                        .putDataItem(mGoogleApiClient, request).await();


                Log.d ("[DEBUG] doInBackground", "/myapp/myevent status, "+getStatus());
                listener.onDataReceived(contents);
                return null;

            }
        }

}
