package dev.m1hackers.funtimes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import java.util.ArrayList;


/** A Fragment for displaying the map with the POI
 */
public class DisplayMapFragment extends CustomFragment  implements OnMapReadyCallback {

    private static final String LOG_TAG = "DisplayMapFragment";
    private static ArrayList<String> keywordList = new ArrayList<>();
    protected Location mLastLocation;
    protected MainActivity mActivity;
    private GoogleMap mMap;

    public DisplayMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("DisplayMapFragment","DisplayMapFragment onCreate method executing.");

        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) keywordList = args.getStringArrayList("keywordList");
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
        Log.d(LOG_TAG, "Executing onMapReady callback.");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
        mFusedLocationClient.getLastLocation().addOnCompleteListener(mActivity,
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Log.d(LOG_TAG, "Executing LastLocation's onCompleteListener ");
                        if (task.isSuccessful() && task.getResult() != null) {
                            Log.i(LOG_TAG, "Successfully determined location");
                            mLastLocation = task.getResult();
                            for(int i = 0; i< keywordList.size(); i++){
                                requestPlacesTaskParams params = new requestPlacesTaskParams();
                                params.keyword = keywordList.get(i);
                                params.latitude = mLastLocation.getLatitude();
                                params.longitude = mLastLocation.getLongitude();
                                Log.d(LOG_TAG, "Executing RequestPlacesTask");
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

    static class Place{
        String reference;
        double lat;
        double lon;
        String name;
    }

    class requestPlacesTaskParams {
        //ArrayList<String> keywords = new ArrayList<>();
        String keyword;
        double latitude;
        double longitude;
    }
}
