package a8080.com.technovanzatransport.firebase;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import a8080.com.technovanzatransport.Config;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage == null)
            return;

        if (remoteMessage.getData().size() > 0) {
            //Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        try {
            if(json.has("data")) {
                JSONObject data = json.getJSONObject("data");

                String title = data.getString("title");
                String message = data.getString("message");
                JSONObject payload = data.getJSONObject("payload");

                String firebase=payload.getString("amb");
                String type=payload.getString("type");
                Double lat=Double.parseDouble(payload.getString("lat"));
                Double lng=Double.parseDouble(payload.getString("lng"));

                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("amb",firebase);
                pushNotification.putExtra("lat",lat);
                pushNotification.putExtra("lng",lng);
                pushNotification.putExtra("title",title);
                pushNotification.putExtra("message",message);
                pushNotification.putExtra("type",type);


                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                //showNotificationMessage(getApplicationContext(), title, message);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }
}
