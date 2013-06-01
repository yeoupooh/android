package com.owncloud.android.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.preference.PreferenceManager;

public class ConnectionUtil {

    private static Context context;
    private static ConnectionUtil instance;
    
    private ConnectionUtil(Context con) {
       context = con;
    }
    
    
    /* This method returns a instance of ConnectionUtil 
     * If a instance already exist with the same Context it won't create a 
     * new one but instead returns the exits one.
     */
    public static ConnectionUtil getInstance(Context con) {
        if (instance != null && context != null && context.equals(con)) {
            return instance;
        }
        else {
           instance = new ConnectionUtil(con);
        }
        return instance;
    }
    
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public boolean isConnectedViaWiFi() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI
                && cm.getActiveNetworkInfo().getState() == State.CONNECTED;
    }

    public boolean instantUploadEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("instant_uploading", false);
    }

    public boolean instantUploadViaWiFiOnly() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("instant_upload_on_wifi", false);
    }
    
    public boolean canInstantUpload() {
        if (!isOnline() || (instantUploadViaWiFiOnly() && !isConnectedViaWiFi())) {
            return false;
        } else {
            return true;
        }
    }
    
}