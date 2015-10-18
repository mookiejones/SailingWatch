package com.firstmate.android.ui;

import android.os.Bundle;


import com.firstmate.android.R;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by cberman on 12/16/2014.
 */
public class JourneyActivity extends BaseActivity {
    SupportMapFragment mapFragment;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);
    }
    }
