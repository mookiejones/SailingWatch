/**
 * TODO Need to reimplement the polylines
 * Need to remove Notification on closing of application
 */

package com.solutions.nerd.sailing.android.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.solutions.nerd.sailing.android.R;
import com.solutions.nerd.sailing.android.model.Marina;
import com.solutions.nerd.sailing.android.model.TagInfo;
import com.solutions.nerd.sailing.android.util.AccountUtils;
import com.solutions.nerd.sailing.android.util.TransparentUrlTileProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Locale;

import static com.solutions.nerd.sailing.android.util.LogUtils.LOGD;


/**
 * Created by cberman on 12/27/2014.
 */
@SuppressWarnings("ConstantConditions")
public class Map_Fragment extends Fragment
implements
        GoogleMap.OnInfoWindowClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        LocationListener, WeatherClass.WeatherListener {

    // Cluster Manager for Marina Items
    private ClusterManager<Marina> mClusterManager;


    // Get Remove marker button
    private FloatingActionButton removeButton;

    private TextView mBearingText;
    private TextView mWindGustText;
    private TextView mWindDirText;

    private TextView mSpeedText;
    private TextView mTempText;
    private Marker selectedMarker;
    private TileOverlay tileOverlay;
    private GoogleMap mMap;
    private Firebase mFirebaseJourneys;
    private static final String ITEM_NUMBER = "item_number";

    private String provider;
    private LocationManager locationManager = null;
    private final static String TAG = Map_Fragment.class.getSimpleName();
    private static final String tileUrl = "http://earthncseamless.s3.amazonaws.com/{zoom}/{x}/{y}.png";

    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private final static LatLng dearborn = new LatLng(42.3222600f, -83.1763100f);
    private Location currentBestLocation = null;
    private Geocoder geocoder;
    @SuppressWarnings("UnusedDeclaration")
    private LatLng mSelectedLatLng = null;
    private final long mTime = 10000;
    private final int mDistance = 100;
    private boolean listening_enabled = false;

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Called when the view previously created by {@link #onCreateView} has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after {@link #onStop()} and before {@link #onDestroy()}.  It is called
     * <em>regardless</em> of whether {@link #onCreateView} returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @SuppressWarnings("UnusedDeclaration")
    private PolylineOptions polyLineOptions = new PolylineOptions().geodesic(true);
    @SuppressWarnings("UnusedDeclaration")
    private Dictionary<String, TagInfo> mMarkerDict;
    private final List<Marker> mTags = new ArrayList<>();
    private Polyline mRouteLine;
    private final List<Marker> mMarkers = new ArrayList<>();



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext());
        mFloatingMenu= (FloatingActionsMenu)view.findViewById(R.id.floating_menu);
        Firebase.setAndroidContext(this.getActivity());

        mBearingText = (TextView)view.findViewById(R.id.bearing_act);
        mSpeedText = (TextView)view.findViewById(R.id.speed_act);
        mTempText = (TextView)view.findViewById(R.id.temperature);
        mWindGustText = (TextView)view.findViewById(R.id.wind_gust);
        mWindDirText = (TextView)view.findViewById(R.id.wind_direction);
        if (mFirebaseJourneys==null)
        {
            String url = AccountUtils.getUserJourneysURL(this.getActivity());
            mFirebaseJourneys = new Firebase(url);
        }
        if (mMap == null) {
            setupMapIfNeeded();
        }

        setHasOptionsMenu(true);

        int i = getArguments().getInt(ITEM_NUMBER);
        String title = getResources().getStringArray(R.array.titles)[i];
        getActivity().setTitle(title);

//        getMap().setOnInfoWindowClickListener(this);
//        addMarkersFromDatabase();

    }

    private void hideRemoveButton(){}


    private LatLng getCurrentLocation() {

        //noinspection PointlessBooleanExpression
        if (!IS_DOGFOOD_BUILD)
            return dearborn;

        LocationManager locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send to the GSP settings
        // Better Solution would be to display a dialog and suggesting to
        // go to settings

        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } else {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);


            return new LatLng(location.getLatitude(), location.getLongitude());
        }


        return null;
    }

    private void setupMapIfNeeded() {

        if (mMap != null) {
            return;
        }




        geocoder = new Geocoder(this.getActivity(), Locale.getDefault());

        mMap = ((SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map)).getMap();


        // Setup cluster manager
        mClusterManager = new ClusterManager<>(getActivity(),mMap);
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        /**
         * Setup Map Listener
         */


        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                 updateMarkers();
            }
        });



        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                selectedMarker=marker;
                if (selectedMarker!=null){
                    removeButton.setVisibility(View.VISIBLE);
                }
                mSelectedLatLng = selectedMarker.getPosition();
                if (marker.isInfoWindowShown())
                    marker.hideInfoWindow();
                else
                    marker.showInfoWindow();
                return true;
            }
        });
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                updatePath();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                updatePath();
            }
        });


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext());

        int opacity = sp.getInt(PrefUtils.PREF_OVERLAY_ALPHA,75);
        final TransparentUrlTileProvider tileProvider = new TransparentUrlTileProvider(tileUrl,opacity);
         tileOverlay =mMap.addTileOverlay(new TileOverlayOptions()
                .fadeIn(false)
                .visible(true)
                .zIndex(0)
                .tileProvider(tileProvider));



        LatLng current = getCurrentLocation();
        if (current!=null){
            String dataUrl = AccountUtils.getUserDataUrl(this.getActivity());
            Firebase ref = new Firebase(AccountUtils.getUserDataUrl(this.getActivity()));

            Firebase child = ref.child("location");
            child.setValue(current);

        }




        /* Get Current Click Location */
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mSelectedLatLng = latLng;
                updateMarkers();
            }
        });

        // Setup Long Click
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                //TODO Remove this when properly implemented
                Firebase newMarker = mFirebaseJourneys.child("markers").push();
                newMarker.setValue(latLng);


                addMarker(latLng);
            }
        });

        addMarker(current);


    }

    private String getAddress(Location mLocation) {

        // Get the current location from the input parameter list

        List<Address> addresses = null;
        try {
                /*
                 * Return 1 address.
                 */
            addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
        } catch (IOException e1) {
            Log.e("LocationSampleActivity",
                    "IO Exception in getFromLocation()");
            e1.printStackTrace();
            return ("IO Exception trying to get address");
        } catch (IllegalArgumentException e2) {
            // Error message to post in the log
            String errorString = "Illegal arguments " +
                    Double.toString(mLocation.getLatitude()) +
                    " , " +
                    Double.toString(mLocation.getLongitude()) +
                    " passed to address service";
            Log.e("LocationSampleActivity", errorString);
            e2.printStackTrace();
            return errorString;
        }
        // If the reverse geocode returned an address
        if (addresses != null && addresses.size() > 0) {
            // Get the first address
            Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
            // Return the text
            return String.format(
                    "%s, %s, %s",
                    // If there's a street address, add it
                    address.getMaxAddressLineIndex() > 0 ?
                            address.getAddressLine(0) : "",
                    // Locality is usually a city
                    address.getLocality(),
                    // The country of the address
                    address.getCountryName());
        } else {
            return "No address found";
        }
    }

    private void addMarker(LatLng latLng) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);

        String address = getAddress(location);

        TagInfo ti = new TagInfo(this.getActivity());
        ti.setLocation(location);

        String title = String.format("Lat:%.3f , Lng:%.3f", latLng.latitude, latLng.longitude);

        MarkerOptions options = new MarkerOptions()
                .position(ti.getLatLng())
                .title(title)
                .snippet(address)
                .draggable(true);

        Marker m = mMap.addMarker(options);

        mMarkers.add(m);




        CameraPosition cameraPosition = CameraPosition.builder()
                .target(ti.getLatLng())
                .zoom(10)
                .build();


        // move camera to location
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
        mTags.add(m);
        updatePath();
    }



    private void updatePath() {
        if (mRouteLine != null)
            mRouteLine.remove();
        List<LatLng> points = new ArrayList<>();
        PolylineOptions polylineOptions = new PolylineOptions()
                .width(5)
                .color(Color.RED);

/**
 *  // Add a thin red line from London to New York.
 Polyline line = map.addPolyline(new PolylineOptions()
 .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
 .width(5)
 .color(Color.RED));
 */
        for (Marker marker : mMarkers) {

            LatLng latLng = marker.getPosition();
            points.add(latLng);
        }

        mRouteLine = mMap.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .width(15)
                        .color(Color.RED)
        );

    }

    private static View view;


