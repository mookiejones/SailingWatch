package com.solutions.nerd.sailing.android.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.stream.JsonReader;
import com.solutions.nerd.sailing.android.model.Marina;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by cberman on 12/29/2014.
 */
public class GetMarinasTask extends AsyncTask<String, Void, List<Marina>> {
    public interface GetMarinasListener {
        void onMarinasRetrieved(List<Marina> results);
    }

    private GetMarinasListener mListener;

    public GetMarinasTask(GetMarinasListener listener) {
        mListener = listener;
    }

    @Override
    protected List<Marina> doInBackground(String... params) {
        List<Marina> mMarinas = new ArrayList<Marina>();
        InputStream rr = null;
        JSONArray jarray=null;
        for (String url : params) {
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } else {
                    Log.e("==>", "Failed to download file");
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } // Parse String to JSON object
           //

           try {
               String result = builder.toString();
               long len = result.length();
               JSONObject json = new JSONObject(result);
               JSONArray array = json.optJSONArray("marinas");
               if (array!=null){
                   for(int i = 0;i<array.length();i++){
                       JSONObject object = array.optJSONObject(i);
                       Marina marina = jsonObjectToMarina(object);
                       if(marina!=null){
                           mMarinas.add(marina);
                       }
                   }
               }

           } catch (JSONException e) {
               Log.e("JSON Parser", "Error parsing data " + e.toString()); }


        }
        return mMarinas;
    }

    private Marina jsonObjectToMarina(JSONObject object) {
        Marina result=null;
        try {
            result = new Marina(object);
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
    @Override
    protected void onPostExecute(List<Marina> result) {



        mListener.onMarinasRetrieved(result);
    }

}
