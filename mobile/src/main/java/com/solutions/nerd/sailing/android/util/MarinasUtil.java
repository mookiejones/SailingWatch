package com.firstmate.android.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.FirebaseException;
import com.firebase.client.ValueEventListener;
import com.firstmate.android.Config;
import com.firstmate.android.model.Marina;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by cberman on 12/30/2014.
 */
public class MarinasUtil {
    private static final List<Marina> mMarinas = new ArrayList<Marina>();
    private HashMap<String,Marina> marinas = new HashMap<String,Marina>();
    public static List<Marina> getMarinas(){
        return mMarinas;
    }
    public static void init(final Context context) {
        Firebase ref = new Firebase("https://first-mate.firebaseio.com/marinas");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                try {
                  HashMap obj = (HashMap<String,Object>)dataSnapshot.getValue();
                    Marina marina = new Marina(obj);


                    mMarinas.add(marina);
                }catch(FirebaseException e){
                    e.printStackTrace();
                }

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

    public static List<Marina> read(InputStream inputStream) throws JSONException {
        List<Marina> items = new ArrayList<Marina>();
        String json = new Scanner(inputStream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("latd");
            double lng = object.getDouble("lond");
            items.add(new Marina(object));

        }
        return items;
    }
    /*
* This matches only once in whole input,
* so Scanner.next returns whole InputStream as a String.
* http://stackoverflow.com/a/5445161/2183804
*/
    private static final String REGEX_INPUT_BOUNDARY_BEGINNING = "\\A";

}