private FloatingActionsMenu mFloatingMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view !=null){
            ViewGroup parent = (ViewGroup)view.getParent();
            if (parent!=null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_map, null);
        }catch(InflateException e){
            Log.e(TAG,e.getMessage());
        }


        removeButton= (FloatingActionButton)view.findViewById(R.id.remove_marker);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMarker!=null)
                    selectedMarker.remove();
                    mMarkers.remove(selectedMarker);
                    currentBestLocation=null;
                    updatePath();
                removeButton.setVisibility(View.GONE);
            }
        });
        final FloatingActionButton createJourneyButton = (FloatingActionButton)view.findViewById(R.id.add_journey);
        createJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createJourney();
            }
        });

        final FloatingActionButton mRecordPointsButton = (FloatingActionButton) view.findViewById(R.id.record_points);
        mRecordPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listening_enabled = !listening_enabled;

                if (listening_enabled) {
                    startListeningLocation();
                    mRecordPointsButton.setTitle("Stop Recording");
                } else {
                    stopListeningLocation();
                    mRecordPointsButton.setTitle("Record Points");
                }

                mFloatingMenu.collapse();
            }


        });
        return view;

    }

    private void createJourney(){
        Toast.makeText(getActivity(),"Create Journey",Toast.LENGTH_SHORT).show();
        mFloatingMenu.collapse();
    }

    @Override
    public void onLocationChanged(Location location) {
        makeUseOfNewLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {this.provider = provider;}

    @Override
    public void onProviderEnabled(String provider) {this.provider = provider;}

    @Override
    public void onProviderDisabled(String provider) {this.provider = provider;}

    @Override
    public void onInfoWindowClick(Marker marker) {


    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {

        switch (key) {
            case "pref_map_view":
                mMap.setMapType(Integer.parseInt(sharedPreferences.getString(key, "1")));
                break;
            case "pref_overlay_alpha":
                int val = sharedPreferences.getInt(key, 0);
                final TransparentUrlTileProvider tileProvider = new TransparentUrlTileProvider(tileUrl,val);
                tileOverlay.remove();
                tileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));


                break;
            default:
                LOGD(TAG, "Shared Preferences Changed");
                updateMarkers();
                break;
        }
    }



    private void updateMarkers(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext());

