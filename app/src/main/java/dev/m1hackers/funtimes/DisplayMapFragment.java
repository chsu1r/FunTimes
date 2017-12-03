package dev.m1hackers.funtimes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.content.Context;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;


/** A Fragment for displaying the map with the POI
 */
public class DisplayMapFragment extends CustomFragment  implements OnMapReadyCallback {

    private static final Hashtable<String, Integer> requestCodeMap = new Hashtable<String, Integer>() {{
        put(Manifest.permission.ACCESS_FINE_LOCATION, 2);
    }};
    private static final String LOG_TAG = "DisplayMapFragment";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_DETAILS = "/details";
    private static final String TYPE_SEARCH = "/nearbysearch";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyCoyESSSVsupzauMVKA24FDf_DC4ETsimI";

    public static ArrayList<String> categories = null;
    private GoogleMap mMap;
    protected Location mLastLocation;
    protected MainActivity mActivity;
    private ArrayList<Place> results = null;
    static class Place{
        String reference;
        double lat;
        double lon;
        String name;
        String formatted_address;
    }

    class requestPlacesTaskParams {
        //ArrayList<String> keywords = new ArrayList<>();
        String keyword;
        double latitude;
        double longitude;
    }


    public DisplayMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("DisplayMapFragment","DisplayMapFragment onCreate method executing.");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_display_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) context;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
        mFusedLocationClient.getLastLocation().addOnCompleteListener(mActivity,
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            for(int i=0;i<categories.size();i++){
                                requestPlacesTaskParams params = new requestPlacesTaskParams();                                inp.keyword = "books";
                                params.keyword = categories.get(i);
                                params.latitude = mLastLocation.getLatitude();
                                params.longitude = mLastLocation.getLongitude();
                                RequestPlacesTask mRequestPlacesTask = new RequestPlacesTask(
                                        DisplayMapFragment.this);
                                mRequestPlacesTask.execute(params);
                            }
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),
                                    14.0f));
                        }
                        else {
                            Toast.makeText(mActivity,R.string.no_location_msg,Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */

    protected void placePointers(ArrayList<Place> results){
        if(results != null) {
            for (int i = 0; i < results.size(); i++) {
                Place current_place = results.get(i);
                LatLng coordinates = new LatLng(current_place.lat, current_place.lon);
                mMap.addMarker(new MarkerOptions().position(coordinates).title(current_place.name));

            }
        }
    }
    private class longop extends AsyncTask<Inputobj,Void,ArrayList<Place>> {
        @Override
        protected ArrayList<Place> doInBackground(Inputobj ...inps){
            Inputobj inp = inps[0];
            ArrayList<Place> resultList = null;
            String keyword = inp.keyword;
            double lat = inp.lat;
            double lng = inp.lon;

            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                StringBuilder sb = new StringBuilder(PLACES_API_BASE);
                sb.append(TYPE_SEARCH);
                sb.append(OUT_JSON);
                sb.append("?sensor=false");
                sb.append("&key=" + API_KEY);
                sb.append("&keyword=" + URLEncoder.encode(keyword, "utf8"));
                sb.append("&location=" + String.valueOf(lat) + "," + String.valueOf(lng));
                sb.append("&radius=" + String.valueOf(2000));
                System.out.println(sb.toString());
                URL url = new URL(sb.toString());
                //Log.i(myTag,sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));


                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }


            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error processing Places API URL", e);
                return resultList;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error connecting to Places API", e);
                return resultList;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
//            return resultList;

            try {
                // Create a JSON object hierarchy from the results
                //Log.i(myTag,jsonResults.toString());
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                JSONArray predsJsonArray = jsonObj.getJSONArray("results");

                // Extract the Place descriptions from the results
                resultList = new ArrayList<>(predsJsonArray.length());
                for (int i = 0; i < predsJsonArray.length(); i++) {
                    Place place = new Place();
                    place.reference = predsJsonArray.getJSONObject(i).getString("reference");
                    place.name = predsJsonArray.getJSONObject(i).getString("name");
                    JSONObject location = predsJsonArray.getJSONObject(i).getJSONObject("geometry");
                    location = location.getJSONObject("location");
                    place.lat = Double.parseDouble(location.getString("lat"));
                    place.lon = Double.parseDouble(location.getString("lng"));
                    Log.i(LOG_TAG,place.name);
                    resultList.add(place);
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error processing JSON results", e);
            }
            results = resultList;
            return resultList;
        }

        @Override
        protected void onPostExecute(ArrayList<Place> results) {
            Log.i(LOG_TAG,"post");
            placePointers(results);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}