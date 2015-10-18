package com.solutions.nerd.sailing.android.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import com.google.android.gms.maps.MapFragment;

import com.google.android.gms.maps.SupportMapFragment;
import com.solutions.nerd.sailing.sailingwatchface.R;

/**
 * Created by mookie on 12/23/14.
 */
public class MapsActivity extends FragmentActivity
 {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //    setSupportActionBar(toolbar);
        //     getSupportActionBar().setElevation(2);

        SupportMapFragment mapFragment= (SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.map);
      //  mapFragment.getMapAsync(this);
  //      mapFragment.getMap(this);


    }
}
