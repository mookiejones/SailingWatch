package com.solutions.nerd.sailing.android.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;


import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.solutions.nerd.sailing.sailingwatchface.R;


/**
 * Created by cberman on 12/17/2014.
 */
public class SettingsActivity extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
