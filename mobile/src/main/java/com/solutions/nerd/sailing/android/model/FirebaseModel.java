package com.firstmate.android.model;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by cberman on 12/17/2014.
 */
public class FirebaseModel {

public static final String USER_KEY = "user_id";
    public static Map<String,Object>createNewUser(String user_id){

        Map<String,Object> result = new Map<String, Object>() {
            @Override
            public void clear() {

            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @NonNull
            @Override
            public Set<Entry<String, Object>> entrySet() {
                return null;
            }

            @Override
            public Object get(Object key) {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @NonNull
            @Override
            public Set<String> keySet() {
                return null;
            }

            @Override
            public Object put(String key, Object value) {
                return null;
            }

            @Override
            public void putAll(Map<? extends String, ?> map) {

            }

            @Override
            public Object remove(Object key) {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @NonNull
            @Override
            public Collection<Object> values() {
                return null;
            }
        };

        result.put(USER_KEY,user_id);


        return result;
    }
}
