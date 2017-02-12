package a8080.com.technovanzaamb;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by AkshanK on 12/24/2016.
 */

public class MapHelper implements OnMapReadyCallback,LocationListener {

    final String UPDATE_LOCATION_URL="http://splitapp.esy.es/updateLocation.php";
    public Polyline routeLines;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    public GoogleMap map;
    MainActivity activity;

    public MapHelper(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
        try {
            map.setMyLocationEnabled(true);
        }catch (SecurityException e){
            Log.e("noaccess",""+e);
        }
        map.getUiSettings().setIndoorLevelPickerEnabled(false);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(true);

        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if(mLastLocation!=null) {
                    animateMap(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    return true;
                }
                return false;
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null ) {
            final double lat = location.getLatitude(), lng = location.getLongitude();
            if ((mLastLocation == null) || mLastLocation.getLatitude()!=lat || mLastLocation.getLongitude()!=lng ){
                //send location update to server

                StringRequest request = new StringRequest(Request.Method.POST, UPDATE_LOCATION_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("response", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLastLocation=null;
                        Log.e("error", "" + error);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("type", "amb");
                        String oldF = activity.config.getRegIdInPref(activity);
                        map.put("firebase", oldF);
                        map.put("currentLat", "" + lat);
                        map.put("currentLng", "" + lng);
                        return map;
                    }
                };
                activity.requestQueue.add(request);

                animateMap(lat, lng);
                mLastLocation = location;
            }
        }
    }

    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000); //5 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        try {
            //location updates moves blue marker
            LocationServices.FusedLocationApi.requestLocationUpdates(activity.mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            Log.e("ex:", e.toString());
        }
    }

    //takes to position smooth transition
    public void animateMap(double lat, double lng) {
        LatLng home = new LatLng(lat, lng);
        CameraPosition target = CameraPosition.builder()
                .target(home)
                .zoom(15)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(target), 1000, null);
    }
}
