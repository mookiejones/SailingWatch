package com.solutions.nerd.sailing.android.listener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


/**
 * Created by cberman on 12/29/2014.
 */
public class OptionBarMenuListener implements Toolbar.OnMenuItemClickListener {
    private Activity mActivity;
    public OptionBarMenuListener(Activity activity){
        mActivity = activity;

    }
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        final SharedPreferences sp = mActivity.getSharedPreferences("pref", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();
        String itemName = "";

        menuItem.setChecked(!menuItem.isChecked());
        int id = menuItem.getItemId();
        // Parse through menu items
        if (id == R.id.weather_item){
            itemName = "show_weather";
        }else if (id == R.id.anchorages_item){
            itemName = "show_anchorages";

        }else if (id == R.id.bridges_item){
            itemName = "show_bridges";

        }else if (id == R.id.marina_item){
            itemName = "show_marinas";

        }else if (id == R.id.navaids_item){
            itemName = "show_navaids";

        }else if (id == R.id.pictures_item){
            itemName = "show_pictures";

        }else if (id == R.id.poi_item){
            itemName = "show_poi";
        }

        if (itemName.isEmpty())
            return false;
        editor.putBoolean(itemName,menuItem.isChecked());
        editor.commit();
        return true;
    }


}
