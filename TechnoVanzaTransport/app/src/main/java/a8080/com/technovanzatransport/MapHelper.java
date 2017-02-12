package a8080.com.technovanzatransport;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import a8080.com.technovanzatransport.firebase.NotificationUtils;

/**
 * Created by AkshanK on 12/24/2016.
 */

public class MapHelper implements OnMapReadyCallback, LocationListener {

    final String UPDATE_LOCATION_URL = "http://splitapp.esy.es/updateLocation.php";
    final String ON_ROUTE = "http://splitapp.esy.es/onRoute.php";
    public Polyline routeLines;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    public GoogleMap map;
    MainActivity activity;
    public BroadcastReceiver mRegistrationBroadcastReceiver;
    private HashMap<String, Marker> markers;
    private HashMap<String, List<Polyline>> polylines;

    private WebView webView;

    public MapHelper(final MainActivity activity) {
        this.activity = activity;
        markers = new HashMap<>();
        polylines = new HashMap<>();
        webView = (WebView) activity.findViewById(R.id.wv);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                /*if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else*/
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    String amb = intent.getStringExtra("amb");
                    String title = intent.getStringExtra("title");
                    String message = intent.getStringExtra("message");
                    String type = intent.getStringExtra("type");
                    double lat = intent.getDoubleExtra("lat", 0.0);
                    double lng = intent.getDoubleExtra("lng", 0.0);

                    if (lat != 0.0 && lng != 0.0) {
                        if (type.equals("add"))
                            addMarkerIfNot(amb, lat, lng, title, message);
                        else if (type.equals("remove"))
                            remove(amb);
                    }

                }
            }
        };
    }

    public void remove(String amb) {
        if (markers.containsKey(amb)) {
            markers.remove(amb).remove();
            if(polylines.containsKey(amb)) {
                List<Polyline> a = polylines.remove(amb);
                for (Polyline x : a) {
                    x.remove();
                }
                a.clear();
            }
        }
    }

    public void addMarkerIfNot(final String amb, double lat, double lng, String title, String message) {
        StringRequest request = new StringRequest(Request.Method.POST, ON_ROUTE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e("response", response);
                //response=response.substring(response.indexOf("<body>")+6);
                //response=response.substring(0,response.indexOf("</body>"));
                webView.getSettings().setJavaScriptEnabled(true);
                webView.loadData(response, "text/html", null);
                //javascript:window.HTML_OUT.processHTML(''+  );
                webView.evaluateJavascript("document.getElementsByTagName('body')[0].innerHTML",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                //Log.e("poly",value);
                                if (!value.equals("\"\"") && routeLines!=null)
                                    new MatchPolyLines(value, amb).execute();
                            }
                        });

                //Log.e("poly",response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mLastLocation = null;
                Log.e("error", "" + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                String oldF = activity.config.getRegIdInPref(activity);
                map.put("amb", amb);
                map.put("driver", oldF);
                return map;
            }
        };
        activity.requestQueue.add(request);


        if (markers.containsKey(amb)) {
            remove(amb);
            MarkerOptions mark = new MarkerOptions();
            mark.position(new LatLng(lat, lng));
            mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.amb));
            markers.put(amb, map.addMarker(mark));
            return;
        }
        MarkerOptions mark = new MarkerOptions();
        mark.position(new LatLng(lat, lng));
        mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.amb));
        markers.put(amb, map.addMarker(mark));
        NotificationUtils notificationUtils = new NotificationUtils(activity);
        notificationUtils.showNotificationMessage(title, message);
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
        } catch (SecurityException e) {
            Log.e("noaccess", "" + e);
        }
        map.getUiSettings().setIndoorLevelPickerEnabled(false);
        map.getUiSettings().setScrollGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(true);

        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (mLastLocation != null) {
                    animateMap(mLastLocation.getLatitude(), mLastLocation.getLongitude());
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
        if (location != null) {
            final double lat = location.getLatitude(), lng = location.getLongitude();
            if ((mLastLocation == null) || mLastLocation.getLatitude() != lat || mLastLocation.getLongitude() != lng) {
                //send location update to server

                StringRequest request = new StringRequest(Request.Method.POST, UPDATE_LOCATION_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Log.e("response", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLastLocation = null;
                        Log.e("error", "" + error);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("type", "driver");
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

    class MatchPolyLines extends AsyncTask<Void, Void, Void> {

        String line2;
        List<LatLng> latlng2;
        String amb;

        public MatchPolyLines(String line2, String amb) {
            this.line2 = line2;
            this.amb = amb;
        }

        @Override
        protected Void doInBackground(Void... params) {
            latlng2 = PolyUtil.decode(line2);
            Log.e("dec", line2);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //PolylineOptions po=new PolylineOptions();
            //po.addAll(latlngs);
            //po.color(Color.TRANSPARENT);
            //Polyline polyline2=map.addPolyline(po);

            LinkedHashMap<Integer, LatLng> commonPts = new LinkedHashMap<>();

            List<LatLng> latLng1 = routeLines.getPoints();

            boolean first = false;
            int ft = latLng1.size();

            for (int i = 0; i < latLng1.size(); i++) {
                for (int j = 0; j < latlng2.size(); j++) {
                    double latDiff=Math.abs(latLng1.get(i).latitude-latlng2.get(j).latitude);
                    double lngDiff=Math.abs(latlng2.get(j).longitude-latLng1.get(i).longitude);

                    if (latDiff>=0.001 && lngDiff>=0.001 ) {
                        if (!first) {
                            ft = i;
                            first = true;
                        }
                        commonPts.put(i, latLng1.get(i));
                    }
                }
            }
            List<Polyline> lines = new ArrayList<>();
            List<LatLng> path = new ArrayList<>();
            int prevIdx = ft;
            for (int i = 0; i < commonPts.keySet().size(); i++) {
                if (i <= prevIdx + 1) {
                    path.add(commonPts.get(i));
                    prevIdx = i;
                } else {
                    PolylineOptions po = new PolylineOptions();
                    po.addAll(path);
                    po.color(Color.YELLOW);
                    Polyline polyline2 = map.addPolyline(po);
                    lines.add(polyline2);
                    path.clear();
                    prevIdx = i;
                }
            }
            PolylineOptions po = new PolylineOptions();
            po.addAll(path);
            po.color(Color.YELLOW);
            Polyline polyline2 = map.addPolyline(po);
            lines.add(polyline2);

            polylines.put(amb, lines);

            //polyline2.remove();
        }
    }
}
