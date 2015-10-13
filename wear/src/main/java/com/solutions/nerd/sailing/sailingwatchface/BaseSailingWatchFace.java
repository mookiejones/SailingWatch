package com.solutions.nerd.sailing.sailingwatchface;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.solutions.nerd.sailing.sailingwatchface.model.WeatherClass;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.TimeZone;


public abstract class BaseSailingWatchFace extends CanvasWatchFaceService {

    private final int watchType = getWatchType();
    private static WeatherClass mWeather;

    private static final int MSG_UPDATE_TIME = 0;

    abstract int getWatchType();


    public abstract class BaseWatchEngine extends CanvasWatchFaceService.Engine
            implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            DataApi.DataListener,
            MessageApi.MessageListener,
            NodeApi.NodeListener {

        static final String TAG = "BaseWatchEngine";
        private static final float MIN_DISTANCE_TO_ANIMATE = 1;

        BaseWatchEngine() {
            mAnimator = new ValueAnimator();
            mGoogleApiClient=new GoogleApiClient.Builder(BaseSailingWatchFace.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API)
                    .build();
        }

        /**
         * Called as the user performs touch-screen interaction with the
         * window that is currently showing this wallpaper.  Note that the
         * events you receive here are driven by the actual application the
         * user is interacting with, so if it is slow you will get fewer
         * move events.
         *
         * @param event
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            Toast.makeText(BaseSailingWatchFace.this,"WatchFaceTouched",Toast.LENGTH_LONG);

        }


        // handler to update the time once a second in interactive mode
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case Consts.MSG_GET_WEATHER:
                        LogInfo("handleMessage");
                        break;
                    case Consts.MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = Consts.INTERACTIVE_UPDATE_RATE_MS  - (timeMs % Consts.INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(Consts.MSG_UPDATE_TIME, delayMs);
                            requireWeatherInfo();
                        }
                        break;
                }
            }
        };


        // Background paint
        Paint mBackgroundPaint = new Paint();
        float mXOffset;
        float mYOffset;

        final ValueAnimator mAnimator;
        String mTemperature="";

        Path mCircle;

        // time Paint
        final Paint mCompassPaint=new Paint();


        boolean mAmbient;
        private boolean mMute;


        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        String digitalText;

        Paint mLocationPaint;
        Paint mWeatherPaint;
        Paint mTimePaint;
        Paint mBearingPaint;

        // Time Items
        float mHours, mMinutes, mSeconds;


        String mDateText;
        float mBearing =170;
        float mSpeed=0;
        double mLatitude,mLongitude;


        int mWidth;
        int mHeight;


        // This is where ill get the time from
        Calendar mCalendar;
        // receiver to update the time zone
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogInfo("onReceive");
                mCalendar.setTimeZone(TimeZone.getDefault());
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                invalidate();
            }
        };

        private final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

        private GoogleApiClient mGoogleApiClient = null;
        private float mMilliseconds;
        private Bitmap mBackgroundScaledBitmap;
        private Bitmap mBackgroundBitmap;
        private boolean mRegisteredTimeZoneReceiver = false;


        /**
         * Animates the view to the specified heading, or simply redraws it immediately if the
         * difference between the current heading and new heading are small enough that it wouldn't be
         * noticeable.
         *
         * @param end the desired heading
         */
        private void animateTo(float end) {
            // Only act if the animator is not currently running. If the user's orientation changes
            // while the animator is running, we wait until the end of the animation to update the
            // display again, to prevent jerkiness.
            if (!mAnimator.isRunning()) {
                float start = mBearing;
                float distance = Math.abs(end - start);
                float reverseDistance = 360.0f - distance;
                float shortest = Math.min(distance, reverseDistance);

                if (Float.isNaN(mBearing) || shortest < MIN_DISTANCE_TO_ANIMATE) {
                    // If the distance to the destination angle is small enough (or if this is the
                    // first time the compass is being displayed), it will be more fluid to just redraw
                    // immediately instead of doing an animation.
                    mBearing = end;
                    invalidate();
                } else {
                    // For larger distances (i.e., if the compass "jumps" because of sensor calibration
                    // issues), we animate the effect to provide a more fluid user experience. The
                    // calculation below finds the shortest distance between the two angles, which may
                    // involve crossing 0/360 degrees.
                    float goal;

                    if (distance < reverseDistance) {
                        goal = end;
                    } else if (end < start) {
                        goal = end + 360.0f;
                    } else {
                        goal = end - 360.0f;
                    }

                    mAnimator.setFloatValues(start, goal);
                    mAnimator.start();
                }
            }
        }

        private void setupUI() {
            Resources resources = BaseSailingWatchFace.this.getResources();
            mBackgroundPaint = new Paint();
            // Create background paint
            switch (watchType) {
                case Consts.DIGITAL:
                    mBackgroundPaint.setColor(resources.getColor(R.color.digital_background));
                    break;
                case Consts.ANALOG:
                    break;
            }

            Drawable backgroundDrawable = resources.getDrawable(R.drawable.bg, null);
            //noinspection ConstantConditions
            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();

            mRequireInterval = resources.getInteger(R.integer.weather_default_require_interval);
            mWeatherInfoRequiredTime = System.currentTimeMillis() - (DateUtils.SECOND_IN_MILLIS * 58);
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);




            createPaints();


            // Hook to touch event
            this.setTouchEventsEnabled(true);

        }


        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        private void createPaints(){
            Resources resources=getResources();
            mTimePaint=createTextPaint(R.color.digital_text);

            mBearingPaint=createTextPaint(Color.GREEN);

            mTimePaint.setColor(resources.getColor(R.color.digital_text));
            mTimePaint.setAntiAlias(true);
            mTimePaint.setTextSize(resources.getDimension(R.dimen.digital_text_size_round));
            mTimePaint.setTypeface(Typeface.DEFAULT);

            mCompassPaint.setTextSize(10);

            mBearingPaint.setAntiAlias(true);
            mBearingPaint.setTextSize(10);
            mBearingPaint.setTypeface(Typeface.DEFAULT);

            mLocationPaint = createTextPaint(resources.getColor(R.color.digital_text));

            mWeatherPaint = createTextPaint(resources.getColor(R.color.digital_text));

        }