//        SharedPreferences pref = getActivity().getSharedPreferences("pref",Context.MODE_PRIVATE);
        final boolean showMarinas = pref.getBoolean(PrefUtils.PREF_SHOW_MARINAS,false);
        if (!showMarinas)
            mClusterManager.clearItems();


    }
    private void getMarinas(){

if (mClusterManager.getMarkerCollection().getMarkers().size()<2) {
    List<Marina> marinas = MarinasUtil.getMarinas();
    if (marinas != null && marinas.size() > 1)
        mClusterManager.addItems(marinas);
}
       /* for(Marina marina: MarinasUtil.getMarinas()){
            Marker marinaMarker = marina.getMarker();
            final boolean isVisible = bounds.contains(marina.getLatLng());
            if (marinaMarker!=null)
                marinaMarker.setVisible(isVisible);

            if (!bounds.contains(marina.getLatLng())) {
                continue;
            }
            BitmapDescriptor bitmapMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);

            MarkerOptions options = new MarkerOptions()
                    .position(marina.getLatLng())
                    .title(marina.getName())
                    .icon(bitmapMarker)
                    .draggable(false);

            Marker m = mMap.addMarker(options);
            marina.setMarker(m);
            builder.include(m.getPosition());

            mClusterManager.addItem(marina);

            if (marinaMarkers.contains(m))
                m.remove();
            else
                marinaMarkers.add(m);





        }
*/
        mClusterManager.cluster();
        //http://www.waterwayguide.com/includes/marinas.php
        //"http://www.marinalife.com/chartviewer_2010/map/php/marinakml_gmap.php?BBOX=-83.76274428176879,42.03908296957315,-81.50230727005004,42.09209704310099"
    }

    private void stopListeningLocation(){
        locationManager.removeUpdates(this);
        final NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

    }
    private void startListeningLocation(){
        locationManager=(LocationManager)this.getActivity().getSystemService(Context.LOCATION_SERVICE);


        // get shared preferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getBaseContext());
        final long time = sp.getInt(PrefUtils.PREF_UPDATE_TIME,0)*1000;
        final float distance = Float.intBitsToFloat(sp.getInt(PrefUtils.PREF_UPDATE_DISTANCE, mDistance));
        final int accuracy = Integer.parseInt(sp.getString(PrefUtils.PREF_UPDATE_ACCURACY, "2"));

        createLocationListenerNotification();

        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // check if enabled and if not send to the GSP settings
        // Better Solution would be to display a dialog and suggesting to
        // go to settings.

        if (!enabled){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }else{
            Criteria criteria = new Criteria();
            criteria.setAccuracy(accuracy);
            criteria.setBearingRequired(true);
            criteria.setSpeedRequired(true);



            final Context c = this.getActivity();
            provider = locationManager.getBestProvider(criteria,false);

            locationManager.requestLocationUpdates(provider,time,distance,this);
        }
    }
    private void createLocationListenerNotification() {
        Resources resources = getResources();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getActivity())
                        .setSmallIcon(R.drawable.ic_action_map)
                        .setCategory(resources.getString(R.string.app_name))
                        .setContentTitle(resources.getString(R.string.recording_locations_title))
                        .setContentText(resources.getString(R.string.recording_locations_text));

        // Creates an explicit intent for an Activity
        Intent resultIntent = new Intent(this.getActivity(), MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.getActivity());

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setVisibility(1);
        }

        int notifyID = 1;
        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        mNotificationManager.notify(notifyID, mBuilder.build());
    }
    @Override
    public void onPause() {
        super.onPause();
        if (listening_enabled&&locationManager!=null)
            locationManager.removeUpdates(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupMapIfNeeded();

        if (listening_enabled) {
            locationManager.requestLocationUpdates(provider, mTime, mDistance, this);
        }
    }


    private void makeUseOfNewLocation(Location location){
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        //TODO Get Current Temperature

        new WeatherClass(this).execute(new String[]{String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude())});




        // Update Drawer
        if(listening_enabled){
            mSpeedText.setText(String.valueOf(location.getSpeed()));
            mBearingText.setText(String.valueOf(location.getBearing()));

            final LinearLayout console = (LinearLayout)this.getActivity().findViewById(R.id.nav_console);
            console.setVisibility(listening_enabled?View.VISIBLE:View.GONE);
        }
        if(isBetterLocation(location,currentBestLocation)){
            currentBestLocation = location;

            // add marker if distance between locations is not minimal
            addMarker(latLng);
        }else{
            return;
        }

    }


    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }
        final float diff = currentBestLocation.distanceTo(location);

        // This should help prevent from adding a million markers
        if (diff <200)
            return false;
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

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void Callback(WeatherClass.WeatherObject weather) {

        mTempText.setText(weather.mTempF + WeatherClass.WeatherObject.Degree_Symbol);
        mWindGustText.setText(weather.mWindGustMPH);
        mWindDirText.setText(weather.mWindDir);

    }
}
