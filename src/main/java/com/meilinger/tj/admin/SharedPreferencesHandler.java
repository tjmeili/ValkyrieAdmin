package com.meilinger.tj.admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesHandler {

    private Context context;
    private SharedPreferences prefs;

    private static final String KEY_PREV_STARTED    = "KEY_PREV_STARTED";
    private static final String KEY_SERVER_IP       = "KEY_SERVER_IP";
    private static final String KEY_SERVER_PORT     = "KEY_SERVER_PORT";

    public SharedPreferencesHandler(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getPreviouslyStarted(){
        return prefs.getBoolean(KEY_PREV_STARTED, false);
    }

    public void setPreviouslyStarted(Boolean previouslyStarted){
        prefs.edit().putBoolean(KEY_PREV_STARTED, previouslyStarted).apply();
    }

    public String getServerIP(){
        return prefs.getString(KEY_SERVER_IP, "");
    }

    public void saveServerIp(String ip){
        prefs.edit().putString(KEY_SERVER_IP, ip).apply();
    }

    public void saveServerPort(int serverPort){
        prefs.edit().putInt(KEY_SERVER_PORT, serverPort).apply();
    }

    public int getServerPort(){
        return prefs.getInt(KEY_SERVER_PORT, 9696);
    }

}
