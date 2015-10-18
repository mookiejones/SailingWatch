package com.solutions.nerd.sailing.android.ui;


import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.solutions.nerd.sailing.android.R;
import com.solutions.nerd.sailing.android.ui.BaseActivity;
import com.solutions.nerd.sailing.android.util.AccountUtils;
import com.solutions.nerd.sailing.android.util.AnalyticsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity {

    NotificationManager mNotificationManager;


    @Override
    protected void onStop() {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        super.onStop();
    }


    ListView jl;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private List<String> items = new ArrayList<String>();
    private final static String SCREEN_LABEL = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AnalyticsManager.sendScreenView(SCREEN_LABEL + ": " + "main");
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        prepareListData();

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        jl = (ListView) findViewById(R.id.journeys_list);
        jl.setAdapter(adapter);

    }

    private void prepareListData() {
        Firebase.setAndroidContext(this);

        String url = AccountUtils.getUserJourneyTitlesURL(this);
        Firebase fb = new Firebase(url);
        fb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Object o = dataSnapshot.getValue();
                HashMap<String, Object> keys = (HashMap<String, Object>) dataSnapshot.getValue();
                String name = keys.get("name").toString();

                items.add(name);

                listDataHeader.add(name);
                listDataChild.put(listDataHeader.get(0), listDataHeader);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    private class StableArrayAdapter extends ArrayAdapter<String> {
        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int resource, List<String> items) {
            super(context, resource);
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
