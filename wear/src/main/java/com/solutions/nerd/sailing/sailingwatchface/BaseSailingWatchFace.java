package com.solutions.nerd.sailing.sailingwatchface;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.view.animation.LinearInterpolator;

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

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.TimeZone;


public abstract class BaseSailingWatchFace extends CanvasWatchFaceService {

    private final int watchType = getWatchType();


    abstract int getWatchType();







    public abstract class BaseWatchEngine extends CanvasWatchFaceService.Engine
            implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            DataApi.DataListener,
            NodeApi.NodeListener {

        static final String TAG = "BaseWatchEngine";
        private static final float MIN_DISTANCE_TO_ANIMATE = 1;

        protected BaseWatchEngine() {
            mAnimator = new ValueAnimator();
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
            LogInfo(TAG, "WatchFaceTouched");

        }

        // handler to update the time once a second in interactive mode
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case Consts.MSG_GET_WEATHER:
                        LogInfo(TAG, "handleMessage");
                        break;
                    case Consts.MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = Consts.INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % Consts.INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(Consts.MSG_UPDATE_TIME, delayMs);
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

        Path mCircle;

        // time Paint
        final Paint mTimePaint = new Paint();
        final Paint mBearingPaint =mTimePaint;

        boolean mAmbient;
        private boolean mMute;


        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        String digitalText;

        Paint mLocationPaint;

        // Time Items
        float mHours, mMinutes, mSeconds;

        String mDateText;
        float mBearing =170;


        int mWidth;
        int mHeight;

        // This is where ill get the time from
        Calendar mCalendar;
        // receiver to update the time zone
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
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
        private AsyncTask<Void, Void, Integer> mLoadWeatherTask;

        private void setupAnimator(){
            Log.d(TAG,"Setting up animator");
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.setDuration(250);

            // Notifies us at each frame of the animation so we can redraw the view.
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    mBearing = ConverterUtil.mod((Float) mAnimator.getAnimatedValue(), 360.0f);
                    invalidate();
                }
            });
            // Notifies us when the animation is over. During an animation, the user's head may have
            // continued to move to a different orientation than the original destination angle of the
            // animation. Since we can't easily change the animation goal while it is running, we call
            // animateTo() again, which will either redraw at the new orientation (if the difference is
            // small enough), or start another animation to the new heading. This seems to produce
            // fluid results.
            mAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animator) {
                    animateTo(mBearing);
                }
            });
        }
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
            setupAnimator();
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
            mBearingPaint.setColor(Color.GREEN);
            mTimePaint.setColor(resources.getColor(R.color.digital_text));

            mTimePaint.setAntiAlias(true);
            mBearingPaint.setAntiAlias(true);
            mBearingPaint.setTextSize(10);
            mTimePaint.setTextSize(10);
            mBearingPaint.setTypeface(Typeface.DEFAULT);
            mTimePaint.setTypeface(Typeface.DEFAULT);


            mLocationPaint = createTextPaint(getResources().getColor(R.color.digital_text));

        }

        Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            setWatchFaceStyle(new WatchFaceStyle.Builder(BaseSailingWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setStatusBarGravity(Gravity.RIGHT | Gravity.TOP)
                    .setHotwordIndicatorGravity(Gravity.LEFT | Gravity.TOP)
                            // Just added this to play with
                    .setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_TRANSLUCENT)
//                    .setShowSystemUiTime(false)
                    .setShowSystemUiTime(false)
                    .build());

            setupUI();

            // allocate a Calendar to calculate local time using the UTC time and time zone
            mCalendar = Calendar.getInstance();

            mGoogleApiClient = new GoogleApiClient.Builder(BaseSailingWatchFace.this)
                    .addOnConnectionFailedListener(this)
                    .addConnectionCallbacks(this)
                    .addApi(Wearable.API)
                    .build();

            // configure the system UI (see next section)

            // load the background image
            LogInfo(TAG, "onCreate");


            mGoogleApiClient.connect();
        }


        @Override
        public void onPeerConnected(Node node) {
            LogInfo(TAG, "onPeerConnected");
        }

        @Override
        public void onPeerDisconnected(Node node) {
            LogInfo(TAG, "onPeerDisconnected");
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            LogDebug(TAG, "onVisibilityChanged: " + visible);

            if (visible) {
                mGoogleApiClient.connect();
                registerReceiver();
//                mLoadWeatherHandler.sendEmptyMessage(MSG_GET_WEATHER);

                mBearing = mBearing==90?180:90;
                float b = ConverterUtil.mod(mBearing, 360.0f);
                animateTo(b);
                updateTime();
            } else {
                unregisterReceiver();
                //              mLoadWeatherHandler.removeMessages(MSG_GET_WEATHER);
                cancelLoadWeatherTask();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
            requireWeatherInfo();


        }

        private void cancelLoadWeatherTask() {
            if (mLoadWeatherTask != null)
                mLoadWeatherTask.cancel(true);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            LogInfo(TAG, "onSurfaceChanged");
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
            Wearable.DataApi.addListener(mGoogleApiClient, this);
            Wearable.NodeApi.addListener(mGoogleApiClient, this);

            mRegisteredTimeZoneReceiver = true;

            IntentFilter mWeatherFilter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
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
                            MessageResult(sendMessageResult);
                            LogInfo(TAG, "SendRequireMessage:" + sendMessageResult.getStatus());
                        }
                    });
        }

        private void MessageResult(MessageApi.SendMessageResult sendMessageResult) {

            LogInfo(TAG, "SendRequireMessage:" + sendMessageResult.getStatus());
        }

        protected long mWeatherInfoRequiredTime;
        protected long mWeatherInfoReceivedTime;
        protected int mRequireInterval;

        private void sendMessage(final String path,final String text){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes= Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for(Node node:nodes.getNodes()){
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                mGoogleApiClient, node.getId(), path, text.getBytes() ).await();
                    }

               }

            }).start();
        }

        @Override
        public void onConnected(Bundle bundle) {
            sendMessage(TAG,Consts.PATH_WEATHER);

            getConfig();
            Wearable.NodeApi.addListener(mGoogleApiClient, this);


            requireWeatherInfo();
            mGoogleApiClient.connect();
        }


        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            LogInfo(TAG, "Location Failed");
        }

        @Override
        public void onConnectionSuspended(int i) {
            LogInfo(TAG, "Connection Suspended");
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            for(DataEvent event:dataEvents){
                DataMap dataMap = DataMap.fromByteArray(event.getDataItem().getData());
                String eventUri = event.getDataItem().getUri().toString();
                LogInfo(TAG,String.format("onDataChanged:\n\turi:%s\n\tdata:%s",eventUri,dataMap));


                if (eventUri.contains ("/WeatherWatchface/WeatherInfo")) {
                    DataMapItem dataItem = DataMapItem.fromDataItem (event.getDataItem());
                    String[] data = dataItem.getDataMap().getStringArray("contents");
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

        void LogDebug(String tag, String msg) {
            if (!BuildConfig.DEBUG)
                return;
            if (Log.isLoggable(tag, Log.DEBUG))
                Log.d(tag, msg);
        }

        void LogInfo(String tag, String msg) {
            if (Log.isLoggable(tag, Log.INFO))
                Log.i(tag, msg);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);

            boolean burnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            mLocationPaint.setTypeface(burnInProtection ? NORMAL_TYPEFACE : Consts.BOLD_TYPEFACE);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            LogDebug(TAG, "onPropertiesChanged: burn-in protection = " + burnInProtection
                    + ", low-bit ambient = " + mLowBitAmbient);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            updateTime();

            // Compute rotations and lengths for the clock hands.
            invalidate();
            requireWeatherInfo();

        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            /* the wearable switched between modes */

            LogInfo(TAG, "onAmbientModeChanged: " + inAmbientMode);


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

            LogDebug(TAG, "onApplyWindowInsets");
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
                float textZ = bounds.exactCenterY() + 60;
                textZ = bounds.height() - 80;
                canvas.drawText(mDateText, (mWidth - dateWidth) / 2, textZ + 30, mLocationPaint);
                canvas.drawText(digitalText, (mWidth - textWidth) / 2, textZ, mLocationPaint);
            }

            final float cx = bounds.exactCenterX();
            final float cy = bounds.exactCenterY();

            drawCompass(canvas, bounds);
//            drawBearing(canvas, bounds);
        }

        private void drawBearing(Canvas canvas,Rect bounds){
            final float cx = bounds.exactCenterX();
            final float cy = bounds.exactCenterY();
            int width = bounds.width();
            int height = bounds.height();

            // Find the center. Ignore the window insets so that, on round watches with a
            // "chin", the watch face is centered on the entire screen, not just the usable
            // portion.
            float centerX = width / 2f;
            float centerY = height / 2f;


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
                float textPos = mTimePaint.measureText(dir);
                float textX = cx - (textPos / 2);

                float textPadding = dir.equals("S") ? 40 : 12;

                path.moveTo(cx, cy);
                mTimePaint.setColor(Color.WHITE);
                canvas.drawText(dir, textX, textPadding, mTimePaint);


                canvas.restore();


            }

        }

        protected void fetchConfig(DataMap config) {
            DataInfo info =new DataInfo(config);
            LogInfo(TAG, "fetchConfig: " + config);
        }

        protected void getConfig() {
            LogInfo(TAG, "Start getting Config");

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

        protected void getConfig(Uri uri) {
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri)
                    .setResultCallback(
                            new ResultCallback<DataApi.DataItemResult>() {
                                @Override
                                public void onResult(DataApi.DataItemResult dataItemResult) {
                                    LogInfo(TAG, "Finish Config: " + dataItemResult.getStatus());
                                    boolean success = dataItemResult.getStatus().isSuccess();
                                    DataItem item = dataItemResult.getDataItem();
                                    if (success && item != null)
                                        fetchConfig(DataMapItem.fromDataItem(dataItemResult.getDataItem()).getDataMap());
                                }
                            }
                    );
        }

        private void updateTimer() {
            LogDebug(TAG, "updateTimer");

            mUpdateTimeHandler.removeMessages(Consts.MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(Consts.MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        private void setupVoiceTranscription() {
            CapabilityApi.GetCapabilityResult result =
                    Wearable.CapabilityApi.getCapability(
                            mGoogleApiClient, Consts.VOICE_TRANSCRIPTION_CAPABILITY_NAME,
                            CapabilityApi.FILTER_REACHABLE).await();

            CapabilityApi.CapabilityListener capabilityListener =
                    new CapabilityApi.CapabilityListener() {
                        @Override
                        public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                            updateTranscriptionCapability(capabilityInfo);
                        }
                    };

            Wearable.CapabilityApi.addCapabilityListener(
                    mGoogleApiClient,
                    capabilityListener,
                    Consts.VOICE_TRANSCRIPTION_CAPABILITY_NAME);

            updateTranscriptionCapability(result.getCapability());
        }

        private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
            LogInfo(TAG, "updateTranscriptionCapability");

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
    }

}
