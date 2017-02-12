package a8080.com.technovanzatransport;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by AkshanK on 12/24/2016.
 */

public class Config {
    public static final int NOTIFICATION_ID=5046;
    public final String SHARED_PREF="a8080.shared.private.pref";
    public final String FIREBASE_ID_PREF = "a8080.firebase.id";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    public String getRegIdInPref(Context context) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF, 0);
        return pref.getString(FIREBASE_ID_PREF,null);
    }

    public void storeRegIdInPref(Context context,String token) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(FIREBASE_ID_PREF, token);
        editor.commit();
    }

    public void removeRedIdInPref(Context context){
        if(getRegIdInPref(context)==null)
            return;
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(FIREBASE_ID_PREF);
    }
}