        protected Resources mResources;
        protected AssetManager mAsserts;
        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            setTouchEventsEnabled(true);

            setWatchFaceStyle(new WatchFaceStyle.Builder(BaseSailingWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_HIDDEN)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());


            mResources = BaseSailingWatchFace.this.getResources();
            mAsserts=BaseSailingWatchFace.this.getAssets();

            setupUI();

            // allocate a Calendar to calculate local time using the UTC time and time zone
            mCalendar = Calendar.getInstance();

            mRequireInterval = mResources.getInteger(R.integer.weather_default_require_interval);
            mWeatherInfoRequiredTime = System.currentTimeMillis() - (DateUtils.SECOND_IN_MILLIS * 58);
            mGoogleApiClient.connect();
        }


        @Override
        public void onPeerConnected(Node node) {
            LogInfo("onPeerConnected name:"+node.getDisplayName()+" id:"+node.getId());
            requireWeatherInfo();
        }

        @Override
        public void onPeerDisconnected(Node node) {
            LogInfo("onPeerDisconnected name:"+node.getDisplayName()+" id:"+node.getId());
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            LogDebug("onVisibilityChanged: " + visible);

            if (visible) {
                mGoogleApiClient.connect();
                registerReceiver();


                mBearing = mBearing==90?180:90;
                float b = ConverterUtil.mod(mBearing, 360.0f);
                animateTo(b);
                updateTime();
            } else {
                if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
                    Wearable.NodeApi.removeListener(mGoogleApiClient,this);
//                    Wearable.MessageApi.removeListener(mGoogleApiClient,this);
                    Wearable.DataApi.removeListener(mGoogleApiClient,this);
                    mGoogleApiClient.disconnect();

                }
                unregisterReceiver();
                //              mLoadWeatherHandler.removeMessages(MSG_GET_WEATHER);
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
            requireWeatherInfo();


        }



        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            LogInfo("onSurfaceChanged");
            if (mBackgroundScaledBitmap == null || mBackgroundScaledBitmap.getWidth() != width || mBackgroundScaledBitmap.getHeight() != height) {
                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, width, height, true /* filter */);
            }
            super.onSurfaceChanged(holder, format, width, height);
        }

        private void registerReceiver() {

            mGoogleApiClient.connect();
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;



            Wearable.DataApi.addListener(mGoogleApiClient, this);
            Wearable.NodeApi.addListener(mGoogleApiClient, this);


            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);
            BaseSailingWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }


        private void requireWeatherInfo() {
            if (!mGoogleApiClient.isConnected())
                return;

            long timeMs = System.currentTimeMillis();

            // The weather info is still up to date.
            if ((timeMs - mWeatherInfoReceivedTime) <= mRequireInterval)
                return;

            // Try once in a min.
            if ((timeMs - mWeatherInfoRequiredTime) <= DateUtils.MINUTE_IN_MILLIS)
                return;

            mWeatherInfoRequiredTime = timeMs;
            Wearable.MessageApi.sendMessage(mGoogleApiClient, "", Consts.PATH_WEATHER_REQUIRE, null)
                    .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
//                            LogInfo("SendRequireMessage:" + sendMessageResult.getStatus());
                        }
                    });
        }


        long mWeatherInfoRequiredTime;
        long mWeatherInfoReceivedTime;
        int mRequireInterval;

        private class MessageResults implements ResultCallback<MessageApi.SendMessageResult> {

            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                Log.d(TAG, "Send Require Message for sendMessage:" + sendMessageResult.getStatus());
            }
        }
        private  final MessageResults MyMessageResults=new MessageResults();
        private void sendMessage(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes= Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for(Node node:nodes.getNodes()){


                        if(node.isNearby())
                            Wearable.MessageApi.sendMessage(mGoogleApiClient,node.getId(),Consts.PATH_WEATHER_REQUIRE,null)
                                    .setResultCallback(MyMessageResults);
                    }
               }

            }).start();
        }

        @Override
        public void onConnected(Bundle bundle) {
            LogDebug("onConnected");

            sendMessage();

            getConfig();
            Wearable.NodeApi.addListener(mGoogleApiClient, this);
            Wearable.DataApi.addListener(mGoogleApiClient,this);
            Wearable.MessageApi.addListener(mGoogleApiClient,this);

            requireWeatherInfo();
            mGoogleApiClient.connect();
        }


        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            LogInfo("Location Failed");
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(TAG, "onConnectionSuspended");
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            for(DataEvent event:dataEvents){
                DataMap dataMap = DataMap.fromByteArray(event.getDataItem().getData());
                String eventUri = event.getDataItem().getUri().toString();

                if (eventUri.contains("/LocationInfo")){
                    Log.d(TAG,"Got Location Info"+dataMap);
                    if (dataMap.containsKey("speed"))
                        mSpeed=dataMap.getFloat("speed");

                    if (dataMap.containsKey("bearing"))
                        mBearing=dataMap.getFloat("bearing");

                    if (dataMap.containsKey("latitude"))
                        mLatitude=dataMap.getDouble("latitude");
                    if (dataMap.containsKey("longitude"))
                        mLongitude=dataMap.getDouble("longitude");
                }
                if (eventUri.contains ("/WeatherInfo")) {
                    DataMapItem dataItem = DataMapItem.fromDataItem (event.getDataItem());
                    String[] data = dataItem.getDataMap().getStringArray("contents");
                    Log.d(TAG,"Getting Weather Info data:"+data);


                }
            }
        }

        private void unregisterReceiver() {
            mGoogleApiClient.disconnect();

            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.NodeApi.removeListener(mGoogleApiClient, this);

            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            BaseSailingWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        void LogDebug(String msg) {
            if (!BuildConfig.DEBUG)
                return;
            if (Log.isLoggable(BaseWatchEngine.TAG, Log.DEBUG))
                Log.d(BaseWatchEngine.TAG, msg);
        }

        void LogInfo(String msg) {
            if (Log.isLoggable(BaseWatchEngine.TAG, Log.INFO))
                Log.i(BaseWatchEngine.TAG, msg);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            mLocationPaint.setTypeface(burnInProtection ? NORMAL_TYPEFACE : Consts.BOLD_TYPEFACE);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            LogDebug("onPropertiesChanged: burn-in protection = " + burnInProtection
                    + ", low-bit ambient = " + mLowBitAmbient);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            updateTime();
            requireWeatherInfo();
            getConfig();

            // Compute rotations and lengths for the clock hands.
            invalidate();

        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            /* the wearable switched between modes */

            LogInfo("onAmbientModeChanged: " + inAmbientMode);


            if (mAmbient) {
                boolean antiAlias = !inAmbientMode;
                mTimePaint.setAntiAlias(antiAlias);
            }
            invalidate();

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);
            if (mMute != inMuteMode) {
                mMute = inMuteMode;
                mTimePaint.setAlpha(inMuteMode ? 100 : 255);
                invalidate();
            }
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            LogDebug("onApplyWindowInsets");
            // Load resources that have alternate values for round watches.
            Resources resources = BaseSailingWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);


            // Does watch have a chin???

            //   mTimePaint.setTextSize(textSize);
        }


        private void updateTime() {

            mCalendar.setTimeZone(TimeZone.getDefault());
            mCalendar.setTimeInMillis(System.currentTimeMillis());

            mHours = mCalendar.get(Calendar.HOUR_OF_DAY);
            mSeconds = mCalendar.get(Calendar.SECOND);
            mMinutes = mCalendar.get(Calendar.MINUTE);
            mMilliseconds = mCalendar.get(Calendar.MILLISECOND);
            mSeconds += mMilliseconds / 1000f;
            mMinutes += mSeconds / 60f;


            float dom = mCalendar.get(Calendar.DAY_OF_MONTH);
            float month = mCalendar.get(Calendar.MONTH);
            float year = mCalendar.get(Calendar.YEAR);

            mDateText = String.format(Consts.DATE_FULL, (int) month, (int) dom, (int) year);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            mWidth = bounds.width();
            mHeight = bounds.height();


            float radius = mWidth / 2;
            boolean hasPeekCard = getPeekCardPosition().top != 0;

            float yOffset = hasPeekCard ? mHeight * 0.05f : 0;

            mCircle = new Path();

            mCircle.addCircle(10, 10, radius, Path.Direction.CW);

            canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);

            updateTime();

            digitalText = mAmbient
                    ? String.format(Consts.time_ambient, (long) mHours, (long) mMinutes)
                    : String.format(Consts.TIME_FULL, (long) mHours, (long) mMinutes, (long) mSeconds);
            digitalText = String.format(Consts.TIME_FULL, (long) mHours, (long) mMinutes, (long) mSeconds);


            float textWidth = mTimePaint.measureText(digitalText);
            float dateWidth = mLocationPaint.measureText(mDateText);

            if (watchType == Consts.DIGITAL) {
                float textZ = bounds.exactCenterY() - 60;
                textZ = bounds.height() - 80;

                canvas.drawText(digitalText, (mWidth - textWidth) / 2, textZ-120, mTimePaint);
                canvas.drawText(mDateText, (mWidth - dateWidth) / 2, textZ - 100, mLocationPaint);
            }

            drawWeatherInfo(canvas,bounds);

            drawCompass(canvas, bounds);

