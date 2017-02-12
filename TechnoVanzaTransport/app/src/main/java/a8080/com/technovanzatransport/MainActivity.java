package a8080.com.technovanzatransport;

import android.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;

import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import a8080.com.technovanzatransport.firebase.NotificationUtils;

public class MainActivity extends AppCompatActivity {

    final String CANCEL_URL = "http://splitapp.esy.es/cancelRide.php";
    AlertDialog ad;
    final int code = 115;
    boolean all_permissions = false;
    GoogleApiClient mGoogleApiClient;
    MapHelper mapHelper;
    public RequestQueue requestQueue;
    public Config config;
    String route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        config = new Config();

        if (config.getRegIdInPref(this) == null) {
            setContentView(R.layout.error_layout);
            return;
        }
        setContentView(R.layout.activity_main);

        requestPermissions();

        isLocationEnabled();

        if (!getNetworkStatus()) {
            ((TextView) findViewById(R.id.nonet)).setVisibility(View.VISIBLE);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("android.intent.extra.TEXT")) {
                try {
                    route = extras.get("android.intent.extra.TEXT").toString();
                    //Log.e("data",route);
                    int s = route.indexOf("https://");
                    if(s==-1){
                        /*s=route.indexOf('(')+1;
                        int l=route.indexOf(')',s);
                        String src=route.substring(s,l);
                        s=route.indexOf("to")+2;
                        l=route.indexOf('\n',s);
                        String dest=route.substring(s,l);
                        Log.e("src",src);
                        Log.e("dest",dest);
                        dest=dest.replaceAll(" ","%20");*/
                        s = route.indexOf("http://");
                        if(route.indexOf("saddr",s)!=-1){
                            String fullUrl=route.substring(s);
                            new PolyGenerator().getUrl(fullUrl, MainActivity.this);
                        }
                        else{
                            Toast.makeText(this,"Unable to load.Try again Later",Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }
                    else {
                        route = route.substring(s);
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    URL url = new URL(route);
                                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
                                    httpURLConnection.setInstanceFollowRedirects(false);
                                    String fullUrl = httpURLConnection.getHeaderField("location");
                                    //Log.e("fullUrl", fullUrl);
                                    httpURLConnection.disconnect();
                                    new PolyGenerator().getUrl(fullUrl, MainActivity.this);
                                } catch (Exception e) {
                                    Log.e("error", "" + e);
                                }
                            }
                        }.start();
                    }
                } catch (Exception e) {
                    Toast.makeText(this,"Unable to load.Try again Later",Toast.LENGTH_SHORT).show();
                    finish();
                }
                //Log.e("ee", route);
            }
        }

        if (all_permissions) {
            buildApi();
        }

        requestQueue = Volley.newRequestQueue(this);
    }

    public void buildApi() {
        mapHelper = new MapHelper(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapHelper);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            mapHelper.onConnected(bundle);
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.e("method:", "Connection suspended");
                            mGoogleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            Log.e("method:", "Connection failed");
                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onBackPressed() {
        ad = new AlertDialog.Builder(this)
                .setTitle("Close App")
                .setMessage("THis will stop all services")
                .setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ad.dismiss();
                    }
                })
                .setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        xyz();
                    }
                })
                .show();
    }

    public void xyz() {
        super.onBackPressed();
    }

    public boolean isLocationEnabled() {
        int locationMode = 0;
        try {
            locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        boolean r = locationMode != Settings.Secure.LOCATION_MODE_OFF;
        if (!r) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "Location is OFF.", Toast.LENGTH_LONG).show();
        }

        return r;
    }

    @Override
    protected void onResume() {
        super.onResume();
/*
        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));
*/
        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        if (config.getRegIdInPref(this) != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mapHelper.mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.PUSH_NOTIFICATION));
        }
        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        if (config.getRegIdInPref(this) != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mapHelper.mRegistrationBroadcastReceiver);
        }
        super.onPause();
    }

    protected void onStart() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (requestQueue != null && config.getRegIdInPref(this) != null) {
            StringRequest request = new StringRequest(Request.Method.POST, CANCEL_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.e("response", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("error", "" + error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("type", "driver");
                    String oldF = config.getRegIdInPref(MainActivity.this);
                    map.put("firebase", oldF);
                    return map;
                }
            };
            requestQueue.add(request);
        }
        super.onDestroy();
    }

    public boolean getNetworkStatus() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            // notify user you are online
            return true;
        } else {
            // notify user you are not online
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    void requestPermissions() {
        all_permissions = true;
        ArrayList<String> arr = new ArrayList<>();
        arr.add(android.Manifest.permission.INTERNET);
        arr.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        arr.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);

        ArrayList<String> reqArr = new ArrayList<>();
        for (String x : arr) {
            if (!checkPermission(x)) {
                all_permissions = false;
                reqArr.add(x);
            }
        }

        Object[] tmp1 = reqArr.toArray();
        String[] req = Arrays.copyOf(tmp1, tmp1.length, String[].class);

        if (req.length > 0)
            ActivityCompat.requestPermissions(this, req, code);
    }

    boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case code: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    all_permissions = true;
                    buildApi();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    all_permissions = false;
                    requestPermissions();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
