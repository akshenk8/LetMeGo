package a8080.com.technovanzatransport.firebase;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;
import java.util.Map;

import a8080.com.technovanzatransport.Config;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    public final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();

    public final String URL = "http://splitapp.esy.es/register.php";


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        final Config config = new Config();

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                config.storeRegIdInPref(getApplicationContext(), refreshedToken);

                Log.e("response", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                config.removeRedIdInPref(getApplicationContext());
                Log.e("error", "" + error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("type", "driver");
                String oldF = config.getRegIdInPref(getApplicationContext());
                if (oldF != null) {
                    map.put("oldF", oldF);
                }

                map.put("firebase", refreshedToken);
                return map;
            }
        };
        requestQueue.add(request);
    }
}