//            drawBearing(canvas, bounds);
        }

        private void drawWeatherInfo(Canvas canvas,Rect bounds){
            if (mWeather==null)
                return;

            String temp=mWeather.getWeatherString();

            drawText(temp, bounds, canvas, 100);

            String wind = mWeather.windString();
            drawText(wind, bounds, canvas, 80);

            String city_state = mWeather.getCity();


            if (mWeather.getIcon()!=null){
                String icon="got icon";
                drawText(icon,bounds,canvas,15);
            }



            drawText(city_state, bounds, canvas, 0);


//            String time=mWeather.getTime();
//            drawText(time, bounds, canvas,60);


            drawSpeed(bounds, canvas);

        }
        private void drawSpeed(Rect bounds,Canvas canvas){
            float left=bounds.width()/4;
            String bearing = String.format("Bearing: %.2f",mBearing);
            drawLeft(bearing,bounds,canvas,0);
            String speed = String.format("Speed: %.1f",ConverterUtil.convertKMToKnots(mSpeed));
            drawLeft(speed,bounds,canvas,20);
            String lat=String.format("latitude: %.2f",mLatitude);
            drawLeft(lat,bounds,canvas,40);
            String lon=String.format("longitude: %.2f",mLongitude);
            drawLeft(lon,bounds,canvas,60);


        }

        private void drawLeft(String text,Rect bounds,Canvas canvas,float height){
            float width=mWeatherPaint.measureText(text);
            float x=(mWidth-width)/4;
            float y=bounds.exactCenterY()+height;
            canvas.drawText(text,x,y,mWeatherPaint);

        }
        private void drawText(String text,Rect bounds,Canvas canvas,float offset){
            float width=mWeatherPaint.measureText(text);
            float x=(mWidth-width)/2;
            float y=bounds.exactCenterY()+offset;
            canvas.drawText(text, x, y, mWeatherPaint);

        }

        private void drawBearing(Canvas canvas,Rect bounds){
            final float cx = bounds.exactCenterX();
            final float cy = bounds.exactCenterY();




            canvas.save();
            final String mBearingText=String.valueOf(mBearing);

            int x=70;
            int y=90;
            Path triangle = new Path();
            triangle.moveTo(x, x);
            triangle.lineTo(y, x);
            triangle.lineTo(y, y);
            triangle.lineTo(x, x);
            mBearingPaint.setColor(Color.RED);
            canvas.drawPath(triangle,mBearingPaint);

            canvas.rotate(mBearing, cx, cy);




            Path bearingPath = new Path();
            float bearingPos= mBearingPaint.measureText(mBearingText);
            bearingPath.moveTo(cx, cy);
            canvas.drawText(mBearingText, cx - (bearingPos / 2), 12, mBearingPaint);

            canvas.restore();
        }

        private void drawCompass(Canvas canvas,Rect bounds){
            final float cx = bounds.exactCenterX();
            final float cy = bounds.exactCenterY();
            final float radius = bounds.width()/2;
            mCircle = new Path();
            mCircle.addCircle(bounds.centerX(), bounds.centerY(), radius, Path.Direction.CW);

            final float rotationAnglePerTick = 360f / Consts.Directions.length;
//            canvas.translate(-mAnimatedHeading * pixelsPerDegree + centerX, centerY);
            int i = 0;
            int dirLength = Consts.Directions.length;
            // Draw the ring (using the stroke, offset by half of the stroke width
            // since the stroke is drawn around the circle (not inside or outside)
            for (i = 0; i < dirLength; i++) {
                String dir = Consts.Directions[i];
                canvas.save();
                canvas.rotate(rotationAnglePerTick * i, cx, cy);

                Path path = new Path();
                float textPos = mCompassPaint.measureText(dir);
                float textX = cx - (textPos / 2);

                float textPadding = dir.equals("S") ? 40 : 12;

                path.moveTo(cx, cy);
                mCompassPaint.setColor(Color.WHITE);
                canvas.drawText(dir, textX, textPadding, mCompassPaint);


                canvas.restore();


            }

        }

        void fetchConfig(DataMap config) {
            DataInfo info =new DataInfo(config);
            if (!BuildConfig.DEBUG)
                LogInfo("fetchConfig: " + config);
            if (config.containsKey("weather")){
                String weatherString = config.getString("weather");
                WeatherTask task = new WeatherTask();
                if (config.containsKey("icon")&&mWeather!=null)
                {
                    mWeather.setIcon(config.getByteArray("icon"));

                }
                task.execute(weatherString);

            }
        }

        void getConfig() {
            Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
                @Override
                public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                    Uri uri = new Uri.Builder()
                            .scheme("wear")
                            .path(Consts.PATH_CONFIG + "BaseSailingWatchFace")
                            .authority(getLocalNodeResult.getNode().getId())
                            .build();

                    getConfig(uri);

                    uri = new Uri.Builder()
                            .scheme("wear")
                            .path(Consts.PATH_WEATHER_INFO)
                            .authority(getLocalNodeResult.getNode().getId())
                            .build();

                    getConfig(uri);
                }
            });
        }

        void getConfig(Uri uri) {
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri)
                    .setResultCallback(
                            new ResultCallback<DataApi.DataItemResult>() {
                                @Override
                                public void onResult(DataApi.DataItemResult dataItemResult) {
                                    boolean success = dataItemResult.getStatus().isSuccess();
                                    DataItem item = dataItemResult.getDataItem();
                                    if (success && item != null)
                                        fetchConfig(DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap());
                                }
                            }
                    );
        }

        private void updateTimer() {
            LogDebug("updateTimer");

            mUpdateTimeHandler.removeMessages(Consts.MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(Consts.MSG_UPDATE_TIME);
            }
        }

        private void updateConfigDataItemAndUiOnStartup(){
            DigitalWatchFaceUtil.fetchConfigDataMap(mGoogleApiClient,
            new DigitalWatchFaceUtil.FetchConfigDataMapCallback(){
                @Override
            public void onConfigDataMapFetched(DataMap startupConfig){
                    // If the DataItem hasn't been created yet or some keys are missing,
                    // use the default values.
                    setDefaultValuesForMissingConfigKeys(startupConfig);
                    DigitalWatchFaceUtil.putConfigDataItem(mGoogleApiClient, startupConfig);

                    updateUiForConfigDataMap(startupConfig);

                }
            });
        }
        private void setDefaultValuesForMissingConfigKeys(DataMap config) {
//            addIntKeyIfMissing(config, DigitalWatchFaceUtil.KEY_BACKGROUND_COLOR,
//                    DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND);

        }
        private void updateUiForConfigDataMap(final DataMap config) {
            boolean uiUpdated = false;
            for (String configKey : config.keySet()) {
                if (!config.containsKey(configKey)) {
                    continue;
                }
                int color = config.getInt(configKey);
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Found watch face config key: " + configKey + " -> "
                            + Integer.toHexString(color));
                }
         //       if (updateUiForKey(configKey, color)) {
         //           uiUpdated = true;
         //       }
            }
            if (uiUpdated) {
                invalidate();
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }



        private Collection<String> getNodes() {
            HashSet<String> results = new HashSet<String>();
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {
                results.add(node.getId());
            }
            return results;
        }

        private class WeatherTask extends AsyncTask<String,Void,WeatherClass>{

            @Override
            protected WeatherClass doInBackground(String... params) {
                Gson gson = new Gson();
                WeatherClass result=null;
                String weatherString = params[0];
                try {
                    result = gson.fromJson(weatherString, WeatherClass.class);
                }catch(Exception e){
                    Log.e(TAG,e.getMessage());
                }

                return result;
            }

            @Override
            protected void onPostExecute(WeatherClass weatherClass) {
//                super.onPostExecute(weatherClass);
                mWeather=weatherClass;
            }
        }
    }

}
